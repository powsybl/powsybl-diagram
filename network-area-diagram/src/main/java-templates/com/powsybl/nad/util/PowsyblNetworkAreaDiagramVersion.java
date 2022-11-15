/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.util;

import com.google.auto.service.AutoService;
import com.powsybl.tools.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
@AutoService(Version.class)
public class PowsyblNetworkAreaDiagramVersion extends AbstractVersion {

    public PowsyblNetworkAreaDiagramVersion() {
        super("powsybl-network-area-diagram", "${project.version}", "${buildNumber}", "${scmBranch}", Long.parseLong("${timestamp}"));
    }
}
