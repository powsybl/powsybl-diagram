/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sld.svg.styles;

import com.powsybl.iidm.network.Network;

/**
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class NominalVoltageStyleProviderFactory implements StyleProviderFactory {
    @Override
    public StyleProvider create(Network network) {
        return new NominalVoltageStyleProvider();
    }
}
