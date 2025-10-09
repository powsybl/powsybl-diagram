/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.cells;
import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.blocks.FeederPrimaryBlock;
import com.powsybl.sld.model.blocks.LegPrimaryBlock;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.*;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface BusCell extends Cell {

    List<BusNode> getBusNodes();

    List<FeederNode> getFeederNodes();

    List<Node> getInternalAdjacentNodes(Node node);

    void blocksSetting(Block rootBlock, List<LegPrimaryBlock> primaryBlocksConnectedToBus, List<FeederPrimaryBlock> feederPrimaryBlocks);

    List<LegPrimaryBlock> getLegPrimaryBlocks();

    List<FeederPrimaryBlock> getFeederPrimaryBlocks();

    int newHPosition(int hPosition);

    Optional<Integer> getOrder();

    void setOrder(int order);

    void removeOrder();

    void setDirection(Direction direction);

}
