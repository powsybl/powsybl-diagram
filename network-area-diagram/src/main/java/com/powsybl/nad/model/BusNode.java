/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BusNode extends AbstractNode {

    public static final BusNode UNKNOWN = new BusNode("", "");

    private int index;
    private int nbNeighbouringBusNodes;

    public BusNode(String diagramId, String id) {
        super(diagramId, id, null);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setNbNeighbouringBusNodes(int nbNeighbouringBusNodes) {
        this.nbNeighbouringBusNodes = nbNeighbouringBusNodes;
    }

    public int getNbNeighbouringBusNodes() {
        return nbNeighbouringBusNodes;
    }
}
