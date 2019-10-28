/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.google.common.io.ByteStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.svg.DefaultDiagramInitialValueProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestUnicityNodeIdWithMutipleNetwork extends AbstractTestCase {

    private FileSystem fileSystem;
    private Path tmpDir;

    private Network network2;
    private GraphBuilder graphBuilder2;
    private Substation substation2;
    private VoltageLevel vl2;

    @Before
    public void setUp() throws IOException {
        // Create first network with a substation and a voltageLevel
        network = Network.create("n1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.newSubstation().setId("s").setCountry(Country.FR).add();
        vl = substation.newVoltageLevel().setId("vl").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(400).add();
        VoltageLevel.NodeBreakerView view = vl.getNodeBreakerView().setNodeCount(10);
        BusbarSection bbs = view.newBusbarSection().setId("bbs").setNode(0).add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs, 1, 1));
        Load l = vl.newLoad().setId("l").setNode(2).setP0(10).setQ0(10).add();
        l.addExtension(ConnectablePosition.class, new ConnectablePosition<>(l,
                new ConnectablePosition.Feeder("l", 0, ConnectablePosition.Direction.TOP), null, null, null));
        view.newDisconnector().setId("d").setNode1(0).setNode2(1).add();
        view.newBreaker().setId("b").setNode1(1).setNode2(2).add();

        // Create second network with a substation and a voltageLevel
        network2 = Network.create("n2", "test");
        graphBuilder2 = new NetworkGraphBuilder(network2);
        substation2 = network2.newSubstation().setId("s").setCountry(Country.FR).add();
        vl2 = substation2.newVoltageLevel().setId("vl").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(400).add();
        VoltageLevel.NodeBreakerView view2 = vl2.getNodeBreakerView().setNodeCount(10);
        BusbarSection bbs2 = view2.newBusbarSection().setId("bbs").setNode(0).add();
        bbs2.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs2, 1, 1));
        Load l2 = vl2.newLoad().setId("l").setNode(2).setP0(10).setQ0(10).add();
        l2.addExtension(ConnectablePosition.class, new ConnectablePosition<>(l2,
                new ConnectablePosition.Feeder("l", 0, ConnectablePosition.Direction.TOP), null, null, null));
        view2.newDisconnector().setId("d").setNode1(0).setNode2(1).add();
        view2.newBreaker().setId("b").setNode1(1).setNode2(2).add();

        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));
    }

    @Test
    public void test() throws IOException {
        ComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");
        LayoutParameters layoutParameters = new LayoutParameters();

        // Generating svg for voltage level in first network
        Path outSvg1 = tmpDir.resolve("vl_network1.svg");
        VoltageLevelDiagram.build(graphBuilder, vl.getId(), new PositionVoltageLevelLayoutFactory(), false, false)
                .writeSvg("network1_", componentLibrary,
                        layoutParameters, new DefaultDiagramInitialValueProvider(network), outSvg1);
        String svgStr1 = normalizeLineSeparator(new String(Files.readAllBytes(outSvg1), StandardCharsets.UTF_8));

//        FileWriter fw1 = new FileWriter(System.getProperty("user.home") + "/TestUnicityNodeIdNetWork1.svg");
//        fw1.write(svgStr1);
//        fw1.close();

        String refSvg1 = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/TestUnicityNodeIdNetWork1.svg")), StandardCharsets.UTF_8));
        assertEquals(refSvg1, svgStr1);

        // Generating svg for voltage level in second network
        Path outSvg2 = tmpDir.resolve("vl_network2.svg");
        VoltageLevelDiagram.build(graphBuilder2, vl2.getId(), new PositionVoltageLevelLayoutFactory(), false, false)
                .writeSvg("network2_", componentLibrary, layoutParameters,
                        new DefaultDiagramInitialValueProvider(network2), outSvg2);
        String svgStr2 = normalizeLineSeparator(new String(Files.readAllBytes(outSvg2), StandardCharsets.UTF_8));

//        FileWriter fw2 = new FileWriter(System.getProperty("user.home") + "/TestUnicityNodeIdNetWork2.svg");
//        fw2.write(svgStr2);
//        fw2.close();

        String refSvg2 = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/TestUnicityNodeIdNetWork2.svg")), StandardCharsets.UTF_8));
        assertEquals(refSvg2, svgStr2);

        assertNotEquals(svgStr1, svgStr2);

        // Generating the svg for the voltage levels, without the prefix identifying each network
        VoltageLevelDiagram.build(graphBuilder, vl.getId(), new PositionVoltageLevelLayoutFactory(), false, false)
                .writeSvg("", componentLibrary, layoutParameters,
                        new DefaultDiagramInitialValueProvider(network), outSvg1);
        svgStr1 = normalizeLineSeparator(new String(Files.readAllBytes(outSvg1), StandardCharsets.UTF_8));

        VoltageLevelDiagram.build(graphBuilder2, vl2.getId(), new PositionVoltageLevelLayoutFactory(), false, false)
                .writeSvg("", componentLibrary, layoutParameters,
                        new DefaultDiagramInitialValueProvider(network2), outSvg2);
        svgStr2 = normalizeLineSeparator(new String(Files.readAllBytes(outSvg2), StandardCharsets.UTF_8));

        assertEquals(svgStr1, svgStr2);
    }
}
