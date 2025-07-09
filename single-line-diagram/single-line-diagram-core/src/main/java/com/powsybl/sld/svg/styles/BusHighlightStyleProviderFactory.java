/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sld.svg.styles;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.svg.styles.iidm.HighlightLineStateStyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at soft.it>}
 */
public class BusHighlightStyleProviderFactory implements StyleProviderFactory {
    @Override
    public StyleProvider create(Network network, SvgParameters svgParameters) {
        return new StyleProvidersList(new TopologicalStyleProvider(network, svgParameters, true), new HighlightLineStateStyleProvider(network));
    }
}
