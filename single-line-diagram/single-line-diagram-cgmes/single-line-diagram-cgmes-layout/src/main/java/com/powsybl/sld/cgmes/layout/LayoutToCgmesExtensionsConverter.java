/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.cgmes.dl.conversion.CgmesDLUtils;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.layout.position.clustering.PositionByClustering;
import com.powsybl.sld.library.SldComponentTypeName;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.library.SldComponentTypeName.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer@rte-france.com>}
 */
public class LayoutToCgmesExtensionsConverter {

    private static final Logger LOG = LoggerFactory.getLogger(LayoutToCgmesExtensionsConverter.class);

    private static final double OFFSET_MULTIPLIER_X = 2.0;

    private final LayoutParameters lparams;
    private final SubstationLayoutFactory sFactory;
    private final VoltageLevelLayoutFactory vFactory;

    public LayoutToCgmesExtensionsConverter(SubstationLayoutFactory sFactory, VoltageLevelLayoutFactory vFactory, LayoutParameters lparams) {
        this.sFactory = Objects.requireNonNull(sFactory);
        this.vFactory = Objects.requireNonNull(vFactory);
        this.lparams = Objects.requireNonNull(lparams);
    }

    public LayoutToCgmesExtensionsConverter() {
        this(new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory(new PositionByClustering()), new LayoutParameters().setCgmesUseNames(true));
    }

    private int getMaxSeq(List<DiagramPoint> diagramPoints) {
        Objects.requireNonNull(diagramPoints);
        return diagramPoints.stream().max(Comparator.naturalOrder()).map(DiagramPoint::getSeq).orElse(0);
    }

    private <T extends Identifiable<T>> void setNodeDiagramPoints(NodeDiagramData<T> diagramData, BusNode busNode, OffsetPoint offsetPoint, String diagramName) {
        double x1 = busNode.getX();
        double y1 = busNode.getY();
        double x2 = x1;
        double y2 = y1;
        if (busNode.getOrientation().isHorizontal()) {
            x2 += busNode.getPxWidth();
        } else {
            y2 += busNode.getPxWidth();
        }

        NodeDiagramData<T>.NodeDiagramDataDetails diagramDetails = diagramData.new NodeDiagramDataDetails();
        DiagramPoint p1 = offsetPoint.newDiagramPoint(x1, y1, 1);
        DiagramPoint p2 = offsetPoint.newDiagramPoint(x2, y2, 2);
        diagramDetails.setPoint1(p1);
        diagramDetails.setPoint2(p2);
        diagramData.addData(diagramName, diagramDetails);
    }

