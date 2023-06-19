/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.svg.styles.StyleProvider;

import java.io.Writer;

/**
 * @author Gilles Brada <gilles.brada at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface SVGWriter {
    GraphMetadata write(Graph graph, LabelProvider initProvider, StyleProvider styleProvider, Writer writer);
}
