/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.svg.LabelProvider;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.svg.styles.StyleProvider;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class Config {

    SvgParameters svgParameters;
    LayoutParameters layoutParameters;
    ComponentLibrary componentLibrary;
    ConfigBuilder.LabelProviderFactory labelProviderFactory;
    ConfigBuilder.StyleProviderFactory styleProviderFactory;
    SubstationLayoutFactory substationLayoutFactory;
    VoltageLevelLayoutFactory voltageLevelLayoutFactory;

    public Config(SvgParameters svgParameters, LayoutParameters layoutParameters, ComponentLibrary componentLibrary, ConfigBuilder.LabelProviderFactory labelProviderFactory, ConfigBuilder.StyleProviderFactory styleProviderFactory, SubstationLayoutFactory substationLayoutFactory, VoltageLevelLayoutFactory voltageLevelLayoutFactory) {
        this.svgParameters = svgParameters;
        this.layoutParameters = layoutParameters;
        this.componentLibrary = componentLibrary;
        this.labelProviderFactory = labelProviderFactory;
        this.styleProviderFactory = styleProviderFactory;
        this.substationLayoutFactory = substationLayoutFactory;
        this.voltageLevelLayoutFactory = voltageLevelLayoutFactory;
    }

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    public LabelProvider createLabelProvider(Network network) {
        return labelProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters);
    }

    public StyleProvider createStyleProvider(Network network) {
        return styleProviderFactory.create(network);
    }

    public SubstationLayoutFactory getSubstationLayoutFactory() {
        return substationLayoutFactory;
    }

    public VoltageLevelLayoutFactory getVoltageLevelLayoutFactory() {
        return voltageLevelLayoutFactory;
    }
}
