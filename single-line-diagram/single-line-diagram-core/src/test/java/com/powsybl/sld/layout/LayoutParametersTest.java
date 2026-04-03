/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Jacques Borsenberger {@literal <jacques.borsenberger at rte-france.com>}
 */
class LayoutParametersTest {

    @Test
    void test() {
        LayoutParameters layoutParameters = new LayoutParameters()
                .setVerticalSpaceBus(20)
                .setHorizontalBusPadding(30)
                .setCellWidth(70)
                .setExternCellHeight(240)
                .setInternCellHeight(50)
                .setStackHeight(40)
                .setHorizontalSnakeLinePadding(25)
                .setVerticalSnakeLinePadding(40)
                .setSpaceForFeederInfos(70)
                .setAdaptCellHeightToContent(true)
                .setMaxComponentHeight(10)
                .setMinSpaceBetweenComponents(30)
                .setMinExternCellHeight(150)
                .setVoltageLevelPadding(15, 35, 25, 45)
                .setDiagrammPadding(20, 40, 30, 50)
                .setBusbarsAlignment(LayoutParameters.Alignment.LAST)
                .setComponentsOnBusbars(List.of("COMPONENT_ON_BUS"))
                .setRemoveFictitiousSwitchNodes(true)
                .setZoneLayoutSnakeLinePadding(120);

        layoutParameters.setComponentsSize(null);

        LayoutParameters layoutParameters2 = new LayoutParameters(layoutParameters);

        assertEquals(layoutParameters.getVerticalSpaceBus(), layoutParameters2.getVerticalSpaceBus(), 0);
        assertEquals(layoutParameters.getHorizontalBusPadding(), layoutParameters2.getHorizontalBusPadding(), 0);
        assertEquals(layoutParameters.getCellWidth(), layoutParameters2.getCellWidth(), 0);
        assertEquals(layoutParameters.getExternCellHeight(), layoutParameters2.getExternCellHeight(), 0);
        assertEquals(layoutParameters.getInternCellHeight(), layoutParameters2.getInternCellHeight(), 0);
        assertEquals(layoutParameters.getStackHeight(), layoutParameters2.getStackHeight(), 0);
        assertEquals(layoutParameters.getHorizontalSnakeLinePadding(), layoutParameters2.getHorizontalSnakeLinePadding(), 0);
        assertEquals(layoutParameters.getVerticalSnakeLinePadding(), layoutParameters2.getVerticalSnakeLinePadding(), 0);
        assertEquals(layoutParameters.getSpaceForFeederInfos(), layoutParameters2.getSpaceForFeederInfos(), 0);
        assertEquals(layoutParameters.isAdaptCellHeightToContent(), layoutParameters2.isAdaptCellHeightToContent());
        assertEquals(layoutParameters.getMaxComponentHeight(), layoutParameters2.getMaxComponentHeight(), 0);
        assertEquals(layoutParameters.getMinSpaceBetweenComponents(), layoutParameters2.getMinSpaceBetweenComponents(), 0);
        assertEquals(layoutParameters.getMinExternCellHeight(), layoutParameters2.getMinExternCellHeight(), 0);
        assertEquals(layoutParameters.getBusbarsAlignment(), layoutParameters2.getBusbarsAlignment());
        assertEquals(layoutParameters.getComponentsOnBusbars(), layoutParameters2.getComponentsOnBusbars());
        assertEquals(layoutParameters.isRemoveFictitiousSwitchNodes(), layoutParameters2.isRemoveFictitiousSwitchNodes());
        assertEquals(layoutParameters.getComponentsSize(), layoutParameters2.getComponentsSize());
        assertEquals(layoutParameters.getZoneLayoutSnakeLinePadding(), layoutParameters2.getZoneLayoutSnakeLinePadding());
    }

    @Test
    void testPadding() {
        LayoutParameters layoutParameters = new LayoutParameters()
                .setVoltageLevelPadding(15, 35, 25, 45)
                .setDiagrammPadding(20, 40, 30, 50);

        LayoutParameters layoutParameters2 = new LayoutParameters(layoutParameters);

        assertEquals(layoutParameters.getVoltageLevelPadding().left(), layoutParameters2.getVoltageLevelPadding().left(), 0);
        assertEquals(layoutParameters.getVoltageLevelPadding().top(), layoutParameters2.getVoltageLevelPadding().top(), 0);
        assertEquals(layoutParameters.getVoltageLevelPadding().right(), layoutParameters2.getVoltageLevelPadding().right(), 0);
        assertEquals(layoutParameters.getVoltageLevelPadding().bottom(), layoutParameters2.getVoltageLevelPadding().bottom(), 0);
        assertEquals(layoutParameters.getDiagramPadding().left(), layoutParameters2.getDiagramPadding().left(), 0);
        assertEquals(layoutParameters.getDiagramPadding().top(), layoutParameters2.getDiagramPadding().top(), 0);
        assertEquals(layoutParameters.getDiagramPadding().right(), layoutParameters2.getDiagramPadding().right(), 0);
        assertEquals(layoutParameters.getDiagramPadding().bottom(), layoutParameters2.getDiagramPadding().bottom(), 0);
    }
}
