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
import com.powsybl.sld.layout.pathfinding.DijkstraPathFinder;
import com.powsybl.sld.layout.pathfinding.ZoneLayoutPathFinderFactory;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.DefaultStyleProviderFactory;
import com.powsybl.sld.svg.styles.StyleProviderFactory;

import java.util.Objects;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class SldParameters {

    private SvgParameters svgParameters = new SvgParameters();
    private LayoutParameters layoutParameters = new LayoutParameters();
    private SldComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
    private LabelProviderFactory labelProviderFactory = DefaultLabelProvider::new;
    private LegendWriterFactory legendWriterFactory = DefaultSVGLegendWriter::new;
    private StyleProviderFactory styleProviderFactory = new DefaultStyleProviderFactory();
    private VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = VoltageLevelLayoutFactoryCreator.newSmartVoltageLevelLayoutFactoryCreator();
    private SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();
    private ZoneLayoutFactory zoneLayoutFactory = new HorizontalZoneLayoutFactory();

    private ZoneLayoutPathFinderFactory zoneLayoutPathFinderFactory = DijkstraPathFinder::new;

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public SldParameters setSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        return this;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public SldParameters setLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        return this;
    }

    public SldComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    public SldParameters setComponentLibrary(SldComponentLibrary componentLibrary) {
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        return this;
    }

    public LabelProvider createLabelProvider(Network network) {
        return labelProviderFactory.create(network, componentLibrary, layoutParameters, svgParameters);
    }

    public SldParameters setLabelProviderFactory(LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = Objects.requireNonNull(labelProviderFactory);
        return this;
    }

    public StyleProviderFactory getStyleProviderFactory() {
        return styleProviderFactory;
    }

    public SldParameters setStyleProviderFactory(StyleProviderFactory styleProviderFactory) {
        this.styleProviderFactory = Objects.requireNonNull(styleProviderFactory);
        return this;
    }

    public VoltageLevelLayoutFactory createVoltageLevelLayoutFactory(Network network) {
        return voltageLevelLayoutFactoryCreator.create(network);
    }

    public SVGLegendWriter createLegendWriter(Network network) {
        return legendWriterFactory.create(network, svgParameters);
    }

    public SldParameters setLegendWriterFactory(LegendWriterFactory legendWriterFactory) {
        this.legendWriterFactory = Objects.requireNonNull(legendWriterFactory);
        return this;
    }

    public SldParameters setVoltageLevelLayoutFactoryCreator(VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator) {
        this.voltageLevelLayoutFactoryCreator = Objects.requireNonNull(voltageLevelLayoutFactoryCreator);
        return this;
    }

    public SubstationLayoutFactory getSubstationLayoutFactory() {
        return substationLayoutFactory;
    }

    public SldParameters setSubstationLayoutFactory(SubstationLayoutFactory substationLayoutFactory) {
        this.substationLayoutFactory = Objects.requireNonNull(substationLayoutFactory);
        return this;
    }

    public ZoneLayoutFactory getZoneLayoutFactory() {
        return zoneLayoutFactory;
    }

    public SldParameters setZoneLayoutFactory(ZoneLayoutFactory zoneLayoutFactory) {
        this.zoneLayoutFactory = Objects.requireNonNull(zoneLayoutFactory);
        return this;
    }

    public ZoneLayoutPathFinderFactory getZoneLayoutPathFinderFactory() {
        return zoneLayoutPathFinderFactory;
    }

    public SldParameters setZoneLayoutPathFinderFactory(ZoneLayoutPathFinderFactory zoneLayoutPathFinderFactory) {
        this.zoneLayoutPathFinderFactory = zoneLayoutPathFinderFactory;
        return this;
    }
}
