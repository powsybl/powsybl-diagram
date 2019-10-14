/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Profiling;
import com.powsybl.sld.cgmes.dl.conversion.importers.BusDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.BusbarDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.GeneratorDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.HvdcLineDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.LineDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.LoadDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.ShuntDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.SvcDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.SwitchDiagramDataImporter;
import com.powsybl.sld.cgmes.dl.conversion.importers.TransformerDiagramDataImporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesDLImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDLImporter.class);

    private Network network;
    private CgmesDLModel cgmesDLModel;
    private Map<String, PropertyBags> terminalsDiagramData = new HashMap<>();
    private Profiling profiling;
    private boolean logProfile = false;

    public CgmesDLImporter(Network network, CgmesDLModel cgmesDLModel) {
        this(network, cgmesDLModel, new Profiling());
        logProfile = true;
    }

    public CgmesDLImporter(Network network, CgmesDLModel cgmesDLModel, Profiling profiling) {
        this.network = Objects.requireNonNull(network);
        this.cgmesDLModel = Objects.requireNonNull(cgmesDLModel);
        this.profiling = Objects.requireNonNull(profiling);
    }

    public void importDLData() {
        importTerminalsDLData();
        importBusesDLData();
        importBusbarsDLData();
        importLinesDLData();
        importGeneratorsDLData();
        importLoadsDLData();
        importShuntsDLData();
        importSwitchesDLData();
        importTransformersDLData();
        importHvdcLinesDLData();
        importSvcsDLData();
        if (logProfile) {
            profiling.report();
        }
    }

    private void importTerminalsDLData() {
        LOG.info("Importing Terminals DL Data");
        profiling.start();
        cgmesDLModel.getTerminalsDiagramData().forEach(terminalDiagramData -> {
            String terminalKey = terminalDiagramData.getId("terminalEquipment") + "_" + terminalDiagramData.get("terminalSide");
            PropertyBags equipmentTerminalsDiagramData = new PropertyBags();
            if (terminalsDiagramData.containsKey(terminalKey)) {
                equipmentTerminalsDiagramData = terminalsDiagramData.get(terminalKey);
            }
            equipmentTerminalsDiagramData.add(terminalDiagramData);
            terminalsDiagramData.put(terminalKey, equipmentTerminalsDiagramData);
        });
        profiling.end("DLTerminals");
    }

    private void importBusesDLData() {
        LOG.info("Importing Buses DL Data");
        profiling.start();
        BusDiagramDataImporter diagramDataImporter = new BusDiagramDataImporter(network);
        cgmesDLModel.getBusesDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLBuses");
    }

    private void importBusbarsDLData() {
        LOG.info("Importing Busbars DL Data");
        profiling.start();
        BusbarDiagramDataImporter diagramDataImporter = new BusbarDiagramDataImporter(network);
        cgmesDLModel.getBusbarsDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLBusbars");
    }

    private void importLinesDLData() {
        LOG.info("Importing Lines DL Data");
        profiling.start();
        LineDiagramDataImporter diagramDataImporter = new LineDiagramDataImporter(network);
        cgmesDLModel.getLinesDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLLines");
    }

    private void importGeneratorsDLData() {
        LOG.info("Importing Generators DL Data");
        profiling.start();
        GeneratorDiagramDataImporter diagramDataImporter = new GeneratorDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getGeneratorsDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLGenerators");
    }

    private void importLoadsDLData() {
        LOG.info("Importing Loads DL Data");
        profiling.start();
        LoadDiagramDataImporter diagramDataImporter = new LoadDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getLoadsDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLLoads");
    }

    private void importShuntsDLData() {
        LOG.info("Importing Shunts DL Data");
        profiling.start();
        ShuntDiagramDataImporter diagramDataImporter = new ShuntDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getShuntsDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLShunts");
    }

    private void importSwitchesDLData() {
        LOG.info("Importing Switches DL Data");
        profiling.start();
        SwitchDiagramDataImporter diagramDataImporter = new SwitchDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getSwitchesDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLSwitches");
    }

    private void importTransformersDLData() {
        LOG.info("Importing Transformers DL Data");
        profiling.start();
        TransformerDiagramDataImporter diagramDataImporter = new TransformerDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getTransformersDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLTransformers");
    }

    private void importHvdcLinesDLData() {
        LOG.info("Importing HVDC Lines DL Data");
        profiling.start();
        HvdcLineDiagramDataImporter diagramDataImporter = new HvdcLineDiagramDataImporter(network);
        cgmesDLModel.getHvdcLinesDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLHVDCLines");
    }

    private void importSvcsDLData() {
        LOG.info("Importing Svcs DL Data");
        profiling.start();
        SvcDiagramDataImporter diagramDataImporter = new SvcDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getSvcsDiagramData().forEach(diagramDataImporter::importDiagramData);
        profiling.end("DLSVCs");
    }

    public Network getNetworkWithDLData() {
        return network;
    }

}
