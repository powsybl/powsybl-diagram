/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.cgmes.iidm.extensions.dl.CouplingDeviceDiagramData;
import com.powsybl.cgmes.iidm.extensions.dl.InjectionDiagramData;
import com.powsybl.cgmes.iidm.extensions.dl.LineDiagramData;
import com.powsybl.cgmes.iidm.extensions.dl.NodeDiagramData;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.cgmes.CgmesVoltageLevelLayoutFactory;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.PositionFree;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayout;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.Graph;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SmartVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    @Override
    public VoltageLevelLayout create(Graph graph) {
        VoltageLevel vl = graph.getVoltageLevel();

        if (vl.getTopologyKind() == TopologyKind.BUS_BREAKER) {
            // because bus/breaker topology is managed by adding position extensions, we need to use layout with
            // extensions
            return new PositionVoltageLevelLayoutFactory().create(graph);
        }

        // check for position extensions
        for (Connectable c : vl.getConnectables()) {
            if (c.getExtension(ConnectablePosition.class) != null
                    || c.getExtension(BusbarSectionPosition.class) != null) {
                return new PositionVoltageLevelLayoutFactory().create(graph);
            }
        }

        // check for cgmes extensions
        for (Connectable c : vl.getConnectables()) {
            if (c.getExtension(InjectionDiagramData.class) != null
                    || c.getExtension(LineDiagramData.class) != null
                    || c.getExtension(NodeDiagramData.class) != null
                    || c.getExtension(CouplingDeviceDiagramData.class) != null) {
                return new CgmesVoltageLevelLayoutFactory().create(graph);
            }
        }

        return new PositionVoltageLevelLayoutFactory(new PositionFree()).create(graph);
    }
}