    private LayoutInfo applyLayout(Network network, String substationId, double xoffset, double yoffset, String diagramName) {
        OffsetPoint offsetPoint = new OffsetPoint(xoffset, yoffset);

        //apply the specified layout
        NetworkGraphBuilder graphBuilder = new NetworkGraphBuilder(network);
        SubstationGraph sgraph = graphBuilder.buildSubstationGraph(substationId);
        Layout sLayout = sFactory.create(sgraph, vFactory);
        sLayout.run(lparams);

        LayoutInfo subsBoundary = new LayoutInfo(0.0, 0.0);
        Substation substation = network.getSubstation(substationId);
        substation.getVoltageLevelStream().forEach(voltageLevel -> {
            VoltageLevelGraph vlGraph = sgraph.getVoltageLevel(voltageLevel.getId());

            // remove fictitious nodes & switches (no CGMES DL data available for them)
            vlGraph.removeUnnecessaryConnectivityNodes();
            AbstractCgmesLayout.removeFictitiousSwitchNodes(vlGraph, voltageLevel);

            // retrieve connectivity nodes which might correspond to CGMES connectivity nodes, to be exported to DL
            vlGraph.getConnectivityNodeStream().forEach(cn ->
                    AbstractCgmesLayout.getIidmEquivalentNode(voltageLevel, cn)
                            .ifPresent(equivalentIidmNode -> VoltageLevelDiagramData.addInternalNodeDiagramPoint(
                                    voltageLevel, diagramName, equivalentIidmNode, new DiagramPoint(cn.getX(), cn.getY(), 0))));

            double vlNodeMaxX = vlGraph.getNodes().stream().map(Node::getX).max(Comparator.naturalOrder()).orElse(0.0);
            double vlNodeMaxY = vlGraph.getNodes().stream().map(Node::getY).max(Comparator.naturalOrder()).orElse(0.0);
            subsBoundary.update(vlNodeMaxX, vlNodeMaxY);

            List<String> componentTypeList = vlGraph.getNodes().stream().map(Node::getComponentType).collect(Collectors.toList());
            LOG.debug("Voltage level id: {} ({}); {} ;component types: {}; max x,y: {}, {}", voltageLevel.getId(), voltageLevel.getNameOrId(), voltageLevel.getTopologyKind(), componentTypeList, vlNodeMaxX, vlNodeMaxY);

            //iterate over the voltage level's equipments, and fill the IIDM CGMES DL extensions with the computed layout info
            voltageLevel.getLoadStream().filter(load -> vlGraph.getNode(load.getId()) != null).forEach(load -> {
                Node node = vlGraph.getNode(load.getId());
                DiagramPoint lDiagramPoint = offsetPoint.newDiagramPoint(node.getX(), node.getY(), 0);
                InjectionDiagramData<Load> loadIidmDiagramData = new InjectionDiagramData<>(load);
                InjectionDiagramData<Load>.InjectionDiagramDetails diagramDetails = loadIidmDiagramData.new InjectionDiagramDetails(lDiagramPoint, 0);
                loadIidmDiagramData.addData(diagramName, diagramDetails);
                LOG.debug("setting CGMES DL IIDM extensions for Load: {}, {}", load.getId(), lDiagramPoint);
                load.addExtension(InjectionDiagramData.class, loadIidmDiagramData);
            });

            voltageLevel.getGeneratorStream().filter(generator -> vlGraph.getNode(generator.getId()) != null).forEach(generator -> {
                Node node = vlGraph.getNode(generator.getId());
                DiagramPoint gDiagramPoint = offsetPoint.newDiagramPoint(node.getX(), node.getY(), 0);
                InjectionDiagramData<Generator> gIidmDiagramData = new InjectionDiagramData<>(generator);
                InjectionDiagramData<Generator>.InjectionDiagramDetails diagramDetails = gIidmDiagramData.new InjectionDiagramDetails(gDiagramPoint, 0);
                gIidmDiagramData.addData(diagramName, diagramDetails);
                LOG.debug("setting CGMES DL IIDM extensions for Generator: {}, {}", generator.getId(), gDiagramPoint);
                generator.addExtension(InjectionDiagramData.class, gIidmDiagramData);
            });

            voltageLevel.getShuntCompensatorStream().filter(shuntCompensator -> vlGraph.getNode(shuntCompensator.getId()) != null).forEach(shuntCompensator -> {
                Node node = vlGraph.getNode(shuntCompensator.getId());
                DiagramPoint scDiagramPoint = offsetPoint.newDiagramPoint(node.getX(), node.getY(), 0);
                InjectionDiagramData<ShuntCompensator> scDiagramData = new InjectionDiagramData<>(shuntCompensator);
                InjectionDiagramData<ShuntCompensator>.InjectionDiagramDetails diagramDetails = scDiagramData.new InjectionDiagramDetails(scDiagramPoint, 0);
                scDiagramData.addData(diagramName, diagramDetails);
                LOG.debug("setting CGMES DL IIDM extensions for ShuntCompensator: {}, {}", shuntCompensator.getId(), scDiagramPoint);
                shuntCompensator.addExtension(InjectionDiagramData.class, scDiagramData);
            });

            voltageLevel.getStaticVarCompensatorStream().filter(staticVarCompensator -> vlGraph.getNode(staticVarCompensator.getId()) != null).forEach(staticVarCompensator -> {
                Node node = vlGraph.getNode(staticVarCompensator.getId());
                DiagramPoint svcDiagramPoint = offsetPoint.newDiagramPoint(node.getX(), node.getY(), 0);
                InjectionDiagramData<StaticVarCompensator> svcDiagramData = new InjectionDiagramData<>(staticVarCompensator);
                InjectionDiagramData<StaticVarCompensator>.InjectionDiagramDetails diagramDetails = svcDiagramData.new InjectionDiagramDetails(svcDiagramPoint, 0);
                svcDiagramData.addData(diagramName, diagramDetails);
                LOG.debug("setting CGMES DL IIDM extensions for StaticVarCompensator: {}, {}", staticVarCompensator.getId(), svcDiagramPoint);
                staticVarCompensator.addExtension(InjectionDiagramData.class, svcDiagramData);
            });

            vlGraph.getNodes().stream().filter(node -> Objects.equals(node.getComponentType(), LINE)).forEach(node -> applyLayoutOnLines(node, voltageLevel, diagramName, offsetPoint));

            vlGraph.getNodes().stream().filter(node -> Objects.equals(node.getComponentType(), DANGLING_LINE)).forEach(node -> applyLayoutOnDanglingLines(node, voltageLevel, diagramName, offsetPoint));

            vlGraph.getNodes().stream().filter(node -> Objects.equals(node.getComponentType(), VSC_CONVERTER_STATION)).forEach(node -> applyLayoutOnVscConverterStation(node, voltageLevel, diagramName, offsetPoint));

            vlGraph.getNodes().stream().filter(node -> Objects.equals(node.getComponentType(), LCC_CONVERTER_STATION)).forEach(node -> applyLayoutOnLccConverterStation(node, voltageLevel, diagramName, offsetPoint));

            if (TopologyKind.BUS_BREAKER.equals(voltageLevel.getTopologyKind())) {
                voltageLevel.getBusBreakerView().getBusStream().forEach(bus ->
                        vlGraph.getNodeBuses().stream().filter(busNode -> busNode.getId().equals(bus.getId())).findFirst().ifPresent(busNode -> {
                            NodeDiagramData<Bus> busDiagramData = NodeDiagramData.getOrCreateDiagramData(bus);
                            setNodeDiagramPoints(busDiagramData, busNode, offsetPoint, diagramName);
                            LOG.debug("setting CGMES DL IIDM extensions for Bus {}, {} - {}", bus.getId(), busDiagramData.getData(diagramName).getPoint1(), busDiagramData.getData(diagramName).getPoint2());
                            bus.addExtension(NodeDiagramData.class, busDiagramData);
                        })
                );

            } else {
                voltageLevel.getNodeBreakerView().getBusbarSectionStream().forEach(busbarSection ->
                        vlGraph.getNodeBuses().stream().filter(busNode -> busNode.getId().equals(busbarSection.getId())).findFirst().ifPresent(busNode -> {
                            NodeDiagramData<BusbarSection> busbarSectionDiagramData = NodeDiagramData.getOrCreateDiagramData(busbarSection);
                            setNodeDiagramPoints(busbarSectionDiagramData, busNode, offsetPoint, diagramName);
                            LOG.debug("setting CGMES DL IIDM extensions for BusbarSection {}, {} - {}", busbarSection.getId(), busbarSectionDiagramData.getData(diagramName).getPoint1(), busbarSectionDiagramData.getData(diagramName).getPoint2());
                            busbarSection.addExtension(NodeDiagramData.class, busbarSectionDiagramData);
                        })
                );

                voltageLevel.getNodeBreakerView().getSwitchStream().filter(Objects::nonNull).forEach(sw -> {
                    Node swNode = vlGraph.getNode(sw.getId());
                    if (checkSwitchNode(swNode)) {
                        CouplingDeviceDiagramData<Switch> switchIidmDiagramData = new CouplingDeviceDiagramData<>(sw);
                        CouplingDeviceDiagramData<Switch>.CouplingDeviceDiagramDetails diagramDetails = switchIidmDiagramData.new CouplingDeviceDiagramDetails(offsetPoint.newDiagramPoint(swNode.getX(), swNode.getY(), 0), switchRotationValue(swNode));
                        switchIidmDiagramData.addData(diagramName, diagramDetails);
                        LOG.debug("setting CGMES DL IIDM extensions for Switch {}, {}", sw.getId(), switchIidmDiagramData);
                        sw.addExtension(CouplingDeviceDiagramData.class, switchIidmDiagramData);
                    }
                });
            }
        });

        substation.getTwoWindingsTransformerStream().forEach(twoWindingsTransformer -> sgraph.getMultiTermNodes().stream()
                .filter(node -> checkNode(twoWindingsTransformer, node)).findFirst().ifPresent(node -> {
                    DiagramPoint tDiagramPoint = offsetPoint.newDiagramPoint(node.getX(), node.getY(), 0);
                    CouplingDeviceDiagramData<TwoWindingsTransformer> transformerIidmDiagramData = new CouplingDeviceDiagramData<>(twoWindingsTransformer);
                    CouplingDeviceDiagramData<TwoWindingsTransformer>.CouplingDeviceDiagramDetails diagramDetails = transformerIidmDiagramData.new CouplingDeviceDiagramDetails(tDiagramPoint, rotationValue(node));
                    transformerIidmDiagramData.addData(diagramName, diagramDetails);
                    LOG.info("setting CGMES DL IIDM extensions for TwoWindingTransformer: {}, {}", twoWindingsTransformer.getId(), tDiagramPoint);
                    twoWindingsTransformer.addExtension(CouplingDeviceDiagramData.class, transformerIidmDiagramData);
                })
        );

        substation.getThreeWindingsTransformerStream().forEach(threeWindingsTransformer -> sgraph.getMultiTermNodes().stream()
                .filter(node -> checkNode(threeWindingsTransformer, node)).findFirst().ifPresent(node -> {
                    DiagramPoint tDiagramPoint = offsetPoint.newDiagramPoint(node.getX(), node.getY(), 0);
                    ThreeWindingsTransformerDiagramData transformerIidmDiagramData = new ThreeWindingsTransformerDiagramData(threeWindingsTransformer);
                    ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails diagramDetails = transformerIidmDiagramData.new ThreeWindingsTransformerDiagramDataDetails(tDiagramPoint, rotationValue(node));
                    transformerIidmDiagramData.addData(diagramName, diagramDetails);
                    LOG.debug("setting CGMES DL IIDM extensions for ThreeWindingTransformer: {}, {}", threeWindingsTransformer.getId(), tDiagramPoint);
                    threeWindingsTransformer.addExtension(ThreeWindingsTransformerDiagramData.class, transformerIidmDiagramData);
                })
        );

        return subsBoundary;
    }

