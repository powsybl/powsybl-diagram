/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.LabelProvider;
import com.powsybl.sld.svg.LabelProviderFactory;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.svg.styles.DefaultStyleProviderFactory;
import com.powsybl.sld.svg.styles.StyleProviderFactory;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class SldParameters {

    private SvgParameters svgParameters = new SvgParameters();
    private LayoutParameters layoutParameters = new LayoutParameters();
    private ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
    private LabelProviderFactory labelProviderFactory = DefaultLabelProvider::new;
    private StyleProviderFactory styleProviderFactory = new DefaultStyleProviderFactory();
    private VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = SmartVoltageLevelLayoutFactory::new;
    private SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public SldParameters setSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        return this;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public SldParameters setLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        return this;
    }

    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    public SldParameters setComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
        return this;
    }

    public LabelProvider createLabelProvider(Network network) {
        return labelProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters);
    }

    public SldParameters setLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = labelProviderFactory;
        return this;
    }

    public StyleProviderFactory getStyleProviderFactory() {
        return styleProviderFactory;
    }

    public SldParameters setStyleProviderFactory(StyleProviderFactory styleProviderFactory) {
        this.styleProviderFactory = styleProviderFactory;
        return this;
    }

    public VoltageLevelLayoutFactory createVoltageLevelLayoutFactory(Network network) {
        return voltageLevelLayoutFactoryCreator.create(network);
    }

    public SldParameters setVoltageLevelLayoutFactoryCreator(VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator) {
        this.voltageLevelLayoutFactoryCreator = voltageLevelLayoutFactoryCreator;
        return this;
    }

    public SubstationLayoutFactory getSubstationLayoutFactory() {
        return substationLayoutFactory;
    }

    public SldParameters setSubstationLayoutFactory(SubstationLayoutFactory substationLayoutFactory) {
        this.substationLayoutFactory = substationLayoutFactory;
        return this;
    }

}
