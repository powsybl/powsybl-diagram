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
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.SVGWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestVoltageLevelDiagram extends AbstractTestCase {

    private FileSystem fileSystem;
    private Path tmpDir;

    @Before
    public void setUp() throws IOException {
        network = Network.create("testCase1", "test");
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

        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));
    }

    @Test
    public void test() throws IOException {
        ComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");
        LayoutParameters layoutParameters = new LayoutParameters();

        Path outSvg = tmpDir.resolve("vl.svg");

        VoltageLevelDiagram.build(vl, new PositionVoltageLevelLayoutFactory(), false, false).writeSvg("", componentLibrary, layoutParameters, network, outSvg);
        String svgStr = normalizeLineSeparator(new String(Files.readAllBytes(outSvg), StandardCharsets.UTF_8));

//        FileWriter fw = new FileWriter(System.getProperty("user.home") + "/TestVL.svg");
//        fw.write(svgStr);
//        fw.close();

        String refSvg = normalizeLineSeparator(new String(
                ByteStreams.toByteArray(getClass().getResourceAsStream("/TestVL.svg")), StandardCharsets.UTF_8));
        assertEquals(refSvg, svgStr);
    }

    @Test
    public void testWriter() throws IOException {
        ComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");
        LayoutParameters layoutParameters = new LayoutParameters();

        SVGWriter writer = new DefaultSVGWriter(componentLibrary, layoutParameters);

        Path outSvg = tmpDir.resolve("vl.svg");

        VoltageLevelDiagram.build(vl, new PositionVoltageLevelLayoutFactory(), false, false).writeSvg("", writer, network, outSvg);
        String svgStr = normalizeLineSeparator(new String(Files.readAllBytes(outSvg), StandardCharsets.UTF_8));

        String refSvg = normalizeLineSeparator(new String(
                ByteStreams.toByteArray(getClass().getResourceAsStream("/TestVL.svg")), StandardCharsets.UTF_8));
        assertEquals(refSvg, svgStr);
    }
}
