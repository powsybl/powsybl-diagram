/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.util.TopologicalStyleProvider;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.powsybl.sld.AbstractSingleLineDiagramCommand.INPUT_FILE;
import static com.powsybl.sld.AbstractSingleLineDiagramCommand.OUTPUT_DIR;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class SingleLineDiagramTool implements Tool {

    private static final String IDS = "ids";
    private static final String ALL_VOLTAGE_LEVELS = "all-voltage-levels";
    private static final String ALL_SUBSTATIONS = "all-substations";

    @Override
    public Command getCommand() {
        return new AbstractSingleLineDiagramCommand() {

            @Override
            public String getName() {
                return "generate-substation-diagram";
            }

            @Override
            public String getDescription() {
                return "generate substation diagram";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                addInputFileOption(options);
                addOutputDirectoryOption(options);
                options.addOption(Option.builder().longOpt(IDS)
                        .desc("voltage level/substation id list")
                        .hasArg()
                        .argName("ID_LIST")
                        .build());
                options.addOption(Option.builder().longOpt(ALL_VOLTAGE_LEVELS)
                        .desc("all voltage levels")
                        .build());
                options.addOption(Option.builder().longOpt(ALL_SUBSTATIONS)
                        .desc("all substations")
                        .build());

                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private Path getSvgFile(Path outputDir, String id) {
        try {
            return outputDir.resolve(URLEncoder.encode(id, StandardCharsets.UTF_8.name()) + ".svg");
        } catch (UnsupportedEncodingException e) {
            throw new PowsyblException(e);
        }
    }

    static class SvgGenerationConfig {

        ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();

        LayoutParameters layoutParameters = new LayoutParameters();

        VoltageLevelLayoutFactory voltageLevelLayoutFactory;

        SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();

        SvgGenerationConfig(Network network) {
            voltageLevelLayoutFactory = new SmartVoltageLevelLayoutFactory(network);
        }
    }

    private void generateSvg(ToolRunningContext context, Path outputDir, SvgGenerationConfig generationConfig,
                             String vlOrSubstationId, Network network) {
        Path svgFile = getSvgFile(outputDir, vlOrSubstationId);
        context.getOutputStream().println("Generating '" + svgFile + "'");
        try {
            SingleLineDiagram.draw(network, vlOrSubstationId, svgFile,
                    generationConfig.layoutParameters, generationConfig.componentLibrary,
                    generationConfig.substationLayoutFactory, generationConfig.voltageLevelLayoutFactory,
                    new DefaultDiagramLabelProvider(network, generationConfig.componentLibrary, generationConfig.layoutParameters),
                    new TopologicalStyleProvider(network), "");
        } catch (Exception e) {
            e.printStackTrace(context.getErrorStream());
        }
    }

    private void generateSome(ToolRunningContext context, Path outputDir, List<String> ids, Network network, SvgGenerationConfig generationConfig) {
        for (String id : ids) {
            generateSvg(context, outputDir, generationConfig, id, network);
        }
    }

    private void generateAll(ToolRunningContext context, boolean allVoltageLevels, boolean allSubstations,
                             Path outputDir, Network network, SvgGenerationConfig generationConfig) {
        // by default, export all voltage levels if no id given and no
        // additional option (all-voltage-levels or all-substations) given
        if (allVoltageLevels || !allSubstations) {
            // export all voltage levels
            for (VoltageLevel vl : network.getVoltageLevels()) {
                generateSvg(context, outputDir, generationConfig, vl.getId(), network);
            }
        }
        if (allSubstations) {
            // export all substations
            for (Substation s : network.getSubstations()) {
                generateSvg(context, outputDir, generationConfig, s.getId(), network);
            }
        }
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) {
        ToolOptions toolOptions = new ToolOptions(line, context);
        Path inputFile = toolOptions.getPath(INPUT_FILE).orElseThrow(() -> new PowsyblException(INPUT_FILE + " option is missing"));
        Path outputDir = toolOptions.getPath(OUTPUT_DIR).orElseThrow(() -> new PowsyblException(OUTPUT_DIR + " option is missing"));
        List<String> ids = toolOptions.getValues(IDS).orElse(Collections.emptyList());
        context.getOutputStream().println("Loading network '" + inputFile + "'...");
        Network network = Network.read(inputFile);
        if (network == null) {
            throw new PowsyblException("File '" + inputFile + "' is not importable");
        }
        SvgGenerationConfig generationConfig = new SvgGenerationConfig(network);
        if (ids.isEmpty()) {
            boolean allVoltageLevels = toolOptions.hasOption(ALL_VOLTAGE_LEVELS);
            boolean allSubstations = toolOptions.hasOption(ALL_SUBSTATIONS);
            generateAll(context, allVoltageLevels, allSubstations, outputDir, network, generationConfig);
        } else {
            generateSome(context, outputDir, ids, network, generationConfig);
        }
    }
}
