/**
 * Copyright (c) 2019-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.builders.GraphBuilder;
import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.LabelProvider;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.StyleProvidersList;
import com.powsybl.sld.svg.styles.iidm.HighlightLineStateStyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public abstract class AbstractTestCaseIidm extends AbstractTestCase {

    protected Network network;
    protected VoltageLevel vl;
    protected Substation substation;
    protected GraphBuilder graphBuilder;

    @Override
    public String toSVG(Graph g, String filename) {
        return toSVG(g, filename, componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider());
    }

    @Override
    public String toMetadata(Graph g, String filename) {
        return toMetadata(g, filename, componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider());
    }

    protected LabelProvider getDefaultDiagramLabelProvider() {
        return new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters);
    }

    protected StyleProvider getDefaultDiagramStyleProvider() {
        return new StyleProvidersList(new TopologicalStyleProvider(network), new HighlightLineStateStyleProvider(network));
    }

    @Override
    protected void voltageLevelGraphLayout(VoltageLevelGraph voltageLevelGraph) {
        new SmartVoltageLevelLayoutFactory(network).create(voltageLevelGraph).run(layoutParameters);
    }

    @Override
    protected void substationGraphLayout(SubstationGraph substationGraph) {
        new HorizontalSubstationLayoutFactory().create(substationGraph, new SmartVoltageLevelLayoutFactory(network)).run(layoutParameters);
    }
}
