/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import com.powsybl.sld.svg.BasicStyleProvider;
import com.powsybl.sld.svg.DiagramStyles;
import com.powsybl.sld.util.AbstractBaseVoltageDiagramStyleProvider;

import java.util.*;

import static com.powsybl.sld.svg.DiagramStyles.NODE_INFOS;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractIidmBaseVoltageDiagramStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    protected final Network network;

    protected AbstractIidmBaseVoltageDiagramStyleProvider(BaseVoltagesConfig baseVoltagesConfig, Network network) {
        super(baseVoltagesConfig);
        this.network = network;
    }

    @Override
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
                return Optional.of(DiagramStyles.FEEDER_DISCONNECTED);
            } else if (Boolean.TRUE.equals(connectionStatus.get(side)) && Boolean.FALSE.equals(connectionStatus.get(otherSide))) {  // connected on side and disconnected on other side
                return Optional.of(DiagramStyles.FEEDER_CONNECTED_DISCONNECTED);
            } else if (Boolean.FALSE.equals(connectionStatus.get(side)) && Boolean.TRUE.equals(connectionStatus.get(otherSide))) {  // disconnected on side and connected on other side
                return Optional.of(DiagramStyles.FEEDER_DISCONNECTED_CONNECTED);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        Bus bus = network.getVoltageLevel(graph.getVoltageLevelInfos().getId()).getBusView().getBus(busId);
        if (bus != null) {
            for (Terminal t : bus.getConnectedTerminals()) {
                for (FeederNode feederNode : graph.getFeederNodes()) {
                    if (feederNode.getEquipmentId().equals(t.getConnectable().getId())) {
                        Optional<String> voltageLevelStyle = getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(), feederNode);
                        if (voltageLevelStyle.isPresent()) {
                            return Arrays.asList(voltageLevelStyle.get(), NODE_INFOS);
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
