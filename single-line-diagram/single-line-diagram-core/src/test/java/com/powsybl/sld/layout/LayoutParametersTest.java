/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public class LayoutParametersTest {

    @Test
    public void test() {
        LayoutParameters layoutParameters = new LayoutParameters()
                .setVoltageLevelPadding(15, 35, 25, 45)
                .setDiagrammPadding(20, 40, 30, 50)
                .setVerticalSpaceBus(20)
                .setHorizontalBusPadding(30)
                .setCellWidth(70)
                .setExternCellHeight(240)
                .setInternCellHeight(50)
                .setStackHeight(40)
                .setShowGrid(true)
                .setShowInternalNodes(true)
                .setScaleFactor(2)
                .setDrawStraightWires(true)
                .setHorizontalSnakeLinePadding(25)
                .setVerticalSnakeLinePadding(40)
                .setFeederInfosOuterMargin(25)
                .setDiagramName("diag")
                .setAvoidSVGComponentsDuplication(true)
                .setAdaptCellHeightToContent(true)
                .setMaxComponentHeight(10)
                .setMinSpaceBetweenComponents(30)
                .setMinExternCellHeight(150)
                .setAngleLabelShift(42)
                .setLabelCentered(true)
                .setLabelDiagonal(true)
                .setHighlightLineState(false)
                .setTooltipEnabled(true)
                .setAddNodesInfos(true)
                .setSpaceForFeederInfos(70)
                .setFeederInfoSymmetry(true)
                .setCssLocation(LayoutParameters.CssLocation.EXTERNAL_NO_IMPORT)
                .setSvgWidthAndHeightAdded(true)
                .setUseName(true)
                .setFeederInfosIntraMargin(21)
                .setBusInfoMargin(22)
                .setBusbarsAlignment(LayoutParameters.Alignment.LAST)
                .setComponentsOnBusbars(List.of("COMPONENT_ON_BUS"))
                .setLanguageTag("fr")
                .setVoltageValuePrecision(3)
                .setPowerValuePrecision(2)
                .setAngleValuePrecision(0)
                .setUndefinedValueSymbol("\u002A");

        layoutParameters.setComponentsSize(null);

        LayoutParameters layoutParameters2 = new LayoutParameters(layoutParameters);

        assertEquals(layoutParameters.getVoltageLevelPadding().getLeft(), layoutParameters2.getVoltageLevelPadding().getLeft(), 0);
        assertEquals(layoutParameters.getVoltageLevelPadding().getTop(), layoutParameters2.getVoltageLevelPadding().getTop(), 0);
        assertEquals(layoutParameters.getVoltageLevelPadding().getRight(), layoutParameters2.getVoltageLevelPadding().getRight(), 0);
        assertEquals(layoutParameters.getVoltageLevelPadding().getBottom(), layoutParameters2.getVoltageLevelPadding().getBottom(), 0);
        assertEquals(layoutParameters.getDiagramPadding().getLeft(), layoutParameters2.getDiagramPadding().getLeft(), 0);
        assertEquals(layoutParameters.getDiagramPadding().getTop(), layoutParameters2.getDiagramPadding().getTop(), 0);
        assertEquals(layoutParameters.getDiagramPadding().getRight(), layoutParameters2.getDiagramPadding().getRight(), 0);
        assertEquals(layoutParameters.getDiagramPadding().getBottom(), layoutParameters2.getDiagramPadding().getBottom(), 0);
        assertEquals(layoutParameters.getVerticalSpaceBus(), layoutParameters2.getVerticalSpaceBus(), 0);
        assertEquals(layoutParameters.getHorizontalBusPadding(), layoutParameters2.getHorizontalBusPadding(), 0);
        assertEquals(layoutParameters.getCellWidth(), layoutParameters2.getCellWidth(), 0);
        assertEquals(layoutParameters.getExternCellHeight(), layoutParameters2.getExternCellHeight(), 0);
        assertEquals(layoutParameters.getInternCellHeight(), layoutParameters2.getInternCellHeight(), 0);
        assertEquals(layoutParameters.getStackHeight(), layoutParameters2.getStackHeight(), 0);
        assertEquals(layoutParameters.isShowGrid(), layoutParameters2.isShowGrid());
        assertEquals(layoutParameters.isShowInternalNodes(), layoutParameters2.isShowInternalNodes());
        assertEquals(layoutParameters.getScaleFactor(), layoutParameters2.getScaleFactor(), 0);
        assertEquals(layoutParameters.isDrawStraightWires(), layoutParameters2.isDrawStraightWires());
        assertEquals(layoutParameters.getHorizontalSnakeLinePadding(), layoutParameters2.getHorizontalSnakeLinePadding(), 0);
        assertEquals(layoutParameters.getVerticalSnakeLinePadding(), layoutParameters2.getVerticalSnakeLinePadding(), 0);
        assertEquals(layoutParameters.getFeederInfosOuterMargin(), layoutParameters2.getFeederInfosOuterMargin(), 0);
        assertEquals(layoutParameters.getDiagramName(), layoutParameters2.getDiagramName());
        assertEquals(layoutParameters.isAvoidSVGComponentsDuplication(), layoutParameters2.isAvoidSVGComponentsDuplication());
        assertEquals(layoutParameters.isAdaptCellHeightToContent(), layoutParameters2.isAdaptCellHeightToContent());
        assertEquals(layoutParameters.getMaxComponentHeight(), layoutParameters2.getMaxComponentHeight(), 0);
        assertEquals(layoutParameters.getMinSpaceBetweenComponents(), layoutParameters2.getMinSpaceBetweenComponents(), 0);
        assertEquals(layoutParameters.getMinExternCellHeight(), layoutParameters2.getMinExternCellHeight(), 0);
        assertEquals(layoutParameters.getAngleLabelShift(), layoutParameters2.getAngleLabelShift(), 0);
        assertEquals(layoutParameters.isLabelCentered(), layoutParameters2.isLabelCentered());
        assertEquals(layoutParameters.isLabelDiagonal(), layoutParameters2.isLabelDiagonal());
        assertEquals(layoutParameters.isHighlightLineState(), layoutParameters2.isHighlightLineState());
        assertEquals(layoutParameters.isTooltipEnabled(), layoutParameters2.isTooltipEnabled());
        assertEquals(layoutParameters.isAddNodesInfos(), layoutParameters2.isAddNodesInfos());
        assertEquals(layoutParameters.getSpaceForFeederInfos(), layoutParameters2.getSpaceForFeederInfos(), 0);
        assertEquals(layoutParameters.isFeederInfoSymmetry(), layoutParameters2.isFeederInfoSymmetry());
        assertEquals(layoutParameters.getCssLocation(), layoutParameters2.getCssLocation());
        assertEquals(layoutParameters.isSvgWidthAndHeightAdded(), layoutParameters2.isSvgWidthAndHeightAdded());
        assertEquals(layoutParameters.isUseName(), layoutParameters2.isUseName());
        assertEquals(layoutParameters.getFeederInfosIntraMargin(), layoutParameters2.getFeederInfosIntraMargin(), 0);
        assertEquals(layoutParameters.getBusInfoMargin(), layoutParameters2.getBusInfoMargin(), 0);
        assertEquals(layoutParameters.getBusbarsAlignment(), layoutParameters2.getBusbarsAlignment());
        assertEquals(layoutParameters.getComponentsOnBusbars(), layoutParameters2.getComponentsOnBusbars());
        assertEquals(layoutParameters.getLanguageTag(), layoutParameters2.getLanguageTag());
        assertEquals(layoutParameters.getVoltageValuePrecision(), layoutParameters2.getVoltageValuePrecision());
        assertEquals(layoutParameters.getPowerValuePrecision(), layoutParameters2.getPowerValuePrecision());
        assertEquals(layoutParameters.getAngleValuePrecision(), layoutParameters2.getAngleValuePrecision());
        assertEquals(layoutParameters.getUndefinedValueSymbol(), layoutParameters2.getUndefinedValueSymbol());
        assertEquals(layoutParameters.getComponentsSize(), layoutParameters2.getComponentsSize());
    }
}
