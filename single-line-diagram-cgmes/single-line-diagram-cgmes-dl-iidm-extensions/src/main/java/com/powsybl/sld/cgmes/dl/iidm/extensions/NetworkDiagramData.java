/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class NetworkDiagramData extends AbstractExtension<Network> {

    static final String NAME = "network-diagram-data";
    private Set<String> diagramsNames = new TreeSet<>();

    private NetworkDiagramData() {
    }

    private static NetworkDiagramData getNetworkDiagramData(Network network) {
        NetworkDiagramData networkDiagramData = network.getExtension(NetworkDiagramData.class);
        if (networkDiagramData == null) {
            networkDiagramData = new NetworkDiagramData();
        }
        return networkDiagramData;
    }

    public static void addDiagramName(Network network, String diagramName) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(diagramName);
        NetworkDiagramData networkDiagramData = getNetworkDiagramData(network);
        networkDiagramData.addDiagramName(diagramName);
        network.addExtension(NetworkDiagramData.class, networkDiagramData);
    }

    public static List<String> getDiagramsNames(Network network) {
        Objects.requireNonNull(network);
        return getNetworkDiagramData(network).getDiagramsNames();
    }

    public static boolean checkNetworkDiagramData(Network network) {
        Objects.requireNonNull(network);
        return network.getExtension(NetworkDiagramData.class) != null;
    }

    public static boolean containsDiagramName(Network network, String diagramName) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(diagramName);
        return checkNetworkDiagramData(network) && getNetworkDiagramData(network).diagramsNames.contains(diagramName);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private void addDiagramName(String diagramName) {
        diagramsNames.add(diagramName);
    }

    private List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsNames);
    }
}
