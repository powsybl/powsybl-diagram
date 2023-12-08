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
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

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
        Network network = Networks.createThreeVoltageLevelsFiveBuses();
        Path svgFile = fileSystem.getPath("nad-test.svg");
        NadParameters nadParameters = new NadParameters()
                .setSvgParameters(getSvgParameters())
                .setStyleProviderFactory(this::getStyleProvider);
        NetworkAreaDiagram.draw(network, svgFile, nadParameters, NO_FILTER);
        assertEquals(toString("/dangling_line_connected.svg"), getContentFile(svgFile));
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
        assertEquals(toString("/IEEE_14_bus_voltage_filter1.svg"), getContentFile(svgFileVoltageFilter));
    }

    @Test
    void testVoltageFilteredDiagramLowBound() {
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), VoltageLevelFilter.createNominalVoltageLowerBoundFilter(network, List.of("VL4"), 120, 2));
        assertEquals(toString("/IEEE_14_bus_voltage_filter1.svg"), getContentFile(svgFileVoltageFilter));
    }

    @Test
    void testVoltageFilteredDiagramHighBound() {
        Network network = IeeeCdfNetworkFactory.create14();
        Path svgFileVoltageFilter = fileSystem.getPath("nad-test-voltage-filter.svg");
        NetworkAreaDiagram.draw(network, svgFileVoltageFilter, new NadParameters(), VoltageLevelFilter.createNominalVoltageUpperBoundFilter(network, List.of("VL4"), 180, 2));
        assertEquals(toString("/IEEE_14_bus_voltage_filter2.svg"), getContentFile(svgFileVoltageFilter));
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
        assertEquals(toString("/IEEE_14_bus_voltage_filter3.svg"), getContentFile(svgFileVoltageFilter));
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
}
