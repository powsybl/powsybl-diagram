/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.test.Networks;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.GeographicalLayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

import static com.powsybl.nad.build.iidm.VoltageLevelFilter.NO_FILTER;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class NetworkAreaDiagramTest extends AbstractTest {

    protected FileSystem fileSystem;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters()) {
        };
    }

    @Test
    void testDrawSvg() {
        Network network = Network.read(Path.of("/home/dupuyflo/Data/pf_with_substation_positions.xiidm"));
        Path svgFile = Path.of("/home/dupuyflo/france_init4.svg");
        NadParameters nadParameters = new NadParameters()
                .setSvgParameters(getSvgParameters())
//                .setLayoutParameters(new LayoutParameters().setMaxSteps(3))
                .setStyleProviderFactory(TopologicalStyleProvider::new)
                .setLayoutFactory(new GeographicalLayoutFactory(network));
        NetworkAreaDiagram.draw(network, svgFile, nadParameters, NO_FILTER);
        assertFileEquals("/dangling_line_connected.svg", svgFile);
    }

    @Test
    void testGetVisibleVoltageLevels() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        List<String> ids = NetworkAreaDiagram.getDisplayedVoltageLevels(network, List.of("VLHV1"), 1);
        assertEquals("VLGEN, VLHV1, VLHV2", String.join(", ", ids));

        ids = NetworkAreaDiagram.getDisplayedVoltageLevels(network, List.of("VLHV1"), 2);
        assertEquals("VLGEN, VLHV1, VLHV2, VLLOAD", String.join(", ", ids));
    }

    @Test
    void testVoltageFilteredDiagramTwoBounds() {
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), VoltageLevelFilter.createNominalVoltageFilter(network, List.of("VL4"), 120, 180, 2));
        assertFileEquals("/IEEE_14_bus_voltage_filter1.svg", svgFileVoltageFilter);
    }

    @Test
    void testVoltageFilteredDiagramLowerBound() {
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), VoltageLevelFilter.createNominalVoltageLowerBoundFilter(network, List.of("VL4"), 120, 2));
        assertFileEquals("/IEEE_14_bus_voltage_filter1.svg", svgFileVoltageFilter);
    }

    @Test
    void testVoltageFilteredDiagramUpperBound() {
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), VoltageLevelFilter.createNominalVoltageUpperBoundFilter(network, List.of("VL4"), 180, 2));
        assertFileEquals("/IEEE_14_bus_voltage_filter2.svg", svgFileVoltageFilter);
    }

    @Test
    void testVoltageFilteredDiagramNoVoltageLevelIdInput() {
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), VoltageLevelFilter.createNominalVoltageFilter(network, 120, 180));
        assertFileEquals("/IEEE_14_bus_voltage_filter1.svg", svgFileVoltageFilter);
    }

    @Test
    void testVoltageFilteredDiagramLowerBoundNoVoltageLevelIdInput() {
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), VoltageLevelFilter.createNominalVoltageLowerBoundFilter(network, 20));
        assertFileEquals("/IEEE_14_bus_voltage_filter5.svg", svgFileVoltageFilter);
    }

    @Test
    void testVoltageFilteredDiagramUpperBoundNoVoltageLevelIdInput() {
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), VoltageLevelFilter.createNominalVoltageUpperBoundFilter(network, 90));
        assertFileEquals("/IEEE_14_bus_voltage_filter4.svg", svgFileVoltageFilter);
    }

    @Test
    void testVoltageFilteredDiagramOutOfBound() {
        ListAppender<ILoggingEvent> logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(VoltageLevelFilter.class)).addAppender(logWatcher);
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        List<String> voltageLevelList = List.of("VL4");
        VoltageLevelFilter voltageLevelFilter = VoltageLevelFilter.createNominalVoltageUpperBoundFilter(network, voltageLevelList, 130, 2);
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), voltageLevelFilter);

        List<ILoggingEvent> logsList = logWatcher.list;
        assertEquals(1, logsList.size());
        assertEquals("vl 'VL4' does not comply with the predicate", logsList.get(0).getFormattedMessage());
        assertFileEquals("/IEEE_14_bus_voltage_filter3.svg", svgFileVoltageFilter);
    }

    @Test
    void testVoltageFilteredDiagramNegativeBound() {
        Network network = IeeeCdfNetworkFactory.create14();
        List<String> voltageLevelList = List.of("VL4");
        PowsyblException e = assertThrows(PowsyblException.class, () -> VoltageLevelFilter.createNominalVoltageFilter(network, voltageLevelList, -100, 180, 2));
        assertTrue(e.getMessage().contains("Voltage bounds must be positive"));
    }

    @Test
    void testVoltageFilteredDiagramInconsistentBounds() {
        Network network = IeeeCdfNetworkFactory.create14();
        List<String> voltageLevelList = List.of("VL4");
        PowsyblException e = assertThrows(PowsyblException.class, () -> VoltageLevelFilter.createNominalVoltageFilter(network, voltageLevelList, 180, 90, 2));
        assertTrue(e.getMessage().contains("Low bound must be less than or equal to high bound"));
    }

    @Test
    void testVoltageFilteredDiagramUnexistingVoltageLevel() {
        Network network = IeeeCdfNetworkFactory.create14();
        List<String> voltageLevelList = List.of("VL456");
        PowsyblException e = assertThrows(PowsyblException.class, () -> VoltageLevelFilter.createNominalVoltageUpperBoundFilter(network, voltageLevelList, 90, 2));
        assertTrue(e.getMessage().contains("Unknown voltage level id 'VL456'"));
    }

    @Test
    void testDraw() {
        Network network = IeeeCdfNetworkFactory.create14();
        // test writers
        try (Writer svgWriter = new StringWriter(); StringWriter metadataWriter = new StringWriter()) {
            NetworkAreaDiagram.draw(network, svgWriter, metadataWriter);
            assertStringEquals("/IEEE_14_bus_voltage_nofilter.svg", svgWriter.toString());
            assertStringEquals("/IEEE_14_bus_voltage_nofilter_metadata.json", metadataWriter.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // test files
        Path svgFile = fileSystem.getPath("nad-ieee-14-bus.svg");
        NetworkAreaDiagram.draw(network, svgFile);
        assertFileEquals("/IEEE_14_bus_voltage_nofilter.svg", svgFile);
        Path metadataFile = fileSystem.getPath("nad-ieee-14-bus_metadata.json");
        assertFileEquals("/IEEE_14_bus_voltage_nofilter_metadata.json", metadataFile);
    }

    @Test
    void testDrawToString() {
        Network network = IeeeCdfNetworkFactory.create14();
        String svg = NetworkAreaDiagram.drawToString(network, new SvgParameters());
        assertStringEquals("/IEEE_14_bus_voltage_nofilter.svg", svg);
    }

    @Test
    void testDrawWithFilter() {
        Network network = IeeeCdfNetworkFactory.create14();
        try (Writer svgWriter = new StringWriter(); StringWriter metadataWriter = new StringWriter()) {
            NetworkAreaDiagram.draw(network, svgWriter, metadataWriter, VoltageLevelFilter.createNominalVoltageLowerBoundFilter(network, 20));
            assertStringEquals("/IEEE_14_bus_voltage_filter5.svg", svgWriter.toString());
            assertStringEquals("/IEEE_14_bus_voltage_filter5_metadata.json", metadataWriter.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void testDrawWithVoltageLevel() {
        Network network = IeeeCdfNetworkFactory.create14();
        // test writers
        try (Writer svgWriter = new StringWriter(); StringWriter metadataWriter = new StringWriter()) {
            NetworkAreaDiagram.draw(network, svgWriter, metadataWriter, "VL4", 2);
            assertStringEquals("/IEEE_14_bus_voltage_filter2.svg", svgWriter.toString());
            assertStringEquals("/IEEE_14_bus_voltage_filter2_metadata.json", metadataWriter.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // test files
        Path svgFile = fileSystem.getPath("nad-ieee-14-bus.svg");
        NetworkAreaDiagram.draw(network, svgFile, "VL4", 2);
        assertFileEquals("/IEEE_14_bus_voltage_filter2.svg", svgFile);
        Path metadataFile = fileSystem.getPath("nad-ieee-14-bus_metadata.json");
        assertFileEquals("/IEEE_14_bus_voltage_filter2_metadata.json", metadataFile);

        Path svgFile2 = fileSystem.getPath("nad-ieee-14-bus2.svg");
        NetworkAreaDiagram.draw(network, svgFile2, List.of("VL4"), 2);
        assertFileEquals("/IEEE_14_bus_voltage_filter2.svg", svgFile2);
        Path metadataFile2 = fileSystem.getPath("nad-ieee-14-bus2_metadata.json");
        assertFileEquals("/IEEE_14_bus_voltage_filter2_metadata.json", metadataFile2);

    }

    @Test
    void testDrawWithVoltageLevels() {
        Network network = IeeeCdfNetworkFactory.create14();
        // test writers
        try (Writer svgWriter = new StringWriter(); StringWriter metadataWriter = new StringWriter()) {
            NetworkAreaDiagram.draw(network, svgWriter, metadataWriter, List.of("VL1", "VL2", "VL3", "VL4", "VL5", "VL8"));
            assertStringEquals("/IEEE_14_bus_voltage_filter5.svg", svgWriter.toString());
            assertStringEquals("/IEEE_14_bus_voltage_filter5_metadata.json", metadataWriter.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // test files
        Path svgFile = fileSystem.getPath("nad-ieee-14-bus.svg");
        NetworkAreaDiagram.draw(network, svgFile, List.of("VL1", "VL2", "VL3", "VL4", "VL5", "VL8"));
        assertFileEquals("/IEEE_14_bus_voltage_filter5.svg", svgFile);
        Path metadataFile = fileSystem.getPath("nad-ieee-14-bus_metadata.json");
        assertFileEquals("/IEEE_14_bus_voltage_filter5_metadata.json", metadataFile);
    }
}
