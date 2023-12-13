/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.*;

import java.util.*;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class MatrixZoneLayoutFactory implements ZoneLayoutFactory {

    public Layout create(ZoneGraph graph, String[][] matrix, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        return new MatrixZoneLayout(graph, matrix, sLayoutFactory, vLayoutFactory);
    }

    @Override
    public Layout create(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        // By default work as Horizontal layout
        List<String> substations = graph.getSubstations().stream().map(SubstationGraph::getId).toList();
        String[] array = new String[substations.size()];
        String[][] matrix = new String[1][];
        matrix[0] = substations.toArray(array);
        return new MatrixZoneLayout(graph, matrix, sLayoutFactory, vLayoutFactory);
    }
}
