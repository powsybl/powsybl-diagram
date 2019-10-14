/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractCouplingDeviceDiagramDataExporter extends AbstractDiagramDataExporter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCouplingDeviceDiagramDataExporter.class);

    public AbstractCouplingDeviceDiagramDataExporter(TripleStore tripleStore, ExportContext context, Map<String, String> terminals) {
        super(tripleStore, context);
        super.terminals = Objects.requireNonNull(terminals);
    }

    protected void addDiagramData(String id, String name, CouplingDeviceDiagramData<?> diagramData, String diagramObjectStyleId) {
        if (diagramData != null) {
            diagramData.getDiagramsNames().forEach(diagramName -> {
                CouplingDeviceDiagramData.CouplingDeviceDiagramDetails details = diagramData.getData(diagramName);
                String diagramId = context.getDiagramId(diagramName);
                String diagramObjectId = addDiagramObject(id, name, details.getRotation(), diagramObjectStyleId, diagramId);
                addDiagramObjectPoint(diagramObjectId, details.getPoint());
                addTerminalData(id, name, 1, details.getTerminalPoints(DiagramTerminal.TERMINAL1), diagramObjectStyleId, diagramId);
                addTerminalData(id, name, 2, details.getTerminalPoints(DiagramTerminal.TERMINAL2), diagramObjectStyleId, diagramId);
            });
        } else {
            LOG.warn("Coupling device {}, name {} has no diagram data, skipping export", id, name);
        }
    }

}
