/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.Objects;

import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class HvdcLineDiagramDataExporter extends AbstractLineDiagramDataExporter {

    public HvdcLineDiagramDataExporter(TripleStore tripleStore, ExportContext context) {
        super(tripleStore, context);
    }

    public void exportDiagramData(HvdcLine hvdcLine) {
        Objects.requireNonNull(hvdcLine);
        LineDiagramData<HvdcLine> hvdcLineDiagramData = hvdcLine.getExtension(LineDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getTopologyKind());
        addDiagramData(hvdcLine.getId(), hvdcLine.getNameOrId(), hvdcLineDiagramData, diagramObjectStyleId);
    }

}
