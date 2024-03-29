/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
class LayoutToCgmesExtensionsTest {

    private final List<Network> networks = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        createNetworks();
    }

    private void createNetworks() {
        networks.add(Networks.createNetworkWithGenerator());
        networks.add(Networks.createNetworkWithLine());
        networks.add(Networks.createNetworkWithDanglingLine());
        networks.add(Networks.createNetworkWithBusbar());
        networks.add(Networks.createNetworkWithBus());
        networks.add(Networks.createNetworkWithLoad());
        networks.add(Networks.createNetworkWithShuntCompensator());
        networks.add(Networks.createNetworkWithStaticVarCompensator());
        networks.add(Networks.createNetworkWithSwitch());
        networks.add(Networks.createNetworkWithTwoWindingsTransformer());
        networks.add(Networks.createNetworkWithThreeWindingsTransformer());
        networks.add(Networks.createNetworkWithBusbarAndSwitch());
        networks.add(Networks.createNetworkWithPhaseShiftTransformer());
        networks.add(Networks.createNetworkWithHvdcLines());
    }

    private void checkExtensionsSet(Network network) {
        network.getVoltageLevelStream().forEach(vl -> {
            vl.visitEquipments(new DefaultTopologyVisitor() {
                @Override
                public void visitDanglingLine(DanglingLine danglingLine) {
                    assertNotNull(danglingLine.getExtension(LineDiagramData.class));
                }

                @Override
                public void visitGenerator(Generator generator) {
                    assertNotNull(generator.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitShuntCompensator(ShuntCompensator sc) {
                    assertNotNull(sc.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitLoad(Load load) {
                    assertNotNull(load.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
                    assertNotNull(staticVarCompensator.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitLine(Line line, TwoSides side) {
                    assertNotNull(line.getExtension(LineDiagramData.class));
                }

                @Override
                public void visitBusbarSection(BusbarSection busBarSection) {
                    assertNotNull(busBarSection.getExtension(NodeDiagramData.class));
                }

                @Override
                public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
                    assertNotNull(transformer.getExtension(CouplingDeviceDiagramData.class));
                }

                @Override
                public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {
                    assertNotNull(transformer.getExtension(ThreeWindingsTransformerDiagramData.class));
                }

                @Override
                public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                    assertNotNull(converterStation.getExtension(LineDiagramData.class));
                }
            });
        });
    }

    private void checkExtensionsUnset(Network network) {
        network.getVoltageLevelStream().forEach(vl -> {
            vl.visitEquipments(new DefaultTopologyVisitor() {
                @Override
                public void visitDanglingLine(DanglingLine danglingLine) {
                    assertNull(danglingLine.getExtension(LineDiagramData.class));
                }

                @Override
                public void visitGenerator(Generator generator) {
                    assertNull(generator.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitShuntCompensator(ShuntCompensator sc) {
                    assertNull(sc.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitLoad(Load load) {
                    assertNull(load.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
                    assertNull(staticVarCompensator.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitLine(Line line, TwoSides side) {
                    assertNull(line.getExtension(LineDiagramData.class));
                }

                @Override
                public void visitBusbarSection(BusbarSection busBarSection) {
                    assertNull(busBarSection.getExtension(NodeDiagramData.class));
                }

                @Override
                public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
                    assertNull(transformer.getExtension(CouplingDeviceDiagramData.class));
                }

                @Override
                public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {
                    assertNull(transformer.getExtension(ThreeWindingsTransformerDiagramData.class));
                }

                @Override
                public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                    assertNull(converterStation.getExtension(LineDiagramData.class));
                }
            });
        });
    }

    @Test
    void testCgmesDlExtensionsEmpty() {
        networks.forEach(this::checkExtensionsUnset);
    }

    @Test
    void testCgmesDlExtensionsSet() {
        networks.forEach(network -> {
            LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
            lconv.convertLayout(network, "new-diagram");
            checkExtensionsSet(network);
        });
    }

    @Test
    void testCgmesDlExtensionsSetNoname() {
        networks.forEach(network -> {
            LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
            lconv.convertLayout(network);
            checkExtensionsSet(network);
        });
    }

    @Test
    void testCgmesDlExtensionsEmptyNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("testEmpty", "testEmpty");
        LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
        lconv.convertLayout(network, null);

        checkExtensionsUnset(network);
    }

    @Test
    void testCgmesDlExtensionsBridgePatternNetwork() {
        Network network = Networks.createNetworkWithBridge();

        LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
        lconv.convertLayout(network);

        checkExtensionsSet(network);

        VoltageLevel vl1 = network.getVoltageLevel("V1");
        assertTrue(VoltageLevelDiagramData.checkDiagramData(vl1));

        VoltageLevel vl2 = network.getVoltageLevel("V2");
        assertFalse(VoltageLevelDiagramData.checkDiagramData(vl2));

        VoltageLevel vl3 = network.getVoltageLevel("V3");
        assertFalse(VoltageLevelDiagramData.checkDiagramData(vl3));

        assertEquals(2, VoltageLevelDiagramData.getInternalNodeDiagramPoints(vl1, vl1.getSubstation().map(Substation::getId).orElse(null)).length);
    }

    @Test
    void testCoverage() {
        // Spying Network
        Network network = Mockito.spy(Networks.createNetworkWithHvdcLines());
        // Spying Substation1
        Substation substation = Mockito.spy(network.getSubstation("Substation1"));
        // Spying VoltageLevel
        VoltageLevel vl1 = Mockito.spy(network.getVoltageLevel("VoltageLevel1"));
        // Using Substation1 only
        Mockito.doReturn(substation).when(network).getSubstation(Mockito.anyString());
        // Using VoltageLevel1 only
        Answer<Stream<VoltageLevel>> answer = invocation -> Stream.of(vl1);
        Mockito.doAnswer(answer).when(substation).getVoltageLevelStream();
        // Faking Converter1 to null
        Mockito.doReturn(null).when(vl1).getConnectable("Converter1", VscConverterStation.class);
        // Faking Converter3 to null
        Mockito.doReturn(null).when(vl1).getConnectable("Converter3", LccConverterStation.class);

        LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
        lconv.convertLayout(network, "new-diagram");

        network.getVscConverterStationStream().forEach(vsc -> assertNull(vsc.getExtension(LineDiagramData.class)));
        network.getLccConverterStationStream().forEach(lcc -> assertNull(lcc.getExtension(LineDiagramData.class)));
    }
}
