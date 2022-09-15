/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import com.powsybl.sld.layout.Layout;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.Node.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.library.ComponentTypeName.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 */
public abstract class AbstractCgmesLayout implements Layout {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCgmesLayout.class);

    protected static final double X_MARGIN = 20;
    protected static final double Y_MARGIN = 10;
    protected static final double LINE_OFFSET = 20;

    protected double minX = 0;
    protected double minY = 0;
    protected boolean rotatedBus = false;
    protected boolean isNodeBreaker = true;
    protected boolean fixTransformersLabel = false;

    protected Network network;

    protected void setMin(double x, double y) {
        if (minX == 0 || x < minX) {
            minX = x;
        }
        if (minY == 0 || y < minY) {
            minY = y;
        }
    }

    protected VoltageLevelGraph removeFictitiousNodes(VoltageLevelGraph graph, VoltageLevel vl) {
        graph.removeUnnecessaryConnectivityNodes();
        removeFictitiousSwitchNodes(graph, vl);
        return graph;
    }

    protected boolean checkDiagram(String diagramName, String equipment) {
        if (diagramName == null) {
            LOG.warn("layout parameter diagramName not set: CGMES-DL layout will not be applied");
            return false;
        }
        if (!NetworkDiagramData.containsDiagramName(network, diagramName)) {
            LOG.warn("diagram name {} not found in network: CGMES-DL layout will not be applied to network {}, {}", diagramName, network.getId(), equipment);
            return false;
        }
        return true;
    }

    protected void setNodeCoordinates(VoltageLevel vl, VoltageLevelGraph graph, String diagramName, boolean useNames) {
        isNodeBreaker = TopologyKind.NODE_BREAKER.equals(vl.getTopologyKind());
        // skip line nodes: I need the coordinates of the adjacent node to know which side of the line belongs to this voltage level
        graph.getNodes().stream().filter(node -> !isLineNode(node)).forEach(node -> setNodeCoordinates(vl, node, diagramName, useNames));
        // set line nodes coordinates: I use the coordinates of the adjacent node to know which side of the line belongs to this voltage level
        graph.getNodes().stream().filter(this::isLineNode).forEach(node -> setLineNodeCoordinates(vl, node, diagramName));
    }

    protected boolean isLineNode(Node node) {
        return Arrays.asList(LINE, DANGLING_LINE, VSC_CONVERTER_STATION).contains(node.getComponentType());
    }

    protected void setNodeCoordinates(VoltageLevel vl, Node node, String diagramName, boolean useNames) {
        LOG.info("Setting coordinates of node {}, type {}, component type {}", node.getId(), node.getType(), node.getComponentType());
        switch (node.getType()) {
            case BUS:
                BusNode busNode = (BusNode) node;
                if (TopologyKind.NODE_BREAKER.equals(vl.getTopologyKind())) {
                    BusbarSection busbar = vl.getConnectable(busNode.getId(), BusbarSection.class);
                    NodeDiagramData<BusbarSection> busbarDiagramData = busbar != null ? busbar.getExtension(NodeDiagramData.class) : null;
                    setBusNodeCoordinates(busNode, busbarDiagramData, diagramName);
                } else {
                    Bus bus = vl.getBusBreakerView().getBus(busNode.getId());
                    NodeDiagramData<Bus> busDiagramData =  bus != null ? bus.getExtension(NodeDiagramData.class) : null;
                    setBusNodeCoordinates(busNode, busDiagramData, diagramName);
                }
                break;
            case SWITCH:
                SwitchNode switchNode = (SwitchNode) node;
                Switch sw = TopologyKind.NODE_BREAKER.equals(vl.getTopologyKind()) ?
                            vl.getNodeBreakerView().getSwitch(switchNode.getId()) :
                            vl.getBusBreakerView().getSwitch(switchNode.getId());
                CouplingDeviceDiagramData<Switch> switchDiagramData =  sw != null ? sw.getExtension(CouplingDeviceDiagramData.class) : null;
                setCouplingDeviceNodeCoordinates(switchNode, switchDiagramData, true, diagramName);
                break;
            case FEEDER:
                setFeederNodeCoordinates(vl, node, diagramName, useNames);
                break;
            default:
                processDefaultNodeCase(vl, node, diagramName);
                break;
        }
    }

    protected void processDefaultNodeCase(VoltageLevel vl, Node node, String diagramName) {
        // retrieve internal nodes points, if available in VoltageLevel extensions
        if (node instanceof ConnectivityNode && isNodeBreaker) {
            Optional<Integer> iidmEquivalentNode = getIidmEquivalentNode(vl, node);
            if (iidmEquivalentNode.isPresent()) {
                DiagramPoint nodePoint = VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, diagramName, iidmEquivalentNode.get());
                if (nodePoint != null) {
                    node.setX(nodePoint.getX());
                    node.setY(nodePoint.getY());
                    return;
                }
            }
        }
        LOG.warn("unable to set coordinates for node {}, type {}, component type {}", node.getId(), node.getType(), node.getComponentType());
    }

    protected static Optional<Integer> getIidmEquivalentNode(VoltageLevel vl, Node node) {
        VoltageLevel.NodeBreakerView nbv = vl.getNodeBreakerView();
        Stream<Integer> iidmNodes1 = node.getAdjacentEdges().stream()
                .filter(edge -> edge.getNode1() == node)
                .map(Edge::getNode2)
                .filter(SwitchNode.class::isInstance) // outgoing neighbour switches
                .map(SwitchNode.class::cast)
                .map(SwitchNode::getEquipmentId)
                .map(nbv::getNode1);
        Stream<Integer> iidmNodes2 = node.getAdjacentEdges().stream()
                .filter(edge -> edge.getNode2() == node)
                .map(Edge::getNode1)
                .filter(SwitchNode.class::isInstance) // incoming neighbour switches
                .map(SwitchNode.class::cast)
                .map(SwitchNode::getEquipmentId)
                .map(nbv::getNode2);
        Map<Integer, Long> equivalentNodeCandidates = Stream.concat(iidmNodes1, iidmNodes2)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return equivalentNodeCandidates.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey);
    }

    protected void setBusNodeCoordinates(BusNode node, NodeDiagramData<?> diagramData, String diagramName) {
        if (diagramData != null) {
            NodeDiagramData.NodeDiagramDataDetails diagramDetails = diagramData.getData(diagramName);
            if (diagramDetails != null) {
                node.setX(diagramDetails.getPoint1().getX());
                node.setY(diagramDetails.getPoint1().getY());
                node.setPxWidth(computeBusWidth(diagramDetails));
                rotatedBus = diagramDetails.getPoint1().getX() == diagramDetails.getPoint2().getX();
                node.setOrientation(rotatedBus ? Orientation.UP : Orientation.RIGHT);
                setMin(diagramDetails.getPoint1().getX(), diagramDetails.getPoint1().getY());
            } else {
                LOG.warn("No CGMES-DL data for {} node {}, bus {}, diagramName {}", node.getType(), node.getId(), node.getName(), diagramName);
            }
        } else {
            LOG.warn("No CGMES-DL data for {} node {}, bus {}", node.getType(), node.getId(), node.getName());
        }
    }

    protected void setCouplingDeviceNodeCoordinates(EquipmentNode node, CouplingDeviceDiagramData<?> diagramData, boolean rotate, String diagramName) {
        if (diagramData != null) {
            CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails = diagramData.getData(diagramName);
            if (diagramDetails != null) {
                node.setX(diagramDetails.getPoint().getX());
                node.setY(diagramDetails.getPoint().getY());
                setOrientation(node, rotate, diagramDetails.getRotation());
                setMin(diagramDetails.getPoint().getX(), diagramDetails.getPoint().getY());
            } else {
                LOG.warn("No CGMES-DL data for {} node {}, name {}, diagramName {}", node.getType(), node.getId(), node.getName(), diagramName);
            }

        } else {
            LOG.warn("No CGMES-DL data for {} node {}, name {}", node.getType(), node.getId(), node.getName());
        }
    }

    protected double computeBusWidth(NodeDiagramData.NodeDiagramDataDetails diagramData) {
        if (diagramData.getPoint1().getX() == diagramData.getPoint2().getX()) {
            return Math.abs(diagramData.getPoint1().getY() - diagramData.getPoint2().getY());
        } else {
            return Math.abs(diagramData.getPoint1().getX() - diagramData.getPoint2().getX());
        }
    }

    protected void setFeederNodeCoordinates(VoltageLevel vl, Node node, String diagramName, boolean useNames) {
        switch (node.getComponentType()) {
            case LOAD:
                FeederNode loadNode = (FeederNode) node;
                Load load = vl.getConnectable(loadNode.getId(), Load.class);
                InjectionDiagramData<Load> loadDiagramData = load != null ? load.getExtension(InjectionDiagramData.class) : null;
                setInjectionNodeCoordinates(loadNode, loadDiagramData, true, diagramName);
                break;
            case GENERATOR:
                FeederNode generatorNode = (FeederNode) node;
                Generator generator = vl.getConnectable(generatorNode.getId(), Generator.class);
                InjectionDiagramData<Generator> generatorDiagramData = generator != null ? generator.getExtension(InjectionDiagramData.class) : null;
                setInjectionNodeCoordinates(generatorNode, generatorDiagramData, false, diagramName);
                break;
            case CAPACITOR:
            case INDUCTOR:
                FeederNode shuntNode = (FeederNode) node;
                ShuntCompensator shunt = vl.getConnectable(shuntNode.getId(), ShuntCompensator.class);
                InjectionDiagramData<ShuntCompensator> shuntDiagramData = shunt != null ? shunt.getExtension(InjectionDiagramData.class) : null;
                setInjectionNodeCoordinates(shuntNode, shuntDiagramData, true, diagramName);
                break;
            case STATIC_VAR_COMPENSATOR:
                FeederNode svcNode = (FeederNode) node;
                StaticVarCompensator svc = vl.getConnectable(svcNode.getId(), StaticVarCompensator.class);
                InjectionDiagramData<StaticVarCompensator> svcDiagramData = svc != null ? svc.getExtension(InjectionDiagramData.class) : null;
                setInjectionNodeCoordinates(svcNode, svcDiagramData, true, diagramName);
                break;
            case TWO_WINDINGS_TRANSFORMER:
            case PHASE_SHIFT_TRANSFORMER:
            case TWO_WINDINGS_TRANSFORMER_LEG:
            case PHASE_SHIFT_TRANSFORMER_LEG:
                FeederNode transformerNode = (FeederNode) node;
                TwoWindingsTransformer transformer = vl.getConnectable(transformerNode.getEquipmentId(), TwoWindingsTransformer.class);
                CouplingDeviceDiagramData<TwoWindingsTransformer> transformerDiagramData = null;
                if (transformer != null) {
                    transformerDiagramData = transformer.getExtension(CouplingDeviceDiagramData.class);
                    setTransformersLabel(transformerNode, useNames, transformer.getNameOrId(), transformer.getId());
                }
                setCouplingDeviceNodeCoordinates(transformerNode, transformerDiagramData, false, diagramName);
                break;
            case THREE_WINDINGS_TRANSFORMER:
            case THREE_WINDINGS_TRANSFORMER_LEG:
                FeederNode transformer3wNode = (FeederNode) node;
                ThreeWindingsTransformer transformer3w = vl.getConnectable(transformer3wNode.getEquipmentId(), ThreeWindingsTransformer.class);
                ThreeWindingsTransformerDiagramData transformer3wDiagramData = null;
                if (transformer3w != null) {
                    transformer3wDiagramData = transformer3w.getExtension(ThreeWindingsTransformerDiagramData.class);
                    setTransformersLabel(transformer3wNode, useNames, transformer3w.getNameOrId(), transformer3w.getId());
                }
                setThreeWindingsTransformerNodeCoordinates(transformer3wNode, transformer3wDiagramData, diagramName);
                break;
            default:
                break;
        }
    }

    protected void setTransformersLabel(FeederNode node, boolean useNames, String name, String id) {
        if (fixTransformersLabel) {
            String label = useNames ? name : id;
            node.setLabel(label);
        }
    }

    protected void setInjectionNodeCoordinates(FeederNode node, InjectionDiagramData<?> diagramData, boolean rotate, String diagramName) {
        if (diagramData != null) {
            InjectionDiagramData.InjectionDiagramDetails diagramDetails = diagramData.getData(diagramName);
            if (diagramDetails != null) {
                node.setX(diagramDetails.getPoint().getX());
                node.setY(diagramDetails.getPoint().getY());
                setOrientation(node, rotate, diagramDetails.getRotation());
                setMin(diagramDetails.getPoint().getX(), diagramDetails.getPoint().getY());
            } else {
                LOG.warn("No CGMES-DL data for {} {} node {}, injection {}, diagramName {}", node.getType(), node.getComponentType(), node.getId(), node.getName(), diagramName);
            }
        } else {
            LOG.warn("No CGMES-DL data for {} {} node {}, injection {}", node.getType(), node.getComponentType(), node.getId(), node.getName());
        }
    }

    private void setOrientation(Node node, boolean rotate, double rotationAngle) {
        boolean isBusNode = node instanceof BusNode;
        if (rotate) {
            if (rotationAngle == 90) {
                node.setOrientation(isBusNode ? Orientation.UP : Orientation.RIGHT);
            } else if (rotationAngle == 270) {
                node.setOrientation(isBusNode ? Orientation.DOWN : Orientation.LEFT);
            }
        } else {
            node.setOrientation(isBusNode ? Orientation.RIGHT : Orientation.UP);
        }
    }

    protected void setThreeWindingsTransformerNodeCoordinates(FeederNode node, ThreeWindingsTransformerDiagramData diagramData, String diagramName) {
        if (diagramData != null) {
            ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails diagramDetails = diagramData.getData(diagramName);
            if (diagramDetails != null) {
                node.setX(diagramDetails.getPoint().getX());
                node.setY(diagramDetails.getPoint().getY());
                setMin(diagramDetails.getPoint().getX(), diagramDetails.getPoint().getY());
            } else {
                LOG.warn("No CGMES-DL data for {} {} node {}, transformer {}, diagramName {}", node.getType(), node.getComponentType(), node.getId(), node.getName(), diagramName);
            }
        } else {
            LOG.warn("No CGMES-DL data for {} {} node {}, transformer {}", node.getType(), node.getComponentType(), node.getId(), node.getName());
        }
    }

    protected void setLineNodeCoordinates(VoltageLevel vl, Node node, String diagramName) {
        LOG.info("Setting coordinates of node {}, type {}, component type {}", node.getId(), node.getType(), node.getComponentType());
        switch (node.getComponentType()) {
            case LINE:
                FeederNode lineNode = (FeederNode) node;
                Line line = vl.getConnectable(lineNode.getEquipmentId(), Line.class);
                LineDiagramData<Line> lineDiagramData = line != null ? line.getExtension(LineDiagramData.class) : null;
                setLineNodeCoordinates(lineNode, lineDiagramData, diagramName);
                break;
            case DANGLING_LINE:
                FeederNode danglingLineNode = (FeederNode) node;
                DanglingLine danglingLine = vl.getConnectable(danglingLineNode.getId(), DanglingLine.class);
                LineDiagramData<DanglingLine> danglingLineDiagramData = danglingLine != null ? danglingLine.getExtension(LineDiagramData.class) : null;
                setLineNodeCoordinates(danglingLineNode, danglingLineDiagramData, diagramName);
                break;
            default:
                break;
        }
    }

    protected void setLineNodeCoordinates(FeederNode node, LineDiagramData<?> diagramData, String diagramName) {
        if (diagramData != null) {
            if (diagramData.getDiagramsNames().contains(diagramName)) {
                DiagramPoint linePoint = getLinePoint(diagramData, node, diagramName);
                node.setX(linePoint.getX());
                node.setY(linePoint.getY());
                node.setOrientation(rotatedBus ? Orientation.RIGHT : Orientation.UP);
                setMin(linePoint.getX(), linePoint.getY());
            } else {
                LOG.warn("No CGMES-DL data for {} {} node {}, line {}, diagramName {}", node.getType(), node.getComponentType(), node.getId(), node.getName(), diagramName);
            }
        } else {
            LOG.warn("No CGMES-DL data for {} {} node {}, line {}", node.getType(), node.getComponentType(), node.getId(), node.getName());
        }
    }

    protected DiagramPoint getLinePoint(LineDiagramData<?> lineDiagramData, Node lineNode, String diagramName) {
        DiagramPoint adjacentNodePoint = getLineAdjacentNodePoint(lineNode);
        if (adjacentNodePoint == null) {
            return getLinePoint(lineDiagramData, true, diagramName);
        }
        double firstPointDistance = Math.hypot(lineDiagramData.getFirstPoint(diagramName).getX() - adjacentNodePoint.getX(),
                lineDiagramData.getFirstPoint(diagramName).getY() - adjacentNodePoint.getY());
        double lastPointDistance = Math.hypot(lineDiagramData.getLastPoint(diagramName).getX() - adjacentNodePoint.getX(),
                lineDiagramData.getLastPoint(diagramName).getY() - adjacentNodePoint.getY());
        return getLinePoint(lineDiagramData, firstPointDistance > lastPointDistance, diagramName);
    }

    protected DiagramPoint getLineAdjacentNodePoint(Node branchNode) {
        List<Node> adjacentNodes = branchNode.getAdjacentNodes();
        if (adjacentNodes == null || adjacentNodes.isEmpty()) {
            return null;
        }
        Node adjacentNode = adjacentNodes.get(0); // as we are working on a single voltage level a line node should be connected to only 1 node
        // a line should not be connected to another line, so I should already have the coordinates of the adjacent node
        return new DiagramPoint(adjacentNode.getX(), adjacentNode.getY(), 0);
    }

    protected DiagramPoint getLinePoint(LineDiagramData<?> lineDiagramData, boolean isLastPointCloser, String diagramName) {
        if (isNodeBreaker) {
            return isLastPointCloser ? lineDiagramData.getLastPoint(diagramName) : lineDiagramData.getFirstPoint(diagramName);
        }
        return isLastPointCloser ? lineDiagramData.getLastPoint(diagramName, LINE_OFFSET) : lineDiagramData.getFirstPoint(diagramName, LINE_OFFSET);
    }

    protected void shiftNodeCoordinates(Node node, double scaleFactor) {
        node.setX(node.getX() - minX + (X_MARGIN / scaleFactor));
        node.setY(node.getY() - minY + (Y_MARGIN / scaleFactor));
    }

    protected void scaleNodeCoordinates(Node node, double scaleFactor) {
        node.setX(node.getX() * scaleFactor);
        node.setY(node.getY() * scaleFactor);
        if (node.getType() == NodeType.BUS) {
            BusNode nodeBus = (BusNode) node;
            nodeBus.setPxWidth(nodeBus.getPxWidth() * scaleFactor);
        }
    }

    protected void setVoltageLevelCoord(VoltageLevelGraph vlGraph) {
        double minNodeX = vlGraph.getNodes().stream().mapToDouble(Node::getX).min().orElse(0);
        double minNodeY = vlGraph.getNodes().stream().mapToDouble(Node::getY).min().orElse(0);
        vlGraph.setCoord(minNodeX, minNodeY);
    }

    public static void removeFictitiousSwitchNodes(VoltageLevelGraph graph, VoltageLevel vl) {
        List<SwitchNode> fictitiousSwithcNodesToRemove = graph.getNodes().stream()
                .filter(SwitchNode.class::isInstance)
                .map(SwitchNode.class::cast)
                .filter(node -> isFictitiousSwitchNode(node, vl))
                .filter(node -> node.getAdjacentNodes().size() == 2)
                .collect(Collectors.toList());
        for (SwitchNode n : fictitiousSwithcNodesToRemove) {
            Node node1 = n.getAdjacentNodes().get(0);
            Node node2 = n.getAdjacentNodes().get(1);
            LOG.info("Remove fictitious switch node {} between {} and {}", n.getName(), node1.getId(), node2.getId());
            graph.removeNode(n);
            graph.addEdge(node1, node2);
        }
    }

    private static boolean isFictitiousSwitchNode(Node node, VoltageLevel vl) {
        Switch sw = TopologyKind.NODE_BREAKER.equals(vl.getTopologyKind()) ?
                vl.getNodeBreakerView().getSwitch(node.getId()) :
                vl.getBusBreakerView().getSwitch(node.getId());
        return sw == null || sw.isFictitious();
    }
}
