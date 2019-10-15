package com.powsybl.sld;

import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;

public interface GraphBuilder {
    Graph buildVoltageLevelGraph(String id,
                                 boolean useName,
                                 boolean forVoltageLevelDiagram,
                                 boolean showInductorFor3WT);

    SubstationGraph buildSubstationGraph(String id,
                                         boolean useName);
}
