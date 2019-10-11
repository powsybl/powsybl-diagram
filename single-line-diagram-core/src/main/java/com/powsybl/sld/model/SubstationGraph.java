/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * This class builds the connectivity among the voltageLevels of a substation
 * buildSubstationGraph establishes the List of nodes, edges
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class SubstationGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubstationGraph.class);

    private Substation substation;

    private final List<Graph> nodes = new ArrayList<>();

    private final List<TwtEdge> edges = new ArrayList<>();

    private final Map<String, Graph> nodesById = new HashMap<>();

    /**
     * Constructor
     */
    public SubstationGraph(Substation substation) {
        this.substation = Objects.requireNonNull(substation);
    }

    public static SubstationGraph create(Substation s) {
        return create(s, false);
    }

    public static SubstationGraph create(Substation s, boolean useName) {
        Objects.requireNonNull(s);
        SubstationGraph g = new SubstationGraph(s);
        g.buildSubstationGraph(useName);
        return g;
    }

    private void buildSubstationGraph(boolean useName) {
        // building the graph for each voltageLevel (ordered by descending voltageLevel nominalV)
        substation.getVoltageLevelStream()
                .sorted(Comparator.comparing(VoltageLevel::getNominalV)
                        .reversed()).forEach(v -> {
                            Graph vlGraph = Graph.create(v, useName, false, false);
                            addNode(vlGraph);
                        });

        LOGGER.info("Number of node : {} ", nodes.size());

        // Creation of snake lines for transformers between the voltage levels
        // in the substation diagram
        addSnakeLines();
    }

    private void addSnakeLines() {
        // Two windings transformer
        //
        for (TwoWindingsTransformer transfo : substation.getTwoWindingsTransformers()) {
            Terminal t1 = transfo.getTerminal1();
            Terminal t2 = transfo.getTerminal2();

            String id1 = transfo.getId() + "_" + transfo.getSide(t1).name();
            String id2 = transfo.getId() + "_" + transfo.getSide(t2).name();

            VoltageLevel v1 = t1.getVoltageLevel();
            VoltageLevel v2 = t2.getVoltageLevel();

            Graph g1 = getNode(v1.getId());
            Graph g2 = getNode(v2.getId());

            Node n1 = g1.getNode(id1);
            Node n2 = g2.getNode(id2);

            addEdge(n1, n2);
        }

        // Three windings transformer
        //
        for (ThreeWindingsTransformer transfo : substation.getThreeWindingsTransformers()) {
            Terminal t1 = transfo.getLeg1().getTerminal();
            Terminal t2 = transfo.getLeg2().getTerminal();
            Terminal t3 = transfo.getLeg3().getTerminal();

            String id12 = transfo.getId() + "_" + transfo.getSide(t1).name() + "_" + transfo.getSide(t2).name();
            String id13 = transfo.getId() + "_" + transfo.getSide(t1).name() + "_" + transfo.getSide(t3).name();

            String id21 = transfo.getId() + "_" + transfo.getSide(t2).name() + "_" + transfo.getSide(t1).name();
            String id23 = transfo.getId() + "_" + transfo.getSide(t2).name() + "_" + transfo.getSide(t3).name();

            String id31 = transfo.getId() + "_" + transfo.getSide(t3).name() + "_" + transfo.getSide(t1).name();
            String id32 = transfo.getId() + "_" + transfo.getSide(t3).name() + "_" + transfo.getSide(t2).name();

            VoltageLevel v1 = t1.getVoltageLevel();
            VoltageLevel v2 = t2.getVoltageLevel();
            VoltageLevel v3 = t3.getVoltageLevel();

            Graph g1 = getNode(v1.getId());
            Graph g2 = getNode(v2.getId());
            Graph g3 = getNode(v3.getId());

            Node n12 = g1.getNode(id12);
            Node n13 = g1.getNode(id13);

            Node n21 = g2.getNode(id21);
            Node n23 = g2.getNode(id23);

            Node n31 = g3.getNode(id31);
            Node n32 = g3.getNode(id32);

            addEdge(n12, n21);
            addEdge(n13, n31);
            addEdge(n23, n32);
        }
    }

    public void addNode(Graph node) {
        nodes.add(node);
        nodesById.put(node.getVoltageLevel().getId(), node);
    }

    public Graph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    public void addEdge(Node n1, Node n2) {
        TwtEdge sl = new TwtEdge(n1, n2);
        edges.add(sl);
    }

    public List<Graph> getNodes() {
        return new ArrayList<>(nodes);
    }

    public List<TwtEdge> getEdges() {
        return new ArrayList<>(edges);
    }

    public boolean graphAdjacents(Graph g1, Graph g2) {
        int nbNodes = nodes.size();
        for (int i = 0; i < nbNodes; i++) {
            if (nodes.get(i) == g1 && i < (nbNodes - 1) && nodes.get(i + 1) == g2) {
                return true;
            }
        }
        return false;
    }

    public Substation getSubstation() {
        return substation;
    }

}
