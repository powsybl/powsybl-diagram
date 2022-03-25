/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.cells;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.nodes.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public abstract class AbstractCell implements Cell {
    private CellType type;
    private int number;
    protected final List<Node> nodes = new ArrayList<>();

    private Block rootBlock;

    AbstractCell(int cellNumber, CellType type, Collection<Node> nodes) {
        this.type = Objects.requireNonNull(type);
        number = cellNumber;
        setNodes(nodes);
    }

    @Override
    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    private void setNodes(Collection<Node> nodes) {
        this.nodes.addAll(nodes);
    }

    @Override
    public void setType(CellType type) {
        this.type = type;
    }

    @Override
    public CellType getType() {
        return this.type;
    }

    @Override
    public Block getRootBlock() {
        return rootBlock;
    }

    @Override
    public void setRootBlock(Block rootBlock) {
        this.rootBlock = rootBlock;
    }

    @Override
    public int getNumber() {
        return number;
    }

    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeStringField("type", type.name());
        generator.writeNumberField("number", number);
    }

    @Override
    public void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeStartObject();
        writeJsonContent(generator, includeCoordinates);
        if (rootBlock != null) {
            generator.writeFieldName("rootBlock");
            rootBlock.writeJson(generator, includeCoordinates);
        }
        generator.writeEndObject();
    }

    @Override
    public String getId() {
        return type + " " + number;
    }

    @Override
    public String getFullId() {
        return type + nodes.stream().map(Node::getId).sorted().collect(Collectors.toList()).toString();
    }

    @Override
    public String toString() {
        return type + " " + nodes;
    }

    @Override
    public double calculateHeight(LayoutParameters layoutParam) {
        return 0.;
    }
}
