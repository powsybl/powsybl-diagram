/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.VoltageLevelLayout;
import com.powsybl.sld.model.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesVoltageLevelLayout extends AbstractCgmesLayout implements VoltageLevelLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesVoltageLevelLayout.class);

    private final Graph graph;

    public CgmesVoltageLevelLayout(Graph graph) {
        Objects.requireNonNull(graph);
        this.graph = removeFictitiousNodes(graph);
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        String diagramName = layoutParam.getDiagramName();
        if (diagramName == null) {
            LOG.warn("layout parameter diagramName not set: CGMES-DL layout will not be applied");
        } else {
            Network network = graph.getVoltageLevel().getSubstation().getNetwork();
            if (NetworkDiagramData.containsDiagramName(network, diagramName)) {
                LOG.info("Applying CGMES-DL layout to network {}, voltage level {}, diagram name {}", network.getId(), graph.getVoltageLevel().getId(), diagramName);

                setNodeCoordinates(graph, diagramName);
                graph.getNodes().forEach(node -> shiftNodeCoordinates(node, layoutParam.getScaleFactor()));
                if (layoutParam.getScaleFactor() != 1) {
                    graph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getScaleFactor()));
                }
            } else {
                LOG.warn("diagram name {} not found in network: CGMES-DL layout will not be applied to network {}, voltage level {}", diagramName, network.getId(), graph.getVoltageLevel().getId());
            }
        }
    }
}
