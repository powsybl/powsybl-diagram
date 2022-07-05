/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.powsybl.cgmes.extensions.DiagramPoint;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractCgmesDLExporterTest {

    protected final String basename = "network";
    protected final String dataNs = "http://" + basename + "/#";
    protected final String diagramId = "diagramId";
    protected final String diagramObjectStyleId = "diagramObjectStyleId";
    protected final String diagramObjectId = "diagramObjectId";

    protected Network network;
    protected TripleStore tripleStore;
    protected CgmesDLModel cgmesDLModel;
    protected DataSource dataSource;

    protected ArgumentCaptor<String> prefixCaptor;
    protected ArgumentCaptor<String> namespaceCaptor;
    protected ArgumentCaptor<String> contextCaptor;
    protected ArgumentCaptor<String> nsCaptor;
    protected ArgumentCaptor<String> typeCaptor;
    protected ArgumentCaptor<PropertyBag> propertiesCaptor;

    @Before
    public void setUp() {
        tripleStore = Mockito.mock(TripleStore.class);
        Mockito.when(tripleStore.add(Mockito.any(String.class), Mockito.eq(CgmesNamespace.CIM_16_NAMESPACE),
                        Mockito.eq("Diagram"), Mockito.any(PropertyBag.class)))
               .thenReturn(diagramId);
        Mockito.when(tripleStore.add(Mockito.any(String.class), Mockito.eq(CgmesNamespace.CIM_16_NAMESPACE),
                        Mockito.eq("DiagramObjectStyle"), Mockito.any(PropertyBag.class)))
               .thenReturn(diagramObjectStyleId);
        Mockito.when(tripleStore.add(Mockito.any(String.class), Mockito.eq(CgmesNamespace.CIM_16_NAMESPACE),
                        Mockito.eq("DiagramObject"), Mockito.any(PropertyBag.class)))
               .thenReturn(diagramObjectId);

        cgmesDLModel = Mockito.mock(CgmesDLModel.class);

        dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getBaseName()).thenReturn(basename);

        prefixCaptor = ArgumentCaptor.forClass(String.class);
        namespaceCaptor = ArgumentCaptor.forClass(String.class);
        contextCaptor = ArgumentCaptor.forClass(String.class);
        nsCaptor = ArgumentCaptor.forClass(String.class);
        typeCaptor = ArgumentCaptor.forClass(String.class);
        propertiesCaptor = ArgumentCaptor.forClass(PropertyBag.class);
    }

    protected PropertyBag getTerminal(String equipmentId, String terminalId, int side) {
        PropertyBag terminal = new PropertyBag(Arrays.asList("equipment", "terminalSide", "terminal"));
        terminal.put("equipment", dataNs + equipmentId);
        terminal.put("terminal", dataNs + terminalId);
        terminal.put("terminalSide", Integer.toString(side));
        return terminal;
    }

    @Test
    public void test() {
        new CgmesDLExporter(network, tripleStore, cgmesDLModel).exportDLData(dataSource);
        checkNamespaces();
        checkStatements();
    }

    protected void checkNamespaces() {
        Mockito.verify(tripleStore, Mockito.times(3)).addNamespace(prefixCaptor.capture(), namespaceCaptor.capture());
        assertEquals("data", prefixCaptor.getAllValues().get(0));
        assertEquals(dataNs, namespaceCaptor.getAllValues().get(0));
        assertEquals("cim", prefixCaptor.getAllValues().get(1));
        assertEquals(CgmesNamespace.CIM_16_NAMESPACE, namespaceCaptor.getAllValues().get(1));
        assertEquals("md", prefixCaptor.getAllValues().get(2));
        assertEquals(CgmesDLExporter.MD_NAMESPACE, namespaceCaptor.getAllValues().get(2));
    }

    protected abstract void checkStatements();

    protected void checkProperties(String context, String namespace, String type, PropertyBag properties, String expectedType,
                                 List<String> expectedProperties, List<String> expectedResources, List<String> expectedClassProperties) {
        assertTrue(CgmesSubset.DIAGRAM_LAYOUT.isValidName(context));
        assertEquals(basename + "_" + CgmesSubset.DIAGRAM_LAYOUT.getIdentifier() + ".xml", context);
        assertEquals(CgmesNamespace.CIM_16_NAMESPACE, namespace);
        assertEquals(expectedType, type);
        assertEquals(expectedProperties.size(), properties.propertyNames().size());
        expectedProperties.forEach(property -> assertTrue(properties.propertyNames().contains(property)));
        expectedResources.forEach(resource -> assertTrue(properties.isResource(resource)));
        expectedClassProperties.forEach(classProperty -> assertTrue(properties.isClassProperty(classProperty)));
    }

    protected void checkDiagram(String context, String namespace, String type, PropertyBag properties) {
        checkProperties(context, namespace, type, properties, "Diagram", Arrays.asList("IdentifiedObject.name", "orientation"),
                        Arrays.asList("orientation"), Arrays.asList("IdentifiedObject.name"));
        assertEquals(basename, properties.get("IdentifiedObject.name"));
        assertEquals(CgmesNamespace.CIM_16_NAMESPACE + "OrientationKind.negative", properties.get("orientation"));
    }

    protected void checkDiagramObjectStyle(String context, String namespace, String type, PropertyBag properties, String expectedName) {
        checkProperties(context, namespace, type, properties, "DiagramObjectStyle", Arrays.asList("IdentifiedObject.name"),
                        Collections.emptyList(), Arrays.asList("IdentifiedObject.name"));
        assertEquals(expectedName, properties.get("IdentifiedObject.name"));
    }

    protected void checkDiagramObject(String context, String namespace, String type, PropertyBag properties,
                                    String expectedName, String expectedId, double expectedRotation) {
        checkProperties(context, namespace, type, properties, "DiagramObject",
                        Arrays.asList("IdentifiedObject.name", "IdentifiedObject", "rotation", "Diagram", "DiagramObjectStyle"),
                        Arrays.asList("IdentifiedObject", "Diagram", "DiagramObjectStyle"), Arrays.asList("IdentifiedObject.name"));
        assertEquals(expectedName, properties.get("IdentifiedObject.name"));
        assertEquals(expectedId, properties.get("IdentifiedObject"));
        assertEquals(expectedRotation, properties.asDouble("rotation"), 0);
        assertEquals(diagramId, properties.get("Diagram"));
        assertEquals(diagramObjectStyleId, properties.get("DiagramObjectStyle"));
    }

    protected void checkDiagramObjectPoint(String context, String namespace, String type, PropertyBag properties, DiagramPoint expectedPoint) {
        checkProperties(context, namespace, type, properties, "DiagramObjectPoint",
                        Arrays.asList("DiagramObject", "sequenceNumber", "xPosition", "yPosition"),
                        Arrays.asList("DiagramObject"), Collections.emptyList());
        assertEquals(diagramObjectId, properties.get("DiagramObject"));
        assertEquals(expectedPoint.getSeq(), properties.asInt("sequenceNumber"));
        assertEquals(expectedPoint.getX(), properties.asDouble("xPosition"), 0);
        assertEquals(expectedPoint.getY(), properties.asDouble("yPosition"), 0);
    }

}
