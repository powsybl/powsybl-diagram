/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.color.BaseVoltageColor;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.Node.NodeType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 */
public class TopologicalStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    private final Map<String, Map<String, RGBColor>> voltageLevelColorMap = new HashMap<>();

    public TopologicalStyleProvider(Network network) {
        this(BaseVoltageColor.fromPlatformConfig(), network);
    }

    public TopologicalStyleProvider(BaseVoltageColor baseVoltageColor, Network network) {
        super(baseVoltageColor, network);
    }

    @Override
    public void reset() {
        voltageLevelColorMap.clear();
    }

    private Map<String, RGBColor> getColorMap(VoltageLevel vl) {
        String basecolor = getBaseColor(vl.getNominalV(), PROFILE);

        AtomicInteger idxColor = new AtomicInteger(0);

        List<Bus> buses = vl.getBusView().getBusStream().collect(Collectors.toList());

        Map<String, RGBColor> colorMap = new HashMap<>();

        HSLColor color = HSLColor.parse(basecolor);

        List<RGBColor> colors = color.getColorGradient(buses.size());

        buses.forEach(b -> {
            RGBColor c = colors.get(idxColor.getAndIncrement());

            b.visitConnectedEquipments(new TopologyVisitor() {
                @Override
                public void visitBusbarSection(BusbarSection e) {
                    colorMap.put(getColorMapKey(e.getId()), c);
                }

                @Override
                public void visitDanglingLine(DanglingLine e) {
                    colorMap.put(getColorMapKey(e.getId()), c);
                }

                @Override
                public void visitGenerator(Generator e) {
                    colorMap.put(getColorMapKey(e.getId()), c);
                }

                @Override
                public void visitLine(Line e, Side s) {
                    colorMap.put(getColorMapKey(e.getId(), s), c);
                }

                @Override
                public void visitLoad(Load e) {
                    colorMap.put(getColorMapKey(e.getId()), c);
                }

                @Override
                public void visitShuntCompensator(ShuntCompensator e) {
                    colorMap.put(getColorMapKey(e.getId()), c);
                }

                @Override
                public void visitStaticVarCompensator(StaticVarCompensator e) {
                    colorMap.put(getColorMapKey(e.getId()), c);
                }

                @Override
                public void visitThreeWindingsTransformer(ThreeWindingsTransformer e,
                                                          ThreeWindingsTransformer.Side s) {
                    colorMap.put(getColorMapKey(e.getId(), s), c);
                }

                @Override
                public void visitTwoWindingsTransformer(TwoWindingsTransformer e, Side s) {
                    colorMap.put(getColorMapKey(e.getId(), s), c);
                }
            });
        });

        return colorMap;
    }

    private String getColorMapKey(String equipmentId) {
        return equipmentId;
    }

    private String getColorMapKey(String equipmentId, Side s) {
        return equipmentId + "_" + s.name();
    }

    private String getColorMapKey(String equipmentId, ThreeWindingsTransformer.Side s) {
        return equipmentId + "_" + s.name();
    }

    private String getColorMapKey(Node node) {
        return node instanceof FeederWithSideNode ? node.getEquipmentId() + "_" + ((FeederWithSideNode) node).getSide().name() : node.getEquipmentId();
    }

    private RGBColor getNodeRgbColor(VoltageLevelInfos voltageLevelInfos, Node node) {
        VoltageLevel vl = network.getVoltageLevel(voltageLevelInfos.getId());
        Map<String, RGBColor> colorMap = voltageLevelColorMap.computeIfAbsent(vl.getId(), k -> getColorMap(vl));
        return colorMap.get(getColorMapKey(node));
    }

    private Set<Node> findConnectedNodes(Node node) {
        Set<Node> visitedNodes = new HashSet<>();
        findConnectedNodes(node, visitedNodes);
        return visitedNodes;
    }

    private void findConnectedNodes(Node node, Set<Node> visitedNodes) {
        if (visitedNodes.contains(node)) {
            return;
        }
        if (node.getType() == NodeType.SWITCH && node.isOpen()) {
            return;
        }
        visitedNodes.add(node);
        for (Node adjNode : node.getAdjacentNodes()) {
            findConnectedNodes(adjNode, visitedNodes);
        }
    }

    private RGBColor getSmartNodeColor(VoltageLevelInfos voltageLevelInfos, Node node) {
        if (node.getType() == NodeType.SWITCH && node.isOpen()) {
            return null;
        }
        RGBColor rgbColor = getNodeRgbColor(voltageLevelInfos, node);
        if (rgbColor != null) {
            return rgbColor;
        }
        Set<Node> connectedNodes = findConnectedNodes(node);
        for (Node connectedNode : connectedNodes) {
            rgbColor = getNodeRgbColor(voltageLevelInfos, connectedNode);
            if (rgbColor != null) {
                return rgbColor;
            }
        }
        return null;
    }

    @Override
    protected String getEdgeColor(Node node1, Node node2) {
        if (node1.getType() == NodeType.SWITCH && node1.isOpen()) {
            return node2.getVoltageLevelInfos() != null ? getNodeColor(node2.getVoltageLevelInfos(), node2) : null;
        }
        if (node2.getType() == NodeType.SWITCH && node2.isOpen()) {
            return node1.getVoltageLevelInfos() != null ? getNodeColor(node1.getVoltageLevelInfos(), node1) : null;
        }

        return super.getEdgeColor(node1, node2);
    }

    @Override
    protected String getEdgeColor(Edge edge) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();
        String color = getEdgeColor(n1, n2);

        if (disconnectedColor.equals(color) && edge instanceof LineEdge) {
            color = getEdgeColor(n2, n1);
        }

        return color;
    }

    @Override
    public String getNodeColor(VoltageLevelInfos voltageLevelInfos, Node node) {
        RGBColor rgbColor = getSmartNodeColor(voltageLevelInfos, node);
        return rgbColor != null ? rgbColor.toString() : disconnectedColor;
    }
}
