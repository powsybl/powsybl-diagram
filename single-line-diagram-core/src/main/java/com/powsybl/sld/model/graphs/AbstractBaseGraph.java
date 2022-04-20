/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.graphs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.FeederTwtLegNode;
import com.powsybl.sld.model.nodes.MiddleTwtNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractBaseGraph extends AbstractGraph implements BaseGraph {

    private static final String EDGE_PREFIX = "EDGE_";

    protected List<BranchEdge> twtEdges = new ArrayList<>();

    protected List<MiddleTwtNode> multiTermNodes = new ArrayList<>();

    AbstractBaseGraph(Graph parentGraph) {
        super(parentGraph);
    }

    @Override
    public List<BranchEdge> getTwtEdges() {
        return new ArrayList<>(twtEdges);
    }

    @Override
    public List<MiddleTwtNode> getMultiTermNodes() {
        return multiTermNodes;
    }

    @Override
    public BranchEdge addTwtEdge(FeederTwtLegNode legNode, MiddleTwtNode twtNode) {
        BranchEdge edge = new BranchEdge(EDGE_PREFIX + legNode.getId(), legNode, twtNode);
        twtNode.addAdjacentEdge(edge);
        twtEdges.add(edge);
        return edge;
    }

    @Override
    public void addMultiTermNode(MiddleTwtNode node) {
        multiTermNodes.add(node);
    }

    protected void writeBranchFields(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeArrayFieldStart("multitermNodes");
        for (MiddleTwtNode multitermNode : multiTermNodes) {
            multitermNode.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("twtEdges");
        for (BranchEdge edge : twtEdges) {
            edge.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("lineEdges");
        for (BranchEdge edge : getLineEdges()) {
            edge.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
    }
}
