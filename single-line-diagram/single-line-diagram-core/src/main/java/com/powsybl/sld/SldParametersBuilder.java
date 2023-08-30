/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sld;

import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.LabelProviderFactory;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.svg.styles.DefaultStyleProviderFactory;
import com.powsybl.sld.svg.styles.StyleProviderFactory;

import java.util.Objects;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */

public class SldParametersBuilder {

    private SvgParameters svgParameters = new SvgParameters();
    private LayoutParameters layoutParameters = new LayoutParameters();
    private ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
    private LabelProviderFactory labelProviderFactory = DefaultLabelProvider::new;
    private StyleProviderFactory styleProviderFactory = new DefaultStyleProviderFactory();
    private VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = SmartVoltageLevelLayoutFactory::new;
    private SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();

    public SldParametersBuilder withSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        return this;
    }

    public SldParametersBuilder withLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        return this;
    }

    public SldParametersBuilder withComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        return this;
    }

    public SldParametersBuilder withLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = Objects.requireNonNull(labelProviderFactory);
        return this;
    }

    public SldParametersBuilder withStyleProviderFactory(StyleProviderFactory styleProviderFactory) {
        this.styleProviderFactory = Objects.requireNonNull(styleProviderFactory);
        return this;
    }

    public SldParametersBuilder withVoltageLevelLayoutFactoryCreator(VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator) {
        this.voltageLevelLayoutFactoryCreator = Objects.requireNonNull(voltageLevelLayoutFactoryCreator);
        return this;
    }

    public SldParametersBuilder withSubstationLayoutFactory(SubstationLayoutFactory substationLayoutFactory) {
        this.substationLayoutFactory = Objects.requireNonNull(substationLayoutFactory);
        return this;
    }

    public SldParameters build() {
        return new SldParameters(svgParameters, layoutParameters, componentLibrary, labelProviderFactory, styleProviderFactory, substationLayoutFactory, voltageLevelLayoutFactoryCreator);
    }

}
