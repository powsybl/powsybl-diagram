/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.sld.cgmes.dl.conversion.importers.*;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class CgmesDLImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDLImporter.class);

    private Network network;
    private CgmesDLModel cgmesDLModel;
    private Map<String, PropertyBags> terminalsDiagramData = new HashMap<>();

    public CgmesDLImporter(Network network, CgmesDLModel cgmesDLModel) {
        this.network = Objects.requireNonNull(network);
        this.cgmesDLModel = Objects.requireNonNull(cgmesDLModel);
    }

    public void importDLData() {
        Boolean[] importDl = new Boolean[]{
                importTerminalsDLData(),
                importBusesDLData(),
                importBusbarsDLData(),
                importLinesDLData(),
                importGeneratorsDLData(),
                importLoadsDLData(),
                importShuntsDLData(),
                importSwitchesDLData(),
                importTransformersDLData(),
                importHvdcLinesDLData(),
                importSvcsDLData(),
                importVoltageLevelDLData()
        };
        if (Arrays.stream(importDl).noneMatch(imported -> imported)) {
            LOG.info("No DL data found for network {}", network.getId());
        }
    }

    private boolean importTerminalsDLData() {
        PropertyBags terminalDiagramData = cgmesDLModel.getTerminalsDiagramData();
        if (terminalDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Terminals DL Data");
        terminalDiagramData.forEach(propertyBag -> {
            String terminalKey = propertyBag.get("diagramName") + "_" + propertyBag.getId("terminalEquipment") + "_" + propertyBag.get("terminalSide");
            PropertyBags equipmentTerminalsDiagramData = new PropertyBags();
            if (terminalsDiagramData.containsKey(terminalKey)) {
                equipmentTerminalsDiagramData = terminalsDiagramData.get(terminalKey);
            }
            equipmentTerminalsDiagramData.add(propertyBag);
            terminalsDiagramData.put(terminalKey, equipmentTerminalsDiagramData);
        });
        return true;
    }

    private boolean importBusesDLData() {
        PropertyBags busesDiagramData = cgmesDLModel.getBusesDiagramData();
        if (busesDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Buses DL Data");
        BusDiagramDataImporter diagramDataImporter = new BusDiagramDataImporter(network);
        busesDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importBusbarsDLData() {
        PropertyBags busbarsDiagramData = cgmesDLModel.getBusbarsDiagramData();
        if (busbarsDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Busbars DL Data");
        BusbarDiagramDataImporter diagramDataImporter = new BusbarDiagramDataImporter(network);
        busbarsDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importLinesDLData() {
        PropertyBags linesDiagramData = cgmesDLModel.getLinesDiagramData();
        if (linesDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Lines DL Data");
        LineDiagramDataImporter diagramDataImporter = new LineDiagramDataImporter(network);
        linesDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importGeneratorsDLData() {
        PropertyBags generatorsDiagramData = cgmesDLModel.getGeneratorsDiagramData();
        if (generatorsDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Generators DL Data");
        GeneratorDiagramDataImporter diagramDataImporter = new GeneratorDiagramDataImporter(network, terminalsDiagramData);
        generatorsDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importLoadsDLData() {
        PropertyBags loadsDiagramData = cgmesDLModel.getLoadsDiagramData();
        if (loadsDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Loads DL Data");
        LoadDiagramDataImporter diagramDataImporter = new LoadDiagramDataImporter(network, terminalsDiagramData);
        loadsDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importShuntsDLData() {
        PropertyBags shuntsDiagramData = cgmesDLModel.getShuntsDiagramData();
        if (shuntsDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Shunts DL Data");
        ShuntDiagramDataImporter diagramDataImporter = new ShuntDiagramDataImporter(network, terminalsDiagramData);
        shuntsDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importSwitchesDLData() {
        PropertyBags switchesDiagramData = cgmesDLModel.getSwitchesDiagramData();
        if (switchesDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Switches DL Data");
        SwitchDiagramDataImporter diagramDataImporter = new SwitchDiagramDataImporter(network, terminalsDiagramData);
        switchesDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importTransformersDLData() {
        PropertyBags transformersDiagramData = cgmesDLModel.getTransformersDiagramData();
        if (transformersDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Transformers DL Data");
        TransformerDiagramDataImporter diagramDataImporter = new TransformerDiagramDataImporter(network, terminalsDiagramData);
        transformersDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importHvdcLinesDLData() {
        PropertyBags hvdcLinesDiagramData = cgmesDLModel.getHvdcLinesDiagramData();
        if (hvdcLinesDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing HVDC Lines DL Data");
        HvdcLineDiagramDataImporter diagramDataImporter = new HvdcLineDiagramDataImporter(network);
        hvdcLinesDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importSvcsDLData() {
        PropertyBags svcsDiagramData = cgmesDLModel.getSvcsDiagramData();
        if (svcsDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing Svcs DL Data");
        SvcDiagramDataImporter diagramDataImporter = new SvcDiagramDataImporter(network, terminalsDiagramData);
        svcsDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    private boolean importVoltageLevelDLData() {
        PropertyBags voltageLevelDiagramData = cgmesDLModel.getVoltageLevelDiagramData();
        if (voltageLevelDiagramData.isEmpty()) {
            return false;
        }
        LOG.info("Importing VoltageLevel DL Data");
        Map<String, Set<String>> nodeSwitches = cgmesDLModel.findCgmesConnectivityNodesSwitchesForks();
        Map<String, Map<String, Integer>> mapCnodeInode = new HashMap<>();
        network.getVoltageLevelStream().forEach(vl -> {
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                for (int n : vl.getNodeBreakerView().getNodes()) {
                    Set<Switch> switches = CgmesDLUtils.findSurroundingSwitches(vl.getNodeBreakerView(), n);
                    if (switches.size() > 1) {
                        mapCnodeInode.putIfAbsent(vl.getId(), new HashMap<>());
                        String matchedCnode = CgmesDLUtils.findMatchingConnectivityNodeId(nodeSwitches, switches);
                        if (matchedCnode != null) {
                            mapCnodeInode.get(vl.getId()).putIfAbsent(matchedCnode, n);
                        }
                    }
                }
            }
        });
        VoltageLevelDiagramDataImporter diagramDataImporter = new VoltageLevelDiagramDataImporter(network, mapCnodeInode);
        voltageLevelDiagramData.forEach(diagramDataImporter::importDiagramData);
        return true;
    }

    public Network getNetworkWithDLData() {
        return network;
    }

}
