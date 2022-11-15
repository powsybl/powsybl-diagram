/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.google.auto.service.AutoService;
import com.powsybl.tools.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Version.class)
public class PowsyblSingleLineDiagramVersion extends AbstractVersion {

    public PowsyblSingleLineDiagramVersion() {
        super("powsybl-single-line-diagram", "${project.version}", "${buildNumber}", "${scmBranch}", Long.parseLong("${timestamp}"));
    }
}
