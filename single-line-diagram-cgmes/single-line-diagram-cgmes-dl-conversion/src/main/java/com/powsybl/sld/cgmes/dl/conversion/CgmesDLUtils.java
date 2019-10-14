/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import com.powsybl.triplestore.api.TripleStore;

import java.util.Objects;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class CgmesDLUtils {
    private CgmesDLUtils() {
    }

    //remove all the iidm cgmes extensions
    public static void removeIidmCgmesExtensions(Network network) {
        Objects.requireNonNull(network);
        network.getHvdcLineStream().forEach(l -> l.removeExtension(LineDiagramData.class));
        network.getThreeWindingsTransformerStream().forEach(t -> t.removeExtension(ThreeWindingsTransformerDiagramData.class));
        network.getTwoWindingsTransformerStream().forEach(t -> t.removeExtension(CouplingDeviceDiagramData.class));
        network.getStaticVarCompensatorStream().forEach(t -> t.removeExtension(InjectionDiagramData.class));
        network.getShuntCompensatorStream().forEach(t -> t.removeExtension(InjectionDiagramData.class));
        network.getLoadStream().forEach(t -> t.removeExtension(InjectionDiagramData.class));
        network.getGeneratorStream().forEach(t -> t.removeExtension(InjectionDiagramData.class));
        network.getDanglingLineStream().forEach(t -> t.removeExtension(LineDiagramData.class));
        network.getLineStream().forEach(t -> t.removeExtension(LineDiagramData.class));
        network.getVoltageLevelStream().forEach(voltageLevel -> {
            switch (voltageLevel.getTopologyKind()) {
                case NODE_BREAKER:
                    voltageLevel.getNodeBreakerView().getBusbarSectionStream().forEach(busBarSection -> busBarSection.removeExtension(NodeDiagramData.class));
                    break;
                case BUS_BREAKER:
                    voltageLevel.getBusBreakerView().getBusStream().forEach(bus -> bus.removeExtension(NodeDiagramData.class));
                    break;
                default:
                    throw new AssertionError("Unexpected topology kind: " + voltageLevel.getTopologyKind());
            }
        });
        network.getSwitchStream().forEach(sw -> sw.removeExtension(CouplingDeviceDiagramData.class));
        network.removeExtension(NetworkDiagramData.class);
    }

    //retrieve, if exists, the tripleStore currently available from the CGMES model in the IIDM CGMES extensions
    public static TripleStore getTripleStore(Network network) {
        Objects.requireNonNull(network);
        CgmesModelExtension ext = network.getExtension(CgmesModelExtension.class);
        return (ext != null) ? ext.getCgmesModel().tripleStore() : null;
    }

    //remove, if exists, the CGMES DL profile data from the network's CGMES tiplestore
    public static void clearCgmesDl(Network network) {
        Objects.requireNonNull(network);
        TripleStore tStore = getTripleStore(network);
        if (tStore != null) {
            tStore.contextNames().stream().filter(CgmesSubset.DIAGRAM_LAYOUT::isValidName).findFirst().ifPresent(tStore::clear);
        }
    }
}
