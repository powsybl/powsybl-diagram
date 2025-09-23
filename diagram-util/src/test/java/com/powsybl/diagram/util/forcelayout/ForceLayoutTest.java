/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.layout.GraphTestData;
import com.powsybl.diagram.util.layout.ResourceUtils;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
@Deprecated(since = "5.0.0", forRemoval = true)
class ForceLayoutTest {

    @Test
    void execute() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext();
        ForceLayout<String, DefaultEdge> forceLayout = new ForceLayout<>(layoutContext);
        forceLayout.execute();
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        StringWriter sw = new StringWriter();
        layoutContext.toSVG(tooltip, sw);
        assertEquals(ResourceUtils.toString("basic_5_nodes.svg"), sw.toString());
    }
}
