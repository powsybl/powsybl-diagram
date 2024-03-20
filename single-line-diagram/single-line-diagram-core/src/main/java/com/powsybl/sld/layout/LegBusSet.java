/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.blocks.LegParallelBlock;
import com.powsybl.sld.model.blocks.LegPrimaryBlock;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A LegBusSet contains the set of BusNodes that shall be vertically presented, and the cells that have a pattern of
 * connection included in the busNodeSet. It is embedded into a LBSCluster. It contains links to all the other
 * LegBusSet of the Graph.
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
public final class LegBusSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegBusSet.class);

    private final Set<BusNode> busNodeSet;
    private final Set<BusNode> extendedNodeSet;
    private final Set<ExternCell> externCells = new LinkedHashSet<>();

    private final Set<InternCellSide> internCellSides = new LinkedHashSet<>();

    private LegBusSet(Map<BusNode, Integer> nodeToNb, List<BusNode> busNodes) {
        busNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
        extendedNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
        busNodeSet.addAll(busNodes);
    }

    private LegBusSet(Map<BusNode, Integer> nodeToNb, ExternCell cell) {
        this(nodeToNb, cell.getBusNodes());
        externCells.add(cell);
    }

    private LegBusSet(Map<BusNode, Integer> nodeToNb, ShuntCell cell) {
        this(nodeToNb, cell.getParentBusNodes());
        externCells.addAll(cell.getSideCells());
    }

    private LegBusSet(Map<BusNode, Integer> nodeToNb, InternCell internCell, Side side) {
        this(nodeToNb, internCell.getSideBusNodes(side));
        addInternCell(internCell, side);
    }

    private LegBusSet(Map<BusNode, Integer> nodeToNb, BusNode busNode) {
        this(nodeToNb, Collections.singletonList(busNode));
    }

    void addInternCell(InternCell internCell, Side side) {
        internCellSides.add(new InternCellSide(internCell, side));
    }

    private boolean contains(LegBusSet lbs) {
        return busNodeSet.containsAll(lbs.getBusNodeSet());
    }

    private void absorbs(LegBusSet lbsToAbsorb) {
        busNodeSet.addAll(lbsToAbsorb.busNodeSet);
        externCells.addAll(lbsToAbsorb.externCells);
        internCellSides.addAll(lbsToAbsorb.internCellSides);
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

    Set<InternCellSide> getInternCellSides() {
        return internCellSides;
    }

    void addToExtendedNodeSet(Collection<BusNode> busNodes) {
        if (busNodes.containsAll(busNodeSet)) {
            // The given busNodes correspond to all vertical bus nodes for a specific index of the horizontalBusLanes:
            // those nodes correspond to a slice of busbars.
            // There can't be more than one busNode per busbar index, we check this by creating the following map with
            // an exception-throwing merge method
            Map<Integer, BusNode> indexToBusNode = busNodes.stream().filter(Objects::nonNull)
                    .collect(Collectors.toMap(BusNode::getBusbarIndex, Function.identity(), this::detectConflictingBusNodes));
            extendedNodeSet.addAll(indexToBusNode.values());
        } else {
            LOGGER.error("ExtendedNodeSet inconsistent with NodeBusSet");
        }
    }

    private BusNode detectConflictingBusNodes(BusNode busNode1, BusNode busNode2) {
        throw new PowsyblException("Inconsistent legBusSet: extended node set contains two busNodes with same index");
    }

    Set<BusNode> getExtendedNodeSet() {
        return extendedNodeSet;
    }

    static List<LegBusSet> createLegBusSets(VoltageLevelGraph graph, Map<BusNode, Integer> nodeToNb, boolean handleShunts) {

        graph.getExternCellStream().toList().forEach(cell -> fixMultisectionExternCells(cell, graph));
        List<ExternCell> externCells = graph.getExternCellStream()
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .collect(Collectors.toList());

        List<LegBusSet> legBusSets = new ArrayList<>();

        if (handleShunts) {
            manageShunts(graph, externCells, legBusSets, nodeToNb);
        }

        externCells.forEach(cell -> pushLBS(legBusSets, new LegBusSet(nodeToNb, cell)));

        graph.getInternCellStream()
                .filter(cell -> cell.checkIsNotShape(InternCell.Shape.MAYBE_ONE_LEG, InternCell.Shape.UNHANDLED_PATTERN))
                .sorted(Comparator.comparing(cell -> -((InternCell) cell).getBusNodes().size())         // bigger first to identify encompassed InternCell at the end with the smaller one
                        .thenComparing(cell -> ((InternCell) cell).getFullId()))                        // avoid randomness
                .forEachOrdered(cell -> pushInternCell(legBusSets, nodeToNb, cell));

        graph.getInternCellStream()
                .filter(cell -> cell.checkIsShape(InternCell.Shape.MAYBE_ONE_LEG))
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .forEachOrdered(cell -> pushInternCell(legBusSets, nodeToNb, cell));

        // find orphan busNodes and build their LBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(legBusSets.stream().
                flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.stream()
                .sorted(Comparator.comparing(Node::getId))              //avoid randomness
                .forEach(busNode -> legBusSets.add(new LegBusSet(nodeToNb, busNode)));
        return legBusSets;
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
        if (busbarIndices.size() < busNodes.size() && busbarIndices.get(0) != 0) {
            List<LegPrimaryBlock> sortedLegs = legPrimaryBlocks.stream().sorted(Comparator.comparing(lpb -> lpb.getBusNode().getBusbarIndex())).toList();
            List<LegPrimaryBlock> legsRemoved = sortedLegs.subList(1, sortedLegs.size());

            LegPrimaryBlock legKept = sortedLegs.get(0);
            externCell.removeOtherLegs(legKept);
            int order = externCell.getFeederNodes().stream().map(FeederNode::getOrder).flatMap(Optional::stream).findFirst().orElse(-1);
            var direction = externCell.getFeederNodes().stream().map(FeederNode::getDirection).findFirst();

            for (LegPrimaryBlock legPrimaryBlock : legsRemoved) {
                List<Node> legNodes = legPrimaryBlock.getNodes();
                Node fork = legNodes.get(legNodes.size() - 1);
                ConnectivityNode shuntNode = graph.insertConnectivityNode(legNodes.get(legNodes.size() - 2), fork, "Shunt-" + legNodes.get(1).getId());

                ShuntCell shunt = ShuntCell.create(graph, List.of(fork, shuntNode));

                List<Node> fakeCellNodes = new ArrayList<>(legNodes.subList(0, legNodes.size() - 1));
                fakeCellNodes.add(shuntNode);
                ExternCell fakeCell = ExternCell.create(graph, fakeCellNodes, List.of(shunt));
                fakeCell.setOrder(++order);
                direction.ifPresent(fakeCell::setDirection);
                externCell.addShuntCell(shunt);

                CellBlockDecomposer.determineShuntCellBlocks(shunt);
                fakeCell.blocksSetting(new LegPrimaryBlock(fakeCellNodes), List.of(legPrimaryBlock), List.of());
            }
        }
    }

    private static void manageShunts(VoltageLevelGraph graph, List<ExternCell> externCells, List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb) {
        List<List<ShuntCell>> sameBusNodesShuntCells = graph.getShuntCellStream()
                .map(sc -> new ArrayList<>(Collections.singletonList(sc)))
                .collect(Collectors.toList());
        int i = 0;
        while (i < sameBusNodesShuntCells.size()) {
            int j = i + 1;
            while (j < sameBusNodesShuntCells.size()) {
                if (crossContains(sameBusNodesShuntCells.get(i).get(0).getParentBusNodes(),
                        sameBusNodesShuntCells.get(j).get(0).getParentBusNodes())) {
                    sameBusNodesShuntCells.get(i).addAll(sameBusNodesShuntCells.get(j));
                    sameBusNodesShuntCells.remove(j);
                } else {
                    j++;
                }
            }
            i++;
        }

        sameBusNodesShuntCells.stream().filter(scs -> scs.size() > 2).flatMap(List::stream)
                .forEach(sc -> {
                    pushLBS(legBusSets, new LegBusSet(nodeToNb, sc));
                    externCells.removeAll(sc.getSideCells());
                });
    }

    private static boolean crossContains(List<BusNode> busNodes1, List<BusNode> busNodes2) {
        return busNodes1.containsAll(busNodes2) && busNodes2.containsAll(busNodes1);
    }

    public static void pushLBS(List<LegBusSet> legBusSets, LegBusSet legBusSet) {
        for (LegBusSet lbs : legBusSets) {
            if (lbs.contains(legBusSet)) {
                lbs.absorbs(legBusSet);
                return;
            }
        }
        List<LegBusSet> absorbedLBS = new ArrayList<>();
        for (LegBusSet lbs : legBusSets) {
            if (legBusSet.contains(lbs)) {
                absorbedLBS.add(lbs);
                legBusSet.absorbs(lbs);
            }
        }
        legBusSets.removeAll(absorbedLBS);
        legBusSets.add(legBusSet);
    }

    private static void pushInternCell(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, InternCell internCell) {
        List<LegBusSet> attachedLegBusSets = new ArrayList<>();
        for (LegBusSet lbs : legBusSets) {
            boolean attachedToLbs = internCell.getBusNodes().stream().anyMatch(lbs.busNodeSet::contains);
            if (attachedToLbs) {
                attachedLegBusSets.add(lbs);
                if (lbs.busNodeSet.containsAll(internCell.getBusNodes())) {
                    lbs.addInternCell(internCell, Side.UNDEFINED);
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
            pushLBS(legBusSets, new LegBusSet(nodeToNb, internCell, Side.LEFT));
            pushLBS(legBusSets, new LegBusSet(nodeToNb, internCell, Side.RIGHT));
        } else {
            pushLBS(legBusSets, new LegBusSet(nodeToNb, internCell, Side.UNDEFINED));
        }
    }

    private static void replaceByMultilegOrSetOneLeg(InternCell internCell, List<LegBusSet> attachedLegBusSets) {
        // We consider that a one leg intern cell should not force the corresponding busNodes to be in the same LegBusSet
        // (forcing them to be parallel), hence we try to replace that one leg by a multileg
        // The goal here is to split the corresponding LegParallelBlock into 2 stacked parts
        LegParallelBlock oneLeg = (LegParallelBlock) internCell.getSideToLeg(Side.UNDEFINED);
        List<LegPrimaryBlock> subBlocks = oneLeg.getSubBlocks();
        if (subBlocks.size() == 2) {
            internCell.replaceOneLegByMultiLeg(subBlocks.get(0), subBlocks.get(1));
        } else {
            // Each subBlock has one BusNode which might be in the existing LegBusSets.
            // The LegBusSets which contain at least one BusNode from current internCell are given as attachedLegBusSets parameter.
            // We first try to split the subBlocks based on the LegBusSets
            Collection<List<LegPrimaryBlock>> groupSubBlocksLbs = subBlocks.stream().collect(Collectors.groupingBy(
                    sb -> attachedLegBusSets.stream().filter(lbs -> lbs.busNodeSet.contains(sb.getBusNode())).findFirst())).values();
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
