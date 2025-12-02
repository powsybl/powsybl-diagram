package com.powsybl.diagram.util;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import static com.powsybl.diagram.util.PermanentLimitPercentageMax.getPermanentLimitPercentageMax;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class PermanentLimitPercentageMaxTest {

    @Test
    void testPermanentLimitPercentageMaxT3T() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();

        // Add current limits
        network.getThreeWindingsTransformer("3WT").getLeg1().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
            .setPermanentLimit(10000.0)
            .beginTemporaryLimit()
            .setName("20'")
            .setValue(12000.0)
            .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("10'")
            .setValue(14000.0)
            .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        network.getThreeWindingsTransformer("3WT").getLeg2().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
            .setPermanentLimit(50000.0)
            .beginTemporaryLimit()
            .setName("20'")
            .setValue(52000.0)
            .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("10'")
            .setValue(54000.0)
            .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        network.getThreeWindingsTransformer("3WT").getLeg3().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
            .setPermanentLimit(100000.0)
            .beginTemporaryLimit()
            .setName("20'")
            .setValue(120000)
            .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("10'")
            .setValue(140000.0)
            .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        // Add power values
        network.getThreeWindingsTransformer("3WT").getLeg1().getTerminal().setP(1400.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg2().getTerminal().setP(1400.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg3().getTerminal().setP(1400.0).setQ(400.0);

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");

        double percentage = getPermanentLimitPercentageMax(twt);
        assertEquals(71.35512, percentage, 0.0001);
    }

    @Test
    void testPermanentLimitPercentageMaxBranch() {
        Network network = Networks.createTwoVoltageLevels();
        Line line = network.getLine("l1");

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

        // Add power values
        line.getTerminal1().setP(1400.0).setQ(400.0);
        line.getTerminal2().setP(1410.0).setQ(410.0);

        // Add voltage values
        line.getTerminal1().getBusBreakerView().getBus().setV(400.0);
        line.getTerminal2().getBusBreakerView().getBus().setV(410.0);

        double percentage = getPermanentLimitPercentageMax(line);
        assertEquals(105.07933, percentage, 0.0001);

    }
}
