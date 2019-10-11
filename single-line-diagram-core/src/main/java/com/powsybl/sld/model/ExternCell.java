/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExternCell extends AbstractBusCell {
    private int order = -1;

    public ExternCell(Graph graph) {
        super(graph, CellType.EXTERN);
    }

    public void orderFromFeederOrders() {
        int sumOrder = 0;
        int nbFeeder = 0;
        for (FeederNode node : getNodes().stream()
                .filter(node -> node.getType() == Node.NodeType.FEEDER)
                .map(node -> (FeederNode) node).collect(Collectors.toList())) {
            sumOrder += node.getOrder();
            nbFeeder++;
        }
        if (nbFeeder != 0) {
            setOrder(sumOrder / nbFeeder);
        }
    }

    @Override
    public void blockSizing() {
        getRootBlock().sizing();
    }

    @Override
    public int newHPosition(int hPosition) {
        getRootBlock().getPosition().setHV(hPosition, 0);
        return hPosition + getRootBlock().getPosition().getHSpan();
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "ExternCell(order=" + order + ", direction=" + getDirection() + ", nodes=" + nodes + ")";
    }
}
