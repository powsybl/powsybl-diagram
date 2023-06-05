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
    LabelProviderFactory labelProviderFactory = new DefaultLabelProviderFactory();
    StyleProviderFactory styleProviderFactory = new DefaultStyleProviderFactory();
    VoltageLevelLayoutFactory voltageLevelLayoutFactory = new PositionVoltageLevelLayoutFactory();
    SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();

    @FunctionalInterface
    public interface LabelProviderFactory {
        LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters);
    }

    @FunctionalInterface
    public interface StyleProviderFactory {
        StyleProvider create(Network network);
    }

    private class DefaultStyleProviderFactory implements StyleProviderFactory {
        @Override
        public StyleProvider create(Network network) {
            return new StyleProvidersList(new TopologicalStyleProvider(network), new HighlightLineStateStyleProvider(network));
        }
    }

    public ConfigBuilder() {
    }

    public ConfigBuilder withSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        return this;
    }

    public ConfigBuilder withLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        return this;
    }

    public ConfigBuilder withComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
        return this;
    }

    public ConfigBuilder withLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = labelProviderFactory;
        return this;
    }

    public ConfigBuilder withStyleProviderFactory(StyleProviderFactory styleProviderFactory) {
        this.styleProviderFactory = styleProviderFactory;
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
        return new Config(svgParameters, layoutParameters, componentLibrary, labelProviderFactory, styleProviderFactory, substationLayoutFactory, voltageLevelLayoutFactory);
    }

}
