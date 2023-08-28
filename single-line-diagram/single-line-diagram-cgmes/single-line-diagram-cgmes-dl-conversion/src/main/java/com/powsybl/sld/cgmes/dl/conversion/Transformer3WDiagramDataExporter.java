/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.sld.cgmes.dl.iidm.extensions.ThreeWindingsTransformerDiagramData;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class Transformer3WDiagramDataExporter extends AbstractDiagramDataExporter {

    private static final Logger LOG = LoggerFactory.getLogger(Transformer3WDiagramDataExporter.class);

    public Transformer3WDiagramDataExporter(TripleStore tripleStore, ExportContext context, Map<String, String> terminals) {
        super(tripleStore, context);
        super.terminals = Objects.requireNonNull(terminals);
    }

    public void exportDiagramData(ThreeWindingsTransformer transformer) {
        Objects.requireNonNull(transformer);
        ThreeWindingsTransformerDiagramData transformerDiagramData = transformer.getExtension(ThreeWindingsTransformerDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(transformer.getLeg1().getTerminal().getVoltageLevel().getTopologyKind());
        if (transformerDiagramData != null) {
            transformerDiagramData.getDiagramsNames().forEach(diagramName -> {
                ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails details = transformerDiagramData.getData(diagramName);
                String diagramId = context.getDiagramId(diagramName);
                String diagramObjectId = addDiagramObject(transformer.getId(), transformer.getNameOrId(), details.getRotation(), diagramObjectStyleId, diagramId);
                addDiagramObjectPoint(diagramObjectId, details.getPoint());
                addTerminalData(transformer.getId(), transformer.getNameOrId(), 1, details.getTerminalPoints(DiagramTerminal.TERMINAL1), diagramObjectStyleId, diagramId);
                addTerminalData(transformer.getId(), transformer.getNameOrId(), 2, details.getTerminalPoints(DiagramTerminal.TERMINAL2), diagramObjectStyleId, diagramId);
                addTerminalData(transformer.getId(), transformer.getNameOrId(), 3, details.getTerminalPoints(DiagramTerminal.TERMINAL3), diagramObjectStyleId, diagramId);
            });
        } else {
            LOG.warn("Transformer {}, name {} has no diagram data, skipping export", transformer.getId(), transformer.getNameOrId());
        }
    }

}
