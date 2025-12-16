/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.zonebygrid;

import com.powsybl.sld.model.graphs.BaseGraph;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public record MatrixCell(BaseGraph graph, int row, int col) {
    public boolean isEmpty() {
        return graph() == null;
    }

    public String getId() {
        return graph == null ? "" : graph.getId();
    }
}
