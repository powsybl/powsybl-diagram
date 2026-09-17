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
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class CgmesSubstationLayout extends AbstractCgmesLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesSubstationLayout.class);

    private final SubstationGraph graph;

    public CgmesSubstationLayout(SubstationGraph graph, Network network) {
        this(graph, network, null, DEFAULT_CGMES_SCALE_FACTOR);
    }

    public CgmesSubstationLayout(SubstationGraph graph, Network network, String cgmesDiagramName, double cgmesScaleFactor) {
        super(network, cgmesDiagramName, cgmesScaleFactor);
        Objects.requireNonNull(graph);
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            removeFictitiousNodes(vlGraph, network.getVoltageLevel(vlGraph.getVoltageLevelInfos().id()));
        }
        fixTransformersLabel = true;
        this.graph = graph;
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        if (checkDiagramFails(cgmesDiagramName, "substation " + graph.getSubstationId())) {
            return;
        }
        LOG.info("Applying CGMES-DL layout to network {}, substation {}, diagram name {}", network.getId(), graph.getSubstationId(), cgmesDiagramName);
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelInfos().id());
            setNodeCoordinates(vl, vlGraph, cgmesDiagramName);
        }
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            vlGraph.getNodes().forEach(n -> shiftAndScaleNodeCoordinates(n, cgmesScaleFactor));
            vlGraph.addPaddingToCoord(layoutParam);
        }

        setMultiNodesCoord(graph);

        setGraphSize(graph, layoutParam);
    }
}
