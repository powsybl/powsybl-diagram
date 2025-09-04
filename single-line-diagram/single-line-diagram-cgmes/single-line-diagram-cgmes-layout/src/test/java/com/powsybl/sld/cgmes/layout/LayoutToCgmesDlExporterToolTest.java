/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.LazyCreatedComputationManager;
import com.powsybl.computation.local.LocalComputationManagerFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.serde.XMLExporter;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.tools.test.AbstractToolTest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class LayoutToCgmesDlExporterToolTest extends AbstractToolTest {

    private LayoutToCgmesDlExporterTool tool;

    private ToolRunningContext runningContext;

    private DefaultParser defaultParser;

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

        runningContext = new ToolRunningContext(
                new PrintStream(PrintStream.nullOutputStream()),
                new PrintStream(PrintStream.nullOutputStream()),
                fileSystem,
                new LazyCreatedComputationManager(new LocalComputationManagerFactory()),
                new LazyCreatedComputationManager(new LocalComputationManagerFactory())
        );

        defaultParser = new DefaultParser();
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    @Test
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
    void missingInputFileOptions() throws ParseException {
        CommandLine commandLine = defaultParser.parse(new Options(), new String[]{});
        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("input-file parameter is missing"));
    }

    @Test
    void missingOutputDirOptions() throws ParseException {
        Options options = new Options();
        options.addOption("input-file", true, "input file");
        CommandLine commandLine = defaultParser.parse(options, new String[]{"-input-file", "/input-dir/sld-tool-test.xiidm"});
        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("output-dir parameter is missing"));
    }

    @Test
    void invalidVoltageLevelLayoutOptions() throws ParseException {
        Options options = new Options();
        options.addOption("input-file", true, "input file");
        options.addOption("output-dir", true, "output-dir");
        options.addOption("voltage-level-layout", true, "voltage level layout");

        CommandLine commandLine = defaultParser.parse(
            options,
            new String[] {
                "-input-file",
                "/input-dir/sld-tool-test.xiidm",
                "-output-dir",
                "/tmp",
                "-voltage-level-layout",
                "InvalidLayoutLevel"
            }
        );

        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("invalid voltage-level-layout: InvalidLayoutLevel"));
    }

    @Test
    void invalidSubstationLayoutOptions() throws ParseException {
        Options options = new Options();
        options.addOption("input-file", true, "input file");
        options.addOption("output-dir", true, "output-dir");
        options.addOption("substation-layout", true, "substation-layout");

        CommandLine commandLine = defaultParser.parse(
            options,
            new String[] {
                "-input-file",
                "/input-dir/sld-tool-test.xiidm",
                "-output-dir",
                "/tmp",
                "-substation-layout",
                "InvalidSubstationLayout"
            }
        );

        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("invalid substation-layout: InvalidSubstationLayout"));
    }

    @Test
    void runTest() throws IOException, ParseException {
        Options options = new Options();
        options.addOption("input-file", true, "input file");
        options.addOption("output-dir", true, "output-dir");
        options.addOption("voltage-level-layout", true, "voltage level layout");
        options.addOption("substation-layout", true, "substation-layout");
        options.addOption("diagram-name", true, "diagram-name");

        CommandLine commandLine = defaultParser.parse(
            options,
            new String[] {
                "-input-file",
                "/input-dir/sld-tool-test.xiidm",
                "-output-dir",
                "/tmp",
                "-voltage-level-layout",
                "auto-without-extensions",
                "-substation-layout",
                "horizontal",
                "-diagram-name",
                "name"
            }
        );

        tool.run(commandLine, runningContext);

        Path resultPath = fileSystem.getPath("/tmp");
        try (Stream<Path> stream = Files.list(resultPath)) {
            assertEquals(1, stream.count());
        }
    }
}
