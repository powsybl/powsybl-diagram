/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.view.app;

import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ComputationManagerFactory;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.sld.cgmes.layout.LayoutToCgmesDlExporterTool;
import com.powsybl.tools.Command;
import com.powsybl.tools.ToolInitializationContext;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class DLExporter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DLExporter.class);

    private static String getUsage(Command command) {
        String res = "Usage: <Program> --input-file=<file>--output-dir=<dir> [--voltage-level-layout=<VOLTAGE LEVEL LAYOUT> --substation-layout=<SUBSTATION LAYOUT> --diagram-name=<diagram name>]";
        res += "\n\n" + command.getUsageFooter();
        return res;
    }

    public static void main(String[] args) {
        LayoutToCgmesDlExporterTool exporterTool = new LayoutToCgmesDlExporterTool();

        CommandLineParser parser = new DefaultParser();
        Command command = exporterTool.getCommand();

        try {
            CommandLine line = parser.parse(command.getOptions(), args);

            Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass;
            try {
                shortTimeExecutionComputationManagerFactoryClass = (Class<? extends ComputationManagerFactory>) Class.forName("com.powsybl.computation.local.LocalComputationManagerFactory");
            } catch (ClassNotFoundException e) {
                throw new UncheckedClassNotFoundException(e);
            }
            DefaultComputationManagerConfig config = new DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass, null);

            ToolInitializationContext initContext = new ToolInitializationContext() {
                @Override
                public PrintStream getOutputStream() {
                    return System.out;
                }

                @Override
                public PrintStream getErrorStream() {
                    return System.err;
                }

                @Override
                public FileSystem getFileSystem() {
                    return FileSystems.getDefault();
                }

                @Override
                public Options getAdditionalOptions() {
                    return new Options();
                }

                @Override
                public ComputationManager createShortTimeExecutionComputationManager(CommandLine commandLine) {
                    return config.createShortTimeExecutionComputationManager();
                }

                @Override
                public ComputationManager createLongTimeExecutionComputationManager(CommandLine commandLine) {
                    return config.createLongTimeExecutionComputationManager();
                }
            };

            exporterTool.run(line, new ToolRunningContext(initContext.getOutputStream(),
                    initContext.getErrorStream(),
                    initContext.getFileSystem(),
                    initContext.createShortTimeExecutionComputationManager(line),
                    initContext.createLongTimeExecutionComputationManager(line)));
        } catch (ParseException e) {
            LOGGER.warn(getUsage(command));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
