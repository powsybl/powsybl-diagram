/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.*;
import com.powsybl.sld.styles.BaseVoltageStyle;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import com.powsybl.sld.svg.DiagramStyles;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractBaseVoltageDiagramStyleProvider extends DefaultDiagramStyleProvider {

    protected static final String BASE_VOLTAGE_PROFILE = "Default";

    private static final String WINDING1 = "WINDING1";
    private static final String WINDING2 = "WINDING2";
    private static final String WINDING3 = "WINDING3";

    protected final Network network;
    protected final BaseVoltageStyle baseVoltageStyle;

    protected AbstractBaseVoltageDiagramStyleProvider(BaseVoltageStyle baseVoltageStyle, Network network) {
        this.baseVoltageStyle = Objects.requireNonNull(baseVoltageStyle);
        this.network = network;
    }

    @Override
    protected Optional<String> getEdgeStyle(Edge edge) {
        Node nodeForStyle = edge.getNode1().getVoltageLevelInfos() != null ? edge.getNode1() : edge.getNode2();
        return getVoltageLevelNodeStyle(nodeForStyle.getVoltageLevelInfos(), nodeForStyle);
    }

    @Override
    public List<String> getSvgNodeStyles(Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        List<String> styles = super.getSvgNodeStyles(node, componentLibrary, showInternalNodes);

        VoltageLevelGraph g = node.getGraph();
        if (g != null) {  // node inside a voltageLevel graph
            // Middle3WTNode and Feeder2WTNode have style depending on their subcomponents -> see getSvgNodeSubcomponentStyles
            if (!(node instanceof Middle3WTNode) && !(node instanceof Feeder2WTNode)) {
                getVoltageLevelNodeStyle(g.getVoltageLevelInfos(), node).ifPresent(styles::add);
            }
        }
        // Nothing is done for nodes outside any voltageLevel graph (multi-terminal node),
        // indeed they have subcomponents with specific styles -> see getSvgNodeSubcomponentStyles

        return styles;
    }

    @Override
    protected Optional<String> getHighlightLineStateStyle(Edge edge) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        if (n1 instanceof FeederWithSideNode || n2 instanceof FeederWithSideNode) {
            FeederWithSideNode n = n1 instanceof FeederWithSideNode ? (FeederWithSideNode) n1 : (FeederWithSideNode) n2;
            Map<FeederWithSideNode.Side, Boolean> connectionStatus = connectionStatus(n);
            FeederWithSideNode.Side side = null;
            FeederWithSideNode.Side otherSide = null;

            if (n.getFeederType() == FeederType.BRANCH || n.getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
                side = n.getSide();
                otherSide = side == FeederWithSideNode.Side.ONE ? FeederWithSideNode.Side.TWO : FeederWithSideNode.Side.ONE;
                if (n instanceof FeederLineNode) {
                    side = Boolean.TRUE.equals(connectionStatus.get(side)) ? side : otherSide;
                    otherSide = side == FeederWithSideNode.Side.ONE ? FeederWithSideNode.Side.TWO : FeederWithSideNode.Side.ONE;
                }
            } else if (n.getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
                String idVl = n.getGraph().getVoltageLevelInfos().getId();
                ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(n.getEquipmentId());
                if (transformer != null) {
                    if (transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getId().equals(idVl)) {
                        side = FeederWithSideNode.Side.ONE;
                    } else if (transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getId().equals(idVl)) {
                        side = FeederWithSideNode.Side.TWO;
                    } else {
                        side = FeederWithSideNode.Side.THREE;
                    }
                }
                otherSide = n.getSide();
            }

            if (side != null && otherSide != null) {
                if (Boolean.FALSE.equals(connectionStatus.get(side)) && Boolean.FALSE.equals(connectionStatus.get(otherSide))) {  // disconnected on both ends
                    return Optional.of(DiagramStyles.FEEDER_DISCONNECTED);
                } else if (Boolean.TRUE.equals(connectionStatus.get(side)) && Boolean.FALSE.equals(connectionStatus.get(otherSide))) {  // connected on side and disconnected on other side
                    return Optional.of(DiagramStyles.FEEDER_CONNECTED_DISCONNECTED);
                } else if (Boolean.FALSE.equals(connectionStatus.get(side)) && Boolean.TRUE.equals(connectionStatus.get(otherSide))) {  // disconnected on side and connected on other side
                    return Optional.of(DiagramStyles.FEEDER_DISCONNECTED_CONNECTED);
                }
            }
        }
        return Optional.empty();
    }

    protected Map<FeederWithSideNode.Side, Boolean> connectionStatus(FeederWithSideNode node) {
        Map<FeederWithSideNode.Side, Boolean> res = new EnumMap<>(FeederWithSideNode.Side.class);
        if (node.getFeederType() == FeederType.BRANCH || node.getFeederType() == FeederType.TWO_WINDINGS_TRANSFORMER_LEG) {
            Branch branch = network.getBranch(node.getEquipmentId());
            if (branch != null) {
                res.put(FeederWithSideNode.Side.ONE, branch.getTerminal(Branch.Side.ONE).isConnected());
                res.put(FeederWithSideNode.Side.TWO, branch.getTerminal(Branch.Side.TWO).isConnected());
            }
        } else if (node.getFeederType() == FeederType.THREE_WINDINGS_TRANSFORMER_LEG) {
            ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
            if (transformer != null) {
                res.put(FeederWithSideNode.Side.ONE, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).isConnected());
                res.put(FeederWithSideNode.Side.TWO, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).isConnected());
                res.put(FeederWithSideNode.Side.THREE, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).isConnected());
            }
        }
        return res;
    }

    @Override
    public List<String> getSvgNodeSubcomponentStyles(Node node, String subComponentName) {

        List<String> styles = new ArrayList<>();

        VoltageLevelGraph g = node.getGraph();
        if (g != null) {
            // node inside a voltageLevel graph
            VoltageLevelInfos vlInfo = null;
            if (node instanceof Feeder2WTNode) {
                vlInfo = getWindingVoltageLevelInfos((Feeder2WTNode) node, subComponentName);
            } else if (node instanceof Middle3WTNode) {
                vlInfo = getWindingVoltageLevelInfos((Middle3WTNode) node, subComponentName, g.getVoltageLevelInfos().getId());
            }
            if (vlInfo != null) {
                getVoltageLevelNodeStyle(vlInfo, node).ifPresent(styles::add);
            }

        } else {
            // node outside any voltageLevel graph (multi-terminal node)
            Node windingNode = null;
            if (node instanceof Middle2WTNode) {
                windingNode = getWindingNode((Middle2WTNode) node, subComponentName);
            } else if (node instanceof Middle3WTNode) {
                windingNode = getWindingNode((Middle3WTNode) node, subComponentName);
            }
            if (windingNode != null) {
                getVoltageLevelNodeStyle(windingNode.getVoltageLevelInfos(), windingNode).ifPresent(styles::add);
            }
        }

        return styles;
    }

    /**
     * Returns the voltage level style if any to apply to the given node
     *
     * @param vlInfo the VoltageLevelInfos related to the given node
     * @param node   the node on which the style if any is applied to
     * @return the voltage level style if any
     */
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos vlInfo, Node node) {
        return baseVoltageStyle.getBaseVoltageName(vlInfo.getNominalVoltage(), BASE_VOLTAGE_PROFILE);
    }

    private Node getWindingNode(Middle3WTNode node, String subComponentName) {
        List<Node> adjacentNodes = node.getAdjacentNodes();
        adjacentNodes.sort(Comparator.comparingDouble(Node::getDiagramX));
        Node n1 = adjacentNodes.get(0);
        Node n2 = adjacentNodes.get(1);
        Node n3 = adjacentNodes.get(2);

        Node n = n1;
        switch (subComponentName) {
            case WINDING1:
                n = !node.isRotated() ? n1 : n3;
                break;
            case WINDING2:
                n = !node.isRotated() ? n3 : n1;
                break;
            case WINDING3:
                n = n2;
                break;
            default:
        }

        return n;
    }

    private Node getWindingNode(Middle2WTNode node, String subComponentName) {
        List<Node> adjacentNodes = node.getAdjacentNodes();
        adjacentNodes.sort(Comparator.comparingDouble(Node::getDiagramX));
        FeederWithSideNode node1 = (FeederWithSideNode) adjacentNodes.get(0);
        FeederWithSideNode node2 = (FeederWithSideNode) adjacentNodes.get(1);
        FeederWithSideNode nodeWinding1 = node1.getSide() == FeederWithSideNode.Side.ONE ? node1 : node2;
        FeederWithSideNode nodeWinding2 = node1.getSide() == FeederWithSideNode.Side.TWO ? node1 : node2;
        FeederWithSideNode nodeWinding = nodeWinding1;

        if (subComponentName.equals(WINDING1)) {
            if (!node.isRotated()) {
                nodeWinding = nodeWinding1.getDiagramY() > nodeWinding2.getDiagramY() ? nodeWinding1 : nodeWinding2;
            } else if (node.getRotationAngle() == 90.) {
                nodeWinding = nodeWinding1.getDiagramX() > nodeWinding2.getDiagramX() ? nodeWinding2 : nodeWinding1;
            } else if (node.getRotationAngle() == 180.) {
                nodeWinding = nodeWinding1.getDiagramY() > nodeWinding2.getDiagramY() ? nodeWinding2 : nodeWinding1;
            } else if (node.getRotationAngle() == 270.) {
                nodeWinding = nodeWinding1.getDiagramX() > nodeWinding2.getDiagramX() ? nodeWinding1 : nodeWinding2;
            }
        } else if (subComponentName.equals(WINDING2)) {
            if (!node.isRotated()) {
                nodeWinding = nodeWinding1.getDiagramY() > nodeWinding2.getDiagramY() ? nodeWinding2 : nodeWinding1;
            } else if (node.getRotationAngle() == 90.) {
                nodeWinding = nodeWinding1.getDiagramX() > nodeWinding2.getDiagramX() ? nodeWinding1 : nodeWinding2;
            } else if (node.getRotationAngle() == 180.) {
                nodeWinding = nodeWinding1.getDiagramY() > nodeWinding2.getDiagramY() ? nodeWinding1 : nodeWinding2;
            } else if (node.getRotationAngle() == 270.) {
                nodeWinding = nodeWinding1.getDiagramX() > nodeWinding2.getDiagramX() ? nodeWinding2 : nodeWinding1;
            }
        }

        return nodeWinding;
    }

    private VoltageLevelInfos getWindingVoltageLevelInfos(Feeder2WTNode node, String subComponentName) {
        if (subComponentName.equals(WINDING1)) {
            return node.getVoltageLevelInfos();
        } else if (subComponentName.equals(WINDING2)) {
            return node.getOtherSideVoltageLevelInfos();
        } else {
            return null;
        }
    }

    private VoltageLevelInfos getWindingVoltageLevelInfos(Middle3WTNode node, String subComponentName, String vlId) {
        VoltageLevelInfos voltageLevelInfosLeg1 = node.getVoltageLevelInfosLeg1();
        VoltageLevelInfos voltageLevelInfosLeg2 = node.getVoltageLevelInfosLeg2();
        VoltageLevelInfos voltageLevelInfosLeg3 = node.getVoltageLevelInfosLeg3();

        VoltageLevelInfos voltageLevelInfos = null;

        // A three windings transformer is represented by 3 circles identified by winding1, winding2 and winding3
        // winding1 and winding2 are horizontally aligned and winding3 is displayed below the others
        // But, if node is rotated (by 180 degrees), winding3 is displayed above the others and winding1 and winding2 are permuted

        if (subComponentName.equals(WINDING1)) {
            // colorizing winding1
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg1, winding1 corresponds to transformer leg2 or leg3, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg2 : voltageLevelInfosLeg3;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg2, winding1 corresponds to transformer leg1 or leg3, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg1 : voltageLevelInfosLeg3;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg3, winding1 corresponds to transformer leg1 or leg2, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg1 : voltageLevelInfosLeg2;
            }
        } else if (subComponentName.equals(WINDING2)) {
            // colorizing winding2
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg1, winding2 corresponds to transformer leg3 or leg2, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg3 : voltageLevelInfosLeg2;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg2, winding2 corresponds to transformer leg3 or leg1, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg3 : voltageLevelInfosLeg1;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg3, winding2 corresponds to transformer leg2 or leg1, depending on whether a rotation occurred or not
                voltageLevelInfos = !node.isRotated() ? voltageLevelInfosLeg2 : voltageLevelInfosLeg1;
            }
        } else if (subComponentName.equals(WINDING3)) {
            // colorizing winding3 : this winding always correspond to the leg of the voltage level displayed
            if (voltageLevelInfosLeg1.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg1, winding3 corresponds to this transformer leg
                voltageLevelInfos = voltageLevelInfosLeg1;
            } else if (voltageLevelInfosLeg2.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg2, winding3 corresponds to this transformer leg
                voltageLevelInfos = voltageLevelInfosLeg2;
            } else if (voltageLevelInfosLeg3.getId().equals(vlId)) {
                // if voltage level displayed is the transformer leg3, winding3 corresponds to this transformer leg
                voltageLevelInfos = voltageLevelInfosLeg3;
            }
        }

        return voltageLevelInfos;
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        Bus bus = network.getBusView().getBus(busId);
        if (bus != null) {
            if (bus.getVoltageLevel().getId().equals(graph.getVoltageLevelInfos().getId())) {
                return findFeederNodeInBus(graph, bus)
                        .map(node -> getVoltageLevelNodeStyle(graph.getVoltageLevelInfos(), node)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList()))
                        .orElse(Collections.emptyList());
            } else {
                Optional<Feeder2WTNode> t2w = findFeeder2WTNodeInBus(graph, bus);
                if (t2w.isPresent()) {
                    Optional<String> voltageLevelStyle = getVoltageLevelNodeStyle(t2w.get().getOtherSideVoltageLevelInfos(), t2w.get());
                    if (voltageLevelStyle.isPresent()) {
                        return Collections.singletonList(voltageLevelStyle.get());
                    }
                }

                Optional<Middle3WTNode> t3w = findMiddle3WTInBus(graph, bus);
                if (t3w.isPresent()) {
                    Optional<VoltageLevelInfos> vlInfo = getVoltageLevelInfos(bus.getVoltageLevel().getId(), t3w.get());
                    if (vlInfo.isPresent()) {
                        Optional<String> voltageLevelStyle = getVoltageLevelNodeStyle(vlInfo.get(), t3w.get());
                        if (voltageLevelStyle.isPresent()) {
                            return Collections.singletonList(voltageLevelStyle.get());
                        }
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private Optional<FeederNode> findFeederNodeInBus(VoltageLevelGraph graph, Bus bus) {
        for (Terminal t : bus.getConnectedTerminals()) {
            for (FeederNode node : graph.getFeederNodes()) {
                if (node.getEquipmentId().equals(t.getConnectable().getId())) {
                    return Optional.of(node);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Feeder2WTNode> findFeeder2WTNodeInBus(VoltageLevelGraph graph, Bus bus) {
        List<Feeder2WTNode> t2w = graph.getNodes().stream()
                .filter(Feeder2WTNode.class::isInstance)
                .map(Feeder2WTNode.class::cast)
                .collect(Collectors.toList());

        for (Feeder2WTNode transfo : t2w) {
            for (Terminal t : bus.getConnectedTerminals()) {
                if (transfo.getEquipmentId().equals(t.getConnectable().getId())) {
                    return Optional.of(transfo);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Middle3WTNode> findMiddle3WTInBus(VoltageLevelGraph graph, Bus bus) {
        List<Middle3WTNode> t3w = graph.getNodes().stream()
                .filter(Middle3WTNode.class::isInstance)
                .map(Middle3WTNode.class::cast)
                .collect(Collectors.toList());
        for (Middle3WTNode transfo : t3w) {
            for (Terminal t : bus.getConnectedTerminals()) {
                if (transfo.getEquipmentId().equals(t.getConnectable().getId())) {
                    return Optional.of(transfo);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<VoltageLevelInfos> getVoltageLevelInfos(String vlId, MiddleTwtNode transfo) {
        VoltageLevelInfos voltageInfo = null;
        if (transfo.getVoltageLevelInfos(FeederWithSideNode.Side.ONE).getId().equals(vlId)) {
            voltageInfo = transfo.getVoltageLevelInfos(FeederWithSideNode.Side.ONE);
        } else if (transfo.getVoltageLevelInfos(FeederWithSideNode.Side.TWO).getId().equals(vlId)) {
            voltageInfo = transfo.getVoltageLevelInfos(FeederWithSideNode.Side.TWO);
        } else  if (transfo.getVoltageLevelInfos(FeederWithSideNode.Side.THREE).getId().equals(vlId)) {
            voltageInfo = transfo.getVoltageLevelInfos(FeederWithSideNode.Side.THREE);
        }
        return Optional.ofNullable(voltageInfo);
    }

    @Override
    public List<String> getCssFilenames() {
        return Arrays.asList("tautologies.css", "baseVoltages.css", "highlightLineStates.css", "baseVoltageConstantColors.css");
    }

}
