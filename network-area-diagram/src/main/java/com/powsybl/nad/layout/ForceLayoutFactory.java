/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Point;

import java.util.Map;
import java.util.Set;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ForceLayoutFactory implements LayoutFactory {

    private final Map<String, Point> initialPositions;
    private final Set<String> fixedPositions;
    private final Map<String, TextPosition> textNodesWithFixedPosition;

    public ForceLayoutFactory() {
        this(Map.of());
    }

    public ForceLayoutFactory(Map<String, Point> initialPositions) {
        this(initialPositions, Set.of());
    }

    public ForceLayoutFactory(Map<String, Point> initialPositions, Set<String> fixedPositions) {
        this(initialPositions, fixedPositions, Map.of());
    }

    public ForceLayoutFactory(Map<String, Point> initialPositions, Set<String> fixedPositions, Map<String, TextPosition> textNodesWithFixedPosition) {
        this.initialPositions = initialPositions;
        this.fixedPositions = fixedPositions;
        this.textNodesWithFixedPosition = textNodesWithFixedPosition;
    }

    @Override
    public Layout create() {
        Layout layout = new BasicForceLayout(true, false);
        layout.setInitialNodePositions(initialPositions);
        layout.setNodesWithFixedPosition(fixedPositions);
        textNodesWithFixedPosition.forEach((k, v) -> layout.setTextNodeFixedPosition(k, v.topLeftPosition(), v.edgeConnection()));
        return layout;
    }
}
