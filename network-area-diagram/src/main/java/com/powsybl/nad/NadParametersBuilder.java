/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.layout.BasicForceLayoutFactory;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.*;

import java.util.Objects;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class NadParametersBuilder {
    private SvgParameters svgParameters = new SvgParameters();
    private LayoutParameters layoutParameters = new LayoutParameters();
    private StyleProviderFactory styleProviderFactory = TopologicalStyleProvider::new;
    private LabelProviderFactory labelProviderFactory = DefaultLabelProvider::new;
    private LayoutFactory layoutFactory = new BasicForceLayoutFactory();
    private IdProviderFactory idProviderFactory = IntIdProvider::new;

    public NadParametersBuilder withSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        return this;
    }

    public NadParametersBuilder withLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        return this;
    }

    public NadParametersBuilder withStyleProviderFactory(StyleProviderFactory styleProviderFactory) {
        this.styleProviderFactory = Objects.requireNonNull(styleProviderFactory);
        return this;
    }

    public NadParametersBuilder withLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = Objects.requireNonNull(labelProviderFactory);
        return this;
    }

    public NadParametersBuilder withLayoutFactory(LayoutFactory layoutFactory) {
        this.layoutFactory = Objects.requireNonNull(layoutFactory);
        return this;
    }

    public NadParametersBuilder withIdProviderFactory(IdProviderFactory idProviderFactory) {
        this.idProviderFactory = Objects.requireNonNull(idProviderFactory);
        return this;
    }

    public NadParameters build() {
        return new NadParameters(svgParameters, layoutParameters, styleProviderFactory, labelProviderFactory, layoutFactory, idProviderFactory);
    }

}
