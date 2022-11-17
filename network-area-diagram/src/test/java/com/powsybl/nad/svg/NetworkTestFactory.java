/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class NetworkTestFactory {

    private NetworkTestFactory() {
    }

    /**
     * <pre>
     *  g1     dl1
     *  |       |
     *  b1 ---- b2
     *      l1 </pre>
     */
    public static Network createTwoVoltageLevels() {
        Network network = Network.create("dl", "test");
        Substation s = network.newSubstation().setId("s1").setName("Substation 1").add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("vl1")
                .setName("Voltage level 1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setConnectableBus("b1")
                .setBus("b1")
                .setTargetP(101.3664)
                .setTargetV(390)
                .setMinP(0)
                .setMaxP(150)
                .setVoltageRegulatorOn(true)
                .add();
        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("vl2")
                .setName("Voltage level 2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newDanglingLine()
                .setId("dl1")
                .setConnectableBus("b2")
                .setBus("b2")
                .setR(0.7)
                .setX(1)
                .setG(1e-6)
                .setB(3e-6)
                .setP0(101)
                .setQ0(150)
                .newGeneration()
                .setTargetP(0)
                .setTargetQ(0)
                .setTargetV(390)
                .setVoltageRegulationOn(false)
                .add()
                .add();
        network.newLine()
                .setId("l1")
                .setVoltageLevel1("vl1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setBus2("b2")
                .setR(1)
                .setX(3)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        return network;
    }

    public static Network createTwoVoltageLevelsThreeBuses() {
        Network network = createTwoVoltageLevels();
        network.getVoltageLevel("vl1").getBusBreakerView().newBus()
                .setId("b0")
                .add();
        network.newLine()
                .setId("l2")
                .setVoltageLevel1("vl1")
                .setBus1("b0")
                .setVoltageLevel2("vl2")
                .setConnectableBus2("b2")
                .setR(1)
                .setX(3)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        return network;
    }

    public static Network createThreeVoltageLevelsFiveBuses() {

        Network network = createTwoVoltageLevelsThreeBuses();

        Substation s = network.getSubstation("s1");
        VoltageLevel vl3 = s.newVoltageLevel()
                .setId("vl3")
                .setNominalV(200)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b4")
                .add();
        vl3.newLoad()
                .setId("load3")
                .setBus("b3")
                .setP0(10.0)
                .setQ0(5.0)
                .add();

        s.newTwoWindingsTransformer()
                .setId("tr1")
                .setVoltageLevel1("vl1")
                .setBus1("b0")
                .setVoltageLevel2("vl3")
                .setBus2("b3")
                .setRatedU1(380)
                .setRatedU2(190)
                .setR(1)
                .setX(30)
                .setG(0)
                .setB(0)
                .add();
        s.newTwoWindingsTransformer()
                .setId("tr2")
                .setVoltageLevel1("vl2")
                .setBus1("b2")
                .setVoltageLevel2("vl3")
                .setBus2("b4")
                .setRatedU1(380)
                .setRatedU2(190)
                .setR(1)
                .setX(30)
                .setG(0)
                .setB(0)
                .add();

        return network;
    }

    /**
     * <pre>
     *   g1         dl1
     *   |    tr1    |
     *   |  --oo--   |
     *  b1 /      \ b2
     *     \      /
     *      --oo--
     *       tr2</pre>
     */
    public static Network createTwoVoltageLevelsTwoTransformers() {
        Network network = Network.create("dl", "test");
        Substation s = network.newSubstation().setId("s1").add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        network.getVoltageLevel("vl1").getBusBreakerView().newBus()
                .setId("b0")
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setConnectableBus("b1")
                .setBus("b1")
                .setTargetP(101.3664)
                .setTargetV(390)
                .setMinP(0)
                .setMaxP(150)
                .setVoltageRegulatorOn(true)
                .add();
        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("vl2")
                .setNominalV(200)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newDanglingLine()
                .setId("dl1")
                .setConnectableBus("b2")
                .setBus("b2")
                .setR(0.7)
                .setX(1)
                .setG(1e-6)
                .setB(3e-6)
                .setP0(101)
                .setQ0(150)
                .newGeneration()
                .setTargetP(0)
                .setTargetQ(0)
                .setTargetV(390)
                .setVoltageRegulationOn(false)
                .add()
                .add();
        s.newTwoWindingsTransformer()
                .setId("tr1")
                .setVoltageLevel1("vl1")
                .setBus1("b0")
                .setVoltageLevel2("vl2")
                .setBus2("b2")
                .setRatedU1(380)
                .setRatedU2(190)
                .setR(1)
                .setX(30)
                .setG(0)
                .setB(0)
                .add();
        s.newTwoWindingsTransformer()
                .setId("tr2")
                .setVoltageLevel1("vl1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setBus2("b2")
                .setRatedU1(380)
                .setRatedU2(190)
                .setR(1)
                .setX(30)
                .setG(0)
                .setB(0)
                .add();
        return network;
    }
}
