/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg.styles;

import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Direction;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class StyleClassConstants {
    public static final String STYLE_PREFIX = "sld-";
    public static final String FEEDER_INFO = STYLE_PREFIX + "feeder-info";
    public static final String ANGLE = STYLE_PREFIX + "angle";
    public static final String VOLTAGE = STYLE_PREFIX + "voltage";
    public static final String LEGEND = STYLE_PREFIX + "legend";
    public static final String CELL_DIRECTION_PREFIX = STYLE_PREFIX + "cell-direction-";
    public static final String CELL_SHAPE_PREFIX = STYLE_PREFIX + "cell-shape-";
    public static final String SHUNT_CELL = STYLE_PREFIX + "shunt-cell";
    public static final String INTERN_CELL = STYLE_PREFIX + "intern-cell";
    public static final String EXTERN_CELL = STYLE_PREFIX + "extern-cell";
    public static final String FICTITIOUS_NODE_STYLE_CLASS = STYLE_PREFIX + "fictitious";
    public static final String OVERLOAD_STYLE_CLASS = STYLE_PREFIX + "overload";
    public static final String VL_OVERVOLTAGE_CLASS = STYLE_PREFIX + "vl-overvoltage";
    public static final String VL_UNDERVOLTAGE_CLASS = STYLE_PREFIX + "vl-undervoltage";
    public static final String NODE_INFOS = STYLE_PREFIX + "node-infos";
    public static final String FRAME_CLASS = STYLE_PREFIX + "frame";
    public static final String TOP_FEEDER = STYLE_PREFIX + "top-feeder";
    public static final String BOTTOM_FEEDER = STYLE_PREFIX + "bottom-feeder";
    public static final String BUS_DISCONNECTED = STYLE_PREFIX + "bus-disconnected";
    public static final String FEEDER_DISCONNECTED_CONNECTED = STYLE_PREFIX + "feeder-disconnected-connected";
    public static final String FEEDER_CONNECTED_DISCONNECTED = STYLE_PREFIX + "feeder-connected-disconnected";
    public static final String FEEDER_DISCONNECTED = STYLE_PREFIX + "feeder-disconnected";
    public static final String TIE_LINE = STYLE_PREFIX + "tie-line";
    public static final String DANGLING_LINE = STYLE_PREFIX + "dangling-line";
    public static final String CLOSED_SWITCH_STYLE_CLASS = STYLE_PREFIX + "closed";
    public static final String OPEN_SWITCH_STYLE_CLASS = STYLE_PREFIX + "open";
    public static final String DISCONNECTED_STYLE_CLASS = STYLE_PREFIX + "disconnected";
    public static final String HIDDEN_NODE_CLASS = STYLE_PREFIX + "hidden-node";
    public static final String IN_CLASS = STYLE_PREFIX + "in";
    public static final String OUT_CLASS = STYLE_PREFIX + "out";
    public static final String GRAPH_LABEL_STYLE_CLASS = STYLE_PREFIX + "graph-label";
    public static final String LABEL_STYLE_CLASS = STYLE_PREFIX + "label";
    public static final String GRID_STYLE_CLASS = STYLE_PREFIX + "grid";
    public static final String WIRE_STYLE_CLASS = STYLE_PREFIX + "wire";

    private StyleClassConstants() {
    }

    public static String buildStyle(Direction direction) {
        return StyleClassConstants.CELL_DIRECTION_PREFIX + direction.name().toLowerCase();
    }

    public static String buildStyle(InternCell.Shape shape) {
        return StyleClassConstants.CELL_SHAPE_PREFIX + shape.name().toLowerCase();
    }
}
