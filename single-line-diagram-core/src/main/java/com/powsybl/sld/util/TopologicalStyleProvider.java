/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Node.NodeType;
import com.powsybl.sld.model.VoltageLevelInfos;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 */
public class TopologicalStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    private final Network network;

    private final Map<String, Map<String, RGBColor>> voltageLevelColorMap = new HashMap<>();

    public TopologicalStyleProvider(Path config, Network network) {
        super(config);
        this.network = network;
    }

    @Override
    public void reset() {
        voltageLevelColorMap.clear();
    }

    /**
     * For a given voltage level, calculate mapping between equipment id and color.
     */
    private Map<String, RGBColor> calculateNodeColorMap(VoltageLevel vl) {
        String baseColor = getBaseColor(vl.getNominalV(), PROFILE);

        AtomicInteger idxColor = new AtomicInteger(0);

        List<Bus> buses = vl.getBusView().getBusStream().collect(Collectors.toList());

        Map<String, RGBColor> colorMap = new HashMap<>();

        HSLColor color = HSLColor.parse(baseColor);

        List<RGBColor> colors = color.getColorGradient(buses.size());

        buses.forEach(b -> {
            RGBColor c = colors.get(idxColor.getAndIncrement());

            b.visitConnectedEquipments(new TopologyVisitor() {
                @Override
                public void visitBusbarSection(BusbarSection e) {
                    colorMap.put(e.getId(), c);
                }

                @Override
                public void visitDanglingLine(DanglingLine e) {
                    colorMap.put(e.getId(), c);
                }

                @Override
                public void visitGenerator(Generator e) {
                    colorMap.put(e.getId(), c);
                }

                @Override
                public void visitLine(Line e, Side s) {
                    colorMap.put(e.getId(), c);
                }

                @Override
                public void visitLoad(Load e) {
                    colorMap.put(e.getId(), c);
                }

                @Override
                public void visitShuntCompensator(ShuntCompensator e) {
                    colorMap.put(e.getId(), c);
                }

                @Override
                public void visitStaticVarCompensator(StaticVarCompensator e) {
                    colorMap.put(e.getId(), c);
                }

                @Override
                public void visitThreeWindingsTransformer(ThreeWindingsTransformer e,
                                                          ThreeWindingsTransformer.Side s) {
                    colorMap.put(e.getId(), c);
                }

                @Override
                public void visitTwoWindingsTransformer(TwoWindingsTransformer e, Side s) {
                    colorMap.put(e.getId(), c);
                }
            });
        });
        System.out.println(colorMap);
        return colorMap;
    }

    private RGBColor getNodeColor(Node node, VoltageLevelInfos voltageLevelInfos) {
        VoltageLevel vl = network.getVoltageLevel(voltageLevelInfos.getId());
        return voltageLevelColorMap.computeIfAbsent(vl.getId(), k -> calculateNodeColorMap(vl)).get(node.getEquipmentId());
    }

    @Override
    protected String getColor(Node node, VoltageLevelInfos voltageLevelInfos) {
        RGBColor c = getNodeColor(node, voltageLevelInfos);
        if (c != null) {
            return c.toString();
        }
        for (Node connectedNode : findConnectedNodes(node)) {
            c = getNodeColor(connectedNode, voltageLevelInfos);
            if (c != null) {
                return c.toString();
            }
        }
        return disconnectedColor;
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
}