    private void applyLayoutOnLines(Node node, VoltageLevel voltageLevel, String diagramName, OffsetPoint offsetPoint) {
        FeederNode lineNode = (FeederNode) node;
        Line line = voltageLevel.getConnectable(lineNode.getEquipmentId(), Line.class);
        if (line != null) {
            LineDiagramData<Line> lineDiagramData = LineDiagramData.getOrCreateDiagramData(line);
            int lineSeq = getMaxSeq(lineDiagramData.getPoints(diagramName)) + 1;
            DiagramPoint linePoint = offsetPoint.newDiagramPoint(lineNode.getX(), lineNode.getY(), lineSeq);
            lineDiagramData.addPoint(diagramName, linePoint);

            LOG.debug("setting CGMES DL IIDM extensions for Line {} ({}), new point {}", line.getId(), line.getNameOrId(), linePoint);
            line.addExtension(LineDiagramData.class, lineDiagramData);
        }
    }

    private void applyLayoutOnDanglingLines(Node node, VoltageLevel voltageLevel, String diagramName, OffsetPoint offsetPoint) {
        FeederNode danglingLineNode = (FeederNode) node;
        DanglingLine danglingLine = voltageLevel.getConnectable(danglingLineNode.getId(), DanglingLine.class);
        if (danglingLine != null) {
            LineDiagramData<DanglingLine> danglingLineDiagramData = LineDiagramData.getOrCreateDiagramData(danglingLine);
            int danglingLineSeq = getMaxSeq(danglingLineDiagramData.getPoints(diagramName)) + 1;
            DiagramPoint danglingLinePoint = offsetPoint.newDiagramPoint(danglingLineNode.getX(), danglingLineNode.getY(), danglingLineSeq);
            danglingLineDiagramData.addPoint(diagramName, danglingLinePoint);

            LOG.debug("setting CGMES DL IIDM extensions for Dangling line {} ({}),  point {}", danglingLine.getId(), danglingLine.getNameOrId(), danglingLinePoint);
            danglingLine.addExtension(LineDiagramData.class, danglingLineDiagramData);
        }
    }

