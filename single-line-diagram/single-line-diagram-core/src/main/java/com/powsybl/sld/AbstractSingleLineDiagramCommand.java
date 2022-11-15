/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.tools.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractSingleLineDiagramCommand implements Command {

    public static final String INPUT_FILE = "input-file";

    public static final String OUTPUT_DIR = "output-dir";

    @Override
    public String getTheme() {
        return "Single line diagram";
    }

    protected void addInputFileOption(Options options) {
        options.addOption(Option.builder().longOpt(INPUT_FILE)
                .desc("the input file")
                .hasArg()
                .argName("INPUT_FILE")
                .required()
                .build());
    }

    protected void addOutputDirectoryOption(Options options) {
        options.addOption(Option.builder().longOpt(OUTPUT_DIR)
                .desc("the output directory")
                .hasArg()
                .argName("OUTPUT_DIR")
                .required()
                .build());
    }
}
