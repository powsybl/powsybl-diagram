/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.*;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
public final class LayoutNetworkFactory {

    private LayoutNetworkFactory() {
        // Empty
    }

    public static Network createDiamond() {
        Network network = com.powsybl.iidm.network.NetworkFactory.findDefault().createNetwork("diamond", "manual");
        network.setName("diamond");

        Substation subA = network.newSubstation().setId("A").add();
        Bus subA400 = createBus(subA, 400);
        Bus subA230 = createBus(subA, 230);
        createTransformer(subA400, subA230);

        Substation subB = network.newSubstation().setId("B").add();
        Bus subB230 = createBus(subB, 230);
        createLine(subA230, subB230);

        Substation subC = network.newSubstation().setId("C").add();
        Bus subC230 = createBus(subC, 230);
        Bus subC66 = createBus(subC, 66);
        Bus subC20 = createBus(subC, 20);
        createTransformer(subC230, subC66);
        createTransformer(subC66, subC20);
        createLine(subB230, subC230);

        Substation subD = network.newSubstation().setId("D").add();
        Bus subD66 = createBus(subD, 66);
        Bus subD10 = createBus(subD, 10);
        createTransformer(subD66, subD10);
        createLine(subC66, subD66);

        Substation subE = network.newSubstation().setId("E").add();
        Bus subE10 = createBus(subE, 10);
        createLine(subD10, subE10);

        Bus subF10 = createBus(network, "F", 10);
        Bus subG10 = createBus(network, "G", 10);
        Bus subH10 = createBus(network, "H", 10);
        Bus subI10 = createBus(network, "I", 10);
        Bus subJ10 = createBus(network, "J", 10);
        Bus subK10 = createBus(network, "K", 10);

        createLine(subE10, subF10);
        createLine(subF10, subG10);
        createLine(subG10, subH10);
        createLine(subH10, subD10);

        createLine(subF10, subI10);
        createLine(subI10, subJ10);
        createLine(subJ10, subK10);
        createLine(subK10, subD10);

        return network;
    }

    private static Bus createBus(Network network, String substationId, double nominalVoltage) {
        Substation substation = network.newSubstation().setId(substationId).add();
        return createBus(substation, nominalVoltage);
    }

    private static Bus createBus(Substation substation, double nominalVoltage) {
        String vlId = String.format("%s %.0f", substation.getId(), nominalVoltage);
        String busId = String.format("%s %s", vlId, "Bus");
        return substation.newVoltageLevel()
                .setId(vlId)
                .setNominalV(nominalVoltage)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add()
                .getBusBreakerView()
                .newBus()
                .setId(busId)
                .add();
    }

    private static void createTransformer(Bus bus1, Bus bus2) {
        Substation substation = bus1.getVoltageLevel().getSubstation().orElseThrow();
        String id = String.format("%s %.0f %.0f",
                substation.getId(),
                bus1.getVoltageLevel().getNominalV(),
                bus2.getVoltageLevel().getNominalV());
        substation.newTwoWindingsTransformer().setId(id)
                .setR(0.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setVoltageLevel1(bus1.getVoltageLevel().getId())
                .setVoltageLevel2(bus2.getVoltageLevel().getId())
                .setConnectableBus1(bus1.getId())
                .setConnectableBus2(bus2.getId())
                .setRatedU1(bus1.getVoltageLevel().getNominalV())
                .setRatedU2(bus2.getVoltageLevel().getNominalV())
                .setBus1(bus1.getId())
                .setBus2(bus2.getId())
                .add();
    }

    private static void createLine(Bus bus1, Bus bus2) {
        String id = String.format("%s - %s",
                bus1.getVoltageLevel().getSubstation().orElseThrow().getId(),
                bus2.getVoltageLevel().getSubstation().orElseThrow().getId());
        bus1.getNetwork().newLine().setId(id)
                .setR(0.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setVoltageLevel1(bus1.getVoltageLevel().getId())
                .setVoltageLevel2(bus2.getVoltageLevel().getId())
                .setConnectableBus1(bus1.getId())
                .setConnectableBus2(bus2.getId())
                .setBus1(bus1.getId())
                .setBus2(bus2.getId())
                .add();
    }
}
