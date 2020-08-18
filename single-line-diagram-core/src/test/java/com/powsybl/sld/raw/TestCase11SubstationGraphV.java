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
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(50)
                .setExternCellHeight(250)
                .setInternCellHeight(40)
                .setStackHeight(30)
                .setShowGrid(true)
                .setShowInternalNodes(false)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50)
                .setDrawStraightWires(false)
                .setHorizontalSnakeLinePadding(30)
                .setVerticalSnakeLinePadding(30);

        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst", false);

        // write Json and compare to reference (with vertical substation layout)
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphVertical.json"), toString("/TestCase11SubstationGraphVertical.json"));
    }
}
