/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.sld.model.graphs.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */

public class RawGraphBuilder implements GraphBuilder {

    private final Map<String, VoltageLevelRawBuilder> vlBuilders = new TreeMap<>();
    private final Map<String, SubstationRawBuilder> ssBuilders = new TreeMap<>();

    private ZoneRawBuilder zBuilder;

    public VoltageLevelRawBuilder createVoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos, SubstationRawBuilder parentBuilder) {
        VoltageLevelRawBuilder vlBuilder = new VoltageLevelRawBuilder(voltageLevelInfos, parentBuilder, this::getVoltageLevelInfosFromId);
        if (parentBuilder != null) {
            parentBuilder.addVlBuilder(vlBuilder);
        }
        vlBuilders.put(voltageLevelInfos.getId(), vlBuilder);
        return vlBuilder;
    }

    public VoltageLevelRawBuilder createVoltageLevelBuilder(String vlId, double vlNominalV, SubstationRawBuilder parentBuilder) {
        return createVoltageLevelBuilder(new VoltageLevelInfos(vlId, vlId, vlNominalV), parentBuilder);
    }

    public VoltageLevelRawBuilder createVoltageLevelBuilder(String vlId, double vlNominalV) {
        return createVoltageLevelBuilder(vlId, vlNominalV, null);
    }

    public VoltageLevelInfos getVoltageLevelInfosFromId(String id) {
        if (vlBuilders.containsKey(id)) {
            return vlBuilders.get(id).getVoltageLevelInfos();
        }
        return new VoltageLevelInfos("OTHER", "OTHER", 0);
    }

    public SubstationRawBuilder createSubstationBuilder(String id, ZoneRawBuilder parentBuilder) {
        SubstationRawBuilder ssb = new SubstationRawBuilder(id, parentBuilder);
        if (parentBuilder != null) {
            parentBuilder.addSubstationBuilder(ssb);
        }
        ssBuilders.put(id, ssb);
        return ssb;
    }

    public SubstationRawBuilder createSubstationBuilder(String id) {
        return createSubstationBuilder(id, null);
    }

    public ZoneRawBuilder createZoneBuilder(List<String> substationIds) {
        zBuilder = new ZoneRawBuilder(substationIds);
        return zBuilder;
    }

    @Override
    public VoltageLevelGraph buildVoltageLevelGraph(String id, Graph parentGraph) {
        return vlBuilders.get(id).getGraph();
    }

    @Override
    public VoltageLevelGraph buildVoltageLevelGraph(String id) {
        return buildVoltageLevelGraph(id, null);
    }

    @Override
    public SubstationGraph buildSubstationGraph(String id, ZoneGraph parentGraph) {
        SubstationRawBuilder sGraphBuilder = ssBuilders.get(id);
        SubstationGraph ssGraph = sGraphBuilder.getGraph();
        sGraphBuilder.voltageLevelBuilders.stream()
                .map(VoltageLevelRawBuilder::getGraph)
                .sorted(Comparator.comparingDouble(vlGraph -> -vlGraph.getVoltageLevelInfos().getNominalVoltage()))
                .forEach(ssGraph::addVoltageLevel);
        if (parentGraph != null) {
            parentGraph.addSubstation(ssGraph);
        }
        return ssGraph;
    }

    @Override
    public SubstationGraph buildSubstationGraph(String id) {
        return buildSubstationGraph(id, null);
    }

    @Override
    public ZoneGraph buildZoneGraph(List<String> substationIds) {
        ZoneRawBuilder zGraphBuilder = zBuilder;
        ZoneGraph zGraph = zGraphBuilder.getGraph();
        substationIds.forEach(id -> buildSubstationGraph(id, zGraph));
        return zGraph;
    }
}
