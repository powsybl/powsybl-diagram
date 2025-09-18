/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.importers;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
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
public class LoadDiagramDataImporter extends AbstractInjectionDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDiagramDataImporter.class);

    public LoadDiagramDataImporter(Network network, Map<String, PropertyBags> terminalsDiagramData) {
        super(network, terminalsDiagramData);
    }

    public void importDiagramData(PropertyBag loadDiagramData) {
        Objects.requireNonNull(loadDiagramData);
        String loadId = loadDiagramData.getId("identifiedObject");
        Load load = network.getLoad(loadId);
        if (load != null) {
            InjectionDiagramData<Load> loadIidmDiagramData = load.getExtension(InjectionDiagramData.class);
            if (loadIidmDiagramData == null) {
                loadIidmDiagramData = new InjectionDiagramData<>(load);
            }
            String diagramName = loadDiagramData.get("diagramName");
            InjectionDiagramData.InjectionDiagramDetails diagramDetails = new InjectionDiagramData.InjectionDiagramDetails(new DiagramPoint(loadDiagramData.asDouble("x"), loadDiagramData.asDouble("y"), loadDiagramData.asInt("seq")),
                loadDiagramData.asDouble("rotation"));
            addTerminalPoints(loadId, load.getNameOrId(), diagramName, diagramDetails);
            loadIidmDiagramData.addData(diagramName, diagramDetails);
            load.addExtension(InjectionDiagramData.class, loadIidmDiagramData);
            NetworkDiagramData.addDiagramName(network, diagramName, load.getTerminal().getVoltageLevel().getSubstation().map(Substation::getId).orElse(null));
        } else {
            LOG.warn("Cannot find load {}, name {} in network {}: skipping load diagram data", loadId, loadDiagramData.get("name"), network.getId());
        }
    }

}
