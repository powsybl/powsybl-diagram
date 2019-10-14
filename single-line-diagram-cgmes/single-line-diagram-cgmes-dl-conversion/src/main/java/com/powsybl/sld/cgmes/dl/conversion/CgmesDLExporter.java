/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.triplestore.api.PrefixNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
*
* @author Massimo Ferraro <massimo.ferraro@techrain.eu>
*/
public class CgmesDLExporter {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDLExporter.class);
    public static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";

    private Network network;
    private TripleStore tripleStore;
    private CgmesDLModel cgmesDLModel;

    public CgmesDLExporter(Network network, TripleStore tripleStore, CgmesDLModel cgmesDLModel) {
        this.network = Objects.requireNonNull(network);
        this.tripleStore = Objects.requireNonNull(tripleStore);
        this.cgmesDLModel = Objects.requireNonNull(cgmesDLModel);
    }

    public CgmesDLExporter(Network network, TripleStore tripleStore) {
        this(network, tripleStore, new CgmesDLModel(tripleStore));
    }

    public CgmesDLExporter(Network network) {
        this(network, TripleStoreFactory.create());
    }

    public void exportDLData(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        ExportContext context = new ExportContext(dataSource, tripleStore);
        Map<String, String> terminals = getTerminals();
        Map<String, String> busbarNodes = getBusbarNodes();
        addNamespaces(context);
        addModel(context);
        addDiagrams(context);
        exportNodesDLData(context, busbarNodes);
        exportLinesDLData(context);
        exportDanglingLinesDLData(context);
        exportGeneratorsDLData(context, terminals);
        exportLoadsDLData(context, terminals);
        exportShuntsDLData(context, terminals);
        exportSvcsDLData(context, terminals);
        exportTransformersDLData(context, terminals);
        exportSwitchesDLData(context, terminals);
        exportTransformers3WDLData(context, terminals);
        exportHvdcLinesDLData(context);
        tripleStore.write(dataSource);
    }

    private Map<String, String> getTerminals() {
        Map<String, String> terminals = new HashMap<>();
        cgmesDLModel.getTerminals().forEach(terminal ->
            terminals.put(terminal.getId("equipment") + "_" + terminal.get("terminalSide"), terminal.getId("terminal"))
        );
        return terminals;
    }

    private Map<String, String> getBusbarNodes() {
        Map<String, String> busbarNodes = new HashMap<>();
        cgmesDLModel.getBusbarNodes().forEach(busbarNode ->
            busbarNodes.put(busbarNode.getId("busbarSection"), busbarNode.getId("busbarNode"))
        );
        return busbarNodes;
    }

    private void addNamespaces(ExportContext context) {
        if (!namespaceAlreadyExist("data")) {
            tripleStore.addNamespace("data", context.getBaseNamespace());
        }
        if (!namespaceAlreadyExist("cim")) {
            tripleStore.addNamespace("cim", CgmesNamespace.CIM_16_NAMESPACE);
        }
        if (!namespaceAlreadyExist("md")) {
            tripleStore.addNamespace("md", MD_NAMESPACE);
        }
    }

    private boolean namespaceAlreadyExist(String prefix) {
        return tripleStore.getNamespaces().stream().map(PrefixNamespace::getPrefix).anyMatch(prefix::equals);
    }

    private void addModel(ExportContext context) {
        PropertyBag modelProperties = new PropertyBag(Arrays.asList(CgmesDLModel.MODEL_SCENARIO_TIME, CgmesDLModel.MODEL_CREATED, CgmesDLModel.MODEL_DESCRIPTION, "" +
                CgmesDLModel.MODEL_VERSION, CgmesDLModel.MODEL_PROFILE, CgmesDLModel.MODEL_DEPENDENT_ON));
        modelProperties.setResourceNames(Arrays.asList(CgmesDLModel.MODEL_DEPENDENT_ON));
        modelProperties.setClassPropertyNames(Arrays.asList(CgmesDLModel.MODEL_SCENARIO_TIME, CgmesDLModel.MODEL_CREATED, CgmesDLModel.MODEL_DESCRIPTION, CgmesDLModel.MODEL_VERSION, CgmesDLModel.MODEL_PROFILE, CgmesDLModel.MODEL_DEPENDENT_ON));
        modelProperties.put(CgmesDLModel.MODEL_SCENARIO_TIME, network.getCaseDate().toString());
        modelProperties.put(CgmesDLModel.MODEL_CREATED, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()));
        modelProperties.put(CgmesDLModel.MODEL_DESCRIPTION, network.getName());
        modelProperties.put(CgmesDLModel.MODEL_VERSION, "1");
        modelProperties.put(CgmesDLModel.MODEL_PROFILE, "http://entsoe.eu/CIM/DiagramLayout/3/1");
        modelProperties.put(CgmesDLModel.MODEL_DEPENDENT_ON, network.getId());
        tripleStore.add(context.getDlContext(), MD_NAMESPACE, "FullModel", modelProperties);
    }

    private void addDiagrams(ExportContext context) {
        NetworkDiagramData.getDiagramsNames(network).forEach(diagramName -> {
            PropertyBag diagramObjectProperties = new PropertyBag(Arrays.asList(CgmesDLModel.IDENTIFIED_OBJECT_NAME, CgmesDLModel.ORIENTATION));
            diagramObjectProperties.setResourceNames(Arrays.asList(CgmesDLModel.ORIENTATION));
            diagramObjectProperties.setClassPropertyNames(Arrays.asList(CgmesDLModel.IDENTIFIED_OBJECT_NAME));
            diagramObjectProperties.put(CgmesDLModel.IDENTIFIED_OBJECT_NAME, diagramName);
            diagramObjectProperties.put(CgmesDLModel.ORIENTATION, CgmesNamespace.CIM_16_NAMESPACE + "OrientationKind.negative");
            String diagramId = tripleStore.add(context.getDlContext(), CgmesNamespace.CIM_16_NAMESPACE, "Diagram", diagramObjectProperties);
            context.setDiagramId(diagramId, diagramName);
        });
    }

    private void exportNodesDLData(ExportContext context, Map<String, String> busbarNodes) {
        LOG.info("Exporting Nodes DL Data");
        network.getVoltageLevelStream().forEach(voltageLavel -> {
            switch (voltageLavel.getTopologyKind()) {
                case NODE_BREAKER:
                    BusbarDiagramDataExporter busbarDiagramDataExporter = new BusbarDiagramDataExporter(tripleStore, context, busbarNodes);
                    voltageLavel.getNodeBreakerView().getBusbarSectionStream().forEach(busbarDiagramDataExporter::exportDiagramData);
                    break;
                case BUS_BREAKER:
                    BusDiagramDataExporter busDiagramDataExporter = new BusDiagramDataExporter(tripleStore, context);
                    voltageLavel.getBusBreakerView().getBusStream().forEach(busDiagramDataExporter::exportDiagramData);
                    break;
                default:
                    throw new AssertionError("Unexpected topology kind: " + voltageLavel.getTopologyKind());
            }
        });
    }

    private void exportLinesDLData(ExportContext context) {
        LOG.info("Exporting Lines DL Data");
        LineDiagramDataExporter diagramDataExporter = new LineDiagramDataExporter(tripleStore, context);
        network.getLineStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportDanglingLinesDLData(ExportContext context) {
        LOG.info("Exporting Dangling Lines DL Data");
        DanglingLineDiagramDataExporter diagramDataExporter = new DanglingLineDiagramDataExporter(tripleStore, context);
        network.getDanglingLineStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportGeneratorsDLData(ExportContext context, Map<String, String> terminals) {
        LOG.info("Exporting Generators DL Data");
        GeneratorDiagramDataExporter diagramDataExporter = new GeneratorDiagramDataExporter(tripleStore, context, terminals);
        network.getGeneratorStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportLoadsDLData(ExportContext context, Map<String, String> terminals) {
        LOG.info("Exporting Loads DL Data");
        LoadDiagramDataExporter diagramDataExporter = new LoadDiagramDataExporter(tripleStore, context, terminals);
        network.getLoadStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportShuntsDLData(ExportContext context, Map<String, String> terminals) {
        LOG.info("Exporting Shunts DL Data");
        ShuntDiagramDataExporter diagramDataExporter = new ShuntDiagramDataExporter(tripleStore, context, terminals);
        network.getShuntCompensatorStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportSvcsDLData(ExportContext context, Map<String, String> terminals) {
        LOG.info("Exporting SVCs DL Data");
        SvcDiagramDataExporter diagramDataExporter = new SvcDiagramDataExporter(tripleStore, context, terminals);
        network.getStaticVarCompensatorStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportTransformersDLData(ExportContext context, Map<String, String> terminals) {
        LOG.info("Exporting Transformers DL Data");
        TransformerDiagramDataExporter diagramDataExporter = new TransformerDiagramDataExporter(tripleStore, context, terminals);
        network.getTwoWindingsTransformerStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportSwitchesDLData(ExportContext context, Map<String, String> terminals) {
        LOG.info("Exporting Switches DL Data");
        SwitchDiagramDataExporter diagramDataExporter = new SwitchDiagramDataExporter(tripleStore, context, terminals);
        network.getSwitchStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportTransformers3WDLData(ExportContext context, Map<String, String> terminals) {
        LOG.info("Exporting Transformers 3W DL Data");
        Transformer3WDiagramDataExporter diagramDataExporter = new Transformer3WDiagramDataExporter(tripleStore, context, terminals);
        network.getThreeWindingsTransformerStream().forEach(diagramDataExporter::exportDiagramData);
    }

    private void exportHvdcLinesDLData(ExportContext context) {
        LOG.info("Exporting HVDC Lines DL Data");
        HvdcLineDiagramDataExporter diagramDataExporter = new HvdcLineDiagramDataExporter(tripleStore, context);
        network.getHvdcLineStream().forEach(diagramDataExporter::exportDiagramData);
    }

}
