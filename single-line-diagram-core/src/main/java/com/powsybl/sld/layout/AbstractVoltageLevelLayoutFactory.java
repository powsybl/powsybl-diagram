/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;

import java.util.List;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    private final LayoutParameters layoutParameters;

    protected AbstractVoltageLevelLayoutFactory(LayoutParameters layoutParameters) {
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
    }

    @Override
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }
}
