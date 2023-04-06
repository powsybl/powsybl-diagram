/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg.styles.iidm;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import com.powsybl.sld.svg.styles.EmptyStyleProvider;
import com.powsybl.sld.svg.styles.StyleClassConstants;

import java.util.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class HighlightLineStateStyleProvider extends EmptyStyleProvider {

    private final Network network;

    public HighlightLineStateStyleProvider(Network network) {
        this.network = network;
    }

    @Override
    public List<String> getSvgWireStyles(Graph graph, Edge edge) {
        return getHighlightLineStateStyle(graph, edge)
                .map(List::of)
                .orElse(Collections.emptyList());
    }

    private Optional<String> getHighlightLineStateStyle(Graph graph, Edge edge) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        FeederNode n;

        if (n1 instanceof FeederNode || n2 instanceof FeederNode) {
            n = (FeederNode) (n1 instanceof FeederNode ? n1 : n2);
            if (n.getFeeder() instanceof FeederWithSides) {
                return getHighlightFeederStateStyle(graph, n);
            }
        } else {
            return Optional.empty();
        }
        return Optional.empty();
    }

    protected Optional<String> getHighlightFeederStateStyle(Graph graph, FeederNode n) {
        FeederWithSides feederWs = (FeederWithSides) n.getFeeder();
        Map<NodeSide, Boolean> connectionStatus = connectionStatus(n);
        NodeSide side = null;
        NodeSide otherSide = null;

        if (feederWs.getFeederType() == FeederType.BRANCH || feederWs.getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
            side = feederWs.getSide();
            otherSide = getOtherSide(side);
            if (ComponentTypeName.LINE.equals(n.getComponentType())) {
                side = Boolean.TRUE.equals(connectionStatus.get(side)) ? side : otherSide;
                otherSide = getOtherSide(side);
            }
        } else if (feederWs.getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
            String idVl = graph.getVoltageLevelInfos(n).getId();
            ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(n.getEquipmentId());
            if (transformer != null) {
                side = getTransformerSide(idVl, transformer);
            }
            otherSide = feederWs.getSide();
        }

        return getFeederStateStyle(side, otherSide, connectionStatus);
    }

    protected Map<NodeSide, Boolean> connectionStatus(FeederNode node) {
        Map<NodeSide, Boolean> res = new EnumMap<>(NodeSide.class);
        if (node.getFeeder().getFeederType() == FeederType.BRANCH || node.getFeeder().getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
            Branch<?> branch = network.getBranch(node.getEquipmentId());
            if (branch != null) {
                res.put(NodeSide.ONE, branch.getTerminal(Branch.Side.ONE).isConnected());
                res.put(NodeSide.TWO, branch.getTerminal(Branch.Side.TWO).isConnected());
            }
        } else if (node.getFeeder().getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
            ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
            if (transformer != null) {
                res.put(NodeSide.ONE, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).isConnected());
                res.put(NodeSide.TWO, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).isConnected());
                res.put(NodeSide.THREE, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).isConnected());
            }
        }
        return res;
    }

    private static NodeSide getOtherSide(NodeSide side) {
        return side == NodeSide.ONE ? NodeSide.TWO : NodeSide.ONE;
    }

    private static NodeSide getTransformerSide(String idVl, ThreeWindingsTransformer transformer) {
        if (transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getId().equals(idVl)) {
            return NodeSide.ONE;
        } else if (transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getId().equals(idVl)) {
            return NodeSide.TWO;
        } else {
            return NodeSide.THREE;
        }
    }

    private static Optional<String> getFeederStateStyle(NodeSide side, NodeSide otherSide, Map<NodeSide, Boolean> connectionStatus) {
        if (side != null && otherSide != null) {
            if (Boolean.FALSE.equals(connectionStatus.get(side)) && Boolean.FALSE.equals(connectionStatus.get(otherSide))) {  // disconnected on both ends
                return Optional.of(StyleClassConstants.FEEDER_DISCONNECTED);
            } else if (Boolean.TRUE.equals(connectionStatus.get(side)) && Boolean.FALSE.equals(connectionStatus.get(otherSide))) {  // connected on side and disconnected on other side
                return Optional.of(StyleClassConstants.FEEDER_CONNECTED_DISCONNECTED);
            } else if (Boolean.FALSE.equals(connectionStatus.get(side)) && Boolean.TRUE.equals(connectionStatus.get(otherSide))) {  // disconnected on side and connected on other side
                return Optional.of(StyleClassConstants.FEEDER_DISCONNECTED_CONNECTED);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<String> getCssFilenames() {
        return List.of("highlightLineStates.css");
    }
}
