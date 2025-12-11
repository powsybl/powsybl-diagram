/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.MiddleTwtNode;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class CgmesSubstationLayout extends AbstractCgmesLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesSubstationLayout.class);

    private final SubstationGraph graph;

    public CgmesSubstationLayout(SubstationGraph graph, Network network) {
        this(graph, network, null);
    }

    public CgmesSubstationLayout(SubstationGraph graph, Network network, String cgmesDiagramName) {
        super(cgmesDiagramName);
        this.network = Objects.requireNonNull(network);
        Objects.requireNonNull(graph);
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            removeFictitiousNodes(vlGraph, network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId()));
        }
        fixTransformersLabel = true;
        this.graph = graph;
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        String diagramName = getCgmesDiagramName();
        if (!checkDiagram(diagramName, "substation " + graph.getSubstationId())) {
            return;
        }
        LOG.info("Applying CGMES-DL layout to network {}, substation {}, diagram name {}", network.getId(), graph.getSubstationId(), diagramName);
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId());
            setNodeCoordinates(vl, vlGraph, diagramName);
        }
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            vlGraph.getNodes().forEach(this::shiftNodeCoordinates);
        }
        if (layoutParam.getCgmesScaleFactor() != 1) {
            for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
                vlGraph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getCgmesScaleFactor()));
            }
        }
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            setVoltageLevelCoord(vlGraph);
        }

        setMultiNodesCoord();
    }

    private void setMultiNodesCoord() {
        for (MiddleTwtNode multiNode : graph.getMultiTermNodes()) {
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();
            multiNode.setCoordinates(adjacentNodes.get(0).getCoordinates());
        }
    }
}
