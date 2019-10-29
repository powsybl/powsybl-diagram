/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Fictitious3WTNode extends FictitiousNode {

    private final String transformerId;

    public Fictitious3WTNode(Graph graph, String id, String transformerId) {
        super(graph, id, THREE_WINDINGS_TRANSFORMER);
        this.transformerId = transformerId;
    }

    public String getTransformerId() {
        return transformerId;
    }
}
