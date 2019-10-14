/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractLineDiagramDataExporter extends AbstractDiagramDataExporter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLineDiagramDataExporter.class);

    public AbstractLineDiagramDataExporter(TripleStore tripleStore, ExportContext context) {
        super(tripleStore, context);
    }

    protected void addDiagramData(String id, String name, LineDiagramData<?> diagramData, String diagramObjectStyleId) {
        if (diagramData != null) {
            diagramData.getDiagramsNames().forEach(diagramName -> {
                String diagramId = context.getDiagramId(diagramName);
                String diagramObjectId = addDiagramObject(id, name, 0, diagramObjectStyleId, diagramId);
                diagramData.getPoints(diagramName).forEach(point -> addDiagramObjectPoint(diagramObjectId, point));
            });
        } else {
            LOG.warn("Line {}, name {} has no diagram data, skipping export", id, name);
        }
    }

}
