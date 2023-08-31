/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.layout.BasicForceLayoutFactory;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.*;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class NadParameters {

    private SvgParameters svgParameters = new SvgParameters();
    private LayoutParameters layoutParameters = new LayoutParameters();
    private StyleProviderFactory styleProviderFactory = TopologicalStyleProvider::new;
    private LabelProviderFactory labelProviderFactory = DefaultLabelProvider::new;
    private LayoutFactory layoutFactory = new BasicForceLayoutFactory();
    private IdProviderFactory idProviderFactory = IntIdProvider::new;

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public NadParameters setSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        return this;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public NadParameters setLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        return this;
    }

    public StyleProviderFactory getStyleProviderFactory() {
        return styleProviderFactory;
    }

    public NadParameters setStyleProviderFactory(StyleProviderFactory styleProviderFactory) {
        this.styleProviderFactory = styleProviderFactory;
        return this;
    }

    public LabelProvider createLabelProvider(Network network) {
        return labelProviderFactory.create(network, svgParameters);
    }

    public NadParameters setLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = labelProviderFactory;
        return this;
    }

    public LayoutFactory getLayoutFactory() {
        return layoutFactory;
    }

    public NadParameters setLayoutFactory(LayoutFactory layoutFactory) {
        this.layoutFactory = layoutFactory;
        return this;
    }

    public IdProviderFactory getIdProviderFactory() {
        return idProviderFactory;
    }

    public NadParameters setIdProviderFactory(IdProviderFactory idProviderFactory) {
        this.idProviderFactory = idProviderFactory;
        return this;
    }

}