    private void applyLayoutOnVscConverterStation(Node node, VoltageLevel voltageLevel, String diagramName, OffsetPoint offsetPoint) {
        FeederNode vscNode = (FeederNode) node;
        VscConverterStation vscConverterStation = voltageLevel.getConnectable(vscNode.getId(), VscConverterStation.class);
        if (vscConverterStation != null) {
            LineDiagramData<VscConverterStation> vscDiagramData = LineDiagramData.getOrCreateDiagramData(vscConverterStation);
            int danglingLineSeq = getMaxSeq(vscDiagramData.getPoints(diagramName)) + 1;
            DiagramPoint vscPoint = offsetPoint.newDiagramPoint(vscNode.getX(), vscNode.getY(), danglingLineSeq);
            vscDiagramData.addPoint(diagramName, vscPoint);

            LOG.debug("setting CGMES DL IIDM extensions for Vsc Converter Station {} ({}),  point {}", vscConverterStation.getId(), vscConverterStation.getNameOrId(), vscPoint);
            vscConverterStation.addExtension(LineDiagramData.class, vscDiagramData);
        }
    }

    private void applyLayoutOnLccConverterStation(Node node, VoltageLevel voltageLevel, String diagramName, OffsetPoint offsetPoint) {
        FeederNode lccNode = (FeederNode) node;
        LccConverterStation lccConverterStation = voltageLevel.getConnectable(lccNode.getId(), LccConverterStation.class);
        if (lccConverterStation != null) {
            LineDiagramData<LccConverterStation> lccDiagramData = LineDiagramData.getOrCreateDiagramData(lccConverterStation);
            int danglingLineSeq = getMaxSeq(lccDiagramData.getPoints(diagramName)) + 1;
            DiagramPoint lccPoint = offsetPoint.newDiagramPoint(lccNode.getX(), lccNode.getY(), danglingLineSeq);
            lccDiagramData.addPoint(diagramName, lccPoint);

            LOG.debug("setting CGMES DL IIDM extensions for Lcc Converter Station {} ({}),  point {}", lccConverterStation.getId(), lccConverterStation.getNameOrId(), lccPoint);
            lccConverterStation.addExtension(LineDiagramData.class, lccDiagramData);
        }
    }

