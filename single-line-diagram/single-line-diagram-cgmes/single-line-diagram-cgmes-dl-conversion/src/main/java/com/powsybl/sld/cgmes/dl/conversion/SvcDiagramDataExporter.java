/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.Map;
import java.util.Objects;

import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SvcDiagramDataExporter extends AbstractInjectionDiagramDataExporter {

    public SvcDiagramDataExporter(TripleStore tripleStore, ExportContext context, Map<String, String> terminals) {
        super(tripleStore, context, terminals);
    }

    public void exportDiagramData(StaticVarCompensator svc) {
        Objects.requireNonNull(svc);
        InjectionDiagramData<StaticVarCompensator> generatorDiagramData = svc.getExtension(InjectionDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(svc.getTerminal().getVoltageLevel().getTopologyKind());
        addDiagramData(svc.getId(), svc.getNameOrId(), generatorDiagramData, diagramObjectStyleId);
    }

}
