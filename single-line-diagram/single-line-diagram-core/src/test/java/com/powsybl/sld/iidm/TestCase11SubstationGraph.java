/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.svg.BasicStyleProvider;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.util.NominalVoltageDiagramStyleProvider;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCase11SubstationGraph extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = NetworkFactory.createTestCase11Network();
        substation = network.getSubstation("subst");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    public void testHorizontal() {

        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run horizontal substation layout
        substationGraphLayout(g);

        assertEquals(toString("/TestCase11SubstationGraphH.json"), toJson(g, "/TestCase11SubstationGraphH.json"));
    }

    @Test
    public void testHorizontalFirstAlignment() {
        runHorizontalALignmentTest(LayoutParameters.Alignment.FIRST);
    }

    @Test
    public void testHorizontalLastAlignment() {
        runHorizontalALignmentTest(LayoutParameters.Alignment.LAST);
    }

    @Test
    public void testHorizontalMiddleAlignment() {
        runHorizontalALignmentTest(LayoutParameters.Alignment.MIDDLE);
    }

    @Test
    public void testHorizontalNoneAlignment() {
        runHorizontalALignmentTest(LayoutParameters.Alignment.NONE);
    }

    private void runHorizontalALignmentTest(LayoutParameters.Alignment alignment) {
        layoutParameters.setBusbarsAlignment(alignment);

        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run horizontal substation layout
        substationGraphLayout(g);

        String filename = "/TestCase11SubstationGraphH" + StringUtils.capitalize(alignment.name().toLowerCase()) + ".svg";
        assertEquals(toString(filename), toSVG(g, filename));
    }

    @Test
    public void testVertical() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run vertical substation layout
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase11SubstationGraphV.json"), toJson(g, "/TestCase11SubstationGraphV.json"));
    }

    @Test
    public void testRight3wtOrientation() {
        // build substation graph
        network.getThreeWindingsTransformer("trf7").remove();
        createThreeWindingsTransformer(substation, "trf7", "trf7", "vl3", "vl2", "vl1",
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                50., 225., 400.,
                8, 19, 31,
                "trf73", 3, ConnectablePosition.Direction.BOTTOM,
                "trf72", 4, ConnectablePosition.Direction.TOP,
                "trf71", 6, ConnectablePosition.Direction.BOTTOM);

        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run vertical substation layout
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase11Right3wtOrientation.json"), toJson(g, "/TestCase11Right3wtOrientation.json"));
    }

    @Test
    public void testLeft3wtOrientation() {
        // build substation graph
        network.getThreeWindingsTransformer("trf7").remove();
        createThreeWindingsTransformer(substation, "trf7", "trf7", "vl2", "vl1", "vl3",
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                225., 400., 50.,
                19, 31, 8,
                "trf72", 4, ConnectablePosition.Direction.TOP,
                "trf71", 6, ConnectablePosition.Direction.BOTTOM,
                "trf73", 3, ConnectablePosition.Direction.BOTTOM);

        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run vertical substation layout
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase11Left3wtOrientation.json"), toJson(g, "/TestCase11Left3wtOrientation.json"));
    }

    @Test
    public void testHorizontalDefaultStyle() {
        // compare metadata of substation diagram with reference
        // (with horizontal substation layout)
        SubstationGraph substationGraph = graphBuilder.buildSubstationGraph(substation.getId());

        compareMetadata(substationGraph, "/substDiag_metadata.json",
                new HorizontalSubstationLayoutFactory(),
                new PositionVoltageLevelLayoutFactory(),
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new BasicStyleProvider());
    }

    @Test
    public void testHorizontalNominalStyle() {
        // compare metadata of substation diagram with reference
        // (with horizontal substation layout)
        SubstationGraph graph = graphBuilder.buildSubstationGraph(substation.getId());

        compareMetadata(graph, "/substDiag_metadata.json",
                new HorizontalSubstationLayoutFactory(),
                new PositionVoltageLevelLayoutFactory(),
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new NominalVoltageDiagramStyleProvider(network));
    }
}
