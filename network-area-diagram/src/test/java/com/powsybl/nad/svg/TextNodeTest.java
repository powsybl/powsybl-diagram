/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class TextNodeTest extends AbstractTest {

    private LabelProvider labelProvider;

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new TopologicalStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        if (labelProvider != null) {
            return labelProvider;
        }
        return new DefaultLabelProvider(network, getSvgParameters()) {
            @Override
            public List<String> getLegendFooter(String voltageLevelId) {
                if (getSvgParameters().isVoltageLevelDetails()) {
                    VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
                    return List.of(
                            vl.getLoadCount() + " loads",
                            vl.getGeneratorCount() + " generators",
                            vl.getBatteryCount() + " batteries",
                            vl.getDanglingLineCount() + " dangling lines");
                } else {
                    return Collections.emptyList();
                }
            }
        };
    }

    @Test
    void testVlId() {
        Network network = Networks.createTwoVoltageLevels();
        getSvgParameters().setIdDisplayed(true).setBusLegend(false);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        assertSvgEquals("/vl_description_id.svg", network);
    }

    @Test
    void testSubstationDescription() {
        Network network = Networks.createTwoVoltageLevels();
        getSvgParameters().setSubstationDescriptionDisplayed(true).setBusLegend(false).setVoltageLevelDetails(true);
        assertSvgEquals("/vl_description_substation.svg", network);
    }

    @Test
    void testSubstationId() {
        Network network = Networks.createTwoVoltageLevels();
        getSvgParameters().setSubstationDescriptionDisplayed(true).setIdDisplayed(true).setBusLegend(false);
        assertSvgEquals("/vl_description_substation_id.svg", network);
    }

    @Test
    void testDetailedTextNodeNoBusLegend() {
        Network network = Networks.createTwoVoltageLevels();
        getSvgParameters().setVoltageLevelDetails(true).setBusLegend(false);
        assertSvgEquals("/detailed_text_node_no_legend.svg", network);
    }

    @Test
    void testDetailedTextNode() {
        Network network = Networks.createTwoVoltageLevels();
        getSvgParameters().setVoltageLevelDetails(true).setSubstationDescriptionDisplayed(true);
        assertSvgEquals("/detailed_text_node.svg", network);
    }

    @Test
    void testProductionConsumption() {
        Network network = Networks.createThreeVoltageLevelsFiveBusesWithValuesAtTerminals();

        getSvgParameters().setVoltageLevelDetails(true).setBusLegend(false);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        assertSvgEquals("/production_consumption_text_node.svg", network);
    }

    @Test
    void testProductionConsumptionWithNaN() {
        Network network = Networks.createThreeVoltageLevelsFiveBuses();

        getSvgParameters().setVoltageLevelDetails(true).setBusLegend(false);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        assertSvgEquals("/production_consumption_text_node_nan.svg", network);
    }
}
