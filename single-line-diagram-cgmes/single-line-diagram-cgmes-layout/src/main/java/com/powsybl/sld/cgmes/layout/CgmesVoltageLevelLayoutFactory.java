/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.Layout;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    private final Network network;

    private final LayoutParameters layoutParameters;

    @Override
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public CgmesVoltageLevelLayoutFactory(Network network, LayoutParameters layoutParameters) {
        this.network = Objects.requireNonNull(network);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
    }

    @Override
    public Layout create(VoltageLevelGraph graph) {
        return new CgmesVoltageLevelLayout(graph, network, getLayoutParameters());
    }
}
