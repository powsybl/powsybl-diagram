/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExternCell extends AbstractBusCell {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternCell.class);

    private int order = -1;
    private ShuntCell shuntCell = null;

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

    public void organizeBlockDirections() {
        List<FeederNode> feederNodes = getNodes().stream()
                .filter(n -> n.getType() == Node.NodeType.FEEDER)
                .map(FeederNode.class::cast).collect(Collectors.toList());
        if (feederNodes.stream().anyMatch(n -> n.getOrientation().isHorizontal())) {
            identifyHorizontalBlocks(feederNodes);
        } else {
            getRootBlock().setOrientation(getDirection().toOrientation());
        }
    }

    private void identifyHorizontalBlocks(List<FeederNode> fn) {
        List<Block> blocksEmbeddingOnlyHFeederNodes = fn.stream().filter(n -> n.getOrientation().isHorizontal())
                .flatMap(n -> getRootBlock().findBlockEmbeddingNode(n).stream())
                .filter(n -> !n.getType().isLeg())      //legBlocks remain always VERTICAL
                .collect(Collectors.toList());
        List<Block> blocksEmbeddingVNodes = fn.stream().filter(n -> n.getOrientation().isVertical())
                .flatMap(n -> getRootBlock().findBlockEmbeddingNode(n).stream()).collect(Collectors.toList());
        blocksEmbeddingOnlyHFeederNodes.removeAll(blocksEmbeddingVNodes);
        blocksEmbeddingOnlyHFeederNodes.forEach(b -> b.setOrientation(Orientation.RIGHT));
    }

    @Override
    public void blockSizing() {
        getRootBlock().sizing();
    }

    @Override
    public int newHPosition(int hPosition) {
        int minHv;
        if (isShunted() && shuntCell.getSideCell(Side.RIGHT) == this) {
            Position leftPos = shuntCell.getSidePosition(Side.LEFT);
            minHv = Math.max(hPosition, leftPos.getH() + leftPos.getHSpan() + shuntCell.getLength());
        } else {
            minHv = hPosition;
        }
        getRootBlock().getPosition().setHV(minHv, 0);
        return minHv + getRootBlock().getPosition().getHSpan();
    }

    public boolean isShunted() {
        return shuntCell != null;
    }

    public ShuntCell getShuntCell() {
        return shuntCell;
    }

    public void setShuntCell(ShuntCell shuntCell) {
        this.shuntCell = shuntCell;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return getType() + " " + order + " " + getDirection() + " " + nodes;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);
        generator.writeNumberField("order", order);
    }
}
