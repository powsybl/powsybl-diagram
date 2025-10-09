/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position;

import com.powsybl.sld.model.blocks.*;
import com.powsybl.sld.model.cells.ArchCell;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A VerticalBusSet contains the set of BusNodes that shall be vertically presented, and the cells that have a pattern of
 * connection included in the busNodeSet. It is embedded into a BSCluster.
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
public final class VerticalBusSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerticalBusSet.class);

    private final Set<BusNode> busNodeSet;
    private final Set<ExternCell> externCells = new LinkedHashSet<>();
    private final List<ArchCell> archCells = new ArrayList<>();
    private final Set<InternCellSide> internCellSides = new LinkedHashSet<>();

    private VerticalBusSet(Map<BusNode, Integer> busToNb, List<BusNode> busNodes) {
        busNodeSet = new TreeSet<>(Comparator.comparingInt(busToNb::get));
        busNodeSet.addAll(busNodes);
    }

    private VerticalBusSet(Map<BusNode, Integer> busToNb, ExternCell cell) {
        this(busToNb, cell.getBusNodes());
        externCells.add(cell);
    }

    private VerticalBusSet(Map<BusNode, Integer> nodeToNb, ArchCell cell) {
        this(nodeToNb, cell.getBusNodes());
        archCells.add(cell);
    }

    private VerticalBusSet(Map<BusNode, Integer> busToNb, InternCell internCell, Side side) {
        this(busToNb, internCell.getSideBusNodes(side));
        addInternCell(internCell, side);
    }

    private VerticalBusSet(Map<BusNode, Integer> busToNb, BusNode busNode) {
        this(busToNb, Collections.singletonList(busNode));
    }

    void addInternCell(InternCell internCell, Side side) {
        internCellSides.add(new InternCellSide(internCell, side));
    }

    private boolean contains(Collection<BusNode> busNodeCollection) {
        return busNodeSet.containsAll(busNodeCollection);
    }

    private boolean contains(VerticalBusSet vbs) {
        return contains(vbs.getBusNodeSet());
    }

    private void absorbs(VerticalBusSet vbsToAbsorb) {
        busNodeSet.addAll(vbsToAbsorb.busNodeSet);
        externCells.addAll(vbsToAbsorb.externCells);
        archCells.addAll(vbsToAbsorb.archCells);
        internCellSides.addAll(vbsToAbsorb.internCellSides);
    }

    List<InternCell> getInternCellsFromShape(InternCell.Shape shape) {
        return internCellSides.stream().map(InternCellSide::getCell)
                .filter(cell -> cell.checkIsShape(shape))
                .distinct()
                .collect(Collectors.toList());
    }

    public Set<BusNode> getBusNodeSet() {
        return busNodeSet;
    }

    public Set<ExternCell> getExternCells() {
        return externCells;
    }

    public List<ArchCell> getArchCells() {
        return archCells;
    }

    Set<InternCellSide> getInternCellSides() {
        return internCellSides;
    }

    static List<VerticalBusSet> createVerticalBusSets(VoltageLevelGraph graph, Map<BusNode, Integer> busToNb) {

        graph.getExternCellStream().toList().forEach(cell -> fixMultisectionExternCells(cell, graph));
        List<ExternCell> externCells = graph.getExternCellStream()
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .collect(Collectors.toList());

        List<VerticalBusSet> verticalBusSets = new ArrayList<>();

        externCells.forEach(cell -> pushVbs(verticalBusSets, new VerticalBusSet(busToNb, cell)));

        graph.getArchCellStream().forEach(cell -> pushVbs(verticalBusSets, new VerticalBusSet(busToNb, cell)));

        graph.getInternCellStream()
                .filter(cell -> cell.checkIsNotShape(InternCell.Shape.MAYBE_ONE_LEG, InternCell.Shape.UNHANDLED_PATTERN))
                .sorted(Comparator.comparing(cell -> -((InternCell) cell).getBusNodes().size())         // bigger first to identify encompassed InternCell at the end with the smaller one
                        .thenComparing(cell -> ((InternCell) cell).getFullId()))                        // avoid randomness
                .forEachOrdered(cell -> pushInternCell(verticalBusSets, busToNb, cell));

        graph.getInternCellStream()
                .filter(cell -> cell.checkIsShape(InternCell.Shape.MAYBE_ONE_LEG))
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .forEachOrdered(cell -> pushInternCell(verticalBusSets, busToNb, cell));

        // find orphan busNodes and build their VBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(verticalBusSets.stream().
                flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.stream()
                .sorted(Comparator.comparing(Node::getId))              //avoid randomness
                .forEach(busNode -> verticalBusSets.add(new VerticalBusSet(busToNb, busNode)));
        return verticalBusSets;
    }

    private static void fixMultisectionExternCells(ExternCell externCell, VoltageLevelGraph graph) {
        List<LegPrimaryBlock> legPrimaryBlocks = externCell.getLegPrimaryBlocks();
        List<BusNode> busNodes = legPrimaryBlocks.stream().map(LegPrimaryBlock::getBusNode).distinct().toList();

        List<Integer> busbarIndices = busNodes.stream().map(BusNode::getBusbarIndex).distinct().toList();
        // Detecting incoherent bus positions set from the user (from extension or directly when creating the
        // busNode, WHEN PositionFromExtension is used instead of PositionByClustering).
        // We rule out PositionByClustering where all busbar indices are set to zero and still are at this point
        // (note that zero means no value in the code so far) by dismissing the detection if there is a zero busbar
        // index. There cannot be any zero busbar index with PositionFromExtension, they are replaced in the call
        // PositionFromExtension::setMissingPositionIndices.
        if (busbarIndices.size() < busNodes.size() && busbarIndices.get(0) != 0
                && externCell.getRootBlock() instanceof SerialBlock rootSerialBlock) {
            List<LegPrimaryBlock> sortedLegs = legPrimaryBlocks.stream().sorted(Comparator.comparing(lpb -> lpb.getBusNode().getBusbarIndex())).toList();
            LegPrimaryBlock legKept = sortedLegs.get(0);
            int order = externCell.getFeederNodes().stream().map(FeederNode::getOrder).flatMap(Optional::stream).findFirst().orElse(-1);
            var direction = externCell.getFeederNodes().stream().map(FeederNode::getDirection).findFirst().orElse(Direction.UNDEFINED);
            if (rootSerialBlock.getLowerBlock() instanceof LegParallelBlock) {
                fixLegParallelExternCell(externCell, graph, sortedLegs, legKept, order, direction);
            } else if (rootSerialBlock.getLowerBlock() instanceof BodyParallelBlock legBodyParallelBlock) {
                fixLegBodyParallelExternCell(externCell, graph, legBodyParallelBlock, sortedLegs, legKept, order, direction);
            } else {
                externCell.setRootBlock(new UndefinedBlock(List.of(externCell.getRootBlock())));
                LOGGER.error("ExternCell pattern not handled");
            }
        }
    }

    private static void fixLegBodyParallelExternCell(ExternCell externCell, VoltageLevelGraph graph,
                                                     BodyParallelBlock legBodyParallelBlock, List<LegPrimaryBlock> sortedLegs,
                                                     LegPrimaryBlock legKept, int orderMin, Direction direction) {
        Map<LegPrimaryBlock, Block> legToSubBlock = new HashMap<>();
        Map<Block, List<LegPrimaryBlock>> subBlockToLegs = new HashMap<>();
        for (LegPrimaryBlock legPrimaryBlock : sortedLegs) {
            Block ancestor = legPrimaryBlock;
            while (ancestor.getParentBlock() != legBodyParallelBlock) {
                ancestor = ancestor.getParentBlock();
            }
            legToSubBlock.put(legPrimaryBlock, ancestor);
            subBlockToLegs.computeIfAbsent(ancestor, b -> new ArrayList<>()).add(legPrimaryBlock);
        }

        Block subBlockKept = legToSubBlock.get(legKept);
        List<Block> subBlocksRemoved = legBodyParallelBlock.getSubBlocks().stream().filter(b -> b != subBlockKept).toList();

        externCell.removeOtherLegs(subBlockKept, legKept);

        Node fork = subBlockKept.getEndingNode();
        int order = orderMin;

        for (int i = 0; i < subBlocksRemoved.size(); i++) {

            Block sBlock = subBlocksRemoved.get(i);
            ConnectivityNode archNode = NodeFactory.createConnectivityNode(graph, "Arch" + i + "_" + fork.getId());
            substituteForkNode(graph, sBlock, archNode, fork);

            if (sBlock instanceof LegPrimaryBlock lpb) {
                // If one of the detached subBlocks is a LegPrimaryBlock, we need to replace it by a SerialBlock so that
                // it gets properly displayed. On extra node needs to be added for the stack line
                ConnectivityNode hookNode = graph.insertConnectivityNode(archNode, fork, archNode.getId() + "_hook");
                BodyPrimaryBlock body = BodyPrimaryBlock.createBodyPrimaryBlockInBusCell(List.of(hookNode, archNode));
                sBlock = new SerialBlock(List.of(lpb, body));
                subBlockToLegs.put(sBlock, subBlockToLegs.get(lpb));
            }

            ArchCell archCell = ArchCell.create(graph, sBlock.getNodeStream().toList(), subBlockKept);
            archCell.setOrder(++order);
            archCell.setDirection(direction);

            archCell.blocksSetting(sBlock, subBlockToLegs.get(sBlock), List.of());
        }
    }

    private static void substituteForkNode(VoltageLevelGraph graph, Block block, ConnectivityNode substitute, Node fork) {
        block.replaceEndingNode(substitute);
        List<Edge> edgesToTransfer = new ArrayList<>(fork.getAdjacentEdges()).stream()
                .filter(edge -> block.contains(edge.getOppositeNode(fork)))
                .toList();
        graph.transferEdges(fork, substitute, edgesToTransfer);
        graph.addEdge(fork, substitute);
    }

    private static void fixLegParallelExternCell(ExternCell externCell, VoltageLevelGraph graph,
                                                 List<LegPrimaryBlock> sortedLegs, LegPrimaryBlock legKept,
                                                 int orderMin, Direction direction) {
        List<LegPrimaryBlock> legsRemoved = sortedLegs.subList(1, sortedLegs.size());

        externCell.removeOtherLegs(legKept);

        int order = orderMin;
        for (LegPrimaryBlock legPrimaryBlock : legsRemoved) {
            List<Node> legNodes = legPrimaryBlock.getNodes();
            Node fork = legNodes.get(legNodes.size() - 1);
            ConnectivityNode archNode = graph.insertConnectivityNode(legNodes.get(legNodes.size() - 2), fork, "Arch_" + legNodes.get(1).getId());

            List<Node> fakeCellNodes = new ArrayList<>(legNodes.subList(0, legNodes.size() - 1));
            fakeCellNodes.add(archNode);
            ArchCell archCell = ArchCell.create(graph, fakeCellNodes, legKept);
            archCell.setOrder(++order);
            archCell.setDirection(direction);

            archCell.blocksSetting(new LegPrimaryBlock(fakeCellNodes), List.of(legPrimaryBlock), List.of());
        }
    }

    public static void pushVbs(List<VerticalBusSet> verticalBusSets, VerticalBusSet verticalBusSet) {
        for (VerticalBusSet vbs : verticalBusSets) {
            if (vbs.contains(verticalBusSet)) {
                vbs.absorbs(verticalBusSet);
                return;
            }
        }
        List<VerticalBusSet> absorbedVbs = new ArrayList<>();
        for (VerticalBusSet vbs : verticalBusSets) {
            if (verticalBusSet.contains(vbs)) {
                absorbedVbs.add(vbs);
                verticalBusSet.absorbs(vbs);
            }
        }
        verticalBusSets.removeAll(absorbedVbs);
        verticalBusSets.add(verticalBusSet);
    }

    private static void pushInternCell(List<VerticalBusSet> verticalBusSets, Map<BusNode, Integer> nodeToNb, InternCell internCell) {
        List<VerticalBusSet> attachedLegBusSets = new ArrayList<>();
        for (VerticalBusSet vbs : verticalBusSets) {
            boolean attachedToLbs = internCell.getBusNodes().stream().anyMatch(vbs.busNodeSet::contains);
            if (attachedToLbs) {
                attachedLegBusSets.add(vbs);
                if (vbs.busNodeSet.containsAll(internCell.getBusNodes())) {
                    vbs.addInternCell(internCell, Side.UNDEFINED);
                    if (internCell.getShape() == InternCell.Shape.MAYBE_ONE_LEG) {
                        internCell.setShape(InternCell.Shape.ONE_LEG);
                    } else {
                        internCell.setShape(InternCell.Shape.VERTICAL);
                    }
                    return;
                }
            }
        }

        // We didn't find any legBusSet which absorbs the intern cell
        if (internCell.getShape() == InternCell.Shape.MAYBE_ONE_LEG) {
            replaceByMultilegOrSetOneLeg(internCell, attachedLegBusSets);
        }
        if (internCell.getShape() != InternCell.Shape.ONE_LEG) {
            pushVbs(verticalBusSets, new VerticalBusSet(nodeToNb, internCell, Side.LEFT));
            pushVbs(verticalBusSets, new VerticalBusSet(nodeToNb, internCell, Side.RIGHT));
        } else {
            pushVbs(verticalBusSets, new VerticalBusSet(nodeToNb, internCell, Side.UNDEFINED));
        }
    }

    private static void replaceByMultilegOrSetOneLeg(InternCell internCell, List<VerticalBusSet> attachedVerticalBusSets) {
        // We consider that a one leg intern cell should not force the corresponding busNodes to be in the same LegBusSet
        // (forcing them to be parallel), hence we try to replace that one leg by a multileg
        // The goal here is to split the corresponding LegParallelBlock into 2 stacked parts
        LegParallelBlock oneLeg = (LegParallelBlock) internCell.getSideToLeg(Side.UNDEFINED);
        List<LegPrimaryBlock> subBlocks = oneLeg.getSubBlocks();
        if (subBlocks.size() == 2) {
            internCell.replaceOneLegByMultiLeg(subBlocks.get(0), subBlocks.get(1));
        } else {
            // Each subBlock has one BusNode which might be in the existing LegBusSets.
            // The LegBusSets which contain at least one BusNode from current internCell are given as attachedVerticalBusSets parameter.
            // We first try to split the subBlocks based on the LegBusSets
            Collection<List<LegPrimaryBlock>> groupSubBlocksLbs = subBlocks.stream().collect(Collectors.groupingBy(
                    sb -> attachedVerticalBusSets.stream().filter(lbs -> lbs.busNodeSet.contains(sb.getBusNode())).findFirst())).values();
            if (groupSubBlocksLbs.size() == 2) {
                replaceByMultiLeg(internCell, groupSubBlocksLbs);
            } else {
                // We then try to split the subBlocks using the sectionIndex of their busNode
                Collection<List<LegPrimaryBlock>> groupSubBlocksSi = subBlocks.stream().collect(Collectors.groupingBy(
                        sb -> sb.getBusNode().getSectionIndex())).values();
                if (groupSubBlocksSi.size() == 2) {
                    replaceByMultiLeg(internCell, groupSubBlocksSi);
                } else {
                    // Failed to replace it by a multileg -> marks it one leg
                    internCell.setOneLeg();
                }
            }
        }
    }

    private static void replaceByMultiLeg(InternCell internCell, Collection<List<LegPrimaryBlock>> groupSubBlocks) {
        var it = groupSubBlocks.iterator();
        List<LegPrimaryBlock> left = it.next();
        List<LegPrimaryBlock> right = it.next();
        internCell.replaceOneLegByMultiLeg(
                left.size() == 1 ? left.get(0) : new LegParallelBlock(left, true),
                right.size() == 1 ? right.get(0) : new LegParallelBlock(right, true));
    }
}
