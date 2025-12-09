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

        PowsyblException exception = assertThrows(PowsyblException.class, () -> labelProvider.getInjectionEdgeInfo("UNKNOWN"));
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
        Optional<EdgeInfo> optionalEdgeInfoMiddle = labelProvider.getBranchEdgeInfo(lineId, BranchEdge.LINE_EDGE);
        assertTrue(optionalEdgeInfoMiddle.isPresent());
        EdgeInfo middleEdgeInfo = optionalEdgeInfoMiddle.get();
        assertEquals(lineId, middleEdgeInfo.getLabelA().orElseThrow());
        assertEquals("105.1 %", middleEdgeInfo.getLabelB().orElseThrow());

        // Side 1
        Optional<EdgeInfo> optionalEdgeInfoSide1 = labelProvider.getBranchEdgeInfo(lineId, BranchEdge.Side.ONE, BranchEdge.LINE_EDGE);
        assertTrue(optionalEdgeInfoSide1.isPresent());
        EdgeInfo edgeInfoSide1 = optionalEdgeInfoSide1.get();
        assertEquals("1,400.0", edgeInfoSide1.getLabelA().orElseThrow());
        assertTrue(edgeInfoSide1.getLabelB().isEmpty());

        // Side 2
        Optional<EdgeInfo> optionalEdgeInfoSide2 = labelProvider.getBranchEdgeInfo(lineId, BranchEdge.Side.TWO, BranchEdge.LINE_EDGE);
        assertTrue(optionalEdgeInfoSide2.isPresent());
        EdgeInfo edgeInfoSide2 = optionalEdgeInfoSide2.get();
        assertEquals("1,410.0", edgeInfoSide2.getLabelA().orElseThrow());
        assertTrue(edgeInfoSide1.getLabelB().isEmpty());
    }
}
