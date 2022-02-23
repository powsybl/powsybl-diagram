/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;

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

    protected AbstractBusCell(int cellIndex, CellType type, List<Node> nodes) {
        super(cellIndex, type, nodes);
    }

    @Override
    public void blocksSetting(Block rootBlock, List<LegPrimaryBlock> primaryBlocksConnectedToBus) {
        setRootBlock(rootBlock);
        this.legPrimaryBlocks = new ArrayList<>(primaryBlocksConnectedToBus);
    }

    @Override
    public List<BusNode> getBusNodes() {
        return nodes.stream()
                .filter(n -> n.getType() == Node.NodeType.BUS)
                .map(BusNode.class::cast)
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
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void removeOrder() {
        this.order = null;
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
    public void calculateCoord(LayoutParameters layoutParam, double firstBusY, double lastBusY, double externCellHeight) {
        getRootBlock().calculateRootCoord(layoutParam, firstBusY, lastBusY, externCellHeight);
    }

    @Override
    public double calculateHeight(LayoutParameters layoutParam) {
        return getRootBlock().calculateRootHeight(layoutParam);
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        if (includeCoordinates) {
            generator.writeStringField("direction", getDirection().name());
            if (order != null) {
                generator.writeNumberField("order", order);
            }
        }
    }

    @Override
    public String toString() {
        return getType() + " " + direction + " " + nodes;
    }
}
