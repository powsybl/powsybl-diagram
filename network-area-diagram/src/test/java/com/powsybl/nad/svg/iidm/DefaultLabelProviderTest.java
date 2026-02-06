package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.test.Networks;
import com.powsybl.diagram.util.ValueFormatter;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.svg.EdgeInfo;
import com.powsybl.nad.svg.VoltageLevelLegend;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DefaultLabelProviderTest {

    @Test
    void testVoltageLevelLegend() {
        Network network = Networks.createTwoVoltageLevels();
        ValueFormatter valueFormatter = new ValueFormatter(1, 2, 3, 4, 5, Locale.US, "N/A");
        DefaultLabelProvider labelProvider = new DefaultLabelProvider(network, valueFormatter);

        VoltageLevelLegend voltageLevelLegend = labelProvider.getVoltageLevelLegend("vl1");
        assertEquals("", voltageLevelLegend.getBusLegend("b1"));
    }

    @Test
    void testUnknownInjection() {
        Network network = Networks.createTwoVoltageLevels();
        ValueFormatter valueFormatter = new ValueFormatter(1, 2, 3, 4, 5, Locale.US, "N/A");
        DefaultLabelProvider labelProvider = new DefaultLabelProvider(network, valueFormatter);

        PowsyblException exception = assertThrows(PowsyblException.class, () -> labelProvider.getInjectionEdgeInfos("UNKNOWN"));
        assertEquals("Unknown injection 'UNKNOWN'", exception.getMessage());
    }

    @Test
    void testGetBranchEdgeInfo() {
        Network network = Networks.createTwoVoltageLevels();
        ValueFormatter valueFormatter = new ValueFormatter(1, 1, 1, 1, 1, Locale.US, "N/A");
        DefaultLabelProvider labelProvider = new DefaultLabelProvider.Builder()
            .setInfoSideInternal(DefaultLabelProvider.EdgeInfoEnum.ACTIVE_POWER)
            .setInfoMiddleSide1(DefaultLabelProvider.EdgeInfoEnum.NAME)
            .setInfoMiddleSide2(DefaultLabelProvider.EdgeInfoEnum.VALUE_PERMANENT_LIMIT_PERCENTAGE)
            .setInfoSideExternal(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .build(network, valueFormatter);

        String lineId = "l1";

        // Add power values
        Line line = network.getLine(lineId);
        line.getTerminal1().setP(1400.0).setQ(400.0);
        line.getTerminal2().setP(1410.0).setQ(410.0);

        // Add current limits
        line.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
            .setPermanentLimit(2000.0)
            .beginTemporaryLimit()
            .setName("20'")
            .setValue(2100)
            .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("10'")
            .setValue(2200.0)
            .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits()
            .setPermanentLimit(2000.0)
            .beginTemporaryLimit()
            .setName("20'")
            .setValue(2100)
            .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("10'")
            .setValue(2400.0)
            .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        // Add voltage values
        line.getTerminal1().getBusBreakerView().getBus().setV(400.0);
        line.getTerminal2().getBusBreakerView().getBus().setV(410.0);

        // Middle
        Optional<EdgeInfo> optionalEdgeInfoMiddle = Optional.of(labelProvider.getBranchEdgeInfos(lineId, BranchEdge.LINE_EDGE).get(0));
        assertTrue(optionalEdgeInfoMiddle.isPresent());
        EdgeInfo middleEdgeInfo = optionalEdgeInfoMiddle.get();
        assertEquals(lineId, middleEdgeInfo.getLabelA().orElseThrow());
        assertEquals("105.1 %", middleEdgeInfo.getLabelB().orElseThrow());

        // Side 1
        List<EdgeInfo> edgeInfosSide1 = labelProvider.getBranchEdgeInfos(lineId, BranchEdge.Side.ONE, BranchEdge.LINE_EDGE);
        assertEquals(1, edgeInfosSide1.size());
        EdgeInfo edgeInfoSide1 = edgeInfosSide1.get(0);
        assertEquals("1,400.0", edgeInfoSide1.getLabelA().orElseThrow());
        assertTrue(edgeInfoSide1.getLabelB().isEmpty());

        // Side 2
        List<EdgeInfo> edgeInfosSide2 = labelProvider.getBranchEdgeInfos(lineId, BranchEdge.Side.TWO, BranchEdge.LINE_EDGE);
        assertEquals(1, edgeInfosSide2.size());
        EdgeInfo edgeInfoSide2 = edgeInfosSide2.get(0);
        assertEquals("1,410.0", edgeInfoSide2.getLabelA().orElseThrow());
        assertTrue(edgeInfoSide2.getLabelB().isEmpty());
    }

    @Test
    void testGetBranchEdgeInfoWithMultipleSideValues() {
        Network network = Networks.createTwoVoltageLevels();
        ValueFormatter valueFormatter = new ValueFormatter(1, 1, 1, 1, 1, Locale.US, "N/A");
        DefaultLabelProvider labelProvider = new DefaultLabelProvider.Builder()
            .setInfoSideInternal(DefaultLabelProvider.EdgeInfoEnum.ACTIVE_POWER)
            .setInfoMiddleSide1(DefaultLabelProvider.EdgeInfoEnum.NAME)
            .setInfoMiddleSide2(DefaultLabelProvider.EdgeInfoEnum.VALUE_PERMANENT_LIMIT_PERCENTAGE)
            .setInfoSideExternal(DefaultLabelProvider.EdgeInfoEnum.REACTIVE_POWER)
            .setDoubleArrowsDisplayed(true)
            .build(network, valueFormatter);

        String lineId = "l1";

        // Add power values
        Line line = network.getLine(lineId);
        line.getTerminal1().setP(1400.0).setQ(400.0);
        line.getTerminal2().setP(1410.0).setQ(410.0);

        // Add current limits
        line.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
            .setPermanentLimit(2000.0)
            .beginTemporaryLimit()
            .setName("20'")
            .setValue(2100)
            .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("10'")
            .setValue(2200.0)
            .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits()
            .setPermanentLimit(2000.0)
            .beginTemporaryLimit()
            .setName("20'")
            .setValue(2100)
            .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("10'")
            .setValue(2400.0)
            .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        // Add voltage values
        line.getTerminal1().getBusBreakerView().getBus().setV(400.0);
        line.getTerminal2().getBusBreakerView().getBus().setV(410.0);

        // Middle
        List<EdgeInfo> edgesInfoMiddle = labelProvider.getBranchEdgeInfos(lineId, BranchEdge.LINE_EDGE);
        assertEquals(1, edgesInfoMiddle.size());
        assertEquals(lineId, edgesInfoMiddle.getFirst().getLabelA().orElseThrow());
        assertEquals("105.1 %", edgesInfoMiddle.getFirst().getLabelB().orElseThrow());

        // Side 1
        List<EdgeInfo> edgeInfosSide1 = labelProvider.getBranchEdgeInfos(lineId, BranchEdge.Side.ONE, BranchEdge.LINE_EDGE);
        assertEquals(2, edgeInfosSide1.size());
        EdgeInfo firstEdgeInfoSide1 = edgeInfosSide1.getFirst();
        assertEquals("1,400.0", firstEdgeInfoSide1.getLabelA().orElseThrow());
        assertTrue(firstEdgeInfoSide1.getLabelB().isEmpty());
        EdgeInfo secondEdgeInfoSide1 = edgeInfosSide1.get(1);
        assertEquals("400.0", secondEdgeInfoSide1.getLabelA().orElseThrow());
        assertTrue(secondEdgeInfoSide1.getLabelB().isEmpty());

        // Side 2
        List<EdgeInfo> edgeInfosSide2 = labelProvider.getBranchEdgeInfos(lineId, BranchEdge.Side.TWO, BranchEdge.LINE_EDGE);
        assertEquals(2, edgeInfosSide2.size());
        EdgeInfo firstEdgeInfoSide2 = edgeInfosSide2.get(0);
        assertEquals("1,410.0", firstEdgeInfoSide2.getLabelA().orElseThrow());
        assertTrue(firstEdgeInfoSide2.getLabelB().isEmpty());
        EdgeInfo secondEdgeInfoSide2 = edgeInfosSide2.get(1);
        assertEquals("410.0", secondEdgeInfoSide2.getLabelA().orElseThrow());
        assertTrue(secondEdgeInfoSide2.getLabelB().isEmpty());
    }
}
