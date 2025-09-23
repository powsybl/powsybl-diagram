/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nad.routing;

import com.powsybl.commons.PowsyblException;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Injection;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.model.TextEdge;
import com.powsybl.nad.model.TextNode;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.model.ThreeWtNode;
import com.powsybl.nad.model.VoltageLevelNode;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.utils.RadiusUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractEdgeRouting implements EdgeRouting {

    @Override
    public void run(Graph graph, SvgParameters svgParameters) {
        graph.getNonMultiBranchEdgesStream().forEach(edge -> computeSingleBranchEdgeCoordinates(graph, edge, svgParameters));
        graph.getMultiBranchEdgesStream().forEach(edges -> computeMultiBranchEdgesCoordinates(graph, edges, svgParameters));
        graph.getThreeWtNodesStream().forEach(threeWtNode -> computeThreeWtEdgeCoordinates(graph, threeWtNode, svgParameters));
        graph.getLoopBranchEdgesMap().forEach((node, edges) -> loopEdgesLayout(graph, node, edges, svgParameters));
        graph.getTextEdgesMap().forEach((edge, nodes) -> computeTextEdgeLayoutCoordinates(nodes.getFirst(), nodes.getSecond(), edge, svgParameters));
        graph.getVoltageLevelNodesStream().forEach(vln -> injectionEdgesLayout(graph, vln, svgParameters));
    }

    protected abstract void computeSingleBranchEdgeCoordinates(Graph graph, BranchEdge edge, SvgParameters svgParameters);

    protected abstract void computeMultiBranchEdgesCoordinates(Graph graph, List<BranchEdge> edges, SvgParameters svgParameters);

    protected abstract void computeTextEdgeLayoutCoordinates(VoltageLevelNode voltageLevelNode, TextNode textNode, TextEdge edge, SvgParameters svgParameters);

    protected Point computeEdgeStart(Node node, Point direction, VoltageLevelNode vlNode, SvgParameters svgParameters) {
        // If edge not connected to a bus node on that side, we use corresponding voltage level with specific extra radius
        if (node == BusNode.UNKNOWN && vlNode != null) {
            double unknownBusRadius = RadiusUtils.getVoltageLevelCircleRadius(vlNode, svgParameters) + svgParameters.getUnknownBusNodeExtraRadius();
            return vlNode.getPosition().atDistance(unknownBusRadius, direction);
        }

        Point edgeStart = node.getPosition();
        if (node instanceof BusNode busNode && vlNode != null) {
            double busAnnulusOuterRadius = RadiusUtils.getBusAnnulusOuterRadius(busNode, vlNode, svgParameters);
            edgeStart = edgeStart.atDistance(busAnnulusOuterRadius - svgParameters.getEdgeStartShift(), direction);
        }
        return edgeStart;
    }

    protected void loopEdgesLayout(Graph graph, VoltageLevelNode node, List<BranchEdge> loopEdges, SvgParameters svgParameters) {
        List<Double> angles = computeLoopAngles(graph, loopEdges, node, svgParameters);
        int i = 0;
        for (BranchEdge edge : loopEdges) {
            double angle = angles.get(i++);
            Point middle = node.getPosition().atDistance(svgParameters.getLoopDistance(), angle);
            loopEdgesHalfLayout(graph, node, svgParameters, edge, BranchEdge.Side.ONE, angle, middle);
            loopEdgesHalfLayout(graph, node, svgParameters, edge, BranchEdge.Side.TWO, angle, middle);
        }
    }

    protected void injectionEdgesLayout(Graph graph, VoltageLevelNode node, SvgParameters svgParameters) {
        List<Double> angles = null;
        int i = 0;
        for (BusNode busNode : node.getBusNodes()) {
            for (Injection injection : busNode.getInjections()) {
                if (angles == null) { // lazy computed, to avoid computing angles if no injections
                    angles = computeInjectionAngles(graph, node, svgParameters);
                }
                Double angle = angles.get(i++);
                injection.setAngle(angle);
                Point injPoint = node.getPosition().atDistance(svgParameters.getInjectionEdgeLength(), angle);
                Point busNodePoint = computeEdgeStart(busNode, injPoint, node, svgParameters);
                injection.setEdge(busNodePoint, injPoint);
                injection.setArrowPoint(getArrowCenter(node, busNode, injection.getEdge(), svgParameters));
            }
        }
    }

    private void loopEdgesHalfLayout(Graph graph, VoltageLevelNode node, SvgParameters svgParameters,
                                     BranchEdge edge, BranchEdge.Side side, double angle, Point middle) {

        int sideSign = side == BranchEdge.Side.ONE ? -1 : 1;
        double startAngle = angle + sideSign * Math.toRadians(svgParameters.getLoopEdgesAperture() / 2);
        double radius = svgParameters.getTransformerCircleRadius();
        double controlsDist = svgParameters.getLoopControlDistance();
        boolean isTransformer = edge.isTransformerEdge();
        double endAngle = angle + sideSign * Math.PI / 2;

        Point fork = node.getPosition().atDistance(svgParameters.getEdgesForkLength(), startAngle);
        BusNode busGraphNode = graph.getBusGraphNode(edge, side);
        Point edgeStart = computeEdgeStart(busGraphNode, fork, node, svgParameters);
        Point control1a = fork.atDistance(controlsDist, startAngle);
        Point middle1 = isTransformer ? middle.atDistance(1.5 * radius, endAngle) : middle;
        Point control1b = middle1.atDistance(isTransformer ? Math.max(0, controlsDist - 1.5 * radius) : controlsDist, endAngle);

        edge.setPoints(side, edgeStart, fork, control1a, control1b, middle1);
        edge.setArrow(side, getArrowCenter(node, busGraphNode, edge.getPoints(side), svgParameters));
        edge.setArrowAngle(side, edge.getEdgeStartAngle(side));
    }

    private List<Double> computeLoopAngles(Graph graph, List<BranchEdge> loopEdges, Node node, SvgParameters svgParameters) {
        List<Double> anglesOtherEdges = graph.getEdgeStream(node)
                .filter(e -> !loopEdges.contains(e))
                .mapToDouble(e -> getAngle(e, graph, node))
                .sorted().boxed().collect(Collectors.toList());
        return findAvailableAngles(anglesOtherEdges, loopEdges.size(), svgParameters.getLoopEdgesAperture() * 1.2);
    }

    private List<Double> computeInjectionAngles(Graph graph, VoltageLevelNode vlNode, SvgParameters svgParameters) {
        List<Double> anglesOtherEdges = graph.getEdgeStream(vlNode)
                .mapToDouble(e -> getAngle(e, graph, vlNode))
                .sorted().boxed().collect(Collectors.toList());
        int nbInjections = vlNode.getBusNodeStream().mapToInt(BusNode::getInjectionCount).sum();
        return findAvailableAngles(anglesOtherEdges, nbInjections, svgParameters.getInjectionAperture());
    }

    private List<Double> findAvailableAngles(List<Double> anglesOtherEdges, int nbAngles, double slotAperture) {
        if (!anglesOtherEdges.isEmpty()) {
            anglesOtherEdges.add(anglesOtherEdges.get(0) + 2 * Math.PI);

            double[] deltaAngles = new double[anglesOtherEdges.size() - 1];
            int[] nbAvailableSlots = new int[anglesOtherEdges.size() - 1];
            double totalDeltaAvailable = 0.;
            for (int i = 0; i < anglesOtherEdges.size() - 1; i++) {
                deltaAngles[i] = anglesOtherEdges.get(i + 1) - anglesOtherEdges.get(i);
                nbAvailableSlots[i] = (int) Math.floor(deltaAngles[i] / Math.toRadians(slotAperture));
                if (nbAvailableSlots[i] > 0) {
                    totalDeltaAvailable += deltaAngles[i];
                }
            }

            if (nbAngles <= Arrays.stream(nbAvailableSlots).sum() && totalDeltaAvailable > 0) {
                // Insert the angles in the "slots" separated by other edges which are large enough
                int[] nbInsertedAngles = computeAnglesInsertedNumber(nbAngles, nbAvailableSlots, totalDeltaAvailable, deltaAngles);
                return calculateInsertedAngles(nbInsertedAngles, deltaAngles, slotAperture, anglesOtherEdges);
            } else {
                // Not enough place in the slots: dividing the circle in nbAngles, starting in the middle of the biggest slot
                int iMaxDelta = IntStream.range(0, deltaAngles.length).boxed()
                        .max(Comparator.comparingDouble(i -> deltaAngles[i]))
                        .orElse(0);
                double startAngle = (anglesOtherEdges.get(iMaxDelta) + anglesOtherEdges.get(iMaxDelta + 1)) / 2;
                return IntStream.range(0, nbAngles).mapToDouble(i -> startAngle + i * 2 * Math.PI / nbAngles).boxed().toList();
            }

        } else {
            // No other edges: dividing the circle in nbAngles
            return IntStream.range(0, nbAngles).mapToDouble(i -> i * 2 * Math.PI / nbAngles).boxed().toList();
        }
    }

    private int[] computeAnglesInsertedNumber(int nbAngles, int[] nbAvailableSlots, double totalDeltaAvailable, double[] deltaAngles) {
        int[] nbInsertedAngles = new int[deltaAngles.length];
        for (int i = 0; i < deltaAngles.length; i++) {
            double deltaAngleNormalized = deltaAngles[i] / totalDeltaAvailable;
            double nbSlotsFractions = deltaAngleNormalized * nbAngles;
            int nbSlotsCeil = (int) Math.ceil(nbSlotsFractions);
            if (nbSlotsCeil <= nbAvailableSlots[i]) {
                nbInsertedAngles[i] = nbSlotsCeil;
            } else {
                nbInsertedAngles[i] = nbSlotsCeil - 1;
            }
        }

        int totalInsertedAngles = Arrays.stream(nbInsertedAngles).sum();
        if (totalInsertedAngles > nbAngles) {
            // Too many slots found: remove slots taken starting from the smallest sliced intervals
            List<Integer> sortedIndices = IntStream.range(0, deltaAngles.length).boxed()
                    .sorted(Comparator.comparingDouble(i -> deltaAngles[i] / nbInsertedAngles[i])).toList();
            int nbExcessiveAngles = totalInsertedAngles - nbAngles;
            for (int iSorted : sortedIndices) {
                nbInsertedAngles[iSorted]--;
                if (--nbExcessiveAngles == 0) {
                    break;
                }
            }
        }

        return nbInsertedAngles;
    }

    private List<Double> calculateInsertedAngles(int[] nbInsertedAngles, double[] deltaAngles, double slotAperture, List<Double> anglesOtherEdges) {
        List<Double> insertedAngles = new ArrayList<>();
        for (int i = 0; i < nbInsertedAngles.length; i++) {
            int nbAnglesInDelta = nbInsertedAngles[i];
            if (nbAnglesInDelta == 0) {
                continue;
            }
            double extraSpace = deltaAngles[i] - Math.toRadians(slotAperture) * nbAnglesInDelta;
            double intraSpace = extraSpace / (nbAnglesInDelta + 1); // space between two added angles and between other edges and first/last angle
            double angleStep = intraSpace + Math.toRadians(slotAperture);
            double startAngle = anglesOtherEdges.get(i) + intraSpace + Math.toRadians(slotAperture) / 2;
            IntStream.range(0, nbAnglesInDelta).mapToDouble(iLoop -> startAngle + iLoop * angleStep).forEach(insertedAngles::add);
        }
        return insertedAngles;
    }

    private double getAngle(Edge edge, Graph graph, Node node) {
        if (edge instanceof BranchEdge branchEdge) {
            BranchEdge.Side side = graph.getNode1(edge) == node ? BranchEdge.Side.ONE : BranchEdge.Side.TWO;
            return branchEdge.getEdgeStartAngle(side);
        } else if (edge instanceof ThreeWtEdge threeWtEdge) {
            return threeWtEdge.getEdgeAngle();
        }
        throw new PowsyblException("Unexpected edge type: " + edge.getClass().getName());
    }

    protected void computeThreeWtEdgeCoordinates(Graph graph, ThreeWtNode threeWtNode, SvgParameters svgParameters) {
        // The 3wt edges are computed by finding the "leading" edge and then placing the other edges at 120°
        // The leading edge is chosen to be the opposite edge of the smallest aperture.
        List<ThreeWtEdge> edges = graph.getThreeWtEdgeStream(threeWtNode).toList();
        List<Double> angles = edges.stream()
                .map(edge -> computeEdgeStart(graph.getBusGraphNode(edge), threeWtNode.getPosition(), graph.getVoltageLevelNode(edge), svgParameters))
                .map(edgeStart -> threeWtNode.getPosition().getAngle(edgeStart))
                .toList();
        List<Integer> sortedIndices = IntStream.range(0, 3)
                .boxed().sorted(Comparator.comparingDouble(angles::get))
                .toList();

        int leadingSortedIndex = getSortedIndexMaximumAperture(angles);
        double leadingAngle = angles.get(sortedIndices.get(leadingSortedIndex));

        List<ThreeWtEdge> edgesSorted = IntStream.range(0, 3)
                .map(i -> (leadingSortedIndex + i) % 3)
                .map(sortedIndices::get)
                .mapToObj(edges::get)
                .toList();
        double dNodeToAnchor = svgParameters.getTransformerCircleRadius() * 1.6;
        for (int i = 0; i < edgesSorted.size(); i++) {
            ThreeWtEdge edge = edgesSorted.get(i);
            VoltageLevelNode voltageLevelNode = graph.getVoltageLevelNode(edge);
            BusNode busGraphNode = graph.getBusGraphNode(edge);
            Point edgeStart = computeEdgeStart(busGraphNode, threeWtNode.getPosition(), voltageLevelNode, svgParameters);
            double anchorAngle = leadingAngle + i * 2 * Math.PI / 3;
            Point threeWtAnchor = threeWtNode.getPosition().shiftRhoTheta(dNodeToAnchor, anchorAngle);
            edge.setPoints(edgeStart, threeWtAnchor);
            edge.setArrowPoint(getArrowCenter(voltageLevelNode, busGraphNode, edge.getPoints(), svgParameters));
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

    protected Point getArrowCenter(VoltageLevelNode vlNode, BusNode busNode, List<Point> line, SvgParameters svgParameters) {
        double nodeOuterRadius = RadiusUtils.getVoltageLevelCircleRadius(vlNode, svgParameters);
        double busAnnulusOuterRadius = RadiusUtils.getBusAnnulusOuterRadius(busNode, vlNode, svgParameters);
        double shift = svgParameters.getArrowShift() + nodeOuterRadius - busAnnulusOuterRadius;
        return line.get(0).atDistance(shift, line.get(1));
    }
}
