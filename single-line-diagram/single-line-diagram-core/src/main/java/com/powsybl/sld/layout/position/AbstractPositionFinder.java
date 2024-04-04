/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position;

import com.powsybl.sld.model.cells.BusCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractPositionFinder implements PositionFinder, HorizontalBusListManager {

    public abstract Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes, List<BusCell> busCells);

    public abstract BSCluster organizeBusSets(VoltageLevelGraph graph, List<VerticalBusSet> verticalBusSets);

    public List<Subsection> buildLayout(VoltageLevelGraph graph, boolean handleShunt) {
        if (graph.getNodes().isEmpty()) {
            return new ArrayList<>();
        }
        Map<BusNode, Integer> busToNb = indexBusPosition(graph.getNodeBuses(), graph.getBusCells());
        List<VerticalBusSet> verticalBusSets = VerticalBusSet.createVerticalBusSets(graph, busToNb);
        BSCluster bsCluster = organizeBusSets(graph, verticalBusSets);
        graph.setMaxBusPosition();
        List<Subsection> subsections = Subsection.createSubsections(graph, bsCluster, busToNb, handleShunt);
        organizeDirections(graph, subsections);
        return subsections;
    }

    public void forceSameOrientationForShuntedCell(VoltageLevelGraph graph) {
        graph.getShuntCellStream().forEach(sc -> sc.alignDirections(Side.LEFT));
    }

    public void organizeDirections(VoltageLevelGraph graph, List<Subsection> subsections) {
        forceSameOrientationForShuntedCell(graph);
    }

    public void mergeHblWithNoLink(BSCluster leftCluster, BSCluster rightCluster) {
        rightCluster.getHorizontalBusLists().forEach(hbl -> {
            hbl.shift(leftCluster.getLength());
            hbl.setBsCluster(leftCluster);
        });
        leftCluster.getHorizontalBusLists().addAll(rightCluster.getHorizontalBusLists());
        rightCluster.getHorizontalBusLists().removeAll(rightCluster.getHorizontalBusLists());
    }
}
