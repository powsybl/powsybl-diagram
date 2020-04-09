/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.*;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.impl.EdgeImpl;
import org.gephi.graph.impl.GraphModelImpl;
import org.gephi.graph.impl.NodeImpl;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ForceSubstationLayout extends AbstractSubstationLayout {

    private final Random random = new Random();
    private ForceSubstationLayoutFactory.CompactionType compactionType;

    public static class ForceInfoCalcPoints extends InfoCalcPoints {
        private String vId1;
        private String vId2;
        private Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom;
        private Map<String, Integer> nbSnakeLinesBetween;

        public String getVId1() {
            return vId1;
        }

        public void setVId1(String vId1) {
            this.vId1 = vId1;
        }

        public String getVId2() {
            return vId2;
        }

        public void setVId2(String vId2) {
            this.vId2 = vId2;
        }

        public Map<String, Map<BusCell.Direction, Integer>> getNbSnakeLinesTopBottom() {
            return nbSnakeLinesTopBottom;
        }

        public void setNbSnakeLinesTopBottom(Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom) {
            this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
        }

        public Map<String, Integer> getNbSnakeLinesBetween() {
            return nbSnakeLinesBetween;
        }

        public void setNbSnakeLinesBetween(Map<String, Integer> nbSnakeLinesBetween) {
            this.nbSnakeLinesBetween = nbSnakeLinesBetween;
        }
    }

    public ForceSubstationLayout(SubstationGraph substationGraph,
                                 VoltageLevelLayoutFactory voltageLevelLayoutFactory,
                                 ForceSubstationLayoutFactory.CompactionType compactionType) {
        super(substationGraph, voltageLevelLayoutFactory);
        this.compactionType = compactionType;
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        // Creating the graph model for the ForceLayout algorithm
        GraphModel graphModel = new GraphModelImpl();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        for (Graph voltageLevelGraph : graph.getNodes()) {
            NodeImpl n = new NodeImpl(voltageLevelGraph.getVoltageLevelInfos().getId());
            n.setPosition(random.nextFloat() * 1000, random.nextFloat() * 1000);
            undirectedGraph.addNode(n);
        }
        for (TwtEdge edge : graph.getEdges()) {
            NodeImpl node1 = (NodeImpl) undirectedGraph.getNode(edge.getNode1().getGraph().getVoltageLevelInfos().getId());
            NodeImpl node2 = (NodeImpl) undirectedGraph.getNode(edge.getNode2().getGraph().getVoltageLevelInfos().getId());
            undirectedGraph.addEdge(new EdgeImpl(edge.toString() + "_1_2", node1, node2, 0, 1, false));
            if (edge.getNode3() != null) {
                NodeImpl node3 = (NodeImpl) undirectedGraph.getNode(edge.getNode3().getGraph().getVoltageLevelInfos().getId());
                undirectedGraph.addEdge(new EdgeImpl(edge.toString() + "_2_3", node2, node3, 0, 1, false));
            }
        }

        // Creating the ForceAtlas and run the algorithm
        ForceAtlas2 forceAtlas2 = new ForceAtlas2Builder().buildLayout();
        forceAtlas2.setGraphModel(graphModel);
        forceAtlas2.resetPropertiesValues();
        forceAtlas2.setAdjustSizes(true);
        forceAtlas2.setOutboundAttractionDistribution(false);
        forceAtlas2.setEdgeWeightInfluence(1.5d);
        forceAtlas2.setGravity(10d);
        forceAtlas2.setJitterTolerance(.02);
        forceAtlas2.setScalingRatio(15.0);
        forceAtlas2.initAlgo();
        int maxSteps = 1000;

        for (int i = 0; i < maxSteps && forceAtlas2.canAlgo(); i++) {
            forceAtlas2.goAlgo();
        }
        forceAtlas2.endAlgo();

        // Memorizing the voltage levels coordinates calculated by the ForceAtlas algorithm
        Map<Graph, Coord> coordsVoltageLevels = new HashMap<>();
        for (Graph voltageLevelGraph : graph.getNodes()) {
            org.gephi.graph.api.Node n = undirectedGraph.getNode(voltageLevelGraph.getVoltageLevelInfos().getId());
            coordsVoltageLevels.put(voltageLevelGraph, new Coord(n.x(), n.y()));
        }

        // Creating and applying the voltage levels layout with these coordinates
        Map<Graph, VoltageLevelLayout> graphsLayouts = new HashMap<>();
        coordsVoltageLevels.entrySet().stream().forEach(e -> {
            VoltageLevelLayout vlLayout = vLayoutFactory.create(e.getKey());
            graphsLayouts.put(e.getKey(), vlLayout);
            vlLayout.run(layoutParameters);
        });

        // Changing the snakeline feeder cells direction using the coordinates calculated by the ForceAtlas algorithm
        changingCellsOrientation(graph, coordsVoltageLevels);

        // List of voltage levels sorted by ascending x value
        List<Graph> graphsX = coordsVoltageLevels.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e1.getValue().getX(), e2.getValue().getX()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // List of voltage levels sorted by ascending y value
        List<Graph> graphsY = coordsVoltageLevels.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e1.getValue().getY(), e2.getValue().getY()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Narrowing / Spreading the voltage levels in the horizontal direction
        // (if no compaction, one voltage level only in a column
        //  if horizontal compaction, a voltage level is positioned horizontally at the middle of the preceding voltage level)
        double graphX = getHorizontalSubstationPadding(layoutParameters);
        for (Graph g : graphsX) {
            g.setX(graphX);
            int maxH = g.getMaxH();
            graphX += layoutParameters.getInitialXBus() + (maxH + 2) * layoutParameters.getCellWidth()
                    + getHorizontalSubstationPadding(layoutParameters);
            if (compactionType == ForceSubstationLayoutFactory.CompactionType.HORIZONTAL) {
                graphX /= 2;
            }
        }

        // Narrowing / Spreading the voltage levels in the vertical direction
        // (if no compaction, one voltage level only in a line
        //  if vertical compaction, a voltage level is positioned vertically at the middle of the preceding voltage level)
        double graphY = getVerticalSubstationPadding(layoutParameters);
        for (Graph g : graphsY) {
            g.setY(graphY);
            int maxV = g.getMaxV();
            graphY += layoutParameters.getInitialYBus() + layoutParameters.getStackHeight()
                    + layoutParameters.getExternCellHeight()
                    + layoutParameters.getVerticalSpaceBus() * (maxV + 2)
                    + getVerticalSubstationPadding(layoutParameters);
            if (compactionType == ForceSubstationLayoutFactory.CompactionType.VERTICAL) {
                graphY /= 2;
            }
        }

        // Finally, running the voltage levels layout a second time with the new adapted voltage levels coordinates
        // (here, we keep the cells and blocks already detected before, and we only recompute the nodes coordinates)
        coordsVoltageLevels.keySet().stream().forEach(g -> {
            g.resetCoords();
            graphsLayouts.get(g).run(layoutParameters);
        });

        // Sorting the nodes in each edges, by ascending x value, for further snakelines coordinates computation
        graph.getEdges().stream().forEach(e -> e.getNodes().sort(Comparator.comparingDouble(Node::getX)));

        // Calculate all the coordinates for the links between the voltageLevel graphs
        // (new fictitious nodes and edges are created here, for the two and three windings transformers)
        manageSnakeLines(layoutParameters);
    }

    @Override
    protected void manageSnakeLines(LayoutParameters layoutParameters) {
        Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom = new HashMap<>();
        graph.getNodes().forEach(g -> nbSnakeLinesTopBottom.put(g.getVoltageLevelInfos().getId(), EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0))));
        Map<String, Integer> nbSnakeLinesBetween = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        List<TwtEdge> newEdges = new ArrayList<>();

        for (TwtEdge edge : graph.getEdges()) {
            if (edge.getNodes().size() == 2) {
                List<Double> pol = calculatePolylineSnakeLine(layoutParameters, edge.getNode1(), edge.getNode2(),
                        nbSnakeLinesTopBottom, nbSnakeLinesBetween);
                // we split the original edge in two parts, with a new fictitious node between the two new edges
                splitEdge2(edge, newEdges, pol);
            } else if (edge.getNodes().size() == 3) {
                List<Double> pol1 = calculatePolylineSnakeLine(layoutParameters, edge.getNode1(), edge.getNode2(),
                        nbSnakeLinesTopBottom, nbSnakeLinesBetween);
                List<Double> pol2 = calculatePolylineSnakeLine(layoutParameters, edge.getNode2(), edge.getNode3(),
                        nbSnakeLinesTopBottom, nbSnakeLinesBetween);
                // we split the original edge in three parts, with a new fictitious node between the three new edges
                splitEdge3(edge, newEdges, pol1, pol2);
            }
        }

        // replace the old edges by the new edges in the substation graph
        graph.setEdges(newEdges);
    }

    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParameters,
                                                      Node node1, Node node2,
                                                      Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom,
                                                      Map<String, Integer> nbSnakeLinesBetween) {
        ForceInfoCalcPoints info = new ForceInfoCalcPoints();
        info.setLayoutParam(layoutParameters);
        info.setVId1(node1.getGraph().getVoltageLevelInfos().getId());
        info.setVId2(node2.getGraph().getVoltageLevelInfos().getId());
        info.setdNode1(getNodeDirection(node1, 1));
        info.setdNode2(getNodeDirection(node2, 2));
        info.setNbSnakeLinesTopBottom(nbSnakeLinesTopBottom);
        info.setNbSnakeLinesBetween(nbSnakeLinesBetween);
        info.setX1(node1.getX());
        info.setX2(node2.getX());
        info.setY1(node1.getY());
        info.setInitY1(node1.getInitY() != -1 ? node1.getInitY() : node1.getY());
        info.setY2(node2.getY());
        info.setInitY2(node2.getInitY() != -1 ? node2.getInitY() : node2.getY());
        info.setxMaxGraph(Math.max(node1.getGraph().getX(), node2.getGraph().getX()));
        info.setIdMaxGraph(node1.getGraph().getX() > node2.getGraph().getX() ? node1.getGraph().getVoltageLevelInfos().getId() : node2.getGraph().getVoltageLevelInfos().getId());

        return calculatePolylinePoints(info);
    }

    public static List<Double> calculatePolylinePoints(ForceInfoCalcPoints info) {
        List<Double> pol = new ArrayList<>();

        LayoutParameters layoutParam = info.getLayoutParam();
        BusCell.Direction dNode1 = info.getdNode1();
        BusCell.Direction dNode2 = info.getdNode2();
        String vId1 = info.getVId1();
        String vId2 = info.getVId2();
        Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom = info.getNbSnakeLinesTopBottom();
        Map<String, Integer> nbSnakeLinesBetween = info.getNbSnakeLinesBetween();
        double x1 = info.getX1();
        double x2 = info.getX2();
        double y1 = info.getY1();
        double initY1 = info.getInitY1();
        double y2 = info.getY2();
        double initY2 = info.getInitY2();
        double xMaxGraph = info.getxMaxGraph();
        String idMaxGraph = info.getIdMaxGraph();

        switch (dNode1) {
            case BOTTOM:
                if (dNode2 == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                    nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                    double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                    double yDecal = Math.max(initY1 + decalV1, initY2 + decalV2);

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, yDecal,
                            x2, yDecal,
                            x2, y2));
                } else {  // BOTTOM to TOP
                    if (y1 < y2) {
                        nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                        double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                        double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                        double yDecal = Math.max(initY1 + decalV1, initY2 - decalV2);

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, yDecal,
                                x2, yDecal,
                                x2, y2));
                    } else {
                        nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                        nbSnakeLinesBetween.compute(idMaxGraph, (k, v) -> v + 1);

                        double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                        double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();
                        double xBetweenGraph = xMaxGraph - (nbSnakeLinesBetween.get(idMaxGraph) * layoutParam.getHorizontalSnakeLinePadding());

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, initY1 + decalV1,
                                xBetweenGraph, initY1 + decalV1,
                                xBetweenGraph, initY2 - decalV2,
                                x2, initY2 - decalV2,
                                x2, y2));
                    }
                }
                break;

            case TOP:
                if (dNode2 == BusCell.Direction.TOP) {  // TOP to TOP
                    nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                    nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                    double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                    double yDecal = Math.min(initY1 - decalV1, initY2 - decalV2);

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, yDecal,
                            x2, yDecal,
                            x2, y2));
                } else {  // TOP to BOTTOM
                    if (y1 > y2) {
                        nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                        double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                        double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                        double yDecal = Math.min(initY1 - decalV1, initY2 + decalV2);

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, yDecal,
                                x2, yDecal,
                                x2, y2));
                    } else {
                        nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                        nbSnakeLinesBetween.compute(idMaxGraph, (k, v) -> v + 1);
                        double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                        double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                        double xBetweenGraph = xMaxGraph - (nbSnakeLinesBetween.get(idMaxGraph) * layoutParam.getHorizontalSnakeLinePadding());

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, initY1 - decalV1,
                                xBetweenGraph, initY1 - decalV1,
                                xBetweenGraph, initY2 + decalV2,
                                x2, initY2 + decalV2,
                                x2, y2));
                    }
                }
                break;
            default:
        }
        return pol;
    }

    @Override
    protected List<Double> splitPolyline3(List<Double> pol1, List<Double> pol2, int numPart, Coord coord) {
        List<Double> res = new ArrayList<>();

        if (numPart == 1 || numPart == 2) {
            res = super.splitPolyline3(pol1, pol2, numPart, coord);
        } else {
            // the third new edge now begins with the fictitious node point
            res.add(pol1.get(pol1.size() - 4));
            res.add(pol1.get(pol1.size() - 3));
            // then we add an intermediate point with the absciss of the third point in the original second polyline
            // and the ordinate of the fictitious node
            res.add(pol2.get(4));
            res.add(pol1.get(pol1.size() - 3));
            // then we had the last three or two points of the original second polyline
            if (pol2.size() > 8) {
                res.addAll(pol2.subList(pol2.size() - 6, pol2.size()));
            } else {
                res.addAll(pol2.subList(pol2.size() - 2, pol2.size()));
            }
        }

        return res;
    }

    @Override
    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Node node1, Node node2,
                                                      InfosNbSnakeLines infosNbSnakeLines, boolean increment) {
        return Collections.emptyList();
    }

    @Override
    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParameters, Graph vlGraph) {
        return null;
    }

    @Override
    protected double getHorizontalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getHorizontalSubstationPadding();
    }

    @Override
    protected double getVerticalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getVerticalSubstationPadding();
    }

    private void changingCellsOrientation(SubstationGraph graph, Map<Graph, Coord> coordsVoltageLevels) {
        for (TwtEdge edge : graph.getEdges()) {
            FeederNode n1 = (FeederNode) edge.getNode1();
            ExternCell cell1 = (ExternCell) n1.getCell();
            FeederNode n2 = (FeederNode) edge.getNode2();
            ExternCell cell2 = (ExternCell) n2.getCell();

            Coord c1 = coordsVoltageLevels.get(n1.getGraph());
            Coord c2 = coordsVoltageLevels.get(n2.getGraph());

            if (c1.getY() < c2.getY()) {
                // cell for node 1 with bottom orientation
                // cell for node 2 with top orientation
                cell1.setDirection(BusCell.Direction.BOTTOM);
                n1.setDirection(BusCell.Direction.BOTTOM);
                cell2.setDirection(BusCell.Direction.TOP);
                n2.setDirection(BusCell.Direction.TOP);
            } else {
                // cell for node 1 with top orientation
                // cell for node 2 with bottom orientation
                cell1.setDirection(BusCell.Direction.TOP);
                n1.setDirection(BusCell.Direction.TOP);
                cell2.setDirection(BusCell.Direction.BOTTOM);
                n2.setDirection(BusCell.Direction.BOTTOM);
            }

            if (edge.getNode3() != null) {
                FeederNode n3 = (FeederNode) edge.getNode3();
                ExternCell cell3 = (ExternCell) n3.getCell();
                Coord c3 = coordsVoltageLevels.get(n3.getGraph());

                if (c3.getY() < c2.getY()) {
                    // cell for node 3 with bottom orientation
                    cell3.setDirection(BusCell.Direction.BOTTOM);
                    n3.setDirection(BusCell.Direction.BOTTOM);
                } else {
                    // cell for node 3 with top orientation
                    cell3.setDirection(BusCell.Direction.TOP);
                    n3.setDirection(BusCell.Direction.TOP);
                }
            }
        }
    }
}
