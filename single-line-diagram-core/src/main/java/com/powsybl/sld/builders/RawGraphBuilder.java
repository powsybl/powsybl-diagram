/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.sld.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

public class RawGraphBuilder implements GraphBuilder {

    private final Map<String, VoltageLevelRawBuilder> vlBuilders = new TreeMap<>();
    private final Map<String, SubstationRawBuilder> ssBuilders = new TreeMap<>();

    public VoltageLevelRawBuilder createVoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos, boolean forVoltageLevelDiagram) {
        VoltageLevelRawBuilder vlBuilder = new VoltageLevelRawBuilder(voltageLevelInfos, forVoltageLevelDiagram, this::getVoltageLevelInfosFromId);
        vlBuilders.put(voltageLevelInfos.getId(), vlBuilder);
        return vlBuilder;
    }

    public VoltageLevelRawBuilder createVoltageLevelBuilder(String vlId, double vlNominalV, boolean forVoltageLevelDiagram) {
        return createVoltageLevelBuilder(new VoltageLevelInfos(vlId, vlId, vlNominalV), forVoltageLevelDiagram);
    }

    public VoltageLevelRawBuilder createVoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos) {
        return createVoltageLevelBuilder(voltageLevelInfos, true);
    }

    public VoltageLevelRawBuilder createVoltageLevelBuilder(String vlId, double vlNominalV) {
        return createVoltageLevelBuilder(new VoltageLevelInfos(vlId, vlId, vlNominalV));
    }

    public VoltageLevelInfos getVoltageLevelInfosFromId(String id) {
        if (vlBuilders.containsKey(id)) {
            return vlBuilders.get(id).getVoltageLevelInfos();
        }
        return new VoltageLevelInfos("OTHER", "OTHER", 0);
    }

    public SubstationRawBuilder createSubstationBuilder(String id) {
        SubstationRawBuilder ssb = new SubstationRawBuilder(id);
        ssBuilders.put(id, ssb);
        return ssb;
    }

    public VoltageLevelGraph buildVoltageLevelGraph(String id,
                                                    boolean forVoltageLevelDiagram) {
        return vlBuilders.get(id).getGraph();
    }

    public SubstationGraph buildSubstationGraph(String id) {
        SubstationRawBuilder sGraphBuilder = ssBuilders.get(id);
        SubstationGraph ssGraph = sGraphBuilder.getGraph();
        sGraphBuilder.voltageLevelBuilders.stream()
            .map(VoltageLevelRawBuilder::getGraph)
            .sorted(Comparator.comparingDouble(vlGraph -> -vlGraph.getVoltageLevelInfos().getNominalVoltage()))
            .forEach(ssGraph::addVoltageLevel);
        return ssGraph;
    }

    //TODO: buildZoneGraph
    public ZoneGraph buildZoneGraph(List<String> substationIds) {
        return null;
    }
}
