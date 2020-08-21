package com.powsybl.sld.raw;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCase11SubstationGraphV extends TestCase11SubstationGraph {

    @Test
    public void test() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst", false);
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphVertical.json"), toString("/TestCase11SubstationGraphVertical.json"));
    }
}
