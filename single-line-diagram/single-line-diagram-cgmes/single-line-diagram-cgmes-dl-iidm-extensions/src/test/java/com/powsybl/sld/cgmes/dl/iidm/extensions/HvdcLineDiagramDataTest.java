/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Network;
import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.VscConverterStation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
class HvdcLineDiagramDataTest extends AbstractLineDiagramDataTest {

    @Test
    void test() {
        Network network = Networks.createNetworkWithHvdcLine();
        HvdcLine hvdcLine = network.getHvdcLine("HvdcLine");

        LineDiagramData<HvdcLine> hvdcLineDiagramData = new LineDiagramData<>(hvdcLine);
        hvdcLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        hvdcLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        hvdcLine.addExtension(LineDiagramData.class, hvdcLineDiagramData);

        HvdcLine hvdcLine2 = network.getHvdcLine("HvdcLine");
        LineDiagramData<HvdcLine> hvdcLineDiagramData2 = hvdcLine2.getExtension(LineDiagramData.class);

        assertEquals(1, hvdcLineDiagramData2.getDiagramsNames().size());
        checkDiagramData(hvdcLineDiagramData2, DIAGRAM_NAME);
    }

    @Test
    void testMultipleDiagrams() {
        Network network = Networks.createNetworkWithHvdcLine();
        HvdcLine hvdcLine = network.getHvdcLine("HvdcLine");

        LineDiagramData<HvdcLine> hvdcLineDiagramData = new LineDiagramData<>(hvdcLine);
        hvdcLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        hvdcLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        hvdcLineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(10, 20, 1));
        hvdcLineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(20, 10, 2));
        hvdcLine.addExtension(LineDiagramData.class, hvdcLineDiagramData);

        HvdcLine hvdcLine2 = network.getHvdcLine("HvdcLine");
        LineDiagramData<HvdcLine> hvdcLineDiagramData2 = hvdcLine2.getExtension(LineDiagramData.class);

        assertEquals(2, hvdcLineDiagramData2.getDiagramsNames().size());
        checkDiagramData(hvdcLineDiagramData2, DIAGRAM_NAME);
        checkDiagramData(hvdcLineDiagramData2, DIAGRAM2_NAME);
    }

    @Test
    void testVsc() {
        Network network = Networks.createNetworkWithHvdcLine();
        VscConverterStation vsc = network.getVscConverterStation("Converter1");

        LineDiagramData<VscConverterStation> vscDiagramData = LineDiagramData.getOrCreateDiagramData(vsc);
        vscDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        vscDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        vsc.addExtension(LineDiagramData.class, vscDiagramData);

        VscConverterStation vsc2 = network.getVscConverterStation("Converter1");
        LineDiagramData<VscConverterStation> vscDiagramData2 = vsc2.getExtension(LineDiagramData.class);

        assertEquals(1, vscDiagramData2.getDiagramsNames().size());
        checkDiagramData(vscDiagramData2, DIAGRAM_NAME);

        LineDiagramData<VscConverterStation> vscDiagramData3 = LineDiagramData.getOrCreateDiagramData(vsc);
        assertSame(vscDiagramData, vscDiagramData3);
    }

    @Test
    void testLcc() {
        Network network = Networks.createNetworkWithHvdcLines();
        LccConverterStation lcc = network.getLccConverterStation("Converter3");

        LineDiagramData<LccConverterStation> lccDiagramData = LineDiagramData.getOrCreateDiagramData(lcc);
        lccDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        lccDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        lcc.addExtension(LineDiagramData.class, lccDiagramData);

        LccConverterStation lcc2 = network.getLccConverterStation("Converter3");
        LineDiagramData<LccConverterStation> lccDiagramData2 = lcc2.getExtension(LineDiagramData.class);

        assertEquals(1, lccDiagramData2.getDiagramsNames().size());
        checkDiagramData(lccDiagramData2, DIAGRAM_NAME);

        LineDiagramData<LccConverterStation> lccDiagramData3 = LineDiagramData.getOrCreateDiagramData(lcc);
        assertSame(lccDiagramData, lccDiagramData3);
    }
}
