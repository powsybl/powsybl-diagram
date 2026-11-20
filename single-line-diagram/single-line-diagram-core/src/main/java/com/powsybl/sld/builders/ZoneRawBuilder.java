package com.powsybl.sld.builders;

import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.svg.IdProvider;
import com.powsybl.sld.svg.LegacyIdProvider;

import java.util.ArrayList;
import java.util.List;

public class ZoneRawBuilder extends AbstractRawBuilder {

    ZoneGraph zoneGraph;
    List<SubstationRawBuilder> substationRawBuilders = new ArrayList<>();

    public ZoneRawBuilder(List<String> ids) {
        this(ids, new LegacyIdProvider(""));
    }

    public ZoneRawBuilder(List<String> ids, IdProvider idProvider) {
        zoneGraph = ZoneGraph.create(ids, idProvider);
    }

    @Override
    public ZoneGraph getGraph() {
        return zoneGraph;
    }

    public void addSubstationBuilder(SubstationRawBuilder ssBuilder) {
        substationRawBuilders.add(ssBuilder);
        ssBuilder.setZoneRawBuilder(this);
    }

    @Override
    protected boolean containsVoltageLevelRawBuilders(VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return vl1.getSubstationBuilder() != vl2.getSubstationBuilder();
    }
}
