/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.LabelProviderParameters;
import com.powsybl.nad.svg.SvgParameters;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultLabelProviderFactory implements LabelProviderFactory {

    private final LabelProviderParameters parameters;
    private final DefaultLabelProvider.EdgeInfoParameters edgeInfoParameters;

    public DefaultLabelProviderFactory(LabelProviderParameters parameters, DefaultLabelProvider.EdgeInfoParameters edgeInfoParameters) {
        this.parameters = parameters;
        this.edgeInfoParameters = edgeInfoParameters;
    }

    @Override
    public LabelProvider create(Network network, SvgParameters svgParameters) {
        return new DefaultLabelProvider(network, edgeInfoParameters, svgParameters.createValueFormatter(), parameters);
    }

    public LabelProviderParameters getParameters() {
        return parameters;
    }

    public DefaultLabelProvider.EdgeInfoParameters getEdgeInfoParameters() {
        return edgeInfoParameters;
    }
}
