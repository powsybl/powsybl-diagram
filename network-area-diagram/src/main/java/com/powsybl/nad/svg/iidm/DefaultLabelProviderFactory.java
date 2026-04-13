/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.svg.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public record DefaultLabelProviderFactory(LabelProviderParameters parameters) implements LabelProviderFactory {

    public DefaultLabelProviderFactory() {
        this(new LabelProviderParameters());
    }

    @Override
    public LabelProvider create(Network network, SvgParameters svgParameters) {
        return new DefaultLabelProvider(network, svgParameters.createValueFormatter(), parameters);
    }

    public LabelProviderParameters getParameters() {
        return parameters;
    }
}
