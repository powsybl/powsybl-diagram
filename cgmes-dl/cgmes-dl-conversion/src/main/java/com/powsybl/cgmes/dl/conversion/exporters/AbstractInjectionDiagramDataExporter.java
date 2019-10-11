/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.dl.conversion.exporters;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.dl.conversion.ExportContext;
import com.powsybl.cgmes.iidm.extensions.dl.InjectionDiagramData;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractInjectionDiagramDataExporter extends AbstractDiagramDataExporter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractInjectionDiagramDataExporter.class);

    public AbstractInjectionDiagramDataExporter(TripleStore tripleStore, ExportContext context, Map<String, String> terminals) {
        super(tripleStore, context);
        super.terminals = Objects.requireNonNull(terminals);
    }

    protected void addDiagramData(String id, String name, InjectionDiagramData<?> diagramData, String diagramObjectStyleId) {
        if (diagramData != null) {
            diagramData.getDiagramsNames().forEach(diagramName -> {
                InjectionDiagramData.InjectionDiagramDetails details = diagramData.getData(diagramName);
                String diagramId = context.getDiagramId(diagramName);
                String diagramObjectId = addDiagramObject(id, name, details.getRotation(), diagramObjectStyleId, diagramId);
                addDiagramObjectPoint(diagramObjectId, details.getPoint());
                addTerminalData(id, name, 1, details.getTerminalPoints(), diagramObjectStyleId, diagramId);
            });
        } else {
            LOG.warn("Injection {}, name {} has no diagram data, skipping export", id, name);
        }
    }

}
