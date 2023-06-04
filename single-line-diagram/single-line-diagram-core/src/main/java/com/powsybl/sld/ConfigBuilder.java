/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.svg.DefaultLabelProviderFactory;
import com.powsybl.sld.svg.LabelProvider;
import com.powsybl.sld.svg.LabelProviderFactory;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.StyleProvidersList;
import com.powsybl.sld.svg.styles.iidm.HighlightLineStateStyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class ConfigBuilder {

    SvgParameters svgParameters = new SvgParameters();
    LayoutParameters layoutParameters = new LayoutParameters();
    ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
    LabelProvider labelProvider;
    LabelProviderFactory labelProviderFactory;
    StyleProvider styleProvider;
    VoltageLevelLayoutFactory voltageLevelLayoutFactory;
    SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();
    Network network;

    public ConfigBuilder(Network network) {
        this.network = network;
        voltageLevelLayoutFactory = network != null ? new SmartVoltageLevelLayoutFactory(network) : new PositionVoltageLevelLayoutFactory();
        labelProviderFactory = new DefaultLabelProviderFactory();
        labelProvider = network != null ? labelProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters) : null;
        styleProvider = new StyleProvidersList(new TopologicalStyleProvider(network), new HighlightLineStateStyleProvider(network));
    }

    public ConfigBuilder withSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        labelProvider = network != null ? labelProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters) : null;
        return this;
    }

    public ConfigBuilder withLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        this.labelProvider = network != null ? labelProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters) : null;
        return this;
    }

    public ConfigBuilder withComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
        this.labelProvider = network != null ? labelProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters) : null;
        return this;
    }

    public ConfigBuilder withLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProvider = labelProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters);
        return this;
    }

    public ConfigBuilder withStyleProvider(StyleProvider styleProvider) {
        this.styleProvider = styleProvider;
        return this;
    }

    public ConfigBuilder withVoltageLevelLayoutFactory(VoltageLevelLayoutFactory voltageLevelLayoutFactory) {
        this.voltageLevelLayoutFactory = voltageLevelLayoutFactory;
        return this;
    }

    public ConfigBuilder withSubstationLayoutFactory(SubstationLayoutFactory substationLayoutFactory) {
        this.substationLayoutFactory = substationLayoutFactory;
        return this;
    }

    public Config build() {
        return new Config(svgParameters, layoutParameters, componentLibrary, labelProvider, styleProvider, substationLayoutFactory, voltageLevelLayoutFactory);
    }
}
