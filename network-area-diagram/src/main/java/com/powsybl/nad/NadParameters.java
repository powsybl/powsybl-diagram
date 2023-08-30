/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.IdProviderFactory;
import com.powsybl.nad.svg.iidm.LabelProviderFactory;
import com.powsybl.nad.svg.iidm.StyleProviderFactory;

import java.util.Objects;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class NadParameters {

    private final SvgParameters svgParameters;
    private final LayoutParameters layoutParameters;
    private final StyleProviderFactory styleProviderFactory;
    private final LabelProviderFactory labelProviderFactory;
    private final LayoutFactory layoutFactory;
    private final IdProviderFactory idProviderFactory;

    public NadParameters(SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProviderFactory styleProviderFactory, LabelProviderFactory labelProviderFactory, LayoutFactory layoutFactory, IdProviderFactory idProviderFactory) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        this.styleProviderFactory = Objects.requireNonNull(styleProviderFactory);
        this.labelProviderFactory = Objects.requireNonNull(labelProviderFactory);
        this.layoutFactory = Objects.requireNonNull(layoutFactory);
        this.idProviderFactory = Objects.requireNonNull(idProviderFactory);
    }

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public StyleProviderFactory getStyleProviderFactory() {
        return styleProviderFactory;
    }

    public LabelProvider createLabelProvider(Network network) {
        return labelProviderFactory.create(network, svgParameters);
    }

    public LayoutFactory getLayoutFactory() {
        return layoutFactory;
    }

    public IdProviderFactory getIdProviderFactory() {
        return idProviderFactory;
    }

    public static NadParametersBuilder builder() {
        return new NadParametersBuilder();
    }

    public static NadParameters defaultParameters() {
        return builder().build();
    }

}
