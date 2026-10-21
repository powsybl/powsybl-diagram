/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Slimane Amar {@literal <slimane.amar at rte-france.com>}
 */
public final class InfosNbSnakeLinesHorizontal {

    private final Map<Direction, Integer> nbSnakeLinesTopBottom;
    private final Map<String, Integer> nbSnakeLinesVerticalBetween;

    static InfosNbSnakeLinesHorizontal create(ZoneGraph zoneGraph) {
        return create(zoneGraph.getVoltageLevelStream());
    }

    static InfosNbSnakeLinesHorizontal create(SubstationGraph substationGraph) {
        return create(substationGraph.getVoltageLevelStream());
    }

    static InfosNbSnakeLinesHorizontal create(VoltageLevelGraph graph) {
        // used only for horizontal layout
        return create(Stream.of(graph));
    }

    private static InfosNbSnakeLinesHorizontal create(Stream<VoltageLevelGraph> voltageLevelGraphStream) {
        Map<Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = voltageLevelGraphStream.collect(Collectors.toMap(g -> g.getVoltageLevelInfos().id(), v -> 0));
        return new InfosNbSnakeLinesHorizontal(nbSnakeLinesTopBottom, nbSnakeLinesBetween);
    }

    private InfosNbSnakeLinesHorizontal(Map<Direction, Integer> nbSnakeLinesTopBottom,
                                        Map<String, Integer> nbSnakeLinesVerticalBetween) {
        this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
        this.nbSnakeLinesVerticalBetween = nbSnakeLinesVerticalBetween;
    }

    public Map<Direction, Integer> getNbSnakeLinesTopBottom() {
        return nbSnakeLinesTopBottom;
    }

    public Map<String, Integer> getNbSnakeLinesVerticalBetween() {
        return nbSnakeLinesVerticalBetween;
    }

    public void reset() {
        nbSnakeLinesTopBottom.keySet().forEach(side -> nbSnakeLinesTopBottom.compute(side, (k, v) -> 0));
        nbSnakeLinesVerticalBetween.keySet().forEach(side -> nbSnakeLinesVerticalBetween.compute(side, (k, v) -> 0));
    }
}
