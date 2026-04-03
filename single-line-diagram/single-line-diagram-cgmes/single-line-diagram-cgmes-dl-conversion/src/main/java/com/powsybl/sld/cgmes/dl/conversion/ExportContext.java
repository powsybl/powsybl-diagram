/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.TripleStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class ExportContext {

    private final String basename;
    private final String baseNamespace;
    private final String dlContext;
    private String busBranchDiagramObjectStyleId;
    private String nodeBreakerDiagramObjectStyleId;
    Map<String, String> diagrams = new HashMap<>();

    public ExportContext(DataSource dataSource, TripleStore tripleStore) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(tripleStore);
        this.basename = dataSource.getBaseName();
        this.baseNamespace = "http://" + dataSource.getBaseName().toLowerCase() + "/#";
        this.dlContext = ContextUtils.contextNameFor(CgmesSubset.DIAGRAM_LAYOUT, tripleStore, basename);
    }

    public String getBasename() {
        return basename;
    }

    public String getBaseNamespace() {
        return baseNamespace;
    }

    public String getDlContext() {
        return dlContext;
    }

    public void setDiagramId(String diagramId, String diagramName) {
        Objects.requireNonNull(diagramId);
        Objects.requireNonNull(diagramName);
        this.diagrams.put(diagramName, diagramId);
    }

    public String getDiagramId(String diagramName) {
        return diagrams.get(diagramName);
    }

    public List<String> getDiagramsIds() {
        return new ArrayList<>(diagrams.keySet());
    }

    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagrams.values());
    }

    public void setBusBranchDiagramObjectStyleId(String busBranchDiagramObjectStyleId) {
        this.busBranchDiagramObjectStyleId = Objects.requireNonNull(busBranchDiagramObjectStyleId);
    }

    public String getBusBranchDiagramObjectStyleId() {
        return busBranchDiagramObjectStyleId;
    }

    public boolean hasBusBranchDiagramObjectStyleId() {
        return busBranchDiagramObjectStyleId != null && !busBranchDiagramObjectStyleId.trim().isEmpty();
    }

    public void setNodeBreakerDiagramObjectStyleId(String nodeBreakerDiagramObjectStyleId) {
        this.nodeBreakerDiagramObjectStyleId = Objects.requireNonNull(nodeBreakerDiagramObjectStyleId);
    }

    public String getNodeBreakerDiagramObjectStyleId() {
        return nodeBreakerDiagramObjectStyleId;
    }

    public boolean hasNodeBreakerDiagramObjectStyleId() {
        return nodeBreakerDiagramObjectStyleId != null && !nodeBreakerDiagramObjectStyleId.trim().isEmpty();
    }

}
