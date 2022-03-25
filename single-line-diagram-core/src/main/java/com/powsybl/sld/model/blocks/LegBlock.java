/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

import java.util.List;

import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FictitiousNode;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LegBlock extends Block {

    default FictitiousNode getLegNode() {
        return (FictitiousNode) getExtremityNode(Extremity.END);
    }

    List<BusNode> getBusNodes();
}
