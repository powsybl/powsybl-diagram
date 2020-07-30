/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;

import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InternCellSide {

    private InternCell cell;
    private Side side;

    InternCellSide(InternCell cell, Side side) {
        this.cell = cell;
        this.side = side;
    }

    public InternCell getCell() {
        return cell;
    }

    public void setCell(InternCell cell) {
        this.cell = cell;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void flipSide() {
        side = side.getFlip();
    }

    public static void identifyVerticalInternCells(Collection<InternCellSide> internCellSides) {
        Map<InternCell, List<InternCellSide>> cellToCellSides = new LinkedHashMap<>();
        internCellSides.forEach(internCellSide -> {
            cellToCellSides.putIfAbsent(internCellSide.getCell(), new ArrayList<>());
            cellToCellSides.get(internCellSide.getCell()).add(internCellSide);
        });
        cellToCellSides.entrySet().stream().filter(entry -> entry.getValue().size() == 2) // both LEFT and RIGHT side of the interncell
                .forEach(entry -> {
                    InternCellSide ics = entry.getValue().get(0);
                    ics.getCell().setShape(InternCell.Shape.VERTICAL);
                    ics.setSide(Side.UNDEFINED);
                    internCellSides.remove(entry.getValue().get(1));
                });
    }
}
