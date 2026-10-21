/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.triplestore.api.TripleStore;

import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class BoundaryLineDiagramDataExporter extends AbstractLineDiagramDataExporter {

    public BoundaryLineDiagramDataExporter(TripleStore tripleStore, ExportContext context) {
        super(tripleStore, context);
    }

    public void exportDiagramData(BoundaryLine boundaryLine) {
        Objects.requireNonNull(boundaryLine);
        LineDiagramData<BoundaryLine> boundaryLineDiagramData = boundaryLine.getExtension(LineDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(boundaryLine.getTerminal().getVoltageLevel().getTopologyKind());
        addDiagramData(boundaryLine.getId(), boundaryLine.getNameOrId(), boundaryLineDiagramData, diagramObjectStyleId);
    }

}
