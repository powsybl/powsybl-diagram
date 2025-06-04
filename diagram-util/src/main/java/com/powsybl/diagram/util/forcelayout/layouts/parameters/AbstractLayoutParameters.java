/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.layouts.parameters;

import com.powsybl.diagram.util.forcelayout.layouts.LayoutAlgorithm;
import com.powsybl.diagram.util.forcelayout.layouts.LayoutEnum;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public abstract class AbstractLayoutParameters<V, E> {

    protected LayoutEnum parameterType;

    public LayoutEnum getParameterType() {
        return parameterType;
    }

    public abstract LayoutAlgorithm<V, E> createLayout();
}
