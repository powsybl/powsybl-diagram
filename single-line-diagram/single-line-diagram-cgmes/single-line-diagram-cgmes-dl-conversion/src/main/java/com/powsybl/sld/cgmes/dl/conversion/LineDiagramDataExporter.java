/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.iidm.network.Line;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.triplestore.api.TripleStore;

import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class LineDiagramDataExporter extends AbstractLineDiagramDataExporter {

    public LineDiagramDataExporter(TripleStore tripleStore, ExportContext context) {
        super(tripleStore, context);
    }

    public void exportDiagramData(Line line) {
        Objects.requireNonNull(line);
        LineDiagramData<Line> lineDiagramData = line.getExtension(LineDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(line.getTerminal1().getVoltageLevel().getTopologyKind());
        addDiagramData(line.getId(), line.getNameOrId(), lineDiagramData, diagramObjectStyleId);
    }

}
