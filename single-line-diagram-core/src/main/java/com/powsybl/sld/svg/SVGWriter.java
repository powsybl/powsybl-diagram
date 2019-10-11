/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;

import java.io.Writer;
import java.nio.file.Path;

/**
 * @author Gilles Brada <gilles.brada at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface SVGWriter {

    GraphMetadata write(String prefixId,
                        Graph graph,
                        SubstationDiagramInitialValueProvider initProvider,
                        SubstationDiagramStyleProvider styleProvider,
                        NodeLabelConfiguration nodeLabelConfiguration,
                        Path svgFile);

    GraphMetadata write(String prefixId,
                        Graph graph,
                        SubstationDiagramInitialValueProvider initProvider,
                        SubstationDiagramStyleProvider styleProvider,
                        NodeLabelConfiguration nodeLabelConfiguration,
                        Writer writer);

    GraphMetadata write(String prefixId,
                        SubstationGraph graph,
                        SubstationDiagramInitialValueProvider initProvider,
                        SubstationDiagramStyleProvider styleProvider,
                        NodeLabelConfiguration nodeLabelConfiguration,
                        Path svgFile);

    GraphMetadata write(String prefixId,
                        SubstationGraph graph,
                        SubstationDiagramInitialValueProvider initProvider,
                        SubstationDiagramStyleProvider styleProvider,
                        NodeLabelConfiguration nodeLabelConfiguration,
                        Writer writer);

    LayoutParameters getLayoutParameters();

    ComponentLibrary getComponentLibrary();
}
