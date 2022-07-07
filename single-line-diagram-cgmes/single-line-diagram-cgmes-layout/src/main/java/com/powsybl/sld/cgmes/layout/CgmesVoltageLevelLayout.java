/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesVoltageLevelLayout extends AbstractCgmesLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesVoltageLevelLayout.class);

    private final VoltageLevelGraph graph;

    public CgmesVoltageLevelLayout(VoltageLevelGraph graph, Network network, LayoutParameters layoutParameters) {
        super(network, layoutParameters);
        Objects.requireNonNull(graph);
        this.graph = removeFictitiousNodes(graph, this.network.getVoltageLevel(graph.getVoltageLevelInfos().getId()));
    }

    @Override
    public void run() {
        VoltageLevel vl = network.getVoltageLevel(graph.getVoltageLevelInfos().getId());
        String diagramName = layoutParameters.getDiagramName();
        if (!checkDiagram(diagramName, "voltage level " + vl.getId())) {
            return;
        }
        LOG.info("Applying CGMES-DL layout to network {}, voltage level {}, diagram name {}", network.getId(), graph.getVoltageLevelInfos().getId(), diagramName);
        setNodeCoordinates(vl, graph, diagramName, layoutParameters.isUseName());
        graph.getNodes().forEach(node -> shiftNodeCoordinates(node, layoutParameters.getScaleFactor()));
        if (layoutParameters.getScaleFactor() != 1) {
            graph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParameters.getScaleFactor()));
        }
    }
}
