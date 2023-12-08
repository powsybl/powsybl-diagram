/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.serde.XMLExporter;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.tools.test.AbstractToolTest;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class LayoutToCgmesDlExporterToolTest extends AbstractToolTest {

    private LayoutToCgmesDlExporterTool tool;

    private ToolRunningContext runningContext;

    private CommandLine commandLine;

    private static final String COMMAND_NAME = "generate-cgmes-dl";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        tool = new LayoutToCgmesDlExporterTool();
        Files.createDirectory(fileSystem.getPath("/tmp"));
        Path inputDir = Files.createDirectory(fileSystem.getPath("/input-dir"));

        Network network = NetworkTest1Factory.create();
        // Write the file
        Path networkFile = inputDir.resolve("sld-tool-test.xiidm");
        Properties properties = new Properties();
        properties.put(XMLExporter.EXTENSIONS_LIST, "");
        properties.put(XMLExporter.VERSION, "1.0");
        network.write("XIIDM", properties, networkFile);

        runningContext = mock(ToolRunningContext.class);
        PrintStream err = mock(PrintStream.class);
        PrintStream out = mock(PrintStream.class);
        when(runningContext.getErrorStream()).thenReturn(err);
        when(runningContext.getOutputStream()).thenReturn(out);
        when(runningContext.getFileSystem()).thenReturn(fileSystem);

        commandLine = mock(CommandLine.class);
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        assertEquals(COMMAND_NAME, tool.getCommand().getName());
        assertEquals("Single line diagram", tool.getCommand().getTheme());
        assertEquals("apply a layout to a network, generate and export a new CGMES-DL profile", tool.getCommand().getDescription());
        assertNotNull(tool.getCommand().getUsageFooter());

        assertCommand(tool.getCommand(), COMMAND_NAME, 5, 2);
        assertOption(tool.getCommand().getOptions(), "input-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-dir", true, true);
        assertOption(tool.getCommand().getOptions(), "voltage-level-layout", false, true);
        assertOption(tool.getCommand().getOptions(), "substation-layout", false, true);
        assertOption(tool.getCommand().getOptions(), "diagram-name", false, true);
    }

    @Test
    void missingInputFileOptions() {
        when(commandLine.hasOption("input-file")).thenReturn(false);
        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("input-file parameter is missing"));
    }

    @Test
    void missingOutputDirOptions() {
        when(commandLine.hasOption("input-file")).thenReturn(true);
        when(commandLine.getOptionValue("input-file")).thenReturn("/input-dir/sld-tool-test.xiidm");
        when(commandLine.hasOption("output-dir")).thenReturn(false);
        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("output-dir parameter is missing"));
    }

    @Test
    void invalidVoltageLevelLayoutOptions() {
        when(commandLine.hasOption("input-file")).thenReturn(true);
        when(commandLine.getOptionValue("input-file")).thenReturn("/input-dir/sld-tool-test.xiidm");
        when(commandLine.hasOption("output-dir")).thenReturn(true);
        when(commandLine.getOptionValue("output-dir")).thenReturn("/tmp");

        when(commandLine.hasOption("voltage-level-layout")).thenReturn(true);
        when(commandLine.getOptionValue("voltage-level-layout")).thenReturn("InvalidLayoutLevel");
        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("invalid voltage-level-layout: InvalidLayoutLevel"));
    }

    @Test
    void invalidSubstationLayoutOptions() {
        when(commandLine.hasOption("input-file")).thenReturn(true);
        when(commandLine.getOptionValue("input-file")).thenReturn("/input-dir/sld-tool-test.xiidm");
        when(commandLine.hasOption("output-dir")).thenReturn(true);
        when(commandLine.getOptionValue("output-dir")).thenReturn("/tmp");

        when(commandLine.hasOption("substation-layout")).thenReturn(true);
        when(commandLine.getOptionValue("substation-layout")).thenReturn("InvalidSubstationLayout");
        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("invalid substation-layout: InvalidSubstationLayout"));
    }

    @Test
    void runTest() throws IOException {
        when(commandLine.hasOption("input-file")).thenReturn(true);
        when(commandLine.getOptionValue("input-file")).thenReturn("/input-dir/sld-tool-test.xiidm");
        when(commandLine.hasOption("output-dir")).thenReturn(true);
        when(commandLine.getOptionValue("output-dir")).thenReturn("/tmp");

        when(commandLine.hasOption("voltage-level-layout")).thenReturn(true);
        when(commandLine.hasOption("substation-layout")).thenReturn(true);
        when(commandLine.hasOption("diagram-name")).thenReturn(true);

        when(commandLine.getOptionValue("voltage-level-layout")).thenReturn("auto-without-extensions");
        when(commandLine.getOptionValue("substation-layout")).thenReturn("horizontal");
        when(commandLine.getOptionValue("diagram-name")).thenReturn("name");

        tool.run(commandLine, runningContext);

        Path resultPath = fileSystem.getPath("/tmp");
        try (Stream<Path> stream = Files.list(resultPath)) {
            assertEquals(1, stream.count());
        }
    }
}
