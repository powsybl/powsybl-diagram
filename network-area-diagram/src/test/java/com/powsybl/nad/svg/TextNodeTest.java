/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.VoltageLevelNode;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TextNodeTest extends AbstractTest {

    private LabelProvider labelProvider;

    @Before
    public void setup() {
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
    public void testVlId() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();
        getSvgParameters().setIdDisplayed(true).setBusLegend(false);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        assertEquals(toString("/vl_description_id.svg"), generateSvgString(network, "/vl_description_id.svg"));
    }

    @Test
    public void testSubstationDescription() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();
        getSvgParameters().setSubstationDescriptionDisplayed(true).setBusLegend(false).setVoltageLevelDetails(true);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        assertEquals(toString("/vl_description_substation.svg"), generateSvgString(network, "/vl_description_substation.svg"));
    }

    @Test
    public void testSubstationId() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();
        getSvgParameters().setSubstationDescriptionDisplayed(true).setIdDisplayed(true).setBusLegend(false);
        assertEquals(toString("/vl_description_substation_id.svg"), generateSvgString(network, "/vl_description_substation_id.svg"));
    }

    @Test
    public void testDetailedTextNodeNoBusLegend() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();
        getSvgParameters().setVoltageLevelDetails(true).setBusLegend(false);
        assertEquals(toString("/detailed_text_node_no_legend.svg"), generateSvgString(network, "/detailed_text_node_no_legend.svg"));
    }

    @Test
    public void testDetailedTextNode() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();
        getSvgParameters().setVoltageLevelDetails(true).setSubstationDescriptionDisplayed(true);
        assertEquals(toString("/detailed_text_node.svg"), generateSvgString(network, "/detailed_text_node.svg"));
    }
}
