/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.cells;
import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.blocks.LegPrimaryBlock;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface BusCell extends Cell {

    List<BusNode> getBusNodes();

    void blocksSetting(Block rootBlock, List<LegPrimaryBlock> primaryBlocksConnectedToBus);

    List<LegPrimaryBlock> getLegPrimaryBlocks();

    int newHPosition(int hPosition);

    Optional<Integer> getOrder();

    void setOrder(int order);

    void removeOrder();

    Direction getDirection();

    void setDirection(Direction direction);

}
