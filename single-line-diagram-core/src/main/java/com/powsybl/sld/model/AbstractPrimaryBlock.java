/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractPrimaryBlock extends AbstractBlock implements PrimaryBlock {

    protected final List<Node> nodes;

    /**
     * Constructor.
     * A layout.block primary is oriented in order to have :
     * <ul>
     * <li>BUS - when in the layout.block - as starting node
     * <li>FEEDER - when in the layout.block - as ending node
     * </ul>
     *
     * @param nodes nodes
     */

    AbstractPrimaryBlock(Type type, List<Node> nodes, Cell cell) {
        super(type);
        if (nodes.isEmpty()) {
            throw new PowsyblException("Empty node list");
        }
        this.nodes = new ArrayList<>(nodes);
        setCardinality(Extremity.START, 1);
        setCardinality(Extremity.END, 1);
        setCell(cell);
    }

    @Override
    public Graph getGraph() {
        return nodes.get(0).getGraph();
    }

    @Override
    public boolean isEmbedingNodeType(Node.NodeType type) {
        return nodes.stream().anyMatch(n -> n.getType() == type);
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    @Override
    public void reverseBlock() {
        Collections.reverse(nodes);
    }

    @Override
    public Node getExtremityNode(Extremity extremity) {
        if (extremity == Extremity.START) {
            return nodes.get(0);
        }
        if (extremity == Extremity.END) {
            return nodes.get(nodes.size() - 1);
        }
        return null;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeFieldName("nodes");
        generator.writeStartArray();
        for (Node node : nodes) {
            node.writeJson(generator);
        }
        generator.writeEndArray();
    }

    @Override
    public double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParameters) {
        double blockHeight = 0.;
        int nbNodes = nodes.size();
        Map<String, ComponentSize> componentsSize = layoutParameters.getComponentsSize();

        for (int i = 0; i < nbNodes; i++) {
            Node node = nodes.get(i);
            if (!encounteredNodes.contains(node) && node.getType() != Node.NodeType.BUS) {
                // the node is not a bus node and has not been already encountered
                encounteredNodes.add(node);
                double nodeHeight = layoutParameters.getMaxComponentHeight();
                if (componentsSize != null) {
                    nodeHeight = componentsSize.containsKey(node.getComponentType())
                            ? componentsSize.get(node.getComponentType()).getHeight()
                            : 0;
                }
                blockHeight += nodeHeight;
                if (i < nbNodes - 1 || node.getType() != Node.NodeType.FEEDER) {
                    // not the last node or last node is not a feeder node
                    blockHeight += layoutParameters.getMinSpaceBetweenComponents();
                }
            }
        }
        return blockHeight;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + nodes;
    }
}
