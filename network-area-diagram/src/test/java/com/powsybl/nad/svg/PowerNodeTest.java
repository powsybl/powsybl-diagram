/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
import com.powsybl.nad.model.VoltageLevelNode;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PowerNodeTest extends AbstractTest {

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
            public List<String> getVoltageLevelDetails(VoltageLevelNode vlNode) {
                VoltageLevel vl = network.getVoltageLevel(vlNode.getEquipmentId());
                return List.of(
                        vl.getLoadCount() + " loads",
                        vl.getGeneratorCount() + " generators",
                        vl.getBatteryCount() + " batteries",
                        vl.getDanglingLineCount() + " dangling lines");
            }
        };
    }

    @Test
    void testProductionConsumption() {
        Network network = Networks.createThreeVoltageLevelsFiveBusesWithValuesAtTerminals();

        getSvgParameters().setVoltageLevelPowerDetails(true).setBusLegend(false);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        assertEquals(toString("/production-consumption-power-nodes.svg"), generateSvgString(network, "/production-consumption-power-nodes.svg"));
    }

    @Test
    void testProductionConsumptionWithNaN() {
        Network network = Networks.createThreeVoltageLevelsFiveBuses();

        getSvgParameters().setVoltageLevelPowerDetails(true).setBusLegend(false);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        assertEquals(toString("/production_consumption_power_node_nan.svg"), generateSvgString(network, "/production_consumption_power_node_nan.svg"));
    }

    @Test
    void testProductionConsumptionForceLayout() {
        Network network = Networks.createThreeVoltageLevelsFiveBusesWithValuesAtTerminals();
        getLayoutParameters().setPowerNodesForceLayout(true);
        getSvgParameters().setVoltageLevelPowerDetails(true).setBusLegend(false);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        assertEquals(toString("/production-consumption-power-nodes-force-layout.svg"), generateSvgString(network, "production-consumption-power-nodes-force-layout.svg"));
    }

}
