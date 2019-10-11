/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.dl.conversion.importers;

import java.util.Map;
import java.util.Objects;

import com.powsybl.cgmes.iidm.extensions.dl.NetworkDiagramData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.iidm.extensions.dl.CouplingDeviceDiagramData;
import com.powsybl.cgmes.iidm.extensions.dl.DiagramPoint;
import com.powsybl.cgmes.iidm.extensions.dl.DiagramTerminal;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SwitchDiagramDataImporter extends AbstractCouplingDeviceDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchDiagramDataImporter.class);

    public SwitchDiagramDataImporter(Network network, Map<String, PropertyBags> terminalsDiagramData) {
        super(network, terminalsDiagramData);
    }

    public void importDiagramData(PropertyBag switchesDiagramData) {
        Objects.requireNonNull(switchesDiagramData);
        String switchId = switchesDiagramData.getId("identifiedObject");
        Switch sw = network.getSwitch(switchId);
        if (sw != null) {
            CouplingDeviceDiagramData<Switch> switchIidmDiagramData = new CouplingDeviceDiagramData<>(sw);
            CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails = switchIidmDiagramData.new CouplingDeviceDiagramDetails(new DiagramPoint(switchesDiagramData.asDouble("x"), switchesDiagramData.asDouble("y"), 0),
                    switchesDiagramData.asDouble("rotation"));
            addTerminalPoints(switchId, sw.getName(), DiagramTerminal.TERMINAL1, "1", diagramDetails);
            addTerminalPoints(switchId, sw.getName(), DiagramTerminal.TERMINAL2, "2", diagramDetails);
            switchIidmDiagramData.addData(switchesDiagramData.get("diagramName"), diagramDetails);
            sw.addExtension(CouplingDeviceDiagramData.class, switchIidmDiagramData);
            NetworkDiagramData.addDiagramName(network, switchesDiagramData.get("diagramName"));
        } else {
            LOG.warn("Cannot find switch {}, name {} in network {}: skipping switch diagram data", switchId, switchesDiagramData.get("name"), network.getId());
        }
    }

}
