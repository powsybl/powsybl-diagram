/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class SvgParametersTest {

    @Test
    void test() {
        SvgParameters svgParameters0 = new SvgParameters()
                .setDiagramPadding(new Padding(5))
                .setInsertNameDesc(false)
                .setSvgWidthAndHeightAdded(true)
                .setCssLocation(SvgParameters.CssLocation.EXTERNAL_NO_IMPORT)
                .setFixedWidth(300)
                .setFixedHeight(500)
                .setFixedScale(0.4)
                .setSizeConstraint(SvgParameters.SizeConstraint.NONE)
                .setArrowShift(1.)
                .setArrowLabelShift(0.5)
                .setConverterStationWidth(2.)
                .setVoltageLevelCircleRadius(3.)
                .setFictitiousVoltageLevelCircleRadius(2.)
                .setTransformerCircleRadius(1.2)
                .setNodeHollowWidth(0.8)
                .setEdgesForkLength(4.)
                .setEdgesForkAperture(10.)
                .setEdgeStartShift(0.)
                .setUnknownBusNodeExtraRadius(10.)
                .setLoopDistance(8.)
                .setLoopEdgesAperture(10.)
                .setLoopControlDistance(1.)
                .setEdgeInfoAlongEdge(false)
                .setEdgeNameDisplayed(false)
                .setInterAnnulusSpace(0.25)
                .setSvgPrefix("TestPrefix")
                .setIdDisplayed(true)
                .setSubstationDescriptionDisplayed(true)
                .setArrowPathIn("M-20 -10 H20 L0 10z")
                .setArrowPathOut("M-5 10 H5 L0 -10z")
                .setBusLegend(false)
                .setVoltageLevelDetails(true)
                .setLanguageTag("de")
                .setVoltageValuePrecision(0)
                .setAngleValuePrecision(2)
                .setPowerValuePrecision(3)
                .setCurrentValuePrecision(1)
                .setEdgeInfoDisplayed(SvgParameters.EdgeInfoEnum.REACTIVE_POWER)
                .setPstArrowHeadSize(20)
                .setUndefinedValueSymbol("\u002A")
                .setInjectionAperture(0.4)
                .setInjectionCircleRadius(1.)
                .setInjectionEdgeLength(5.);

        SvgParameters svgParameters1 = new SvgParameters(svgParameters0);

        assertEquals(svgParameters0.getDiagramPadding().getLeft(), svgParameters1.getDiagramPadding().getLeft(), 0);
        assertEquals(svgParameters0.getDiagramPadding().getTop(), svgParameters1.getDiagramPadding().getTop(), 0);
        assertEquals(svgParameters0.getDiagramPadding().getRight(), svgParameters1.getDiagramPadding().getRight(), 0);
        assertEquals(svgParameters0.getDiagramPadding().getBottom(), svgParameters1.getDiagramPadding().getBottom(), 0);
        assertEquals(svgParameters0.isInsertNameDesc(), svgParameters1.isInsertNameDesc());
        assertEquals(svgParameters0.isSvgWidthAndHeightAdded(), svgParameters1.isSvgWidthAndHeightAdded());
        assertEquals(svgParameters0.getCssLocation(), svgParameters1.getCssLocation());
        assertEquals(svgParameters0.getFixedWidth(), svgParameters1.getFixedWidth());
        assertEquals(svgParameters0.getFixedHeight(), svgParameters1.getFixedHeight());
        assertEquals(svgParameters0.getFixedScale(), svgParameters1.getFixedScale(), 0);
        assertEquals(svgParameters0.getSizeConstraint(), svgParameters1.getSizeConstraint());
        assertEquals(svgParameters0.getArrowShift(), svgParameters1.getArrowShift(), 0);
        assertEquals(svgParameters0.getArrowLabelShift(), svgParameters1.getArrowLabelShift(), 0);
        assertEquals(svgParameters0.getConverterStationWidth(), svgParameters1.getConverterStationWidth(), 0);
        assertEquals(svgParameters0.getVoltageLevelCircleRadius(), svgParameters1.getVoltageLevelCircleRadius(), 0);
        assertEquals(svgParameters0.getFictitiousVoltageLevelCircleRadius(), svgParameters1.getFictitiousVoltageLevelCircleRadius(), 0);
        assertEquals(svgParameters0.getTransformerCircleRadius(), svgParameters1.getTransformerCircleRadius(), 0);
        assertEquals(svgParameters0.getNodeHollowWidth(), svgParameters1.getNodeHollowWidth(), 0);
        assertEquals(svgParameters0.getEdgesForkLength(), svgParameters1.getEdgesForkLength(), 0);
        assertEquals(svgParameters0.getEdgesForkAperture(), svgParameters1.getEdgesForkAperture(), 0);
        assertEquals(svgParameters0.getEdgeStartShift(), svgParameters1.getEdgeStartShift(), 0);
        assertEquals(svgParameters0.getUnknownBusNodeExtraRadius(), svgParameters1.getUnknownBusNodeExtraRadius(), 0);
        assertEquals(svgParameters0.getLoopDistance(), svgParameters1.getLoopDistance(), 0);
        assertEquals(svgParameters0.getLoopEdgesAperture(), svgParameters1.getLoopEdgesAperture(), 0);
        assertEquals(svgParameters0.getLoopControlDistance(), svgParameters1.getLoopControlDistance(), 0);
        assertEquals(svgParameters0.isEdgeInfoAlongEdge(), svgParameters1.isEdgeInfoAlongEdge());
        assertEquals(svgParameters0.isEdgeNameDisplayed(), svgParameters1.isEdgeNameDisplayed());
        assertEquals(svgParameters0.getInterAnnulusSpace(), svgParameters1.getInterAnnulusSpace(), 0);
        assertEquals(svgParameters0.getSvgPrefix(), svgParameters1.getSvgPrefix());
        assertEquals(svgParameters0.isIdDisplayed(), svgParameters1.isIdDisplayed());
        assertEquals(svgParameters0.isSubstationDescriptionDisplayed(), svgParameters1.isSubstationDescriptionDisplayed());
        assertEquals(svgParameters0.getArrowPathIn(), svgParameters1.getArrowPathIn());
        assertEquals(svgParameters0.getArrowPathOut(), svgParameters1.getArrowPathOut());
        assertEquals(svgParameters0.isBusLegend(), svgParameters1.isBusLegend());
        assertEquals(svgParameters0.isVoltageLevelDetails(), svgParameters1.isVoltageLevelDetails());
        assertEquals(svgParameters0.getLanguageTag(), svgParameters1.getLanguageTag());
        assertEquals(svgParameters0.getVoltageValuePrecision(), svgParameters1.getVoltageValuePrecision());
        assertEquals(svgParameters0.getAngleValuePrecision(), svgParameters1.getAngleValuePrecision());
        assertEquals(svgParameters0.getPowerValuePrecision(), svgParameters1.getPowerValuePrecision());
        assertEquals(svgParameters0.getCurrentValuePrecision(), svgParameters1.getCurrentValuePrecision());
        assertEquals(svgParameters0.getEdgeInfoDisplayed(), svgParameters1.getEdgeInfoDisplayed());
        assertEquals(svgParameters0.getPstArrowHeadSize(), svgParameters1.getPstArrowHeadSize(), 0);
        assertEquals(svgParameters0.getUndefinedValueSymbol(), svgParameters1.getUndefinedValueSymbol());
        assertEquals(svgParameters0.getInjectionAperture(), svgParameters1.getInjectionAperture());
        assertEquals(svgParameters0.getInjectionCircleRadius(), svgParameters1.getInjectionCircleRadius());
        assertEquals(svgParameters0.getInjectionEdgeLength(), svgParameters1.getInjectionEdgeLength());
    }
}
