/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.extensions.NodeDiagramData;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusbarDiagramDataExporter extends AbstractNodeDiagramDataExporter {

    private static final Logger LOG = LoggerFactory.getLogger(BusbarDiagramDataExporter.class);

    private Map<String, String> busbarNodes;

    public BusbarDiagramDataExporter(TripleStore tripleStore, ExportContext context, Map<String, String> busbarNodes) {
        super(tripleStore, context);
        this.busbarNodes = Objects.requireNonNull(busbarNodes);
    }

    public void exportDiagramData(BusbarSection busbar) {
        Objects.requireNonNull(busbar);
        NodeDiagramData<BusbarSection> busbarDiagramData = busbar.getExtension(NodeDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(TopologyKind.NODE_BREAKER);
        String busbarNodeId = getBusbarNodeId(busbar.getId());
        addDiagramData(busbarNodeId, busbar.getName(), busbarDiagramData, diagramObjectStyleId);
    }

    protected String getBusbarNodeId(String busbarId) {
        if (busbarNodes.containsKey(busbarId)) {
            return busbarNodes.get(busbarId);
        }
        LOG.warn("Cannot find node id of busbar {} in triple store: creating new id", busbarId);
        return "_" + UUID.randomUUID().toString();
    }

}
