/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesDLModel {

    public static final String TERMINAL_DIAGRAM_DATA_QUERY_KEY = "terminalDiagramData";
    public static final String BUS_DIAGRAM_DATA_QUERY_KEY = "busDiagramData";
    public static final String BUSBAR_DIAGRAM_DATA_QUERY_KEY = "busbarDiagramData";
    public static final String LINE_DIAGRAM_DATA_QUERY_KEY = "lineDiagramData";
    public static final String GENERATOR_DIAGRAM_DATA_QUERY_KEY = "generatorDiagramData";
    public static final String LOAD_DIAGRAM_DATA_QUERY_KEY = "loadDiagramData";
    public static final String SHUNT_DIAGRAM_DATA_QUERY_KEY = "shuntDiagramData";
    public static final String SWITCH_DIAGRAM_DATA_QUERY_KEY = "switchDiagramData";
    public static final String TRANSFORMER_DIAGRAM_DATA_QUERY_KEY = "transformerDiagramData";
    public static final String HVDC_LINE_DIAGRAM_DATA_QUERY_KEY = "hvdcLineDiagramData";
    public static final String SVC_DIAGRAM_DATA_QUERY_KEY = "svcDiagramData";
    public static final String TERMINALS_QUERY_KEY = "terminals";
    public static final String BUSBAR_NODES_QUERY_KEY = "busbarNodes";
    public static final String MODEL_DESCRIPTION = "Model.description";
    public static final String MODEL_DEPENDENT_ON = "Model.DependentOn";
    public static final String MODEL_VERSION = "Model.version";
    public static final String MODEL_PROFILE = "Model.profile";
    public static final String MODEL_CREATED = "Model.created";
    public static final String MODEL_SCENARIO_TIME = "Model.scenarioTime";
    public static final String IDENTIFIED_OBJECT_NAME = "IdentifiedObject.name";
    public static final String ORIENTATION = "orientation";
    public static final String IDENTIFIED_OBJECT = "IdentifiedObject";
    public static final String DIAGRAM = "Diagram";
    public static final String DIAGRAM_OBJECT_STYLE = "DiagramObjectStyle";
    public static final String DIAGRAM_OBJECT = "DiagramObject";
    public static final String DIAGRAM_NAME = "diagramName";

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDLModel.class);

    private final TripleStore tripleStore;
    private final QueryCatalog queryCatalog;

    public CgmesDLModel(TripleStore tripleStore) {
        this(tripleStore, new QueryCatalog("CGMES-DL.sparql"));
    }

    public CgmesDLModel(TripleStore tripleStore, QueryCatalog queryCatalog) {
        this.tripleStore = Objects.requireNonNull(tripleStore);
        tripleStore.defineQueryPrefix("cim", CgmesNamespace.CIM_16_NAMESPACE);
        this.queryCatalog = Objects.requireNonNull(queryCatalog);
    }

    private PropertyBags queryTripleStore(String queryKey) {
        String query = queryCatalog.get(queryKey);
        if (query == null) {
            LOG.warn("Query [{}] not found in catalog", queryKey);
            return new PropertyBags();
        }
        return tripleStore.query(query);
    }

    public PropertyBags getTerminalsDiagramData() {
        LOG.info("Querying triple store for terminals diagram data");
        return queryTripleStore(TERMINAL_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getBusesDiagramData() {
        LOG.info("Querying triple store for buses diagram data");
        return queryTripleStore(BUS_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getBusbarsDiagramData() {
        LOG.info("Querying triple store for busbars diagram data");
        return queryTripleStore(BUSBAR_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getLinesDiagramData() {
        LOG.info("Querying triple store for lines diagram data");
        return queryTripleStore(LINE_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getGeneratorsDiagramData() {
        LOG.info("Querying triple store for generators diagram data");
        return queryTripleStore(GENERATOR_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getLoadsDiagramData() {
        LOG.info("Querying triple store for loads diagram data");
        return queryTripleStore(LOAD_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getShuntsDiagramData() {
        LOG.info("Querying triple store for shunts diagram data");
        return queryTripleStore(SHUNT_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getSwitchesDiagramData() {
        LOG.info("Querying triple store for switches diagram data");
        return queryTripleStore(SWITCH_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getTransformersDiagramData() {
        LOG.info("Querying triple store for transformers diagram data");
        return queryTripleStore(TRANSFORMER_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getHvdcLinesDiagramData() {
        LOG.info("Querying triple store for HVDC lines diagram data");
        return queryTripleStore(HVDC_LINE_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getSvcsDiagramData() {
        LOG.info("Querying triple store for SVCs diagram data");
        return queryTripleStore(SVC_DIAGRAM_DATA_QUERY_KEY);
    }

    public PropertyBags getTerminals() {
        LOG.info("Querying triple store for terminals");
        return queryTripleStore(TERMINALS_QUERY_KEY);
    }

    public PropertyBags getBusbarNodes() {
        LOG.info("Querying triple store for busbar nodes");
        return queryTripleStore(BUSBAR_NODES_QUERY_KEY);
    }

}
