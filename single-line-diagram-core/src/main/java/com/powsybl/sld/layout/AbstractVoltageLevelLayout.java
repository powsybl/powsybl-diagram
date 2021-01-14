/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.Graph;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractVoltageLevelLayout extends AbstractLayout implements VoltageLevelLayout {

    protected AbstractVoltageLevelLayout(Graph graph) {
        super(graph);
    }

    public Graph getGraph() {
        return (Graph) graph;
    }

    @Override
    protected void manageSnakeLines(LayoutParameters layoutParameters) {
        if (getGraph().isForVoltageLevelDiagram()) {
            manageSnakeLines(getGraph(), layoutParameters, InfosNbSnakeLines.create(getGraph()));
        }
    }
}
