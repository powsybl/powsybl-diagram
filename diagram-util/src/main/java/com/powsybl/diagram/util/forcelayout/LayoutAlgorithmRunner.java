/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.layouts.AbstractLayoutAlgorithm;
import com.powsybl.diagram.util.forcelayout.layouts.layoutsparameters.AbstractLayoutParameters;
import com.powsybl.diagram.util.forcelayout.setup.Setup;
import com.powsybl.diagram.util.forcelayout.setup.SetupEnum;
import com.powsybl.diagram.util.forcelayout.setup.SpringySetup;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class LayoutAlgorithmRunner<V, E> {
    private Setup<V, E> setup;
    private AbstractLayoutAlgorithm<V, E> layoutAlgorithm;

    LayoutAlgorithmRunner(SetupEnum setupChoice, AbstractLayoutParameters<V, E> layoutParameters) {
        chooseSetup(setupChoice);
        chooseLayoutAlgorithm(layoutParameters);
    }

    private void chooseSetup(SetupEnum setupChoice) {
        this.setup = switch (setupChoice) {
            case SPRINGY -> new SpringySetup<>();
        };
    }

    private void chooseLayoutAlgorithm(AbstractLayoutParameters<V, E> layoutParameters) {
        this.layoutAlgorithm = layoutParameters.createLayout();
    }

    public void run(ForceGraph<V, E> forceGraph) {
        setup.setup(forceGraph);
        layoutAlgorithm.calculateLayout(forceGraph);
    }
}
