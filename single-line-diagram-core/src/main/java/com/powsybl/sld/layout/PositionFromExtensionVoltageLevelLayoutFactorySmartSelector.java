/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(VoltageLevelLayoutFactorySmartSelector.class)
public class PositionFromExtensionVoltageLevelLayoutFactorySmartSelector implements VoltageLevelLayoutFactorySmartSelector {

    private static final int PRIORITY = 1000;

    @Override
    public int getPriority(VoltageLevel vl) {
        return vl.getTopologyKind() == TopologyKind.BUS_BREAKER ? Integer.MAX_VALUE : PRIORITY;
    }

    private static boolean hasAtLeastOneExtension(VoltageLevel vl) {
        // check for position extensions
        for (Connectable c : vl.getConnectables()) {
            if (c.getExtension(ConnectablePosition.class) != null
                    || c.getExtension(BusbarSectionPosition.class) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSelectable(VoltageLevel vl) {
        return vl.getTopologyKind() == TopologyKind.BUS_BREAKER || hasAtLeastOneExtension(vl);
    }

    @Override
    public VoltageLevelLayoutFactory createFactory() {
        return new PositionVoltageLevelLayoutFactory();
    }
}
