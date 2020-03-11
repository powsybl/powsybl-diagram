/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder2WTNode extends FeederBranchNode {

    public enum Side {
        ONE,
        TWO
    }

    public Feeder2WTNode(String id, String name, String componentType, boolean fictitious,
                         Graph graph, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(id, name, componentType, fictitious, graph, otherSideVoltageLevelInfos);
    }

    public static Feeder2WTNode create(Graph graph, String id, String name, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new Feeder2WTNode(id, name, TWO_WINDINGS_TRANSFORMER, false, graph, otherSideVoltageLevelInfos);
    }
}
