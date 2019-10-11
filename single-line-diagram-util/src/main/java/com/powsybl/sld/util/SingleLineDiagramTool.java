/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.SubstationDiagram;
import com.powsybl.sld.VoltageLevelDiagram;
import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
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
import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class SingleLineDiagramTool implements Tool {

    private static final String INPUT_FILE = "input-file";
    private static final String OUTPUT_DIR = "output-dir";
    private static final String IDS = "ids";
    private static final String ALL_VOLTAGE_LEVELS = "all-voltage-levels";
    private static final String ALL_SUBSTATIONS = "all-substations";

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "generate-single-line-diagram";
            }

            @Override
            public String getTheme() {
                return "Single line diagram";
            }

            @Override
            public String getDescription() {
                return "generate single line diagram";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(INPUT_FILE)
                        .desc("the input file")
                        .hasArg()
                        .argName("INPUT_FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_DIR)
                        .desc("the output directory")
                        .hasArg()
                        .argName("OUTPUT_DIR")
                        .required()
                        .build());
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

    private void generateSvg(ToolRunningContext context, Path outputDir, ComponentLibrary componentLibrary,
                             LayoutParameters parameters, VoltageLevelLayoutFactory voltageLevelLayoutFactory, VoltageLevel vl, Network network) throws UnsupportedEncodingException {
        Path svgFile = outputDir.resolve(URLEncoder.encode(vl.getId(), StandardCharsets.UTF_8.name()) + ".svg");
        context.getOutputStream().println("Generating '" + svgFile + "' (" + vl.getNominalV() + ")");
        try {
            VoltageLevelDiagram.build(vl, voltageLevelLayoutFactory, true, parameters.isShowInductorFor3WT())
                    .writeSvg("", componentLibrary, parameters, network, svgFile);
        } catch (Exception e) {
            e.printStackTrace(context.getErrorStream());
        }
    }

    private void generateSvg(ToolRunningContext context, Path outputDir, ComponentLibrary componentLibrary,
                             LayoutParameters parameters, VoltageLevelLayoutFactory voltageLevelLayoutFactory,
                             SubstationLayoutFactory substationLayoutFactory,
                             Substation s, Network network) throws UnsupportedEncodingException {
        Path svgFile = outputDir.resolve(URLEncoder.encode(s.getId(), StandardCharsets.UTF_8.name()) + ".svg");
        context.getOutputStream().println("Generating '" + svgFile + "'");
        try {
            SubstationDiagram.build(s, substationLayoutFactory, voltageLevelLayoutFactory, true)
                    .writeSvg("", componentLibrary, parameters, network, svgFile);
        } catch (Exception e) {
            e.printStackTrace(context.getErrorStream());
        }
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws UnsupportedEncodingException {
        ToolOptions toolOptions = new ToolOptions(line, context);
        Path inputFile = toolOptions.getPath(INPUT_FILE).orElseThrow(() -> new PowsyblException(INPUT_FILE  + " option is missing"));
        Path outputDir = toolOptions.getPath(OUTPUT_DIR).orElseThrow(() -> new PowsyblException(OUTPUT_DIR  + " option is missing"));
        Optional<List<String>> ids = toolOptions.getValues(IDS);
        context.getOutputStream().println("Loading network '" + inputFile + "'...");
        Network network = Importers.loadNetwork(inputFile);
        if (network == null) {
            throw new PowsyblException("File '" + inputFile + "' is not importable");
        }
        ComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");
        LayoutParameters parameters = new LayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = new SmartVoltageLevelLayoutFactory();
        SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();
        if (ids.isPresent()) {
            for (String id : ids.get()) {
                VoltageLevel vl = network.getVoltageLevel(id);
                if (vl == null) {
                    Substation s = network.getSubstation(id);
                    if (s == null) {
                        throw new PowsyblException("No voltage level or substation with id : '" + id + "'");
                    } else {  // id is a substation id
                        generateSvg(context, outputDir, componentLibrary, parameters,
                                    voltageLevelLayoutFactory, substationLayoutFactory, s, network);
                    }
                } else {  // id is a voltage level id
                    generateSvg(context, outputDir, componentLibrary, parameters, voltageLevelLayoutFactory, vl, network);
                }
            }
        } else {
            boolean allVoltageLevels = toolOptions.hasOption(ALL_VOLTAGE_LEVELS);
            boolean allSubstations = toolOptions.hasOption(ALL_SUBSTATIONS);

            // by default, export all voltage levels if no id given and no
            // additional option (all-voltage-levels or all-substations) given
            if (!allVoltageLevels && !allSubstations) {
                allVoltageLevels = true;
            }

            if (allVoltageLevels) {
                // export all voltage levels
                for (VoltageLevel vl : network.getVoltageLevels()) {
                    generateSvg(context, outputDir, componentLibrary, parameters, voltageLevelLayoutFactory, vl, network);
                }
            }
            if (allSubstations) {
                // export all substations
                for (Substation s : network.getSubstations()) {
                    generateSvg(context, outputDir, componentLibrary, parameters,
                                voltageLevelLayoutFactory, substationLayoutFactory, s, network);
                }
            }
        }
    }
}
