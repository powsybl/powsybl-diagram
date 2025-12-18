/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.iidm.network.Switch;
import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.triplestore.api.TripleStore;

import java.util.Map;
import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class SwitchDiagramDataExporter extends AbstractCouplingDeviceDiagramDataExporter {

    public SwitchDiagramDataExporter(TripleStore tripleStore, ExportContext context, Map<String, String> terminals) {
        super(tripleStore, context, terminals);
    }

    public void exportDiagramData(Switch sw) {
        Objects.requireNonNull(sw);
        CouplingDeviceDiagramData<Switch> switchDiagramData = sw.getExtension(CouplingDeviceDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(sw.getVoltageLevel().getTopologyKind());
        addDiagramData(sw.getId(), sw.getNameOrId(), switchDiagramData, diagramObjectStyleId);
    }

}
