/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class SvgParametersTest {

    @Test
    void test() {
        SvgParameters svgParameters0 = new SvgParameters()
                .setPrefixId("test")
                .setUndefinedValueSymbol("\u002A")
                .setLanguageTag("es")
                .setVoltageValuePrecision(0)
                .setPowerValuePrecision(3)
                .setAngleValuePrecision(2)
                .setCurrentValuePrecision(1)
                .setActivePowerUnit("MW")
                .setReactivePowerUnit("MVAR")
                .setCurrentUnit("A")
                .setBusInfoMargin(22)
                .setFeederInfosIntraMargin(21)
                .setFeederInfosOuterMargin(25)
                .setFeederInfoSymmetry(true)
                .setAddNodesInfos(true)
                .setUseName(true)
                .setAngleLabelShift(42)
                .setLabelCentered(true)
                .setLabelDiagonal(true)
                .setTooltipEnabled(true)
                .setSvgWidthAndHeightAdded(true)
                .setCssLocation(SvgParameters.CssLocation.EXTERNAL_NO_IMPORT)
                .setAvoidSVGComponentsDuplication(true)
                .setDiagramName("diag")
                .setDrawStraightWires(true)
                .setShowGrid(true)
                .setShowInternalNodes(true)
                .setDisplayCurrentFeederInfo(true)
                .setDisplayEquipmentNodesLabel(true)
                .setDisplayConnectivityNodesId(true)
                .setUnifyVoltageLevelColors(true);

        SvgParameters svgParameters1 = new SvgParameters(svgParameters0);

        assertEquals(svgParameters0.getPrefixId(), svgParameters1.getPrefixId());
        assertEquals(svgParameters0.getUndefinedValueSymbol(), svgParameters1.getUndefinedValueSymbol());
        assertEquals(svgParameters0.getLanguageTag(), svgParameters1.getLanguageTag());
        assertEquals(svgParameters0.getVoltageValuePrecision(), svgParameters1.getVoltageValuePrecision());
        assertEquals(svgParameters0.getPowerValuePrecision(), svgParameters1.getPowerValuePrecision());
        assertEquals(svgParameters0.getAngleValuePrecision(), svgParameters1.getAngleValuePrecision());
        assertEquals(svgParameters0.getCurrentValuePrecision(), svgParameters1.getCurrentValuePrecision());
        assertEquals(svgParameters0.getActivePowerUnit(), svgParameters1.getActivePowerUnit());
        assertEquals(svgParameters0.getReactivePowerUnit(), svgParameters1.getReactivePowerUnit());
        assertEquals(svgParameters0.getCurrentUnit(), svgParameters1.getCurrentUnit());
        assertEquals(svgParameters0.getBusInfoMargin(), svgParameters1.getBusInfoMargin(), 0);
        assertEquals(svgParameters0.getFeederInfosIntraMargin(), svgParameters1.getFeederInfosIntraMargin(), 0);
        assertEquals(svgParameters0.getFeederInfosOuterMargin(), svgParameters1.getFeederInfosOuterMargin(), 0);
        assertEquals(svgParameters0.isFeederInfoSymmetry(), svgParameters1.isFeederInfoSymmetry());
        assertEquals(svgParameters0.isAddNodesInfos(), svgParameters1.isAddNodesInfos());
        assertEquals(svgParameters0.isUseName(), svgParameters1.isUseName());
        assertEquals(svgParameters0.getAngleLabelShift(), svgParameters1.getAngleLabelShift(), 0);
        assertEquals(svgParameters0.isLabelCentered(), svgParameters1.isLabelCentered());
        assertEquals(svgParameters0.isLabelDiagonal(), svgParameters1.isLabelDiagonal());
        assertEquals(svgParameters0.isTooltipEnabled(), svgParameters1.isTooltipEnabled());
        assertEquals(svgParameters0.isSvgWidthAndHeightAdded(), svgParameters1.isSvgWidthAndHeightAdded());
        assertEquals(svgParameters0.getCssLocation(), svgParameters1.getCssLocation());
        assertEquals(svgParameters0.isAvoidSVGComponentsDuplication(), svgParameters1.isAvoidSVGComponentsDuplication());
        assertEquals(svgParameters0.getDiagramName(), svgParameters1.getDiagramName());
        assertEquals(svgParameters0.isDrawStraightWires(), svgParameters1.isDrawStraightWires());
        assertEquals(svgParameters0.isShowGrid(), svgParameters1.isShowGrid());
        assertEquals(svgParameters0.isShowInternalNodes(), svgParameters1.isShowInternalNodes());
        assertEquals(svgParameters0.isDisplayCurrentFeederInfo(), svgParameters1.isDisplayCurrentFeederInfo());
        assertEquals(svgParameters0.isDisplayEquipmentNodesLabel(), svgParameters1.isDisplayEquipmentNodesLabel());
        assertEquals(svgParameters0.isDisplayConnectivityNodesId(), svgParameters1.isDisplayConnectivityNodesId());
        assertEquals(svgParameters0.isUnifyVoltageLevelColors(), svgParameters1.isUnifyVoltageLevelColors());
    }
}
