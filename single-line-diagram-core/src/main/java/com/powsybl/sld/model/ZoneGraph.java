/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Substation;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.NetworkGraphBuilder;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ZoneGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZoneGraph.class);

    private List<Substation> zone;
    private List<SubstationGraph> nodes = new ArrayList<>();
    private List<LineEdge> edges = new ArrayList<>();
    private Map<String, SubstationGraph> nodesById = new HashMap<>();

    public ZoneGraph(List<Substation> zone) {
        this.zone = Objects.requireNonNull(zone);
    }

    public static ZoneGraph create(List<Substation> zone) {
        return create(zone, false);
    }

    public static ZoneGraph create(List<Substation> zone, boolean useName) {
        Objects.requireNonNull(zone);
        ZoneGraph g = new ZoneGraph(zone);
        g.buildGraph(useName);
        return g;
    }

    private void buildGraph(boolean useName) {
        if (zone.isEmpty()) {
            LOGGER.warn("No substations in the zone: skipping graph building");
            return;
        }
        // add nodes -> substation graphs
        GraphBuilder graphBuilder = new NetworkGraphBuilder(zone.get(0).getNetwork());
        zone.forEach(substation -> {
            LOGGER.info("Adding substation {} to zone graph", substation.getId());
            SubstationGraph sGraph = graphBuilder.buildSubstationGraph(substation.getId(), useName);
            addNode(sGraph);
        });
        // add edges -> lines
        List<String> lines = new ArrayList<>();
        zone.forEach(substation ->
            substation.getVoltageLevelStream().forEach(voltageLevel ->
                voltageLevel.getConnectableStream(Line.class).forEach(line -> {
                    if (!lines.contains(line.getId())) {
                        String nodeId1 = line.getId() + "_" + Branch.Side.ONE;
                        String nodeId2 = line.getId() + "_" + Branch.Side.TWO;
                        String voltageLevelId1 = line.getTerminal1().getVoltageLevel().getId();
                        String voltageLevelId2 = line.getTerminal2().getVoltageLevel().getId();
                        String substationId1 = line.getTerminal1().getVoltageLevel().getSubstation().getId();
                        String substationId2 = line.getTerminal2().getVoltageLevel().getSubstation().getId();
                        if (nodesById.containsKey(substationId1) && nodesById.containsKey(substationId2)) {
                            SubstationGraph sGraph1 = nodesById.get(substationId1);
                            SubstationGraph sGraph2 = nodesById.get(substationId2);
                            Graph vlGraph1 = sGraph1.getNode(voltageLevelId1);
                            Graph vlGraph2 = sGraph2.getNode(voltageLevelId2);
                            Node node1 = vlGraph1.getNode(nodeId1);
                            Node node2 = vlGraph2.getNode(nodeId2);
                            LOGGER.info("Adding line {} to zone graph", line.getId());
                            edges.add(new LineEdge(line.getId(), node1, node2));
                            lines.add(line.getId());
                        }
                    }
                })
            )
        );
    }

    private void addNode(SubstationGraph sGraph) {
        nodes.add(sGraph);
        nodesById.put(sGraph.getSubstationId(), sGraph);
    }

    public List<Substation> getZone() {
        return zone;
    }

    public List<SubstationGraph> getNodes() {
        return nodes;
    }

    public List<LineEdge> getEdges() {
        return edges;
    }

    public SubstationGraph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

}
