/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

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
    StyleProvider styleProvider;
    LabelProvider labelProvider;
    LayoutFactory layoutFactory;
    IdProvider idProvider;

    public Config(SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider) {
        this.svgParameters = svgParameters;
        this.layoutParameters = layoutParameters;
        this.styleProvider = styleProvider;
        this.labelProvider = labelProvider;
        this.layoutFactory = layoutFactory;
        this.idProvider = idProvider;
    }

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public StyleProvider getStyleProvider() {
        return styleProvider;
    }

    public LabelProvider getLabelProvider() {
        return labelProvider;
    }

    public LayoutFactory getLayoutFactory() {
        return layoutFactory;
    }

    public IdProvider getIdProvider() {
        return idProvider;
    }

}
