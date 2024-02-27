/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Point;

import java.util.Map;
import java.util.Objects;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class ImprovedForceLayoutFactory implements LayoutFactory {

    private final Map<String, Point> fixedPositions;

    public ImprovedForceLayoutFactory(Map<String, Point> fixedPositions) {
        Objects.requireNonNull(fixedPositions);
        this.fixedPositions = fixedPositions;
    }

    @Override
    public Layout create() {
        AbstractLayout layout = new BasicForceLayout();
        layout.setFixedNodePositions(fixedPositions);
        return layout;
    }
}
