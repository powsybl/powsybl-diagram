/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.BusCell.Direction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractSubstationLayout implements SubstationLayout {

    protected SubstationGraph graph;
    protected VoltageLevelLayoutFactory vLayoutFactory;

    protected class InfosNbSnakeLines {
        public InfosNbSnakeLines(Map<Direction, Integer> nbSnakeLinesTopBottom,
                                 Map<String, Integer> nbSnakeLinesBetween,
                                 Map<Side, Integer> nbSnakeLinesLeftRight,
                                 Map<String, Integer> nbSnakeLinesBottomVL,
                                 Map<String, Integer> nbSnakeLinesTopVL) {
            this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
            this.nbSnakeLinesBetween = nbSnakeLinesBetween;
            this.nbSnakeLinesLeftRight = nbSnakeLinesLeftRight;
            this.nbSnakeLinesBottomVL = nbSnakeLinesBottomVL;
            this.nbSnakeLinesTopVL = nbSnakeLinesTopVL;
        }

        public Map<Direction, Integer> getNbSnakeLinesTopBottom() {
            return nbSnakeLinesTopBottom;
        }

        public Map<String, Integer> getNbSnakeLinesBetween() {
            return nbSnakeLinesBetween;
        }

        public Map<Side, Integer> getNbSnakeLinesLeftRight() {
            return nbSnakeLinesLeftRight;
        }

        public Map<String, Integer> getNbSnakeLinesBottomVL() {
            return nbSnakeLinesBottomVL;
        }

        public Map<String, Integer> getNbSnakeLinesTopVL() {
            return nbSnakeLinesTopVL;
        }

        private Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom;
        private Map<String, Integer> nbSnakeLinesBetween;
        private Map<Side, Integer> nbSnakeLinesLeftRight;
        private Map<String, Integer> nbSnakeLinesBottomVL;
        private Map<String, Integer> nbSnakeLinesTopVL;
    }

    public AbstractSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        this.graph = Objects.requireNonNull(graph);
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        // Calculate all the coordinates for the voltageLevel graphs in the substation graph
        double graphX = layoutParameters.getHorizontalSubstationPadding();
        double graphY = layoutParameters.getVerticalSubstationPadding();

        for (Graph vlGraph : graph.getNodes()) {
            vlGraph.setX(graphX);
            vlGraph.setY(graphY);

            // Calculate the objects coordinates inside the voltageLevel graph
            VoltageLevelLayout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            // Calculate the global coordinate of the voltageLevel graph
            Coord posVLGraph = calculateCoordVoltageLevel(layoutParameters, vlGraph);

            graphX += posVLGraph.getX() + getHorizontalSubstationPadding(layoutParameters);
            graphY += posVLGraph.getY() + getVerticalSubstationPadding(layoutParameters);
        }

        // Calculate all the coordinates for the links between the voltageLevel graphs
        // (new fictitious nodes and new edges are introduced in this stage)
        manageSnakeLines(layoutParameters);
    }

    protected abstract Coord calculateCoordVoltageLevel(LayoutParameters layoutParameters, Graph vlGraph);

    protected abstract double getHorizontalSubstationPadding(LayoutParameters layoutParameters);

    protected abstract double getVerticalSubstationPadding(LayoutParameters layoutParameters);

    protected abstract List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Node node1, Node node2,
                                                               InfosNbSnakeLines infosNbSnakeLines, boolean increment);

    protected BusCell.Direction getNodeDirection(Node node, int nb) {
        if (node.getType() != Node.NodeType.FEEDER) {
            throw new PowsyblException("Node " + nb + " is not a feeder node");
        }
        BusCell.Direction dNode = node.getCell() != null ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.TOP;
        if (dNode != BusCell.Direction.TOP && dNode != BusCell.Direction.BOTTOM) {
            throw new PowsyblException("Node " + nb + " cell direction not TOP or BOTTOM");
        }
        return dNode;
    }

    protected void manageSnakeLines(LayoutParameters layoutParameters) {
        // used only for horizontal layout
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        // used only for vertical layout
        Map<Side, Integer> nbSnakeLinesLeftRight = EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBottomVL = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));
        Map<String, Integer> nbSnakeLinesTopVL = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        InfosNbSnakeLines infos = new InfosNbSnakeLines(nbSnakeLinesTopBottom, nbSnakeLinesBetween,
                nbSnakeLinesLeftRight, nbSnakeLinesBottomVL, nbSnakeLinesTopVL);

        for (StarNode starNode : graph.getStarNodes()) {
            Coord starNodeCoord = new Coord(-1, -1);

            List<Edge> edges = starNode.getAdjacentEdges();
            if (edges.size() == 2) {
                WindingEdge edge1 = (WindingEdge) edges.get(0);
                WindingEdge edge2 = (WindingEdge) edges.get(1);
                Node node1 = edge1.getNode1() == starNode ? edge1.getNode2() : edge1.getNode1();
                Node node2 = edge2.getNode1() == starNode ? edge2.getNode2() : edge2.getNode1();

                // set intermediate coordinates of the snake lines
                List<Double> pol = calculatePolylineSnakeLine(layoutParameters, node1, node2, infos, true);
                edge1.setSnakeLine(splitPolyline2(pol, 1, starNodeCoord));
                edge2.setSnakeLine(splitPolyline2(pol, 2, null));
            } else if (edges.size() == 3) {
                WindingEdge edge1 = (WindingEdge) edges.get(0);
                WindingEdge edge2 = (WindingEdge) edges.get(1);
                WindingEdge edge3 = (WindingEdge) edges.get(2);
                Node node1 = edge1.getNode1() == starNode ? edge1.getNode2() : edge1.getNode1();
                Node node2 = edge2.getNode1() == starNode ? edge2.getNode2() : edge2.getNode1();
                Node node3 = edge3.getNode1() == starNode ? edge3.getNode2() : edge3.getNode1();

                // set intermediate coordinates of the snake lines
                List<Double> pol1 = calculatePolylineSnakeLine(layoutParameters, node1, node2, infos, true);
                List<Double> pol2 = calculatePolylineSnakeLine(layoutParameters, node2, node3, infos, false);
                edge1.setSnakeLine(splitPolyline3(pol1, pol2, 1, starNodeCoord));
                edge2.setSnakeLine(splitPolyline3(pol1, pol2, 2, null));
                edge3.setSnakeLine(splitPolyline3(pol1, pol2, 3, null));
            } else {
                throw new IllegalStateException();
            }

            // set coordinates of the star node
            starNode.setX(starNodeCoord.getX(), false, false);
            starNode.setY(starNodeCoord.getY(), false, false);
        }
    }

    protected List<Double> splitPolyline2(List<Double> pol, int numPart, Coord coord) {
        List<Double> res = new ArrayList<>();

        double xSplit = 0;
        double ySplit = 0;
        if (pol.size() == 8) {
            xSplit = (pol.get(2) + pol.get(4)) / 2;
            ySplit = (pol.get(3) + pol.get(5)) / 2;
            if (numPart == 1) {
                res.addAll(pol.subList(0, 4));
                res.addAll(Arrays.asList(xSplit, ySplit));
            } else {
                res.addAll(Arrays.asList(xSplit, ySplit));
                res.addAll(pol.subList(4, 8));
            }
        } else if (pol.size() == 12) {
            xSplit = (pol.get(4) + pol.get(6)) / 2;
            ySplit = (pol.get(5) + pol.get(7)) / 2;
            if (numPart == 1) {
                res.addAll(pol.subList(0, 6));
                res.addAll(Arrays.asList(xSplit, ySplit));
            } else {
                res.addAll(Arrays.asList(xSplit, ySplit));
                res.addAll(pol.subList(6, 12));
            }
        }

        if (coord != null) {
            coord.setX(xSplit);
            coord.setY(ySplit);
        }

        return res;
    }

    protected List<Double> splitPolyline3(List<Double> pol1, List<Double> pol2, int numPart, Coord coord) {
        List<Double> res = new ArrayList<>();

        if (numPart == 1) {
            // for the first new edge, we keep all the original first polyline points, except the last one
            res.addAll(pol1.subList(0, pol1.size() - 2));
            if (coord != null) {
                // the fictitious node point is the last point of the new edge polyline
                coord.setX(pol1.get(pol1.size() - 4));
                coord.setY(pol1.get(pol1.size() - 3));
            }
        } else if (numPart == 2) {
            // for the second new edge, we keep the last two points of the original first polyline
            res.addAll(pol1.subList(pol1.size() - 4, pol1.size()));
        } else {
            // the third new edge is made with the original second polyline, except the first point
            res.addAll(pol2.subList(2, pol2.size()));
        }

        return res;
    }
}
