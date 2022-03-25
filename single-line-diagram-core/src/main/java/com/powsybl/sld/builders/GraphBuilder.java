/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import java.util.List;

import com.powsybl.sld.model.graphs.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface GraphBuilder {

    /**
     * Build voltage level graph within the given parent graph.
     */
    VoltageLevelGraph buildVoltageLevelGraph(String id, Graph parentGraph);

    /**
     * Build voltage level graph with no parent graph. This means the root graph is the created voltage level graph.
     */
    VoltageLevelGraph buildVoltageLevelGraph(String id);

    /**
     * Build substation graph within the given parent zone graph.
     */
    SubstationGraph buildSubstationGraph(String id, ZoneGraph parentGraph);

    /**
     * Build substation graph with no parent graph. This means the root graph is the created substation graph.
     */
    SubstationGraph buildSubstationGraph(String id);

    /**
     * Build the zone graph containing the given substations
     */
    ZoneGraph buildZoneGraph(List<String> substationIds);

}
