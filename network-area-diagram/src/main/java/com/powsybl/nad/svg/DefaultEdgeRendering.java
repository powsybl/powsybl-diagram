/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultEdgeRendering implements EdgeRendering {

    @Override
    public void run(Graph graph, SvgParameters svgParameters) {
        graph.getNonMultiBranchEdgesStream().forEach(edge -> computeSingleBranchEdgeCoordinates(graph, edge, svgParameters));
        graph.getMultiBranchEdgesStream().forEach(edges -> computeMultiBranchEdgesCoordinates(graph, edges, svgParameters));
        graph.getLoopBranchEdgesMap().forEach((node, edges) -> loopEdgesLayout(graph, node, edges, svgParameters));
        graph.getThreeWtNodesStream().forEach(threeWtNode -> computeThreeWtEdgeCoordinates(graph, threeWtNode, svgParameters));
        graph.getTextEdgesMap().forEach((edge, nodes) -> computeTextEdgeLayoutCoordinates(nodes.getFirst(), nodes.getSecond(), edge));
    }

    private void computeTextEdgeLayoutCoordinates(Node node1, Node node2, TextEdge edge) {
        edge.setPoints(node1.getPosition(), node2.getPosition());
    }

    private void computeSingleBranchEdgeCoordinates(Graph graph, BranchEdge edge, SvgParameters svgParameters) {
        Node node1 = graph.getBusGraphNode1(edge);
        Node node2 = graph.getBusGraphNode2(edge);

        Point direction1 = getDirection(node2, () -> graph.getNode2(edge));
        Point edgeStart1 = computeEdgeStart(node1, direction1, graph.getVoltageLevelNode1(edge), svgParameters);

        Point direction2 = getDirection(node1, () -> graph.getNode1(edge));
        Point edgeStart2 = computeEdgeStart(node2, direction2, graph.getVoltageLevelNode2(edge), svgParameters);

        Point middle = Point.createMiddlePoint(edgeStart1, edgeStart2);
        if (edge.isTransformerEdge()) {
            double radius = svgParameters.getTransformerCircleRadius();
            edge.setPoints1(edgeStart1, middle.atDistance(1.5 * radius, direction2));
            edge.setPoints2(edgeStart2, middle.atDistance(1.5 * radius, direction1));
        } else {
            edge.setPoints1(edgeStart1, middle);
            edge.setPoints2(edgeStart2, middle);
        }
    }

    private Point getDirection(Node directionBusGraphNode, Supplier<Node> vlNodeSupplier) {
        if (directionBusGraphNode == BusNode.UNKNOWN) {
            return vlNodeSupplier.get().getPosition();
        }
        return directionBusGraphNode.getPosition();
    }

    private Point computeEdgeStart(Node node, Point direction, VoltageLevelNode vlNode, SvgParameters svgParameters) {
        // If edge not connected to a bus node on that side, we use corresponding voltage level with specific extra radius
        if (node == BusNode.UNKNOWN && vlNode != null) {
            double unknownBusRadius = SvgWriter.getVoltageLevelCircleRadius(vlNode, svgParameters) + svgParameters.getUnknownBusNodeExtraRadius();
            return vlNode.getPosition().atDistance(unknownBusRadius, direction);
        }

        Point edgeStart = node.getPosition();
        if (node instanceof BusNode && vlNode != null) {
            double busAnnulusOuterRadius = SvgWriter.getBusAnnulusOuterRadius((BusNode) node, vlNode, svgParameters);
            edgeStart = edgeStart.atDistance(busAnnulusOuterRadius - svgParameters.getEdgeStartShift(), direction);
        }
        return edgeStart;
    }

    private void computeMultiBranchEdgesCoordinates(Graph graph, List<BranchEdge> edges, SvgParameters svgParameters) {
        BranchEdge firstEdge = edges.iterator().next();
        VoltageLevelNode nodeA = graph.getVoltageLevelNode1(firstEdge);
        VoltageLevelNode nodeB = graph.getVoltageLevelNode2(firstEdge);
        Point pointA = nodeA.getPosition();
        Point pointB = nodeB.getPosition();

        double dx = pointB.getX() - pointA.getX();
        double dy = pointB.getY() - pointA.getY();
        double angle = Math.atan2(dy, dx);

        int nbForks = edges.size();
        double forkAperture = svgParameters.getEdgesForkAperture();
        double forkLength = svgParameters.getEdgesForkLength();
        double angleStep = forkAperture / (nbForks - 1);

        int i = 0;
        for (BranchEdge edge : edges) {
            if (2 * i + 1 == nbForks) { // in the middle, hence alpha = 0
                computeSingleBranchEdgeCoordinates(graph, edge, svgParameters);
            } else {
                double alpha = -forkAperture / 2 + i * angleStep;
                double angleForkA = angle - alpha;
                double angleForkB = angle + Math.PI + alpha;

                Point forkA = pointA.shift(forkLength * Math.cos(angleForkA), forkLength * Math.sin(angleForkA));
                Point forkB = pointB.shift(forkLength * Math.cos(angleForkB), forkLength * Math.sin(angleForkB));
                Point middle = Point.createMiddlePoint(forkA, forkB);
                BranchEdge.Side sideA = graph.getNode1(edge) == nodeA ? BranchEdge.Side.ONE : BranchEdge.Side.TWO;

                computeHalfForkCoordinates(graph, svgParameters, nodeA, edge, forkA, middle, sideA);
                computeHalfForkCoordinates(graph, svgParameters, nodeB, edge, forkB, middle, sideA.getOpposite());
            }
            i++;
        }
    }

    private void computeHalfForkCoordinates(Graph graph, SvgParameters svgParameters, VoltageLevelNode node, BranchEdge edge, Point fork, Point middle, BranchEdge.Side side) {
        Node busNodeA = side == BranchEdge.Side.ONE ? graph.getBusGraphNode1(edge) : graph.getBusGraphNode2(edge);
        Point edgeStart = computeEdgeStart(busNodeA, fork, node, svgParameters);
        Point endFork = edge.isTransformerEdge()
                ? middle.atDistance(1.5 * svgParameters.getTransformerCircleRadius(), fork)
                : middle;
        edge.setPoints(side, edgeStart, fork, endFork);
    }

    private void loopEdgesLayout(Graph graph, VoltageLevelNode node, List<BranchEdge> loopEdges, SvgParameters svgParameters) {
        List<Double> angles = computeLoopAngles(graph, loopEdges, node, svgParameters);
        int i = 0;
        for (BranchEdge edge : loopEdges) {
            double angle = angles.get(i++);
            Point middle = node.getPosition().atDistance(svgParameters.getLoopDistance(), angle);
            loopEdgesHalfLayout(graph, node, svgParameters, edge, BranchEdge.Side.ONE, angle, middle);
            loopEdgesHalfLayout(graph, node, svgParameters, edge, BranchEdge.Side.TWO, angle, middle);
        }
    }

    private void loopEdgesHalfLayout(Graph graph, VoltageLevelNode node, SvgParameters svgParameters,
                                     BranchEdge edge, BranchEdge.Side side, double angle, Point middle) {

        int sideSign = side == BranchEdge.Side.ONE ? -1 : 1;
        double startAngle = angle + sideSign * svgParameters.getLoopEdgesAperture() / 2;
        double radius = svgParameters.getTransformerCircleRadius();
        double controlsDist = svgParameters.getLoopControlDistance();
        boolean isTransformer = edge.isTransformerEdge();
        double endAngle = angle + sideSign * Math.PI / 2;

        Point fork = node.getPosition().atDistance(svgParameters.getEdgesForkLength(), startAngle);
        Point edgeStart = computeEdgeStart(graph.getBusGraphNode(edge, side), fork, node, svgParameters);
        Point control1a = fork.atDistance(controlsDist, startAngle);
        Point middle1 = isTransformer ? middle.atDistance(1.5 * radius, endAngle) : middle;
        Point control1b = middle1.atDistance(isTransformer ? Math.max(0, controlsDist - 1.5 * radius) : controlsDist, endAngle);

        edge.setPoints(side, edgeStart, fork, control1a, control1b, middle1);
    }

    private List<Double> computeLoopAngles(Graph graph, List<BranchEdge> loopEdges, Node node, SvgParameters svgParameters) {
        int nbLoops = loopEdges.size();

        List<Double> anglesOtherEdges = graph.getBranchEdgeStream(node)
                .filter(e -> !loopEdges.contains(e))
                .mapToDouble(e -> getAngle(e, graph, node))
                .sorted().boxed().collect(Collectors.toList());

        List<Double> loopAngles = new ArrayList<>();
        if (!anglesOtherEdges.isEmpty()) {
            anglesOtherEdges.add(anglesOtherEdges.get(0) + 2 * Math.PI);
            double apertureWithMargin = svgParameters.getLoopEdgesAperture() * 1.2;

            double[] deltaAngles = new double[anglesOtherEdges.size() - 1];
            int nbSeparatedSlots = 0;
            int nbSharedSlots = 0;
            for (int i = 0; i < anglesOtherEdges.size() - 1; i++) {
                deltaAngles[i] = anglesOtherEdges.get(i + 1) - anglesOtherEdges.get(i);
                nbSeparatedSlots += deltaAngles[i] > apertureWithMargin ? 1 : 0;
                nbSharedSlots += Math.floor(deltaAngles[i] / apertureWithMargin);
            }

            List<Integer> sortedIndices = IntStream.range(0, deltaAngles.length)
                    .boxed().sorted(Comparator.comparingDouble(i -> deltaAngles[i]))
                    .collect(Collectors.toList());

            if (nbLoops <= nbSeparatedSlots) {
                // Place loops in "slots" separated by non-loop edges
                computeLoopAnglesWhenEnoughSeparatedSlotsPresent(sortedIndices, nbLoops, anglesOtherEdges, loopAngles);
            } else if (nbLoops <= nbSharedSlots) {
                // Place the maximum of loops in "slots" separated by non-loop edges, and put the excessive ones in the bigger "slots"
                int nbExcessiveRemaining = nbLoops - nbSeparatedSlots;
                computeLoopAnglesWhenEnoughSharedSlotsPresent(nbExcessiveRemaining, sortedIndices, deltaAngles, apertureWithMargin, svgParameters, anglesOtherEdges, loopAngles);
            } else {
                // Not enough place in the slots: dividing the circle in nbLoops, starting in the middle of the biggest slot
                int iMaxDelta = sortedIndices.get(sortedIndices.size() - 1);
                double startAngle = (anglesOtherEdges.get(iMaxDelta) + anglesOtherEdges.get(iMaxDelta + 1)) / 2;
                IntStream.range(0, nbLoops).mapToDouble(i -> startAngle + i * 2 * Math.PI / nbLoops).forEach(loopAngles::add);
            }

        } else {
            // No other edges: dividing the circle in nbLoops
            IntStream.range(0, nbLoops).mapToDouble(i -> i * 2 * Math.PI / nbLoops).forEach(loopAngles::add);
        }

        return loopAngles;
    }

    private void computeLoopAnglesWhenEnoughSeparatedSlotsPresent(List<Integer> sortedIndices, int nbLoops, List<Double> anglesOtherEdges,
                                                                  List<Double> loopAngles) {
        for (int i = sortedIndices.size() - nbLoops; i < sortedIndices.size(); i++) {
            int iSorted = sortedIndices.get(i);
            loopAngles.add((anglesOtherEdges.get(iSorted) + anglesOtherEdges.get(iSorted + 1)) / 2);
        }
    }

    private void computeLoopAnglesWhenEnoughSharedSlotsPresent(int initNbExcessiveRemaining, List<Integer> sortedIndices,
                                                               double[] deltaAngles, double apertureWithMargin,
                                                               SvgParameters svgParameters, List<Double> anglesOtherEdges,
                                                               List<Double> loopAngles) {
        int nbExcessiveRemaining = initNbExcessiveRemaining;
        for (int i = sortedIndices.size() - 1; i >= 0; i--) {
            int iSorted = sortedIndices.get(i);
            int nbAvailableSlots = (int) Math.floor(deltaAngles[iSorted] / apertureWithMargin);
            if (nbAvailableSlots == 0) {
                break;
            }
            int nbLoopsInDelta = Math.min(nbAvailableSlots, nbExcessiveRemaining + 1);
            double extraSpace = deltaAngles[iSorted] - svgParameters.getLoopEdgesAperture() * nbLoopsInDelta; // extra space without margins
            double intraSpace = extraSpace / (nbLoopsInDelta + 1); // space between two loops and between non-loop edges and first/last loop
            double angleStep = (anglesOtherEdges.get(iSorted + 1) - anglesOtherEdges.get(iSorted) - intraSpace) / nbLoopsInDelta;
            double startAngle = anglesOtherEdges.get(iSorted) + intraSpace / 2 + angleStep / 2;
            IntStream.range(0, nbLoopsInDelta).mapToDouble(iLoop -> startAngle + iLoop * angleStep).forEach(loopAngles::add);
            nbExcessiveRemaining -= nbLoopsInDelta - 1;
        }
    }

    private double getAngle(BranchEdge edge, Graph graph, Node node) {
        BranchEdge.Side side = graph.getNode1(edge) == node ? BranchEdge.Side.ONE : BranchEdge.Side.TWO;
        return edge.getEdgeStartAngle(side);
    }

    private void computeThreeWtEdgeCoordinates(Graph graph, ThreeWtNode threeWtNode, SvgParameters svgParameters) {
        // The 3wt edges are computed by finding the "leading" edge and then placing the other edges at 120°
        // The leading edge is chosen to be the opposite edge of the smallest aperture.
        List<ThreeWtEdge> edges = graph.getThreeWtEdgeStream(threeWtNode).collect(Collectors.toList());
        List<Double> angles = edges.stream()
                .map(edge -> computeEdgeStart(graph.getBusGraphNode(edge), threeWtNode.getPosition(), graph.getVoltageLevelNode(edge), svgParameters))
                .map(edgeStart -> threeWtNode.getPosition().getAngle(edgeStart))
                .collect(Collectors.toList());
        List<Integer> sortedIndices = IntStream.range(0, 3)
                .boxed().sorted(Comparator.comparingDouble(angles::get))
                .collect(Collectors.toList());

        int leadingSortedIndex = getSortedIndexMaximumAperture(angles);
        double leadingAngle = angles.get(sortedIndices.get(leadingSortedIndex));

        List<ThreeWtEdge> edgesSorted = IntStream.range(0, 3)
                .map(i -> (leadingSortedIndex + i) % 3)
                .map(sortedIndices::get)
                .mapToObj(edges::get)
                .collect(Collectors.toList());
        double dNodeToAnchor = svgParameters.getTransformerCircleRadius() * 1.6;
        for (int i = 0; i < edgesSorted.size(); i++) {
            ThreeWtEdge edge = edgesSorted.get(i);
            Point edgeStart = computeEdgeStart(graph.getBusGraphNode(edge), threeWtNode.getPosition(), graph.getVoltageLevelNode(edge), svgParameters);
            double anchorAngle = leadingAngle + i * 2 * Math.PI / 3;
            Point threeWtAnchor = threeWtNode.getPosition().shiftRhoTheta(dNodeToAnchor, anchorAngle);
            edge.setPoints(edgeStart, threeWtAnchor);
        }
    }

    private int getSortedIndexMaximumAperture(List<Double> angles) {
        // Sorting the given angles
        List<Double> sortedAngles = angles.stream().sorted().collect(Collectors.toList());

        // Then calculating the apertures
        sortedAngles.add(sortedAngles.get(0) + 2 * Math.PI);
        double[] deltaAngles = new double[3];
        for (int i = 0; i < 3; i++) {
            deltaAngles[i] = sortedAngles.get(i + 1) - sortedAngles.get(i);
        }

        // Returning the (sorted) index of the angle facing the minimal aperture
        int minDeltaIndex = IntStream.range(0, 3)
                .boxed().min(Comparator.comparingDouble(i -> deltaAngles[i]))
                .orElse(0);
        return ((minDeltaIndex - 1) + 3) % 3;
    }
}
