/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.sld.coordinate.Position.Dimension.*;
import static com.powsybl.sld.coordinate.Side.*;
import static com.powsybl.sld.model.Cell.CellType.*;
import static com.powsybl.sld.model.Node.NodeType.*;

import com.powsybl.sld.coordinate.Position;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExternCell extends AbstractBusCell {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternCell.class);

    private ShuntCell shuntCell = null;

    public ExternCell(int cellNumber) {
        super(cellNumber, EXTERN);
    }

    public void organizeBlockDirections() {
        getRootBlock().setOrientation(getDirection().toOrientation());
    }

    @Override
    public int newHPosition(int hPosition) {
        int minHv;
        if (isShunted() && shuntCell.getSideCell(RIGHT) == this) {
            Position leftPos = shuntCell.getSidePosition(LEFT);
            minHv = Math.max(hPosition, leftPos.get(H) + leftPos.getSpan(H) + shuntCell.getLength());
        } else {
            minHv = hPosition;
        }
        Position pos = getRootBlock().getPosition();
        pos.set(H, minHv);
        pos.set(V, 0);
        return minHv + pos.getSpan(H);
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

    @Override
    public String toString() {
        return getType() + " " + getOrder() + " " + getDirection() + " " + nodes;
    }

    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
        getNodes().stream().filter(f -> f.getType() == FEEDER)
                .map(FeederNode.class::cast)
                .forEach(fn -> {
                    if (fn.getOrientation() == null || !fn.getOrientation().isHorizontal()) {
                        fn.setOrientation(direction.toOrientation());
                        fn.setDirection(direction);
                    }
                });
    }

}
