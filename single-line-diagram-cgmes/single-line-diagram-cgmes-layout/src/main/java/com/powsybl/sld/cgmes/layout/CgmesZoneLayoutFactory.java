/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.sld.layout.ZoneLayout;
import com.powsybl.sld.layout.ZoneLayoutFactory;
import com.powsybl.sld.model.ZoneGraph;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class CgmesZoneLayoutFactory implements ZoneLayoutFactory {

    @Override
    public ZoneLayout create(ZoneGraph graph) {
        return new CgmesZoneLayout(graph);
    }

}
