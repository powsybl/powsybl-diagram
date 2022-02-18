/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import java.util.List;

import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.ZoneGraph;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface GraphBuilder {

    VoltageLevelGraph buildVoltageLevelGraph(String id,
                                             Graph parentGraph);

    VoltageLevelGraph buildOrphanVoltageLevelGraph(String id);

    SubstationGraph buildSubstationGraph(String id, ZoneGraph parentGraph);

    SubstationGraph buildOrphanSubstationGraph(String id);

    ZoneGraph buildZoneGraph(List<String> substationIds);

}
