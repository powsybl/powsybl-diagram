package com.powsybl.sld.raw;

import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCase11SubstationGraphH extends TestCase11SubstationGraph {

    @Test
    public void test() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst", false);
        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphHorizontal.json"), toJson(g, "/TestCase11SubstationGraphHorizontal.json"));
    }
}
