/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.TripleStore;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractDiagramDataExporter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDiagramDataExporter.class);

    protected TripleStore tripleStore;
    protected ExportContext context;
    protected Map<String, String> terminals;

    protected AbstractDiagramDataExporter(TripleStore tripleStore, ExportContext context) {
        this.tripleStore = Objects.requireNonNull(tripleStore);
        this.context = Objects.requireNonNull(context);
    }

    protected String addDiagramObject(String id, String name, double rotation, String diagramObjectStyleId, String diagramId) {
        PropertyBag diagramObjectProperties = new PropertyBag(Arrays.asList(CgmesDLModel.IDENTIFIED_OBJECT_NAME, CgmesDLModel.IDENTIFIED_OBJECT, "rotation", CgmesDLModel.DIAGRAM, CgmesDLModel.DIAGRAM_OBJECT_STYLE));
        diagramObjectProperties.setResourceNames(Arrays.asList(CgmesDLModel.IDENTIFIED_OBJECT, CgmesDLModel.DIAGRAM, CgmesDLModel.DIAGRAM_OBJECT_STYLE));
        diagramObjectProperties.setClassPropertyNames(Arrays.asList(CgmesDLModel.IDENTIFIED_OBJECT_NAME));
        diagramObjectProperties.put(CgmesDLModel.IDENTIFIED_OBJECT_NAME, name);
        diagramObjectProperties.put(CgmesDLModel.IDENTIFIED_OBJECT, id);
        diagramObjectProperties.put("rotation", Double.toString(rotation));
        diagramObjectProperties.put(CgmesDLModel.DIAGRAM, diagramId);
        diagramObjectProperties.put(CgmesDLModel.DIAGRAM_OBJECT_STYLE, diagramObjectStyleId);
        return tripleStore.add(context.getDlContext(), CgmesNamespace.CIM_16_NAMESPACE, CgmesDLModel.DIAGRAM_OBJECT, diagramObjectProperties);
    }

    protected void addDiagramObjectPoint(String diagramObjectId, DiagramPoint point) {
        PropertyBag diagramObjectPointProperties = new PropertyBag(Arrays.asList(CgmesDLModel.DIAGRAM_OBJECT, "sequenceNumber", "xPosition", "yPosition"));
        diagramObjectPointProperties.setResourceNames(Arrays.asList(CgmesDLModel.DIAGRAM_OBJECT));
        diagramObjectPointProperties.put(CgmesDLModel.DIAGRAM_OBJECT, diagramObjectId);
        diagramObjectPointProperties.put("sequenceNumber", Integer.toString(point.getSeq()));
        diagramObjectPointProperties.put("xPosition", Double.toString(point.getX()));
        diagramObjectPointProperties.put("yPosition", Double.toString(point.getY()));
        tripleStore.add(context.getDlContext(), CgmesNamespace.CIM_16_NAMESPACE, "DiagramObjectPoint", diagramObjectPointProperties);
    }

    protected String addDiagramObjectStyle(String name) {
        PropertyBag diagramObjectStyleProperties = new PropertyBag(Arrays.asList(CgmesDLModel.IDENTIFIED_OBJECT_NAME));
        diagramObjectStyleProperties.setClassPropertyNames(Arrays.asList(CgmesDLModel.IDENTIFIED_OBJECT_NAME));
        diagramObjectStyleProperties.put(CgmesDLModel.IDENTIFIED_OBJECT_NAME, name);
        return tripleStore.add(context.getDlContext(), CgmesNamespace.CIM_16_NAMESPACE, CgmesDLModel.DIAGRAM_OBJECT_STYLE, diagramObjectStyleProperties);
    }

    protected String addDiagramObjectStyle(TopologyKind topologyKind) {
        switch (topologyKind) {
            case NODE_BREAKER:
                if (context.hasNodeBreakerDiagramObjectStyleId()) {
                    return context.getNodeBreakerDiagramObjectStyleId();
                }
                String nodeBreakerDiagramObjectStyleId = addDiagramObjectStyle("node-breaker");
                context.setNodeBreakerDiagramObjectStyleId(nodeBreakerDiagramObjectStyleId);
                return nodeBreakerDiagramObjectStyleId;
            case BUS_BREAKER:
                if (context.hasBusBranchDiagramObjectStyleId()) {
                    return context.getBusBranchDiagramObjectStyleId();
                }
                String busBranchdiagramObjectStyleId = addDiagramObjectStyle("bus-branch");
                context.setBusBranchDiagramObjectStyleId(busBranchdiagramObjectStyleId);
                return busBranchdiagramObjectStyleId;
            default:
                throw new AssertionError("Unexpected topology kind: " + topologyKind);
        }
    }

    protected void addTerminalData(String id, String name, int side, List<DiagramPoint> terminalPoints, String diagramObjectStyleId, String diagramId) {
        String diagramObjectId = addDiagramObject(getTerminalId(id, side), getTerminalName(name, side), 0, diagramObjectStyleId, diagramId);
        terminalPoints.forEach(point -> addDiagramObjectPoint(diagramObjectId, point));
    }

    protected String getTerminalId(String equipmentId, int terminalSide) {
        String terminalKey = equipmentId + "_" + terminalSide;
        if (terminals.containsKey(terminalKey)) {
            return terminals.get(terminalKey);
        }
        LOG.warn("Cannot find terminal id of equipment {} side {} in triple store: creating new id", equipmentId, terminalSide);
        return "_" + UUID.randomUUID().toString();
    }

    protected String getTerminalName(String equipmentName, int terminalSide) {
        return equipmentName + "_" + (terminalSide - 1);
    }

}
