/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.importers;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.cgmes.extensions.DiagramPoint;
import com.powsybl.cgmes.extensions.VoltageLevelDiagramData;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class VoltageLevelDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(VoltageLevelDiagramDataImporter.class);

    private Network network;
    Map<String, Map<String, Integer>> mapCnodeInode;

    public VoltageLevelDiagramDataImporter(Network network, Map<String, Map<String, Integer>> mapCnodeInode) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(mapCnodeInode);
        this.network = Objects.requireNonNull(network);
        this.mapCnodeInode = mapCnodeInode;
    }

    public void importDiagramData(PropertyBag diagramData) {
        Objects.requireNonNull(diagramData);
        String connectivityNode = diagramData.getLocal("connectivityNode");
        String switchName = diagramData.getLocal("switch");
        String diagramName = diagramData.getLocal("diagramName");
        double x = diagramData.asDouble("x");
        double y = diagramData.asDouble("y");
        int seq = diagramData.asInt("seq");

        Switch aSwitch = network.getSwitch(switchName);
        if (aSwitch != null) {
            VoltageLevel vl = aSwitch.getVoltageLevel();
            if (mapCnodeInode.containsKey(vl.getId())) {
                Integer iNode = mapCnodeInode.get(vl.getId()).get(connectivityNode);
                if (iNode != null) {
                    VoltageLevelDiagramData.addInternalNodeDiagramPoint(vl, diagramName, iNode, new DiagramPoint(x, y, seq));
                }
            }
        } else {
            LOG.warn("Cannot find voltage level for switch {} in network {}: skipping switch diagram data", switchName, network.getId());
        }
    }
}
