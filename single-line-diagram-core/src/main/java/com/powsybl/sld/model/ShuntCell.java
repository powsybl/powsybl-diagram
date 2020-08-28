/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ShuntCell extends AbstractCell {
    private ExternCell cell1;
    private ExternCell cell2;

    public ShuntCell(Graph graph) {
        super(graph, CellType.SHUNT);
    }

    public static ShuntCell create(ExternCell cell1, ExternCell cell2, List<Node> nodes) {
        ShuntCell shuntCell = new ShuntCell(cell1.getGraph());
        shuntCell.cell1 = cell1;
        shuntCell.cell2 = cell2;
        shuntCell.addNodes(nodes);
        return shuntCell;
    }

    public void calculateCoord(LayoutParameters layoutParam) {
        if (getRootBlock() instanceof BodyPrimaryBlock) {
            ((BodyPrimaryBlock) getRootBlock()).coordShuntCase();
        } else {
            throw new PowsyblException("ShuntCell can only be composed of a single BodyPrimaryBlock");
        }
    }

    public ExternCell getCell1() {
        return cell1;
    }

    public ExternCell getCell2() {
        return cell2;
    }

    public List<ExternCell> getCells() {
        return Arrays.asList(cell1, cell2);
    }

    public List<BusNode> getParentBusNodes() {
        return getCells().stream().flatMap(c -> c.getBusNodes().stream()).collect(Collectors.toList());
    }


    @Override
    public String toString() {
        return "ShuntCell(" + nodes + " )";
    }
}
