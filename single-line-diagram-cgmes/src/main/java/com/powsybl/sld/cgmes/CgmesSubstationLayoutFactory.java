/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes;

import com.powsybl.sld.layout.SubstationLayout;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesSubstationLayoutFactory implements SubstationLayoutFactory {

    @Override
    public SubstationLayout create(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        return new CgmesSubstationLayout(graph);
    }

}
