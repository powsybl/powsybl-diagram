/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

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
import org.junit.jupiter.api.AfterEach;
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
 * Note: this class does not use a mock because it had some strange interaction with other tests, where it would greatly
 * increase the time it would take for some other tests to finish (making it so that running all tests would take 5 minutes instead of 30s)
 * The problem was also that this behaviour depends on the order in which tests pass (so sometimes the issue would appear, sometimes not)
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
class SingleLineDiagramToolTest extends AbstractToolTest {

    private SingleLineDiagramTool tool;

    private ToolRunningContext runningContext;

    private DefaultParser defaultParser;

    private static final String COMMAND_NAME = "generate-substation-diagram";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        tool = new SingleLineDiagramTool();
        Files.createDirectory(fileSystem.getPath("/tmp"));
        Path inputDir = Files.createDirectory(fileSystem.getPath("/input-dir"));

        Network network = NetworkTest1Factory.create();
        // Write the file
        Path networkFile = inputDir.resolve("sld-tool-test.xiidm");
        Properties properties = new Properties();
        properties.put(XMLExporter.EXTENSIONS_INCLUDED_LIST, "");
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

    @AfterEach
    @Override
    public void tearDown() throws IOException {
        this.fileSystem.close();
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
        assertEquals("generate substation diagram", tool.getCommand().getDescription());
        assertNull(tool.getCommand().getUsageFooter());

        assertCommand(tool.getCommand(), COMMAND_NAME, 5, 2);
        assertOption(tool.getCommand().getOptions(), "input-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-dir", true, true);
        assertOption(tool.getCommand().getOptions(), "ids", false, true);
        assertOption(tool.getCommand().getOptions(), "all-voltage-levels", false, false);
        assertOption(tool.getCommand().getOptions(), "all-substations", false, false);
    }

    @Test
    void missingInputFileOptions() throws ParseException {
        CommandLine commandLine = defaultParser.parse(new Options(), new String[]{});
        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("input-file option is missing"));
    }

    @Test
    void missingOutputDirOptions() throws ParseException {
        Options options = new Options();
        options.addOption("input-file", true, "input file");
        CommandLine commandLine = defaultParser.parse(options, new String[]{"-input-file", "/input-dir/sld-tool-test.xiidm"});
        PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(commandLine, runningContext));
        assertTrue(e.getMessage().contains("output-dir option is missing"));
    }

    @Test
    void generateSome() throws IOException, ParseException {
        String ids = "VLGEN";

        Options options = new Options();
        options.addOption("input-file", true, "input file");
        options.addOption("output-dir", true, "output-dir");
        options.addOption("ids", true, "ids");

        //the options use single dash because add option uses single dash by default when passing just opt (and not longOpt), like -i for short and --input for long
        //except here we only gave input so that defaults to short option, thus the -input instead of --input
        CommandLine commandLine = defaultParser.parse(
            options,
            new String[] {
                "-input-file",
                "/input-dir/sld-tool-test.xiidm",
                "-output-dir",
                "/tmp",
                "-ids",
                ids
            }
        );

        tool.run(commandLine, runningContext);

        Path resultPath = fileSystem.getPath("/tmp");
        try (Stream<Path> stream = Files.list(resultPath)) {
            stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .forEach(f -> assertTrue(f.contains(ids)));
        }
    }

    @Test
    void generateWithAllVoltageLevels() throws IOException, ParseException {
        Options options = new Options();
        options.addOption("input-file", true, "input file");
        options.addOption("output-dir", true, "output-dir");
        options.addOption("all-voltage-levels", true, "all-voltage-levels");
        options.addOption("all-substations", true, "all-substations");

        CommandLine commandLine = defaultParser.parse(
            options,
            new String[] {
                "-input-file",
                "/input-dir/sld-tool-test.xiidm",
                "-output-dir",
                "/tmp",
                "-all-voltage-levels",
                String.valueOf(false),
                "-all-substations",
                String.valueOf(false)
            }
        );

        tool.run(commandLine, runningContext);

        Path resultPath = fileSystem.getPath("/tmp");
        try (Stream<Path> stream = Files.list(resultPath)) {
            assertEquals(4, stream.count());
        }
    }

    @Test
    void generateWithAllSubstations() throws IOException, ParseException {
        Options options = new Options();
        options.addOption("input-file", true, "input file");
        options.addOption("output-dir", true, "output-dir");
        options.addOption("all-substations", true, "all-substations");

        CommandLine commandLine = defaultParser.parse(
            options,
            new String[] {
                "-input-file",
                "/input-dir/sld-tool-test.xiidm",
                "-output-dir",
                "/tmp",
                "-all-substations",
                String.valueOf(true)
            }
        );

        tool.run(commandLine, runningContext);

        Path resultPath = fileSystem.getPath("/tmp");
        try (Stream<Path> stream = Files.list(resultPath)) {
            assertEquals(2, stream.count());
        }
    }
}
