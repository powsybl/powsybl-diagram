/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesDLModelTest extends AbstractCgmesDLTest {

    protected TripleStore tripleStore;
    protected QueryCatalog queryCatalog;
    protected CgmesDLModel cgmesDLModel;

    @BeforeEach
    public void setUp() {
        super.setUp();
        tripleStore = Mockito.mock(TripleStore.class);
        queryCatalog = Mockito.mock(QueryCatalog.class);
        setQuery(CgmesDLModel.TERMINAL_DIAGRAM_DATA_QUERY_KEY, "TerminalQuery", terminalsPropertyBags);
        setQuery(CgmesDLModel.BUS_DIAGRAM_DATA_QUERY_KEY, "BusQuery", busesPropertyBags);
        setQuery(CgmesDLModel.BUSBAR_DIAGRAM_DATA_QUERY_KEY, "BusbarQuery", busbarsPropertyBags);
        setQuery(CgmesDLModel.LINE_DIAGRAM_DATA_QUERY_KEY, "LineQuery", linesPropertyBags);
        setQuery(CgmesDLModel.GENERATOR_DIAGRAM_DATA_QUERY_KEY, "GeneratorQuery", generatorsPropertyBags);
        setQuery(CgmesDLModel.LOAD_DIAGRAM_DATA_QUERY_KEY, "LoadQuery", loadsPropertyBags);
        setQuery(CgmesDLModel.SHUNT_DIAGRAM_DATA_QUERY_KEY, "ShuntQuery", shuntsPropertyBags);
        setQuery(CgmesDLModel.SWITCH_DIAGRAM_DATA_QUERY_KEY, "SwitchQuery", switchesPropertyBags);
        setQuery(CgmesDLModel.TRANSFORMER_DIAGRAM_DATA_QUERY_KEY, "TransformerQuery", tranformersPropertyBags);
        setQuery(CgmesDLModel.HVDC_LINE_DIAGRAM_DATA_QUERY_KEY, "HvdcLineQuery", hvdcLinesPropertyBags);
        setQuery(CgmesDLModel.SVC_DIAGRAM_DATA_QUERY_KEY, "SvcLineQuery", svcsPropertyBags);
        setQuery(CgmesDLModel.TERMINALS_QUERY_KEY, "TerminalsQuery", terminals);
        setQuery(CgmesDLModel.BUSBAR_NODES_QUERY_KEY, "BusbarNodesQuery", busbarNodes);
        Mockito.when(tripleStore.contextNames()).thenReturn(new HashSet<>(Arrays.asList("Network_EQ.xml", "Network_SV.xml", "Network_TP.xml", "Network_DL.xml")));
        cgmesDLModel = new CgmesDLModel(tripleStore, queryCatalog);
    }

    protected void setQuery(String key, String query, PropertyBags queryResults) {
        Mockito.when(queryCatalog.get(key)).thenReturn(query);
        Mockito.when(tripleStore.query(query)).thenReturn(queryResults);
    }

    protected void removeQueryCatalogKey(String key) {
        Mockito.when(queryCatalog.get(key)).thenReturn(null);
    }

    @Test
    void getTerminalsDiagramData() {
        assertEquals(terminalsPropertyBags, cgmesDLModel.getTerminalsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.TERMINAL_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getTerminalsDiagramData());
    }

    @Test
    void getBusesDiagramData() {
        assertEquals(busesPropertyBags, cgmesDLModel.getBusesDiagramData());
        removeQueryCatalogKey(CgmesDLModel.BUS_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getBusesDiagramData());
    }

    @Test
    void getBusbarsDiagramData() {
        assertEquals(busbarsPropertyBags, cgmesDLModel.getBusbarsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.BUSBAR_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getBusbarsDiagramData());
    }

    @Test
    void getLinesDiagramData() {
        assertEquals(busbarsPropertyBags, cgmesDLModel.getLinesDiagramData());
        removeQueryCatalogKey(CgmesDLModel.LINE_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getLinesDiagramData());
    }

    @Test
    void getGeneratorsDiagramData() {
        assertEquals(generatorsPropertyBags, cgmesDLModel.getGeneratorsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.GENERATOR_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getGeneratorsDiagramData());
    }

    @Test
    void getLoadsDiagramData() {
        assertEquals(loadsPropertyBags, cgmesDLModel.getLoadsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.LOAD_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getLoadsDiagramData());
    }

    @Test
    void getShuntsDiagramData() {
        assertEquals(shuntsPropertyBags, cgmesDLModel.getShuntsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.SHUNT_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getShuntsDiagramData());
    }

    @Test
    void getSwitchesDiagramData() {
        assertEquals(switchesPropertyBags, cgmesDLModel.getSwitchesDiagramData());
        removeQueryCatalogKey(CgmesDLModel.SWITCH_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getSwitchesDiagramData());
    }

    @Test
    void getTransformersDiagramData() {
        assertEquals(tranformersPropertyBags, cgmesDLModel.getTransformersDiagramData());
        removeQueryCatalogKey(CgmesDLModel.TRANSFORMER_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getTransformersDiagramData());
    }

    @Test
    void getHvdcLineDiagramData() {
        assertEquals(hvdcLinesPropertyBags, cgmesDLModel.getHvdcLinesDiagramData());
        removeQueryCatalogKey(CgmesDLModel.HVDC_LINE_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getHvdcLinesDiagramData());
    }

    @Test
    void getSvcsDiagramData() {
        assertEquals(svcsPropertyBags, cgmesDLModel.getSvcsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.SVC_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getSvcsDiagramData());
    }

    @Test
    void getTerminals() {
        assertEquals(terminals, cgmesDLModel.getTerminals());
        removeQueryCatalogKey(CgmesDLModel.TERMINALS_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getTerminals());
    }

    @Test
    void getBusbarNodes() {
        assertEquals(busbarNodes, cgmesDLModel.getBusbarNodes());
        removeQueryCatalogKey(CgmesDLModel.BUSBAR_NODES_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getBusbarNodes());
    }

}
