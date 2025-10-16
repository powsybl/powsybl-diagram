/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BusNode extends AbstractNode {

    public static final BusNode UNKNOWN = new BusNode("", "", Collections.emptyList(), "");

    private int ringIndex;
    private int busIndex;
    private int nbNeighbouringBusNodes;
    private final List<Injection> injections = new ArrayList<>();
    private final String legend;

    public BusNode(String diagramId, String id, List<Injection> injections, String legend) {
        super(diagramId, id, null, false);
        this.injections.addAll(Objects.requireNonNull(injections));
        this.legend = legend;
    }

    public void setRingIndex(int ringIndex) {
        this.ringIndex = ringIndex;
    }

    public int getRingIndex() {
        return ringIndex;
    }

    public void setBusIndex(int busIndex) {
        this.busIndex = busIndex;
    }

    public int getBusIndex() {
        return busIndex;
    }

    public void setNbNeighbouringBusNodes(int nbNeighbouringBusNodes) {
        this.nbNeighbouringBusNodes = nbNeighbouringBusNodes;
    }

    public int getNbNeighbouringBusNodes() {
        return nbNeighbouringBusNodes;
    }

    public int getInjectionCount() {
        return injections.size();
    }

    public List<Injection> getInjections() {
        return Collections.unmodifiableList(injections);
    }

    public String getLegend() {
        return legend;
    }
}

