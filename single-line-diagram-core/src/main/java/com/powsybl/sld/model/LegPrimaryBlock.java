/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LegPrimaryBlock extends AbstractPrimaryBlock implements LegBlock {

    private final List<LegPrimaryBlock> stackableBlocks = new ArrayList<>();

    public LegPrimaryBlock(List<Node> nodes, Cell cell) {
        super(Type.LEGPRIMARY, nodes, cell);
        if (getExtremityNode(Extremity.END).getType() == Node.NodeType.BUS) {
            super.reverseBlock();
        }
        if (!checkConsistency()) {
            throw new PowsyblException("LegPrimaryBlock not consistent");
        }
    }

    private boolean checkConsistency() {
        return nodes.size() == 3
                && nodes.get(0).getType() == Node.NodeType.BUS
                && nodes.get(1).getType() == Node.NodeType.SWITCH
                && (nodes.get(2).getType() == Node.NodeType.FICTITIOUS
                || nodes.get(2).getType() == Node.NodeType.SHUNT);
    }

    public BusNode getBusNode() {
        return (BusNode) getExtremityNode(Extremity.START);
    }

    @Override
    public List<BusNode> getBusNodes() {
        return Collections.singletonList(getBusNode());
    }

    private SwitchNode getSwNode() {
        return (SwitchNode) nodes.get(1);
    }

    public void addStackableBlock(LegPrimaryBlock block) {
        stackableBlocks.add(block);
    }

    public List<LegPrimaryBlock> getStackableBlocks() {
        return new ArrayList<>(stackableBlocks);
    }

    @Override
    public void reverseBlock() {
        // Will never happen.
    }

    @Override
    public void sizing() {
        if (((BusCell) getCell()).getDirection() == BusCell.Direction.FLAT
                || !getStackableBlocks().isEmpty()) {
            getPosition().setHSpan(0);
            getPosition().setVSpan(0);
        } else {
            getPosition().setHSpan(1);
            getPosition().setVSpan(0);
        }
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        getSwNode().setX(getCoord().getX() + getCoord().getXSpan() / 2);
        getSwNode().setY(getBusNode().getY(), false);
        getLegNode().setY(getBusNode().getY(), false);
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        getSwNode().setX(getCoord().getX());
        getSwNode().setY(getBusNode().getY(), false);

        getLegNode().setX(getCoord().getX());
        if (getCell().getType() == Cell.CellType.INTERN && ((InternCell) getCell()).checkShape(InternCell.Shape.UNILEG)) {
            InternCell cell = (InternCell) getCell();
            if (cell.getDirection() == BusCell.Direction.TOP) {
                getLegNode().setY(layoutParam.getInitialYBus() - layoutParam.getInternCellHeight());
            } else {
                getLegNode().setY(layoutParam.getInitialYBus() + layoutParam.getInternCellHeight()
                        + (cell.getMaxBusPosition().getV() - 1) * layoutParam.getVerticalSpaceBus());
            }
        }
    }

}
