/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusCell.Direction;
import com.powsybl.sld.model.Coord;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.FictitiousNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Side;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.TwtEdge;

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

    private void manageSnakeLines(LayoutParameters layoutParameters) {
        // used only for horizontal layout
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = graph.getNodes().stream().collect(Collectors.toMap(Graph::getVoltageLevelId, v -> 0));

        // used only for vertical layout
        Map<Side, Integer> nbSnakeLinesLeftRight = EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBottomVL = graph.getNodes().stream().collect(Collectors.toMap(Graph::getVoltageLevelId, v -> 0));
        Map<String, Integer> nbSnakeLinesTopVL = graph.getNodes().stream().collect(Collectors.toMap(Graph::getVoltageLevelId, v -> 0));

        InfosNbSnakeLines infos = new InfosNbSnakeLines(nbSnakeLinesTopBottom, nbSnakeLinesBetween,
                nbSnakeLinesLeftRight, nbSnakeLinesBottomVL, nbSnakeLinesTopVL);

        List<TwtEdge> newEdges = new ArrayList<>();

        for (TwtEdge edge : graph.getEdges()) {
            if (edge.getNodes().size() == 2) {
                List<Double> pol = calculatePolylineSnakeLine(layoutParameters, edge.getNode1(), edge.getNode2(), infos, true);
                // we split the original edge in two parts, with a new fictitious node between the two new edges
                splitEdge2(edge, newEdges, pol);
            } else if (edge.getNodes().size() == 3) {
                List<Double> pol1 = calculatePolylineSnakeLine(layoutParameters, edge.getNode1(), edge.getNode2(), infos, true);
                List<Double> pol2 = calculatePolylineSnakeLine(layoutParameters, edge.getNode2(), edge.getNode3(), infos, false);
                // we split the original edge in three parts, with a new fictitious node between the three new edges
                splitEdge3(edge, newEdges, pol1, pol2);
            }
        }

        // replace the old edges by the new edges in the substation graph
        graph.setEdges(newEdges);
    }

    private void splitEdge2(TwtEdge edge, List<TwtEdge> edges, List<Double> pol) {
        // Creation of a new fictitious node outside any graph
        String idNodeFict = edge.getNode1().getId() + "_" + edge.getNode2().getId();
        Node nodeFict = new FictitiousNode(null, idNodeFict, edge.getComponentType());
        Coord coordNodeFict = new Coord(-1, -1);

        // Creation of a new edge between node1 and the new fictitious node
        TwtEdge edge1 = new TwtEdge(edge.getComponentType(), edge.getNode1(), nodeFict);
        edge1.setSnakeLine(splitPolyline2(pol, 1, coordNodeFict));
        edges.add(edge1);

        // Creation of a new edge between the new fictitious node and node2
        TwtEdge edge2 = new TwtEdge(edge.getComponentType(), nodeFict, edge.getNode2());
        edge2.setSnakeLine(splitPolyline2(pol, 2, null));
        edges.add(edge2);

        // Setting the coordinates of the new fictitious node
        nodeFict.setX(coordNodeFict.getX(), false, false);
        nodeFict.setY(coordNodeFict.getY(), false, false);
        nodeFict.addAdjacentEdge(edge1);
        nodeFict.addAdjacentEdge(edge2);

        // the new fictitious node is store in the substation graph
        graph.addMultiTermNode(nodeFict);
    }

    private void splitEdge3(TwtEdge edge, List<TwtEdge> edges, List<Double> pol1, List<Double> pol2) {
        // Creation of a new fictitious node outside any graph
        String idNodeFict = edge.getNode1().getId() + "_" + edge.getNode2().getId() + "_" + edge.getNode3().getId();
        Node nodeFict = new FictitiousNode(null, idNodeFict, edge.getComponentType());
        Coord coordNodeFict = new Coord(-1, -1);

        // Creation of a new edge between node1 and the new fictitious node
        TwtEdge edge1 = new TwtEdge(edge.getComponentType(), edge.getNode1(), nodeFict);
        edge1.setSnakeLine(splitPolyline3(pol1, 1, coordNodeFict));
        edges.add(edge1);

        // Creation of a new edge between the new fictitious node and node2
        TwtEdge edge2 = new TwtEdge(edge.getComponentType(), nodeFict, edge.getNode2());
        edge2.setSnakeLine(splitPolyline3(pol1, 2, null));
        edges.add(edge2);

        // Creation of a new edge between the new fictitious node and node3
        TwtEdge edge3 = new TwtEdge(edge.getComponentType(), nodeFict, edge.getNode3());
        edge3.setSnakeLine(splitPolyline3(pol2, 3, null));
        edges.add(edge3);

        // Setting the coordinates of the new fictitious node
        nodeFict.setX(coordNodeFict.getX(), false, false);
        nodeFict.setY(coordNodeFict.getY(), false, false);
        nodeFict.addAdjacentEdge(edge1);
        nodeFict.addAdjacentEdge(edge2);
        nodeFict.addAdjacentEdge(edge3);

        // the new fictitious node is store in the substation graph
        graph.addMultiTermNode(nodeFict);
    }

    private List<Double> splitPolyline2(List<Double> pol, int numPart, Coord coord) {
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

    private List<Double> splitPolyline3(List<Double> pol, int numPart, Coord coord) {
        List<Double> res = new ArrayList<>();

        if (numPart == 1) {
            res.addAll(pol.subList(0, pol.size() - 2));
            if (coord != null) {
                coord.setX(pol.get(pol.size() - 4));
                coord.setY(pol.get(pol.size() - 3));
            }
        } else if (numPart == 2) {
            res.addAll(pol.subList(pol.size() - 4, pol.size()));
        } else {
            res.addAll(pol.subList(2, pol.size()));
        }

        return res;
    }
}
