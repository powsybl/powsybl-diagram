/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NodeDiagramData;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactorySmartSelector;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(VoltageLevelLayoutFactorySmartSelector.class)
public class CgmesVoltageLevelLayoutFactorySmartSelector implements VoltageLevelLayoutFactorySmartSelector {

    private static final int PRIORITY = 500;

    @Override
    public int getPriority(VoltageLevel vl) {
        return PRIORITY;
    }

    private static boolean hasOneCgmesExtension(VoltageLevel vl) {
        // check for a cgmes extension
        for (Connectable<?> c : vl.getConnectables()) {
            if (c.getExtension(InjectionDiagramData.class) != null
                    || c.getExtension(LineDiagramData.class) != null
                    || c.getExtension(NodeDiagramData.class) != null
                    || c.getExtension(CouplingDeviceDiagramData.class) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSelectable(VoltageLevel vl) {
        return hasOneCgmesExtension(vl);
    }

    @Override
    public VoltageLevelLayoutFactory createFactory(Network network) {
        return new CgmesVoltageLevelLayoutFactory(network, null, AbstractCgmesLayout.DEFAULT_CGMES_SCALE_FACTOR);
    }
}
