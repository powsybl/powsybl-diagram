/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Point;

import java.util.Map;
import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class BasicFixedLayoutFactory implements LayoutFactory {

    private final Map<String, Point> fixedPositions;
    private final LayoutFactory layoutFactory;

    public BasicFixedLayoutFactory(Map<String, Point> fixedPositions) {
        this(fixedPositions, BasicFixedLayout::new);
    }

    public BasicFixedLayoutFactory(Map<String, Point> fixedPositions, LayoutFactory layoutFactory) {
        this.fixedPositions = Objects.requireNonNull(fixedPositions);
        this.layoutFactory = Objects.requireNonNull(layoutFactory);
    }

    @Override
    public Layout create() {
        Layout layout = layoutFactory.create();
        layout.setInitialNodePositions(fixedPositions);
        layout.setNodesWithFixedPosition(fixedPositions.keySet());
        return layout;
    }
}
