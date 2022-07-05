/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.Objects;

import com.powsybl.cgmes.extensions.LineDiagramData;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class DanglingLineDiagramDataExporter extends AbstractLineDiagramDataExporter {

    public DanglingLineDiagramDataExporter(TripleStore tripleStore, ExportContext context) {
        super(tripleStore, context);
    }

    public void exportDiagramData(DanglingLine danglingLine) {
        Objects.requireNonNull(danglingLine);
        LineDiagramData<DanglingLine> danglingLineDiagramData = danglingLine.getExtension(LineDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(danglingLine.getTerminal().getVoltageLevel().getTopologyKind());
        addDiagramData(danglingLine.getId(), danglingLine.getName(), danglingLineDiagramData, diagramObjectStyleId);
    }

}
