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
import com.powsybl.nad.library.DefaultComponentLibrary;
import com.powsybl.nad.library.NadComponentLibrary;
import com.powsybl.nad.routing.StraightEdgeRouting;
import com.powsybl.nad.routing.EdgeRouting;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.*;

import java.util.Objects;

/**
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class NadParameters {

    private SvgParameters svgParameters = new SvgParameters();
    private LayoutParameters layoutParameters = new LayoutParameters();
    private StyleProviderFactory styleProviderFactory = TopologicalStyleProvider::new;
    private LabelProviderFactory labelProviderFactory = DefaultLabelProvider::new;
    private LayoutFactory layoutFactory = new BasicForceLayoutFactory();
    private IdProviderFactory idProviderFactory = IntIdProvider::new;
    private NadComponentLibrary componentLibrary = new DefaultComponentLibrary();
    private EdgeRouting edgeRouting = new StraightEdgeRouting();

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public NadParameters setSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        return this;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public NadParameters setLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        return this;
    }

    public StyleProviderFactory getStyleProviderFactory() {
        return styleProviderFactory;
    }

    public NadParameters setStyleProviderFactory(StyleProviderFactory styleProviderFactory) {
        this.styleProviderFactory = Objects.requireNonNull(styleProviderFactory);
        return this;
    }

    public LabelProvider createLabelProvider(Network network) {
        return labelProviderFactory.create(network, svgParameters);
    }

    public NadParameters setLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = Objects.requireNonNull(labelProviderFactory);
        return this;
    }

    public LayoutFactory getLayoutFactory() {
        return layoutFactory;
    }

    public NadParameters setLayoutFactory(LayoutFactory layoutFactory) {
        this.layoutFactory = Objects.requireNonNull(layoutFactory);
        return this;
    }

    public IdProviderFactory getIdProviderFactory() {
        return idProviderFactory;
    }

    public NadParameters setIdProviderFactory(IdProviderFactory idProviderFactory) {
        this.idProviderFactory = Objects.requireNonNull(idProviderFactory);
        return this;
    }

    public NadComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    public NadParameters setComponentLibrary(NadComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
        return this;
    }

    public EdgeRouting getEdgeRouting() {
        return edgeRouting;
    }

    public NadParameters setEdgeRouting(EdgeRouting edgeRouting) {
        this.edgeRouting = edgeRouting;
        return this;
    }
}
