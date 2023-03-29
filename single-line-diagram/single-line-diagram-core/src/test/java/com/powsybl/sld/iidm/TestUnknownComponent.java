/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.library.Component;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TestUnknownComponent extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        layoutParameters.setCellWidth(80);

        network = NetworkFactory.createTestCase11Network();
        substation = network.getSubstation("subst");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Override
    protected ResourcesComponentLibrary getResourcesComponentLibrary() {
        return Mockito.spy(new ConvergenceComponentLibrary());
    }

    @Test
    public void test() {
        layoutParameters.setAddNodesInfos(true);

        // build voltage level 1 graph
        VoltageLevelGraph g1 = graphBuilder.buildVoltageLevelGraph("vl1");

        voltageLevelGraphLayout(g1);

        // Make all 3wt unknown SVG component
        Map<String, List<Element>> unknownSvgElements = componentLibrary.getSvgElements(ComponentTypeName.UNKNOWN_COMPONENT);
        Optional<String> unknownComponentStyleClass = componentLibrary.getComponentStyleClass(ComponentTypeName.UNKNOWN_COMPONENT);
        ComponentSize unknownSize = componentLibrary.getSize(ComponentTypeName.UNKNOWN_COMPONENT);
        Map<Orientation, Component.Transformation> unknownTransformations = componentLibrary.getTransformations(ComponentTypeName.UNKNOWN_COMPONENT);
        List<AnchorPoint> unknownAnchorPoints = componentLibrary.getAnchorPoints(ComponentTypeName.UNKNOWN_COMPONENT);
        Optional<String> unknownSubComponentStyleClass = Optional.empty();

        Mockito.when(componentLibrary.getSvgElements(ComponentTypeName.THREE_WINDINGS_TRANSFORMER)).thenReturn(unknownSvgElements);
        Mockito.when(componentLibrary.getComponentStyleClass(ComponentTypeName.THREE_WINDINGS_TRANSFORMER)).thenReturn(unknownComponentStyleClass);
        Mockito.when(componentLibrary.getSize(ComponentTypeName.THREE_WINDINGS_TRANSFORMER)).thenReturn(unknownSize);
        Mockito.when(componentLibrary.getTransformations(ComponentTypeName.THREE_WINDINGS_TRANSFORMER)).thenReturn(unknownTransformations);
        Mockito.when(componentLibrary.getAnchorPoints(ComponentTypeName.THREE_WINDINGS_TRANSFORMER)).thenReturn(unknownAnchorPoints);
        Mockito.when(componentLibrary.getSubComponentStyleClass(ComponentTypeName.THREE_WINDINGS_TRANSFORMER, "WINDING1")).thenReturn(unknownSubComponentStyleClass);
        Mockito.when(componentLibrary.getSubComponentStyleClass(ComponentTypeName.THREE_WINDINGS_TRANSFORMER, "WINDING2")).thenReturn(unknownSubComponentStyleClass);
        Mockito.when(componentLibrary.getSubComponentStyleClass(ComponentTypeName.THREE_WINDINGS_TRANSFORMER, "WINDING3")).thenReturn(unknownSubComponentStyleClass);

        // write SVGs and compare to reference
        assertEquals(toString("/TestUnknownConvergence.svg"), toSVG(g1, "/TestUnknownConvergence.svg"));
    }
}
