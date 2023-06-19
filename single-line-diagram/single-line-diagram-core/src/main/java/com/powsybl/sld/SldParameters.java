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
import com.powsybl.sld.svg.LabelProviderFactory;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.svg.styles.StyleProviderFactory;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class SldParameters {

    private final SvgParameters svgParameters;
    private final LayoutParameters layoutParameters;
    private final ComponentLibrary componentLibrary;
    private final LabelProviderFactory labelProviderFactory;
    private final StyleProviderFactory styleProviderFactory;
    private final SubstationLayoutFactory substationLayoutFactory;
    private final VoltageLevelLayoutFactory voltageLevelLayoutFactory;

    public SldParameters(SvgParameters svgParameters, LayoutParameters layoutParameters, ComponentLibrary componentLibrary, LabelProviderFactory labelProviderFactory, StyleProviderFactory styleProviderFactory, SubstationLayoutFactory substationLayoutFactory, VoltageLevelLayoutFactory voltageLevelLayoutFactory) {
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

    public StyleProviderFactory getStyleProviderFactory() {
        return styleProviderFactory;
    }

    public SubstationLayoutFactory getSubstationLayoutFactory() {
        return substationLayoutFactory;
    }

    public VoltageLevelLayoutFactory getVoltageLevelLayoutFactory() {
        return voltageLevelLayoutFactory;
    }

    public static SldParametersBuilder builder() {
        return new SldParametersBuilder();
    }

    public static SldParameters defaultParameters() {
        return builder().build();
    }
}
