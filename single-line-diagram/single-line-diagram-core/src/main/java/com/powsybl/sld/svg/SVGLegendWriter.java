/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.StyleProvider;
import org.w3c.dom.Element;

/**
 * @author Slimane Amar {@literal <slimane.amar at rte-france.com>}
 */
public interface SVGLegendWriter {
    void drawLegend(VoltageLevelGraph graph, GraphMetadata metadata, StyleProvider styleProvider, Element legendRootElement, double positionX, double positionY);

    void addLegendMetadataInfos(VoltageLevelGraph graph, GraphMetadata metadata);
}
