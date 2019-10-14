/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.sld.layout.VoltageLevelLayout;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.Graph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    @Override
    public VoltageLevelLayout create(Graph graph) {
        return new CgmesVoltageLevelLayout(graph);
    }

}
