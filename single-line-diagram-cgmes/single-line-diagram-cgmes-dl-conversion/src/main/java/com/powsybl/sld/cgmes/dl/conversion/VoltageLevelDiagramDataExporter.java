/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.cgmes.extensions.VoltageLevelDiagramData;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class VoltageLevelDiagramDataExporter extends AbstractDiagramDataExporter {
    private static final Logger LOG = LoggerFactory.getLogger(VoltageLevelDiagramDataExporter.class);
    Map<String, Set<String>> nodeSwitches;

    public VoltageLevelDiagramDataExporter(TripleStore tripleStore, ExportContext context, Map<String, Set<String>> nodeSwitches) {
        super(tripleStore, context);
        this.nodeSwitches = nodeSwitches;
    }

    public void exportDiagramData(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        VoltageLevelDiagramData voltageLevelDiagramData = voltageLevel.getExtension(VoltageLevelDiagramData.class);
        String diagramObjectStyleId = addDiagramObjectStyle(voltageLevel.getTopologyKind());

        if (voltageLevelDiagramData != null) {
            voltageLevelDiagramData.getDiagramsNames().forEach(diagramName -> {
                int[] nodes = VoltageLevelDiagramData.getInternalNodeDiagramPoints(voltageLevel, diagramName);
                String diagramId = context.getDiagramId(diagramName);
                Arrays.stream(nodes).forEach(n -> {
                    Set<Switch> switches = CgmesDLUtils.findSurroundingSwitches(voltageLevel.getNodeBreakerView(), n);
                    String matchingConnectivityNodeId = CgmesDLUtils.findMatchingConnectivityNodeId(nodeSwitches, switches);
                    if (matchingConnectivityNodeId != null) {
                        String diagramObjectId = addDiagramObject(matchingConnectivityNodeId, "" + n, 0, diagramObjectStyleId, diagramId);
                        addDiagramObjectPoint(diagramObjectId, VoltageLevelDiagramData.getInternalNodeDiagramPoint(voltageLevel, diagramName, n));
                    } else {
                        LOG.warn("could not find a matching CGMES connectivity node id for node {}", n);
                    }
                });
            });
        } else {
            LOG.warn("VoltageLevel {}, name {} has no diagram data, skipping export", voltageLevel.getId(), voltageLevel.getName());
        }
    }
}
