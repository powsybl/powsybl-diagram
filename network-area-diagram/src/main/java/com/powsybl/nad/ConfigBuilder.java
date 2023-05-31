/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.IdProvider;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.layout.BasicForceLayoutFactory;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class ConfigBuilder {
    SvgParameters svgParameters = new SvgParameters();
    LayoutParameters layoutParameters = new LayoutParameters();
    StyleProviderFactory styleProviderFactory = TopologicalStyleProvider::new;
    LabelProviderFactory labelProviderFactory = DefaultLabelProvider::new;
    LayoutFactory layoutFactory = new BasicForceLayoutFactory();
    IdProviderFactory idProviderFactory = IntIdProvider::new;

    @FunctionalInterface
    public interface LabelProviderFactory {
        LabelProvider create(Network network, SvgParameters svgParameters);
    }

    @FunctionalInterface
    public interface IdProviderFactory {
        IdProvider create();
    }

    @FunctionalInterface
    public interface StyleProviderFactory {
        StyleProvider create(Network network);
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

    public ConfigBuilder withStyleProviderFactory(StyleProviderFactory styleProviderFactory) {
        this.styleProviderFactory = styleProviderFactory;
        return this;
    }

    public ConfigBuilder withLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = labelProviderFactory;
        return this;
    }

    public ConfigBuilder withLayoutFactory(LayoutFactory layoutFactory) {
        this.layoutFactory = layoutFactory;
        return this;
    }

    public ConfigBuilder withIdProviderFactory(IdProviderFactory idProviderFactory) {
        this.idProviderFactory = idProviderFactory;
        return this;
    }

    public Config build() {
        return new Config(svgParameters, layoutParameters, styleProviderFactory, labelProviderFactory, layoutFactory, idProviderFactory);
    }

}