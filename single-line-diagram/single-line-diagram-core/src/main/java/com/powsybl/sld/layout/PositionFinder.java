/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.cells.BusCell;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.List;
import java.util.Map;

/**
 * a PositionFinder determines:
 * <ul>
 * <li>the positions of nodeBuses</li>
 * <li>cell order and direction of each cell connected to Bus (ie all cells except Shunt ones)</li>
 * </ul>
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface PositionFinder {

    Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes, List<BusCell> busCells);

    LBSCluster organizeLegBusSets(VoltageLevelGraph graph, List<LegBusSet> legBusSets);

    List<Subsection> buildLayout(VoltageLevelGraph graph, boolean handleShunt);

    void forceSameOrientationForShuntedCell(VoltageLevelGraph graph);

    void organizeDirections(VoltageLevelGraph graph, List<Subsection> subsections);
}
