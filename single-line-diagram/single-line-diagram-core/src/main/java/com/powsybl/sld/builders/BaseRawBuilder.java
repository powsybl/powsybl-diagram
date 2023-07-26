/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.FeederNode;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public interface BaseRawBuilder {

    Map<VoltageLevelRawBuilder, FeederNode> createLine(String id,
                                                       List<VoltageLevelRawBuilder> vls,
                                                       List<Integer> orders,
                                                       List<Direction> directions);

    Map<VoltageLevelRawBuilder, FeederNode> createLine(String id,
                                                       VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2);
}
