/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Node.NodeType;
import com.powsybl.sld.model.TwtEdge;
import com.powsybl.sld.svg.DiagramStyles;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.svg.DiagramStyles.escapeId;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class TopologicalStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    private final Network network;

    private final HashMap<String, HashMap<String, RGBColor>> voltageLevelColorMap = new HashMap<>();

    public TopologicalStyleProvider(Path config, Network network) {
        super(config);
        this.network = network;
    }

    @Override
    public void reset() {
        voltageLevelColorMap.clear();
    }

    private RGBColor getBusColor(Node node) {
        String id = node.getId();
        // il faut enlever le side de l'id du node pour retrouver l'id du composant
        if (node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER)) {
            id = id.substring(0, id.lastIndexOf('_'));
        }
        VoltageLevel vl = network.getVoltageLevel(node.getGraph().getVoltageLevelInfos().getId());
        return voltageLevelColorMap.computeIfAbsent(vl.getId(), k -> getColorMap(vl)).get(id);
    }

    private HashMap<String, RGBColor> getColorMap(VoltageLevel vl) {
        String basecolor = getBaseColor(vl.getNominalV(), PROFILE);

        AtomicInteger idxColor = new AtomicInteger(0);
        long buses = vl.getBusView().getBusStream().count();

        HashMap<String, RGBColor> colorMap = new HashMap<>();

        HSLColor color = HSLColor.parse(basecolor);

        List<RGBColor> colors = color.getColorGradient((int) buses);

        vl.getBusView().getBuses().forEach(b -> {
            RGBColor c = colors.get(idxColor.getAndIncrement());

            vl.getBusView().getBus(b.getId()).visitConnectedEquipments(new TopologyVisitor() {
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
        return colorMap;
    }

    @Override
    public Optional<String> getNodeStyle(Node node, boolean avoidSVGComponentsDuplication, boolean isShowInternalNodes) {
        Optional<String> defaultStyle = super.getNodeStyle(node, avoidSVGComponentsDuplication, isShowInternalNodes);
        if (node.getType() == NodeType.SWITCH) {
            return defaultStyle;
        }

        RGBColor c = getBusColor(node);

        String color = c != null ? c.toString() : disconnectedColor;

        return Optional.of(defaultStyle.orElse("") + " #"
                + escapeId(node.getId()) + " {stroke:"
                + color + ";}");
    }

    @Override
    public Optional<String> getWireStyle(Edge edge, String id, int index) {
        String wireId = DiagramStyles.escapeId(id + "_Wire" + index);
        Node bus;
        if (!(edge instanceof TwtEdge)) {
            bus = edge.getNode1().getType() == NodeType.BUS ? edge.getNode1() : findConnectedBus(edge.getNode1(), new ArrayList<>());
            if (bus == null) {
                bus = edge.getNode2().getType() == NodeType.BUS ? edge.getNode2() : findConnectedBus(edge.getNode2(), new ArrayList<>());
            }
        } else {
            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();
            if (node1.getGraph() != null) {
                bus = node1.getType() == NodeType.BUS ? node1 : findConnectedBus(node1, new ArrayList<>());
            } else {
                bus = node2.getType() == NodeType.BUS ? node2 : findConnectedBus(node2, new ArrayList<>());
            }
        }

        String color = disconnectedColor;
        if (bus != null) {
            RGBColor c = getBusColor(bus);
            if (c != null) {
                color = c.toString();
            }
        }
        return Optional.of(" #" + wireId + " {stroke:" + color + ";stroke-width:1;fill-opacity:0;}");
    }

    private Node findConnectedBus(Node node, List<Node> visitedNodes) {
        List<Node> nodesToVisit = new ArrayList<>(node.getAdjacentNodes());
        if (!visitedNodes.contains(node)) {
            visitedNodes.add(node);
            if (node.getType().equals(NodeType.SWITCH) && node.isOpen()) {
                return null;
            }
            for (Node n : nodesToVisit) {
                if (n.getType().equals(NodeType.BUS)) {
                    return n;
                } else {
                    Node n1 = findConnectedBus(n, visitedNodes);
                    if (n1 != null) {
                        return n1;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Optional<String> getColor(double nominalV, Node node) {
        Optional<String> res = Optional.empty();
        if (node != null) {
            Node bus = findConnectedBus(node, new ArrayList<>());
            String color = disconnectedColor;
            if (bus != null) {
                RGBColor c = getBusColor(bus);
                if (c != null) {
                    color = c.toString();
                }
            }
            res = Optional.of(color);
        }

        return res;
    }
}
