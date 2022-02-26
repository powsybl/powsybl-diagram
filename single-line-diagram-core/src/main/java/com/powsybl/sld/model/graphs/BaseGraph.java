/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.graphs;

import java.util.List;

import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.FeederTwtLegNode;
import com.powsybl.sld.model.nodes.MiddleTwtNode;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public interface BaseGraph extends Graph {

    BranchEdge addTwtEdge(FeederTwtLegNode legNode, MiddleTwtNode twtNode);

    List<BranchEdge> getTwtEdges();

    List<MiddleTwtNode> getMultiTermNodes();

    void addMultiTermNode(MiddleTwtNode node);

}
