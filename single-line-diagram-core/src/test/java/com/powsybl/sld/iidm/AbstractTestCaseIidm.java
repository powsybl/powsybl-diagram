/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractTestCaseIidm extends AbstractTestCase {

    protected Network network;
    protected VoltageLevel vl;
    protected Substation substation;
    protected GraphBuilder graphBuilder;

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    VoltageLevel getVl() {
        return vl;
    }

    Substation getSubstation() {
        return substation;
    }

    protected static Substation createSubstation(Network n, String id, String name, Country country) {
        return n.newSubstation()
                .setId(id)
                .setName(name)
                .setCountry(country)
                .add();
    }

    protected static VoltageLevel createVoltageLevel(Substation s, String id, String name,
                                                     TopologyKind topology, double vNom, int nodeCount) {
        VoltageLevel vl = s.newVoltageLevel()
                .setId(id)
                .setName(name)
                .setTopologyKind(topology)
                .setNominalV(vNom)
                .add();
        return vl;
    }

    protected static void createSwitch(VoltageLevel vl, String id, String name, SwitchKind kind, boolean retained, boolean open, boolean fictitious, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(name)
                .setKind(kind)
                .setRetained(retained)
                .setOpen(open)
                .setFictitious(fictitious)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    protected static void createBusBarSection(VoltageLevel vl, String id, String name, int node, int busbarIndex, int sectionIndex) {
        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection()
                .setId(id)
                .setName(name)
                .setNode(node)
                .add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs, busbarIndex, sectionIndex));
    }

    protected static void createLoad(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                     ConnectablePosition.Direction direction, int node, double p0, double q0) {
        Load load = vl.newLoad()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setP0(p0)
                .setQ0(q0)
                .add();
        load.addExtension(ConnectablePosition.class, new ConnectablePosition<>(load, new ConnectablePosition
                .Feeder(feederName, feederOrder, direction), null, null, null));
    }

    protected static void createGenerator(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                          ConnectablePosition.Direction direction, int node,
                                          double minP, double maxP, boolean voltageRegulator,
                                          double targetP, double targetQ) {
        Generator gen = vl.newGenerator()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulatorOn(voltageRegulator)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .add();
        gen.addExtension(ConnectablePosition.class, new ConnectablePosition<>(gen, new ConnectablePosition
                .Feeder(feederName, feederOrder, direction), null, null, null));
    }

    protected static void createShunt(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                      ConnectablePosition.Direction direction, int node,
                                      double bPerSection, int maximumSectionCount, int currentSectionCount) {
        ShuntCompensator shunt = vl.newShuntCompensator()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setbPerSection(bPerSection)
                .setMaximumSectionCount(maximumSectionCount)
                .setCurrentSectionCount(currentSectionCount)
                .add();
        shunt.addExtension(ConnectablePosition.class, new ConnectablePosition<>(shunt, new ConnectablePosition
                .Feeder(feederName, feederOrder, direction), null, null, null));
    }

    protected static void createTwoWindingsTransformer(Substation s, String id, String name,
                                                       double r, double x, double g, double b,
                                                       double ratedU1, double ratedU2,
                                                       int node1, int node2,
                                                       String idVoltageLevel1, String idVoltageLevel2,
                                                       String feederName1, int feederOrder1, ConnectablePosition.Direction direction1,
                                                       String feederName2, int feederOrder2, ConnectablePosition.Direction direction2) {
        TwoWindingsTransformer t = s.newTwoWindingsTransformer()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2)
                .setNode1(node1)
                .setVoltageLevel1(idVoltageLevel1)
                .setNode2(node2)
                .setVoltageLevel2(idVoltageLevel2)
                .add();
        t.addExtension(ConnectablePosition.class,
                new ConnectablePosition<>(t,
                        null,
                        new ConnectablePosition.Feeder(feederName1, feederOrder1, direction1),
                        new ConnectablePosition.Feeder(feederName2, feederOrder2, direction2),
                        null));
    }

    protected static void createThreeWindingsTransformer(Substation s, String id, String name,
                                                         String vl1, String vl2, String vl3,
                                                         double r1, double r2, double r3,
                                                         double x1, double x2, double x3,
                                                         double g1, double b1,
                                                         double ratedU1, double ratedU2, double ratedU3,
                                                         int node1, int node2, int node3,
                                                         String feederName1, int feederOrder1, ConnectablePosition.Direction direction1,
                                                         String feederName2, int feederOrder2, ConnectablePosition.Direction direction2,
                                                         String feederName3, int feederOrder3, ConnectablePosition.Direction direction3) {
        ThreeWindingsTransformer t = s.newThreeWindingsTransformer()
                .setId(id)
                .setName(name)
                .newLeg1()
                .setR(r1)
                .setX(x1)
                .setG(g1)
                .setB(b1)
                .setRatedU(ratedU1)
                .setVoltageLevel(vl1)
                .setNode(node1)
                .add()
                .newLeg2()
                .setR(r2)
                .setX(x2)
                .setRatedU(ratedU2)
                .setVoltageLevel(vl2)
                .setNode(node2)
                .add()
                .newLeg3()
                .setR(r3)
                .setX(x3)
                .setRatedU(ratedU3)
                .setVoltageLevel(vl3)
                .setNode(node3)
                .add()
                .add();

        t.addExtension(ConnectablePosition.class,
                new ConnectablePosition<>(t,
                        null,
                        new ConnectablePosition.Feeder(feederName1, feederOrder1, direction1),
                        new ConnectablePosition.Feeder(feederName2, feederOrder2, direction2),
                        new ConnectablePosition.Feeder(feederName3, feederOrder3, direction3)));
    }

    protected static void createLine(Network network,
                                     String id, String name,
                                     double r, double x,
                                     double g1, double b1,
                                     double g2, double b2,
                                     int node1, int node2,
                                     String idVoltageLevel1, String idVoltageLevel2,
                                     String feederName1, int feederOrder1, ConnectablePosition.Direction direction1,
                                     String feederName2, int feederOrder2, ConnectablePosition.Direction direction2) {
        Line line = network.newLine()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2)
                .setNode1(node1)
                .setVoltageLevel1(idVoltageLevel1)
                .setNode2(node2)
                .setVoltageLevel2(idVoltageLevel2)
                .add();
        line.addExtension(ConnectablePosition.class,
                new ConnectablePosition<>(line,
                        null,
                        new ConnectablePosition.Feeder(feederName1, feederOrder1, direction1),
                        new ConnectablePosition.Feeder(feederName2, feederOrder2, direction2),
                        null));
    }

}
