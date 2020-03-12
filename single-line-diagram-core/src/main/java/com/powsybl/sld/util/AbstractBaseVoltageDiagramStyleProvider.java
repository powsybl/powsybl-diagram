/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.color.BaseVoltageColor;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;

import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractBaseVoltageDiagramStyleProvider extends DefaultDiagramStyleProvider {

    protected static final String PROFILE = "Default";

    protected final BaseVoltageColor baseVoltageColor;
    protected final String disconnectedColor;

    protected AbstractBaseVoltageDiagramStyleProvider(Path config) {
        baseVoltageColor = config != null ? new BaseVoltageColor(config) : new BaseVoltageColor();
        disconnectedColor = getBaseColor(0, PROFILE);
    }

    protected String getBaseColor(double baseVoltage) {
        return getBaseColor(baseVoltage, PROFILE);
    }

    String getBaseColor(double baseVoltage, String profile) {
        return baseVoltageColor.getColor(baseVoltage, profile).orElseThrow(() -> new PowsyblException("No color found for base voltage " + baseVoltage + " and profile " + profile));
    }
}
