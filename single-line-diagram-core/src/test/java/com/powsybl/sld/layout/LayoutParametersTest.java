/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class LayoutParametersTest {

    @Test
    public void test() {
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(25)
                .setTranslateY(60)
                .setInitialXBus(10)
                .setInitialYBus(280)
                .setVerticalSpaceBus(20)
                .setHorizontalBusPadding(30)
                .setCellWidth(70)
                .setExternCellHeight(240)
                .setInternCellHeight(50)
                .setStackHeight(40)
                .setShowGrid(true)
                .setShowInternalNodes(true)
                .setScaleFactor(2)
                .setHorizontalSubstationPadding(60)
                .setVerticalSubstationPadding(70)
                .setDrawStraightWires(true)
                .setHorizontalSnakeLinePadding(25)
                .setVerticalSnakeLinePadding(40)
                .setArrowDistance(25)
                .setShowInductorFor3WT(true)
                .setDiagramName("diag")
                .setShiftFeedersPosition(false)
                .setScaleShiftFeedersPosition(2)
                .setAvoidSVGComponentsDuplication(true);
        LayoutParameters layoutParameters2 = new LayoutParameters(layoutParameters);

        assertEquals(layoutParameters.getTranslateX(), layoutParameters2.getTranslateX(), 0);
        assertEquals(layoutParameters.getTranslateY(), layoutParameters2.getTranslateY(), 0);
        assertEquals(layoutParameters.getInitialXBus(), layoutParameters2.getInitialXBus(), 0);
        assertEquals(layoutParameters.getInitialYBus(), layoutParameters2.getInitialYBus(), 0);
        assertEquals(layoutParameters.getVerticalSpaceBus(), layoutParameters2.getVerticalSpaceBus(), 0);
        assertEquals(layoutParameters.getHorizontalBusPadding(), layoutParameters2.getHorizontalBusPadding(), 0);
        assertEquals(layoutParameters.getCellWidth(), layoutParameters2.getCellWidth(), 0);
        assertEquals(layoutParameters.getExternCellHeight(), layoutParameters2.getExternCellHeight(), 0);
        assertEquals(layoutParameters.getInternCellHeight(), layoutParameters2.getInternCellHeight(), 0);
        assertEquals(layoutParameters.getStackHeight(), layoutParameters2.getStackHeight(), 0);
        assertEquals(layoutParameters.isShowGrid(), layoutParameters2.isShowGrid());
        assertEquals(layoutParameters.isShowInternalNodes(), layoutParameters2.isShowInternalNodes());
        assertEquals(layoutParameters.getScaleFactor(), layoutParameters2.getScaleFactor(), 0);
        assertEquals(layoutParameters.getHorizontalSubstationPadding(), layoutParameters2.getHorizontalSubstationPadding(), 0);
        assertEquals(layoutParameters.getVerticalSubstationPadding(), layoutParameters2.getVerticalSubstationPadding(), 0);
        assertEquals(layoutParameters.isDrawStraightWires(), layoutParameters2.isDrawStraightWires());
        assertEquals(layoutParameters.getHorizontalSnakeLinePadding(), layoutParameters2.getHorizontalSnakeLinePadding(), 0);
        assertEquals(layoutParameters.getVerticalSnakeLinePadding(), layoutParameters2.getVerticalSnakeLinePadding(), 0);
        assertEquals(layoutParameters.getArrowDistance(), layoutParameters2.getArrowDistance(), 0);
        assertEquals(layoutParameters.isShiftFeedersPosition(), layoutParameters2.isShiftFeedersPosition());
        assertEquals(layoutParameters.getScaleShiftFeedersPosition(), layoutParameters2.getScaleShiftFeedersPosition(), 0);
        assertEquals(layoutParameters.isShowInductorFor3WT(), layoutParameters2.isShowInductorFor3WT());
        assertEquals(layoutParameters.getDiagramName(), layoutParameters2.getDiagramName());
        assertEquals(layoutParameters.isAvoidSVGComponentsDuplication(), layoutParameters2.isAvoidSVGComponentsDuplication());
    }
}
