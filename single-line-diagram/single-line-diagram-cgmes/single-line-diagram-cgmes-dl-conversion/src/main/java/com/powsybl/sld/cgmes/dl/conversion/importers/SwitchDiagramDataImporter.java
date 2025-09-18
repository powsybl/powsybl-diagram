/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.importers;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Switch;
import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
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
            CouplingDeviceDiagramData<Switch> switchIidmDiagramData = sw.getExtension(CouplingDeviceDiagramData.class);
            if (switchIidmDiagramData == null) {
                switchIidmDiagramData = new CouplingDeviceDiagramData<>(sw);
            }
            String diagramName = switchesDiagramData.get("diagramName");
            CouplingDeviceDiagramData<Switch>.CouplingDeviceDiagramDetails diagramDetails = switchIidmDiagramData.new CouplingDeviceDiagramDetails(new DiagramPoint(switchesDiagramData.asDouble("x"), switchesDiagramData.asDouble("y"), 0),
                    switchesDiagramData.asDouble("rotation"));
            addTerminalPoints(switchId, sw.getNameOrId(), diagramName, DiagramTerminal.TERMINAL1, "1", diagramDetails);
            addTerminalPoints(switchId, sw.getNameOrId(), diagramName, DiagramTerminal.TERMINAL2, "2", diagramDetails);
            switchIidmDiagramData.addData(diagramName, diagramDetails);
            sw.addExtension(CouplingDeviceDiagramData.class, switchIidmDiagramData);
            NetworkDiagramData.addDiagramName(network, diagramName, sw.getVoltageLevel().getSubstation().map(Substation::getId).orElse(""));
        } else {
            LOG.warn("Cannot find switch {}, name {} in network {}: skipping switch diagram data", switchId, switchesDiagramData.get("name"), network.getId());
        }
    }

}
