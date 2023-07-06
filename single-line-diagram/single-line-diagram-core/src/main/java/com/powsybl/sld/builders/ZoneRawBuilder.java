package com.powsybl.sld.builders;

import com.powsybl.sld.model.graphs.ZoneGraph;

import java.util.ArrayList;
import java.util.List;

public class ZoneRawBuilder {

    ZoneGraph zoneGraph;
    List<SubstationRawBuilder> substationRawBuilders = new ArrayList<>();

    public ZoneRawBuilder(List<String> ids) {
        zoneGraph = ZoneGraph.create(ids);
    }

    public ZoneGraph getGraph() {
        return zoneGraph;
    }

    public void addSubstationBuilder(SubstationRawBuilder ssBuilder) {
        substationRawBuilders.add(ssBuilder);
        ssBuilder.setZoneRawBuilder(this);
    }
}
