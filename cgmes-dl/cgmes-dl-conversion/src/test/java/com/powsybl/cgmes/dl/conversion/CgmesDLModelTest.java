/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.dl.conversion;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesDLModelTest extends AbstractCgmesDLTest {

    protected TripleStore tripleStore;
    protected QueryCatalog queryCatalog;
    protected CgmesDLModel cgmesDLModel;

    @Before
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
    public void getTerminalsDiagramData() {
        assertEquals(terminalsPropertyBags, cgmesDLModel.getTerminalsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.TERMINAL_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getTerminalsDiagramData());
    }

    @Test
    public void getBusesDiagramData() {
        assertEquals(busesPropertyBags, cgmesDLModel.getBusesDiagramData());
        removeQueryCatalogKey(CgmesDLModel.BUS_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getBusesDiagramData());
    }

    @Test
    public void getBusbarsDiagramData() {
        assertEquals(busbarsPropertyBags, cgmesDLModel.getBusbarsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.BUSBAR_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getBusbarsDiagramData());
    }

    @Test
    public void getLinesDiagramData() {
        assertEquals(busbarsPropertyBags, cgmesDLModel.getLinesDiagramData());
        removeQueryCatalogKey(CgmesDLModel.LINE_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getLinesDiagramData());
    }

    @Test
    public void getGeneratorsDiagramData() {
        assertEquals(generatorsPropertyBags, cgmesDLModel.getGeneratorsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.GENERATOR_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getGeneratorsDiagramData());
    }

    @Test
    public void getLoadsDiagramData() {
        assertEquals(loadsPropertyBags, cgmesDLModel.getLoadsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.LOAD_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getLoadsDiagramData());
    }

    @Test
    public void getShuntsDiagramData() {
        assertEquals(shuntsPropertyBags, cgmesDLModel.getShuntsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.SHUNT_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getShuntsDiagramData());
    }

    @Test
    public void getSwitchesDiagramData() {
        assertEquals(switchesPropertyBags, cgmesDLModel.getSwitchesDiagramData());
        removeQueryCatalogKey(CgmesDLModel.SWITCH_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getSwitchesDiagramData());
    }

    @Test
    public void getTransformersDiagramData() {
        assertEquals(tranformersPropertyBags, cgmesDLModel.getTransformersDiagramData());
        removeQueryCatalogKey(CgmesDLModel.TRANSFORMER_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getTransformersDiagramData());
    }

    @Test
    public void getHvdcLineDiagramData() {
        assertEquals(hvdcLinesPropertyBags, cgmesDLModel.getHvdcLinesDiagramData());
        removeQueryCatalogKey(CgmesDLModel.HVDC_LINE_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getHvdcLinesDiagramData());
    }

    @Test
    public void getSvcsDiagramData() {
        assertEquals(svcsPropertyBags, cgmesDLModel.getSvcsDiagramData());
        removeQueryCatalogKey(CgmesDLModel.SVC_DIAGRAM_DATA_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getSvcsDiagramData());
    }

    @Test
    public void getTerminals() {
        assertEquals(terminals, cgmesDLModel.getTerminals());
        removeQueryCatalogKey(CgmesDLModel.TERMINALS_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getTerminals());
    }

    @Test
    public void getBusbarNodes() {
        assertEquals(busbarNodes, cgmesDLModel.getBusbarNodes());
        removeQueryCatalogKey(CgmesDLModel.BUSBAR_NODES_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesDLModel.getBusbarNodes());
    }

}
