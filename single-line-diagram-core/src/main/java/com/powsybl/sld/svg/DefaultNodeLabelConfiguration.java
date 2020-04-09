/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultNodeLabelConfiguration implements NodeLabelConfiguration {

    private final ComponentLibrary componentLibrary;

    private static final double LABEL_OFFSET = 5d;
    private static final int FONT_SIZE = 8;

    public DefaultNodeLabelConfiguration(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
    }

    @Override
    public List<LabelPosition> getLabelsPosition(Node node) {
        Objects.requireNonNull(node);

        List<LabelPosition> res = new ArrayList<>();

        if (node instanceof FeederNode) {
            BusCell.Direction direction = node.getCell() != null
                    ? ((ExternCell) node.getCell()).getDirection()
                    : BusCell.Direction.UNDEFINED;

            double yShift = -LABEL_OFFSET;
            String positionName = "";
            if (node.getCell() != null) {
                yShift = direction == BusCell.Direction.TOP
                        ? -LABEL_OFFSET
                        : ((int) (componentLibrary.getSize(node.getComponentType()).getHeight()) + FONT_SIZE + LABEL_OFFSET);
                positionName = direction == BusCell.Direction.TOP ? "N" : "S";
            }

            res.add(new LabelPosition(node.getId() + "_" + positionName + "_LABEL", -LABEL_OFFSET, yShift));
        } else if (node instanceof BusNode) {
            res.add(new LabelPosition(node.getId() + "_NW_LABEL", -LABEL_OFFSET, -LABEL_OFFSET));
        }

        return res;
    }
}
