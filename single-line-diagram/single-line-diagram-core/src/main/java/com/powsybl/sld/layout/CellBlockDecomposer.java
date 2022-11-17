/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.blocks.*;
import com.powsybl.sld.model.cells.*;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contain function to dispose components of cells based on Hierarchical Layout
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
final class CellBlockDecomposer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellBlockDecomposer.class);

    private CellBlockDecomposer() {
    }

    static void determineShuntCellBlocks(ShuntCell shuntCell) {
        shuntCell.setRootBlock(BodyPrimaryBlock.createBodyPrimaryBlockForShuntCell(shuntCell.getNodes()));
    }

    /**
     * Search BlockPrimary and build Block hierarchy by merging blocks together; also
     * list blocks connected to busbar
     */
    static void determineComplexCell(VoltageLevelGraph vlGraph, BusCell busCell, boolean exceptionIfPatternNotHandled) {
        List<Block> blocks = createPrimaryBlocks(vlGraph, busCell);
        mergeBlocks(vlGraph, busCell, blocks, exceptionIfPatternNotHandled);
    }

    private static List<Block> createPrimaryBlocks(VoltageLevelGraph voltageLevelGraph, BusCell busCell) {
        // Search all primary blocks
        List<Block> blocks = new ArrayList<>();
        Map<Node, Integer> nodeRemainingSlots = new HashMap<>();
        busCell.getNodes().stream()
                .filter(n -> n.getType() != Node.NodeType.BUS && n.getType() != Node.NodeType.FEEDER)
                .forEach(n -> nodeRemainingSlots.put(n, n.getCardinality(voltageLevelGraph)));
        elaborateLegPrimaryBlock(busCell, nodeRemainingSlots, blocks);
        elaborateFeederPrimaryBlock(busCell, nodeRemainingSlots, blocks);
        rElaborateBodyPrimaryBlocks(busCell, blocks.get(0).getEndingNode(), nodeRemainingSlots, blocks); // the first block is a LegPrimaryBlock, the endingNode is a good start

        return blocks;
    }

    private static void mergeBlocks(VoltageLevelGraph vlGraph, BusCell busCell, List<Block> blocks, boolean exceptionIfPatternNotHandled) {
        List<LegPrimaryBlock> legPrimaryBlocks = blocks.stream().filter(LegPrimaryBlock.class::isInstance).map(LegPrimaryBlock.class::cast).collect(Collectors.toList());
        List<FeederPrimaryBlock> feederPrimaryBlocks = blocks.stream().filter(FeederPrimaryBlock.class::isInstance).map(FeederPrimaryBlock.class::cast).collect(Collectors.toList());

        // Merge blocks to obtain a hierarchy of blocks
        while (blocks.size() != 1) {
            boolean merged = searchParallelMerge(blocks);
            merged |= searchSerialMerge(vlGraph, blocks);
            if (!merged) {
                if (exceptionIfPatternNotHandled) {
                    throw new PowsyblException("Blocks detection impossible for cell " + busCell);
                } else {
                    LOGGER.error("{} busCell, cannot merge any additional blocks, {} blocks remains", busCell.getType(), blocks.size());
                    Block undefinedBlock = new UndefinedBlock(new ArrayList<>(blocks));
                    blocks.clear();
                    blocks.add(undefinedBlock);
                    break;
                }
            }
        }
        busCell.blocksSetting(blocks.get(0), legPrimaryBlocks, feederPrimaryBlocks);
    }

    /**
     * Search possibility to merge two blocks into a chain layout.block and do the merging
     *
     * @param vlGraph VoltageLevelGraph
     * @param blocks list of blocks we can merge
     */
    private static boolean searchSerialMerge(VoltageLevelGraph vlGraph, List<Block> blocks) {
        int i = 0;
        boolean identifiedMerge = false;

        while (i < blocks.size() - 1) {
            List<Block> blockToRemove = new ArrayList<>();
            boolean chainIdentified = false;
            Block b1 = blocks.get(i);
            SerialBlock serialBlock = new SerialBlock(b1);
            for (int j = i + 1; j < blocks.size(); j++) {
                Block b2 = blocks.get(j);
                if (serialBlock.addSubBlock(vlGraph, b2)) {
                    chainIdentified = true;
                    blockToRemove.add(b2);
                }
            }
            if (chainIdentified) {
                blockToRemove.add(b1);
                identifiedMerge = true;
                blocks.removeAll(blockToRemove);
                blocks.add(i, serialBlock);
                i = -1; // start again from the beginning
            }
            i++;
        }
        return identifiedMerge;
    }

    /**
     * Search possibility to merge some blocks into a parallel layout.block and do the merging
     *
     * @param blocks list of blocks we can merge
     */
    private static boolean searchParallelMerge(List<Block> blocks) {
        List<List<Block>> blocksBundlesToMerge = new ArrayList<>();
        Node commonNode;
        int i = 0;
        while (i < blocks.size()) {
            List<Block> blocksBundle = new ArrayList<>();
            for (int j = i + 1; j < blocks.size(); j++) {
                commonNode = checkParallelCriteria(blocks.get(i), blocks.get(j));
                if (commonNode != null) {
                    blocksBundle.add(blocks.get(j));
                }
            }
            if (blocksBundle.isEmpty()) {
                i++;
            } else {
                blocksBundle.add(0, blocks.get(i));
                blocks.removeAll(blocksBundle);
                blocksBundlesToMerge.add(blocksBundle);
            }
        }
        for (List<Block> blocksBundle : blocksBundlesToMerge) {
            Block parallelBlock;
            if (blocksBundle.stream().anyMatch(b -> !(b instanceof LegPrimaryBlock))) {
                parallelBlock = new BodyParallelBlock(blocksBundle, true);
            } else {
                parallelBlock = new LegParallelBlock(blocksBundle, true);
            }
            blocks.add(parallelBlock);
        }
        return !blocksBundlesToMerge.isEmpty();
    }

    /**
     * Compare two blocks to see if they are parallel
     *
     * @param block1 layout.block
     * @param block2 layout.block
     * @return true if the two blocks are similar : same start and end
     */
    private static Node checkParallelCriteria(Block block1, Block block2) {
        Node s1 = block1.getExtremityNode(Block.Extremity.START);
        Node e1 = block1.getExtremityNode(Block.Extremity.END);
        Node s2 = block2.getExtremityNode(Block.Extremity.START);
        Node e2 = block2.getExtremityNode(Block.Extremity.END);

        if (s1.checkNodeSimilarity(s2) && e1.checkNodeSimilarity(e2)
                || s1.checkNodeSimilarity(e2) && e1.checkNodeSimilarity(s2)) {
            if (s1.equals(s2) || s1.equals(e2)) {
                return s1;
            } else {
                return e1;
            }
        } else {
            return null;
        }
    }

    private static void addNodeInBlockNodes(Map<Node, Integer> nodeRemainingSlots, List<Node> nodes, Node node, int weight) {
        nodes.add(node);
        nodeRemainingSlots.computeIfPresent(node, (n, remainingSlots) -> remainingSlots - weight);
    }

    private static void elaborateLegPrimaryBlock(BusCell busCell, Map<Node, Integer> nodeRemainingSlots, List<Block> blocks) {
        for (BusNode busNode : busCell.getBusNodes()) {
            for (Node busConnection : busCell.getInternalAdjacentNodes(busNode)) {
                List<Node> legPrimaryBlockNodes = new ArrayList<>();
                addNodeInBlockNodes(nodeRemainingSlots, legPrimaryBlockNodes, busNode, 1);
                addNodeInBlockNodes(nodeRemainingSlots, legPrimaryBlockNodes, busConnection, 2);
                addNodeInBlockNodes(nodeRemainingSlots, legPrimaryBlockNodes, getNextNode(busConnection, busNode), 1);
                blocks.add(new LegPrimaryBlock(legPrimaryBlockNodes));
            }
        }
    }

    private static void elaborateFeederPrimaryBlock(BusCell busCell, Map<Node, Integer> nodeRemainingSlots, List<Block> blocks) {
        for (FeederNode feederNode : busCell.getFeederNodes()) {
            for (Node feederConnection : busCell.getInternalAdjacentNodes(feederNode)) {
                List<Node> feederPrimaryBlockNodes = new ArrayList<>();
                addNodeInBlockNodes(nodeRemainingSlots, feederPrimaryBlockNodes, feederNode, 1);
                addNodeInBlockNodes(nodeRemainingSlots, feederPrimaryBlockNodes, feederConnection, 1);
                blocks.add(new FeederPrimaryBlock(feederPrimaryBlockNodes));
            }
        }
    }

    private static boolean checkRemainingSlots(Map<Node, Integer> nodeRemainingSlots, Node node, int greaterOrEqVal) {
        return nodeRemainingSlots.containsKey(node) && nodeRemainingSlots.get(node) >= greaterOrEqVal;
    }

    /**
     * Search for primary blocks. A {@link PrimaryBlock} is identified when the the following pattern is detected:
     * BUS|FICTITIOUS|FEEDER|SHUNT - n * SWITCH - BUS|FICTITIOUS|FEEDER|SHUNT (with n >= 0).
     * When there is one BUS, it is instantiated as LegPrimaryBlock with this pattern (only allowed pattern with a bus):
     * BUS - SWITCH - FICTITIOUS.
     * When there is one FEEDER, it is instantiated as FeederPrimaryBlock with this pattern (only allowed pattern with a
     * feeder): FICTITIOUS - FEEDER.
     * Otherwise it is instantiated as BodyPrimaryBlock.
     * @param busCell         the busCell on which we elaborate primary blocks
     * @param entryNode       first node for the primary block (non-switch node)
     * @param nodeRemainingSlots map of the nodes to organize with their number of available belongings to PrimaryBlocks
     * @param blocks          the list of elaborated primary blocks
     */
    private static void rElaborateBodyPrimaryBlocks(BusCell busCell, Node entryNode,
                                                    Map<Node, Integer> nodeRemainingSlots, List<Block> blocks) {

        if (checkRemainingSlots(nodeRemainingSlots, entryNode, 1)) {
            for (Node node : busCell.getInternalAdjacentNodes(entryNode)) {
                if (checkRemainingSlots(nodeRemainingSlots, node, 1)) {
                    List<Node> primaryPattern = pileUp2adjNodes(entryNode, node, nodeRemainingSlots);
                    blocks.add(BodyPrimaryBlock.createBodyPrimaryBlockInBusCell(primaryPattern));
                    Node lastNode = primaryPattern.get(primaryPattern.size() - 1);
                    rElaborateBodyPrimaryBlocks(busCell, lastNode, nodeRemainingSlots, blocks);
                }
            }
        }
    }

    /**
     * Pile up switches encountered in given array, starting from node in the direction opposite to parentNode, stopping
     * when encountering a node which is not a switch and returning that node.
     * @param parentNode Parent node for direction
     * @param node Node on which to start
     * @param nodeRemainingSlots map of the nodes to organize with their number of available belongings to PrimaryBlocks
     * @return list of nodes that constitute a BodyPrimaryBlockPattern
     */
    private static List<Node> pileUp2adjNodes(Node parentNode, Node node, Map<Node, Integer> nodeRemainingSlots) {
        List<Node> nodes = new ArrayList<>();
        addNodeInBlockNodes(nodeRemainingSlots, nodes, parentNode, 1);
        Node parentCurrentNode = parentNode;
        Node currentNode = node;
        while (currentNode.getAdjacentNodes().size() == 2 && checkRemainingSlots(nodeRemainingSlots, currentNode, 2)) {
            addNodeInBlockNodes(nodeRemainingSlots, nodes, currentNode, 2); // the node is not an extremity of the PrimaryBlock -> 2 slots are taken
            Node nextNode = getNextNode(currentNode, parentCurrentNode);
            parentCurrentNode = currentNode;
            currentNode = nextNode;
        }
        addNodeInBlockNodes(nodeRemainingSlots, nodes, currentNode, 1);
        return nodes;
    }

    private static Node getNextNode(Node currentNode, Node parentCurrentNode) {
        List<Node> adjacentNodes = currentNode.getAdjacentNodes();
        return adjacentNodes.get(adjacentNodes.get(0).equals(parentCurrentNode) ? 1 : 0);
    }
}
