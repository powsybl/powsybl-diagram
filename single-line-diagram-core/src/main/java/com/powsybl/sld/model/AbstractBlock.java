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
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractBlock implements Block {

    protected final Type type;

    private Map<Extremity, Integer> cardinality;

    private Block parentBlock;

    private Cell cell;

    private Position position;

    private Coord coord;

    /**
     * Constructor for primary layout.block with the list of nodes corresponding to the
     * layout.block
     */
    AbstractBlock(Type type) {
        cardinality = new EnumMap<>(Extremity.class);
        cardinality.put(Extremity.START, 0);
        cardinality.put(Extremity.END, 0);
        this.type = Objects.requireNonNull(type);
        position = new Position(-1, -1);
        coord = new Coord(-1, -1);
    }

    @Override
    public Node getStartingNode() {
        return getExtremityNode(Extremity.START);
    }

    @Override
    public Node getEndingNode() {
        return getExtremityNode(Extremity.END);
    }

    @Override
    public Extremity getExtremity(Node node) {
        if (node.equals(getExtremityNode(Extremity.START))) {
            return Extremity.START;
        }
        if (node.equals(getExtremityNode(Extremity.END))) {
            return Extremity.END;
        }
        return Extremity.NONE;
    }

    @Override
    public int getCardinality(Node node) {
        return getCardinality(getExtremity(node));
    }

    @Override
    public int getCardinality(Extremity extremity) {
        return cardinality.get(extremity);
    }

    @Override
    public void setCardinality(Extremity extremity, int i) {
        cardinality.put(extremity, i);
    }

    public Block getParentBlock() {
        return parentBlock;
    }

    @Override
    public void setParentBlock(Block parentBlock) {
        this.parentBlock = parentBlock;
    }

    @Override
    public Cell getCell() {
        return cell;
    }

    @Override
    public void setCell(Cell cell) {
        this.cell = cell;
        if (cell == null) {
            return;
        }
        if (cell.getType() == Cell.CellType.SHUNT) {
            setOrientation(Orientation.HORIZONTAL);
        } else {
            setOrientation(Orientation.VERTICAL);
        }
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setOrientation(Orientation orientation) {
        getPosition().setOrientation(orientation);
    }

    @Override
    public Coord getCoord() {
        return coord;
    }

    @Override
    public void setXSpan(double xSpan) {
        getCoord().setXSpan(xSpan);
    }

    @Override
    public void setYSpan(double ySpan) {
        getCoord().setYSpan(ySpan);
    }

    @Override
    public void setX(double x) {
        getCoord().setX(x);
    }

    @Override
    public void setY(double y) {
        getCoord().setY(y);
    }

    @Override
    public void calculateCoord(LayoutParameters layoutParam) {
        if (getPosition().getOrientation() == Orientation.VERTICAL) {
            coordVerticalCase(layoutParam);
        } else {
            coordHorizontalCase(layoutParam);
        }
    }

    @Override
    public void calculateRootCoord(LayoutParameters layoutParam) {
        double dyToBus = 0;
        coord.setXSpan((double) position.getHSpan() * layoutParam.getCellWidth());
        if (cell.getType() == Cell.CellType.INTERN) {
            coord.setYSpan(0);
            if (((InternCell) cell).getDirection() != BusCell.Direction.FLAT) {
                dyToBus = layoutParam.getInternCellHeight() * position.getV();
            }
        } else {
            coord.setYSpan(layoutParam.getExternCellHeight());
            dyToBus = layoutParam.getExternCellHeight() / 2 + layoutParam.getStackHeight();
        }

        coord.setX(layoutParam.getInitialXBus()
                + layoutParam.getCellWidth() * position.getH()
                + coord.getXSpan() / 2);

        switch (((BusCell) cell).getDirection()) {
            case BOTTOM:
                coord.setY(layoutParam.getInitialYBus()
                        + (((BusCell) cell).getMaxBusPosition().getV() - 1) * layoutParam.getVerticalSpaceBus()
                        + dyToBus);
                break;
            case TOP:
                coord.setY(layoutParam.getInitialYBus()
                        - dyToBus);
                break;
            case FLAT:
                coord.setY(layoutParam.getInitialYBus()
                        + (getPosition().getV() - 1) * layoutParam.getVerticalSpaceBus());
                break;
            default:
        }
        calculateCoord(layoutParam);
    }

    @Override
    public Block.Type getType() {
        return this.type;
    }

    protected abstract void writeJsonContent(JsonGenerator generator) throws IOException;

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("type", type.name());
        generator.writeArrayFieldStart("cardinalities");
        for (Map.Entry<Extremity, Integer> ex : cardinality.entrySet()) {
            generator.writeStartObject();
            generator.writeObjectField(ex.getKey().name(), ex.getValue());
            generator.writeEndObject();
        }
        generator.writeEndArray();
        generator.writeFieldName("position");
        position.writeJsonContent(generator);
        if (getGraph().isGenerateCoordsInJson()) {
            generator.writeFieldName("coord");
            coord.writeJsonContent(generator);
        }
        writeJsonContent(generator);
        generator.writeEndObject();
    }
}