    private boolean checkSwitchNode(Node swNode) {
        return swNode != null && swNode.getType().equals(Node.NodeType.SWITCH);
    }

    private boolean checkNode(ThreeWindingsTransformer threeWindingsTransformer, MiddleTwtNode node) {
        return node.getComponentType().equals(THREE_WINDINGS_TRANSFORMER)
                && node.getAdjacentNodes().stream().allMatch(n -> SldComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG.equals(n.getComponentType()) && n instanceof EquipmentNode && ((EquipmentNode) n).getEquipmentId().equals(threeWindingsTransformer.getId()));
    }

    private boolean checkNode(TwoWindingsTransformer twoWindingsTransformer, Node node) {
        return (node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER) || node.getComponentType().equals(PHASE_SHIFT_TRANSFORMER))
                && node.getAdjacentNodes().stream().allMatch(n -> n.getId().startsWith(twoWindingsTransformer.getId()));
    }

    private double rotationValue(Node node) {
        return node.getOrientation() == Orientation.UP ? 0.0 : 180.0;
    }

    private double switchRotationValue(Node node) {
        return node.getOrientation().isHorizontal() ? 90.0 : 0.0;
    }

    private void convertLayoutSingleDiagram(Network network, Stream<Substation> subsStream, String diagramName) {
        //creates a single CGMES-DL diagram (named diagramName), where each substation
        final double[] xoffset = {0.0};
        subsStream.forEach(s -> {
            LOG.debug("Substation {}({} offset: {})", s.getId(), s.getNameOrId(), xoffset[0]);
            NetworkDiagramData.addDiagramName(network, diagramName, s.getId());
            LayoutInfo li = applyLayout(network, s.getId(), xoffset[0], 0.0, diagramName);
            xoffset[0] += OFFSET_MULTIPLIER_X * li.getMaxX();
        });
    }

    private void convertLayoutMultipleDiagrams(Network network, Stream<Substation> subsStream) {
        // creates one CGMES-DL diagram for each substation (where each diagram name is the substation's name)
        subsStream.forEach(s -> {
            String subDiagramName = s.getNameOrId();
            NetworkDiagramData.addDiagramName(network, subDiagramName, s.getId());
            LOG.debug("Substation {}", subDiagramName);
            applyLayout(network, s.getId(), 0.0, 0.0, subDiagramName);
        });
    }

    /**
     * Apply the layout to the network, creating one or more CGMES-DL diagrams.
     * Note that a CGMES-DL diagram refers to a global coordinate system and can include all the network equipments,
     * whereas layouts are currently created per-substation (or per-voltage), using a coordinate system that is local to
     * the specific substation/voltage.
     * <p>
     * This method creates either a single CGMES-DL diagram (where each substation is placed on a single row, one next to the other),
     * or multiple CGMES_DL diagrams, one per substation.
     *
     * @param network network on which to apply the layout and convert it
     * @param diagramName the diagram's name, if <code>null</code> it creates one CGMES-DL diagram for each substation
     *                    (where each diagram name is the substation's name). Otherwise it creates a single CGMES-DL diagram
     *                    (named diagramName).
     */
    public void convertLayout(Network network, String diagramName) {
        Objects.requireNonNull(network);
        LOG.info("Converting layout {} to IIDM CGMES DL extensions for network: {}", sFactory.getClass(), network.getId());

        //Network could have already defined a set of iidm cgmes extensions, as loaded via the cgmes importer/cgmesDLImport postprocessor.
        //Also associated to the network, we have the triplestore with the DL related triples
        //clear the  CGMES DL profile data from the network's CGMES tiplestore, if it already exists
        //and remove any exising IIDM CGMES equipments' extensions
        CgmesDLUtils.clearCgmesDl(network);
        CgmesDLUtils.removeIidmCgmesExtensions(network);

        if (diagramName != null) {
            convertLayoutSingleDiagram(network, network.getSubstationStream(), diagramName);
        } else {
            convertLayoutMultipleDiagrams(network, network.getSubstationStream());
        }
    }

    /**
     * Apply the layout to the given network, creating one CGMES-DL diagrams per substation.
     */
    public void convertLayout(Network network) {
        convertLayout(network, null);
    }

    static class LayoutInfo {
        double maxX;
        double maxY;

        LayoutInfo(double maxNodeX, double maxNodeY) {
            this.maxX = maxNodeX;
            this.maxY = maxNodeY;
        }

        double getMaxX() {
            return maxX;
        }

        double getMaxY() {
            return maxY;
        }

        void update(double maxX, double maxY) {
            if (maxX > this.maxX) {
                this.maxX = maxX;
            }
            if (maxY > this.maxY) {
                this.maxY = maxY;
            }
        }
    }

    static class OffsetPoint {
        private final double dx;
        private final double dy;

        OffsetPoint(double dx, double dy) {
            this.dx = dx;
            this.dy = dy;
        }

        DiagramPoint newDiagramPoint(double x, double y, int seq) {
            return new DiagramPoint(x + dx, y + dy, seq);
        }
    }
}
