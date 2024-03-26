/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.cells;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.blocks.*;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public abstract class AbstractBusCell extends AbstractCell implements BusCell {

    private final List<LegPrimaryBlock> legPrimaryBlocks = new ArrayList<>();
    private final List<FeederPrimaryBlock> feederPrimaryBlocks = new ArrayList<>();

    private Integer order = null;

    private Direction direction = Direction.UNDEFINED;

    protected AbstractBusCell(int cellIndex, CellType type, Collection<Node> nodes) {
        super(cellIndex, type, nodes);
    }

    @Override
    public void blocksSetting(Block rootBlock, List<LegPrimaryBlock> primaryBlocksConnectedToBus, List<FeederPrimaryBlock> feederPrimaryBlocks) {
        setRootBlock(rootBlock);
        this.legPrimaryBlocks.addAll(primaryBlocksConnectedToBus);
        this.feederPrimaryBlocks.addAll(feederPrimaryBlocks);
    }

    @Override
    public List<BusNode> getBusNodes() {
        return nodes.stream()
                .filter(n -> n.getType() == Node.NodeType.BUS)
                .map(BusNode.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeederNode> getFeederNodes() {
        return nodes.stream()
                .filter(n -> n.getType() == Node.NodeType.FEEDER)
                .map(FeederNode.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Node> getInternalAdjacentNodes(Node node) {
        return node.getAdjacentNodes().stream()
                .filter(nodes::contains)
                .collect(Collectors.toList());
    }

    @Override
    public List<LegPrimaryBlock> getLegPrimaryBlocks() {
        return Collections.unmodifiableList(legPrimaryBlocks);
    }

    @Override
    public List<FeederPrimaryBlock> getFeederPrimaryBlocks() {
        return Collections.unmodifiableList(feederPrimaryBlocks);
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
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        if (includeCoordinates) {
            generator.writeStringField("direction", getDirection().name());
            if (order != null) {
                generator.writeNumberField("order", order);
            }
        }
    }

    public void removeOtherLegs(LegPrimaryBlock legPrimaryBlockKept) {
        removeOtherLegs(legPrimaryBlockKept, legPrimaryBlockKept);
    }

    public void removeOtherLegs(Block legKept, LegPrimaryBlock legPrimaryBlockKept) {
        if (feederPrimaryBlocks.isEmpty()
                || !(getRootBlock() instanceof SerialBlock serialBlock)) {
            return;
        }

        Block legBlock = serialBlock.getLowerBlock();
        Block feederBlock = serialBlock.getUpperBlock();
        Block body = serialBlock.extractBody(List.of(legBlock, feederBlock));

        setRootBlock(new SerialBlock(List.of(legKept, body, feederBlock)));

        legPrimaryBlocks.stream()
                .filter(l -> l != legPrimaryBlockKept)
                .flatMap(Block::getNodeStream)
                .filter(n -> !legPrimaryBlockKept.getNodes().contains(n))
                .forEach(nodes::remove);
        legPrimaryBlocks.clear();
        legPrimaryBlocks.add(legPrimaryBlockKept);
    }

    @Override
    public String toString() {
        return getType() + " " + direction + " " + nodes;
    }
}
