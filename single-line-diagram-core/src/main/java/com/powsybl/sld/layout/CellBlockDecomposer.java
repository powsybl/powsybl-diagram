/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contain function to dispose components of cells based on Hierarchical Layout
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CellBlockDecomposer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellBlockDecomposer.class);

    /**
     * Search BlockPrimary and build Block hierarchy by merging blocks together; also
     * list blocks connected to busbar
     *
     * @param cell Cell we are working on
     */
    public void determineBlocks(Cell cell) {
        if (cell.getType() == Cell.CellType.SHUNT) {
            determineShuntCellBlocks((ShuntCell) cell);
        } else {
            determineBusCellBlocks((BusCell) cell);
        }
    }

    private void determineBusCellBlocks(BusCell busCell) {
        if (busCell.getType() == Cell.CellType.INTERN && busCell.getNodes().size() == 3) {
            SwitchNode switchNode = (SwitchNode) busCell.getNodes().get(1);
            busCell.getGraph().extendSwitchBetweenBus(switchNode);
            List<Node> adj = switchNode.getAdjacentNodes();
            busCell.addNodes(adj);
            busCell.addNodes(adj.stream()
                    .flatMap(node -> node.getAdjacentNodes().stream())
                    .filter(node -> node != switchNode)
                    .collect(Collectors.toList()));
        }
        determineComplexCell(busCell);
    }

    private void determineShuntCellBlocks(ShuntCell shuntCell) {
        BodyPrimaryBlock bpy = new BodyPrimaryBlock(shuntCell.getNodes(), shuntCell);
        shuntCell.setRootBlock(bpy);
    }

    private void determineComplexCell(BusCell busCell) {
        List<Block> blocks = createPrimaryBlock(busCell);
        mergeBlocks(busCell, blocks);
    }

    private List<Block> createPrimaryBlock(BusCell busCell) {
        List<Node> alreadyTreated = new ArrayList<>();
        List<Block> blocks = new ArrayList<>();
        Node currentNode = busCell.getBusNodes().get(0);

        // Search all primary blocks
        currentNode.getListNodeAdjInCell(busCell).forEach(n -> {
            if (!alreadyTreated.contains(n)) {
                List<Node> blockNodes = new ArrayList<>();
                blockNodes.add(currentNode);
                rElaboratePrimaryBlocks(busCell, n, currentNode, alreadyTreated, blockNodes, blocks);
            }
        });
        return blocks;
    }

    private void mergeBlocks(BusCell busCell, List<Block> blocks) {
        // Search all blocks connected to a busbar inside the primary blocks list
        List<LegPrimaryBlock> primaryLegBlocks = blocks.stream()
                .filter(b -> b instanceof LegPrimaryBlock)
                .map(LegPrimaryBlock.class::cast)
                .collect(Collectors.toList());

        // Merge blocks to obtain a hierarchy of blocks
        while (blocks.size() != 1) {
            boolean merged = searchParallelMerge(blocks, busCell);
            merged |= searchSerialMerge(blocks, busCell);
            if (!merged) {
                LOGGER.warn("{} busCell, cannot merge any additional blocks, {} blocks remains", busCell.getType(), blocks.size());
                Block undefinedBlock = new UndefinedBlock(new ArrayList<>(blocks), busCell);
                blocks.clear();
                blocks.add(undefinedBlock);
                break;
            }
        }
        busCell.blocksSetting(blocks.get(0), primaryLegBlocks);
    }

    /**
     * Search possibility to merge two blocks into a chain layout.block and do the merging
     *
     * @param blocks list of blocks we can merge
     * @param cell   current cell
     */
    private boolean searchSerialMerge(List<Block> blocks, Cell cell) {
        int i = 0;
        boolean identifiedMerge = false;

        while (i < blocks.size()) {
            List<Block> blockToRemove = new ArrayList<>();
            boolean chainIdentified = false;
            Block b1 = blocks.get(i);
            SerialBlock serialBlock = new SerialBlock(b1, cell);
            for (int j = i + 1; j < blocks.size(); j++) {
                Block b2 = blocks.get(j);
                if (serialBlock.addSubBlock(b2)) {
                    chainIdentified = true;
                    blockToRemove.add(b2);
                }
            }
            if (chainIdentified) {
                blockToRemove.add(b1);
                identifiedMerge = true;
                blocks.removeAll(blockToRemove);
                blocks.add(i, serialBlock);
            }
            i++;
        }
        return identifiedMerge;
    }


    /**
     * Search possibility to merge some blocks into a parallel layout.block and do the merging
     *
     * @param blocks list of blocks we can merge
     * @param cell   current cell
     */
    private boolean searchParallelMerge(List<Block> blocks, Cell cell) {
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
                blocksBundle.add(blocks.get(i));
                blocks.removeAll(blocksBundle);
                blocksBundlesToMerge.add(blocksBundle);
            }
        }
        for (List<Block> blocksBundle : blocksBundlesToMerge) {
            ParallelBlock bPar;
            if (blocksBundle.stream().anyMatch(b -> !(b instanceof LegPrimaryBlock))) {
                bPar = new BodyParallelBlock(blocksBundle, cell, true);
            } else {
                bPar = new LegParralelBlock(blocksBundle, cell, true);
            }
            blocks.add(bPar);
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
    private Node checkParallelCriteria(Block block1, Block block2) {
        Node s1 = block1.getExtremityNode(Block.Extremity.START);
        Node e1 = block1.getExtremityNode(Block.Extremity.END);
        Node s2 = block2.getExtremityNode(Block.Extremity.START);
        Node e2 = block2.getExtremityNode(Block.Extremity.END);

        if ((s1.checkNodeSimilarity(s2) && e1.checkNodeSimilarity(e2))
                || (s1.checkNodeSimilarity(e2) && e1.checkNodeSimilarity(s2))) {
            if (s1.equals(s2) || s1.equals(e2)) {
                return s1;
            } else {
                return e1;
            }
        } else {
            return null;
        }
    }

    /**
     * Search for primaryBlock
     * a primaryBlock is identified when the the following pattern is detected:
     * BUS|FICTICIOUS|FEEDER|SHUNT - n * SWITCH - BUS|FICTICIOUS|FEEDER|SHUNT
     * when there is one BUS, it is instanciated as PrimaryLegBlock with this pattern (only allowed pattern with a bus) :
     * BUS - SWITCH - FICTICIOUS
     * otherwise it is instanciated as PrimaryBodyBlock
     *
     * @param cell           cell
     * @param currentNode    currentnode
     * @param alreadyTreated alreadyTreated
     * @param blockNodes     blockNodes
     * @param blocks         blocks
     */
    private void rElaboratePrimaryBlocks(Cell cell, Node currentNode, Node parentNode,
                                         List<Node> alreadyTreated,
                                         List<Node> blockNodes,
                                         List<Block> blocks) {
        Node currentNode2 = currentNode;
        Node parentNode2 = parentNode;

        alreadyTreated.add(currentNode2);
        blockNodes.add(currentNode2);
        while (currentNode2.getType() == Node.NodeType.SWITCH) {
            Node nextNode = currentNode2.getAdjacentNodes().get(
                    currentNode2.getAdjacentNodes().get(0).equals(parentNode2) ? 1 : 0);
            parentNode2 = currentNode2;
            currentNode2 = nextNode;
            if (currentNode2.getType() != Node.NodeType.BUS) {
                alreadyTreated.add(currentNode2);
            }
            blockNodes.add(currentNode2);
        }
        PrimaryBlock b;
        if (blockNodes.stream().anyMatch(node -> node.getType() == Node.NodeType.BUS)) {
            b = new LegPrimaryBlock(blockNodes, cell);
        } else {
            b = new BodyPrimaryBlock(blockNodes, cell);
        }
        blocks.add(b);
        // If we did'nt reach a Busbar, continue to search for other
        // blocks
        if (currentNode2.getType() != Node.NodeType.BUS) {
            Node finalCurrentNode = currentNode2;
            currentNode2.getListNodeAdjInCell(cell)
                    .filter(node -> !alreadyTreated.contains(node) && !blockNodes.contains(node))
                    .forEach(node -> {
                        List<Node> blockNode = new ArrayList<>();
                        blockNode.add(finalCurrentNode);
                        rElaboratePrimaryBlocks(cell, node, finalCurrentNode, alreadyTreated, blockNode, blocks);
                    });
        }
    }
}
