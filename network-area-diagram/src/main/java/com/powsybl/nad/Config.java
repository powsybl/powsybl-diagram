/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.IdProvider;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class Config {

    SvgParameters svgParameters;
    LayoutParameters layoutParameters;
    ConfigBuilder.StyleProviderFactory styleProviderFactory;
    ConfigBuilder.LabelProviderFactory labelProviderFactory;
    LayoutFactory layoutFactory;
    ConfigBuilder.IdProviderFactory idProviderFactory;

    public Config(SvgParameters svgParameters, LayoutParameters layoutParameters, ConfigBuilder.StyleProviderFactory styleProviderFactory, ConfigBuilder.LabelProviderFactory labelProviderFactory, LayoutFactory layoutFactory, ConfigBuilder.IdProviderFactory idProviderFactory) {
        this.svgParameters = svgParameters;
        this.layoutParameters = layoutParameters;
        this.styleProviderFactory = styleProviderFactory;
        this.labelProviderFactory = labelProviderFactory;
        this.layoutFactory = layoutFactory;
        this.idProviderFactory = idProviderFactory;
    }

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public StyleProvider createStyleProvider(Network network) {
        return styleProviderFactory.create(network);
    }

    public LabelProvider createLabelProvider(Network network) {
        return labelProviderFactory.create(network, svgParameters);
    }

    public LayoutFactory getLayoutFactory() {
        return layoutFactory;
    }

    public IdProvider createIdProvider() {
        return idProviderFactory.create();
    }

}