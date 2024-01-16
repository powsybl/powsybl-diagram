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
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class VerticalZoneLayoutFactory implements ZoneLayoutFactory {

    @Override
    public Layout create(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        List<String> substations = graph.getSubstations().stream().map(SubstationGraph::getId).toList();
        String[][] matrix = new String[substations.size()][1];
        for (int row = 0; row < substations.size(); row++) {
            matrix[row][0] = substations.get(row);
        }
        return new MatrixZoneLayout(graph, matrix, sLayoutFactory, vLayoutFactory);
    }
}
