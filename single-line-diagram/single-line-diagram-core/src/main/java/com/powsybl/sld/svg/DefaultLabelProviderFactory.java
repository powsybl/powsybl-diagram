/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.ConfigBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class DefaultLabelProviderFactory implements ConfigBuilder.LabelProviderFactory {

    @Override
    public LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        return new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters);
    }
}
