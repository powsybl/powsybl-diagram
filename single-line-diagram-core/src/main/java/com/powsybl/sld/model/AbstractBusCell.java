/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractBusCell extends AbstractCell implements BusCell {

    private List<LegPrimaryBlock> legPrimaryBlocks = new ArrayList<>();

    private Integer order = null;

    private Direction direction = Direction.UNDEFINED;

    protected AbstractBusCell(VoltageLevelGraph graph, CellType type) {
        super(graph, type);
    }

    public void averageOrder() {
        int sumOrder = 0;
        int nbFeeder = 0;
        for (Node node : getNodes()) {
            Optional<Integer> oOrder = node.getOrder();
            if (oOrder.isPresent()) {
                sumOrder += oOrder.get();
                nbFeeder++;
            }
        }
        if (nbFeeder != 0) {
            setOrder(sumOrder / nbFeeder);
        }
    }

    @Override
    public void blocksSetting(Block rootBlock, List<LegPrimaryBlock> primaryBlocksConnectedToBus) {
        setRootBlock(rootBlock);
        this.legPrimaryBlocks = new ArrayList<>(primaryBlocksConnectedToBus);
    }

    @Override
    public List<BusNode> getBusNodes() {
        return nodes.stream().filter(n -> n.getType() == Node.NodeType.BUS).map(BusNode.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<LegPrimaryBlock> getLegPrimaryBlocks() {
        return new ArrayList<>(legPrimaryBlocks);
    }

    @Override
    public Optional<Integer> getOrder() {
        return Optional.ofNullable(order);
    }

    @Override
    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void calculateCoord(LayoutParameters layoutParam) {
        getRootBlock().calculateRootCoord(layoutParam);
    }

    @Override
    public double calculateHeight(LayoutParameters layoutParam) {
        return getRootBlock().calculateRootHeight(layoutParam);
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);
        if (graph.isGenerateCoordsInJson()) {
            generator.writeStringField("direction", getDirection().name());
            Optional<Integer> oOrder = getOrder();
            if (oOrder.isPresent()) {
                generator.writeNumberField("order", oOrder.get());
            }
        }
    }

    @Override
    public String toString() {
        return getType() + " " + direction + " " + nodes;
    }
}
