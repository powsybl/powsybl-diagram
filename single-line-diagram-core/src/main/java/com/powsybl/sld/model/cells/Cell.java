/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.cells;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutContext;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.nodes.Node;

import java.io.IOException;
import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface Cell {
    enum CellType {
        INTERN, EXTERN, SHUNT;
    }

    List<Node> getNodes();

    void setType(CellType type);

    CellType getType();

    Block getRootBlock();

    default void blockSizing() {
        getRootBlock().sizing();
    }

    void setRootBlock(Block rootBlock);

    int getNumber();

    void calculateCoord(LayoutParameters layoutParam, LayoutContext layoutContext);

    double calculateHeight(LayoutParameters layoutParam);

    void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException;

    String getId();

    String getFullId();

}
