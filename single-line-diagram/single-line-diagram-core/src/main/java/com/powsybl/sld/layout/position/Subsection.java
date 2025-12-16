/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.blocks.BodyPrimaryBlock;
import com.powsybl.sld.model.cells.ArchCell;
import com.powsybl.sld.model.cells.BusCell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.util.GraphTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.model.coordinate.Side.LEFT;
import static com.powsybl.sld.model.coordinate.Side.RIGHT;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */

public class Subsection {

    private final int size;
    private final BusNode[] busNodes;
    private final Set<InternCellSide> internCellSides = new LinkedHashSet<>();
    private final List<ExternCell> externCells = new ArrayList<>();
    private final List<ArchCell> archCells = new ArrayList<>();
    private static final Comparator<BusCell> COMPARE_ORDER = Comparator
            .comparingInt(extCell -> extCell.getOrder().orElse(-1));
    private static final Logger LOGGER = LoggerFactory.getLogger(Subsection.class);

    Subsection(int size) {
        this.size = size;
        busNodes = new BusNode[size];
    }

    private boolean checkAbsorbability(Set<BusNode> extendedNodeSet) {
        return extendedNodeSet.stream().noneMatch(busNode -> {
            int vIndex = busNode.getBusbarIndex() - 1;
            return busNodes[vIndex] != null && busNodes[vIndex] != busNode;
        });
    }

    private void addVerticalBusSet(VerticalBusSet vbs, Set<BusNode> extendedNodeSet) {
        extendedNodeSet.forEach(bus -> busNodes[bus.getBusbarIndex() - 1] = bus);
        externCells.addAll(vbs.getExternCells());
        externCells.sort(COMPARE_ORDER);
        archCells.addAll(vbs.getArchCells());
        archCells.sort(COMPARE_ORDER);
        internCellSides.addAll(vbs.getInternCellSides());
    }

    public int getSize() {
        return size;
    }

    public BusNode[] getBusNodes() {
        return busNodes;
    }

    BusNode getBusNode(int index) {
        return busNodes[index];
    }

    List<InternCell> getInternCells(InternCell.Shape shape, Side side) {
        return internCellSides.stream()
                .filter(ics -> ics.getCell().checkIsShape(shape) && ics.getSide() == side)
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    List<InternCell> getVerticalInternCells() {
        return internCellSides.stream()
                .filter(ics -> ics.getCell().checkIsShape(InternCell.Shape.VERTICAL)
                        || ics.getCell().checkIsShape(InternCell.Shape.ONE_LEG))
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    public List<ExternCell> getExternCells() {
        return externCells;
    }

    public List<ArchCell> getArchCells() {
        return archCells;
    }

    private boolean containsAllBusNodes(List<BusNode> nodes) {
        return new HashSet<>(Arrays.asList(busNodes)).containsAll(nodes);
    }

    static List<Subsection> createSubsections(VoltageLevelGraph graph, BSCluster bsCluster, Map<BusNode, Integer> busToNb, boolean handleShunts) {
        List<Subsection> subsections = new ArrayList<>();

        int vSize = graph.getMaxVerticalBusPosition();
        Subsection currentSubsection = new Subsection(vSize);
        subsections.add(currentSubsection);
        int i = 0;
        for (VerticalBusSet vbs : bsCluster.getVerticalBusSets()) {
            Set<BusNode> extendedNodeSet = new TreeSet<>(Comparator.comparingInt(busToNb::get));
            List<BusNode> vbn = bsCluster.getVerticalBusNodes(i);
            if (vbs.getBusNodeSet().containsAll(vbn)) {
                // The given busNodes correspond to all vertical bus nodes for a specific index of the horizontalBusLanes:
                // those nodes correspond to a slice of busbars.
                // There can't be more than one busNode per busbar index, we check this by creating a HashSet with busbar indices
                Set<Integer> indices = new HashSet<>();
                for (BusNode busNode : vbn) {
                    if (!indices.add(busNode.getBusbarIndex())) {
                        throw new PowsyblException("Inconsistent legBusSet: extended node set contains two busNodes with same index");
                    }
                }
                extendedNodeSet.addAll(vbn);
            } else {
                LOGGER.error("ExtendedNodeSet inconsistent with NodeBusSet");
            }
            if (!currentSubsection.checkAbsorbability(extendedNodeSet)) {
                currentSubsection = new Subsection(vSize);
                subsections.add(currentSubsection);
            }
            currentSubsection.addVerticalBusSet(vbs, extendedNodeSet);
            i++;
        }

        internCellCoherence(graph, bsCluster.getVerticalBusSets(), subsections);

        graph.getShuntCellStream().forEach(ShuntCell::alignExternCells);
        if (handleShunts) {
            shuntCellCoherence(graph, subsections);
        }

        return subsections;
    }

    private static void internCellCoherence(VoltageLevelGraph vlGraph, List<VerticalBusSet> vbsList, List<Subsection> subsections) {
        identifyOneLegInternCells(vlGraph, subsections);
        identifyVerticalInternCells(vlGraph, subsections);
        identifyFlatInternCells(vbsList);
        identifyCrossOverAndCheckOrientation(subsections);
        slipInternCellSideToEdge(subsections);
    }

    private static void identifyOneLegInternCells(VoltageLevelGraph graph, List<Subsection> subsections) {
        graph.getInternCellStream()
                .filter(c -> c.checkIsShape(InternCell.Shape.MAYBE_FLAT, InternCell.Shape.UNDEFINED))
                .filter(c -> c.getBodyBlock() instanceof BodyPrimaryBlock bpb && bpb.getNodes().size() == 1 && bpb.getNodes().getFirst() instanceof ConnectivityNode)
                .forEach(c -> subsections.stream().filter(subsection -> subsection.containsAllBusNodes(c.getBusNodes())).findFirst().ifPresent(s -> {
                    c.replaceBackMultiLegByOneLeg();
                    s.internCellSides.removeIf(ics -> ics.getCell() == c);
                    s.internCellSides.add(new InternCellSide(c, Side.UNDEFINED));
                }));
    }

    private static void identifyVerticalInternCells(VoltageLevelGraph graph, List<Subsection> subsections) {
        Map<InternCell, Subsection> verticalCells = new LinkedHashMap<>();

        graph.getInternCellStream()
                .filter(c -> c.checkIsNotShape(InternCell.Shape.ONE_LEG, InternCell.Shape.UNHANDLED_PATTERN))
                .forEach(c ->
                        subsections.stream()
                                .filter(subsection -> subsection.containsAllBusNodes(c.getBusNodes()))
                                .findFirst().ifPresent(subsection -> verticalCells.putIfAbsent(c, subsection)));

        verticalCells.forEach((cell, sub) -> {
            cell.setShape(InternCell.Shape.VERTICAL);
            sub.internCellSides.removeIf(ics -> ics.getCell() == cell);
            sub.internCellSides.add(new InternCellSide(cell, Side.UNDEFINED));
        });

    }

    private static void identifyFlatInternCells(List<VerticalBusSet> vbsList) {
        vbsList.stream()
                .flatMap(vbs -> vbs.getInternCellsFromShape(InternCell.Shape.MAYBE_FLAT).stream())
                .distinct()
                .forEach(internCell -> {
                    List<BusNode> buses = internCell.getBusNodes();
                    if (Math.abs(buses.get(1).getSectionIndex() - buses.get(0).getSectionIndex()) == 1 && buses.get(1).getBusbarIndex() == buses.get(0).getBusbarIndex()) {
                        internCell.setFlat();
                        internCell.getRootBlock().setOrientation(Orientation.RIGHT);
                    } else {
                        internCell.setShape(InternCell.Shape.CROSSOVER);
                    }
                });
    }

    private static void identifyCrossOverAndCheckOrientation(List<Subsection> subsections) {
        record SideSs(Side side, Subsection ss) {
        }

        Map<InternCell, List<SideSs>> cellToSideSs = new LinkedHashMap<>();
        for (Subsection ss : subsections) {
            ss.internCellSides.stream()
                    .filter(ics -> {
                        InternCell.Shape shape = ics.getCell().getShape();
                        return shape == InternCell.Shape.UNDEFINED
                                || shape == InternCell.Shape.FLAT
                                || shape == InternCell.Shape.CROSSOVER;
                    })
                    .forEach(ics -> {
                        cellToSideSs.putIfAbsent(ics.getCell(), new ArrayList<>());
                        cellToSideSs.get(ics.getCell()).add(new SideSs(ics.getSide(), ss));
                    });
        }
        cellToSideSs.forEach((cell, sideSses) -> {
            if (sideSses.size() == 2) {
                if (!cell.checkIsShape(InternCell.Shape.FLAT)) {
                    cell.setShape(InternCell.Shape.CROSSOVER);
                }
                if (sideSses.getFirst().side == RIGHT) {
                    cell.reverseCell();
                    sideSses.stream().flatMap(sss -> sss.ss.internCellSides.stream())
                            .filter(ics -> ics.getCell() == cell)
                            .forEach(InternCellSide::flipSide);
                }
            }
        });
    }

    private static void slipInternCellSideToEdge(List<Subsection> subsections) {
        Map<InternCellSide, Subsection> cellSideToMove = new LinkedHashMap<>();
        new ArrayList<>(subsections).forEach(ss -> {
            List<InternCellSide> cellToRemove = new ArrayList<>();
            ss.internCellSides.stream()
                    .filter(ics -> ics.getCell().checkIsShape(InternCell.Shape.FLAT, InternCell.Shape.CROSSOVER))
                    .forEach(ics -> {
                        List<BusNode> nodes = ics.getCell().getSideBusNodes(ics.getSide());
                        List<Subsection> candidateSss = subsections.stream().filter(ss2 -> ss2.containsAllBusNodes(nodes)).toList();
                        if (!candidateSss.isEmpty()) {
                            Subsection candidateSs = ics.getSide() == LEFT ? candidateSss.getLast() : candidateSss.getFirst();
                            if (ss != candidateSs) {
                                cellToRemove.add(ics);
                                cellSideToMove.put(ics, candidateSs);
                            }
                        }
                    });
            cellToRemove.forEach(ss.internCellSides::remove);
        });
        cellSideToMove.forEach((cellSide, ss) -> ss.internCellSides.add(cellSide));
    }

    private static void shuntCellCoherence(VoltageLevelGraph vlGraph, List<Subsection> subsections) {
        Map<ShuntCell, List<BusNode>> shuntCells2Buses = vlGraph.getShuntCellStream()
                .collect(Collectors.toMap(Function.identity(), ShuntCell::getParentBusNodes, (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                }, LinkedHashMap::new));
        if (shuntCells2Buses.isEmpty()) {
            return;
        }

        List<ShuntCell> sameSubsectionShunts = identifySameSubsectionShuntCells(subsections, shuntCells2Buses);
        slipInternShuntedCellsToEdge(subsections, shuntCells2Buses.keySet(), sameSubsectionShunts);
        alignMultiFeederShunt(shuntCells2Buses.keySet());
        arrangeExternCellsOrders(subsections);
    }

    private static List<ShuntCell> identifySameSubsectionShuntCells(List<Subsection> subsections, Map<ShuntCell, List<BusNode>> shuntCells2Buses) {
        List<ShuntCell> modifiedShunts = new ArrayList<>();
        subsections.forEach(ss -> shuntCells2Buses.keySet().stream()
                .filter(sc -> ss.containsAllBusNodes(shuntCells2Buses.get(sc)))
                .forEach(sc -> {
                    sc.getSideCells().forEach(c -> moveExternCellToSubsection(c, ss, subsections, Side.UNDEFINED));
                    int iLeft = ss.externCells.indexOf(sc.getSideCell(LEFT));
                    int iRight = ss.externCells.indexOf(sc.getSideCell(RIGHT));
                    if (iRight != iLeft + 1) {
                        ExternCell leftCell = sc.getSideCell(LEFT);
                        ss.externCells.remove(leftCell);
                        ss.externCells.add(ss.externCells.indexOf(sc.getSideCell(RIGHT)), leftCell);
                    }
                    modifiedShunts.add(sc);
                })
        );
        return modifiedShunts;
    }

    private static void slipInternShuntedCellsToEdge(List<Subsection> subsections, Set<ShuntCell> shuntCells, List<ShuntCell> sameSubsectionShunts) {
        shuntCells.stream().filter(sc -> !sameSubsectionShunts.contains(sc))
                .forEach(sc -> {
                    for (Side side : Side.defined()) {
                        ExternCell cell = sc.getSideCell(side);
                        subsections.stream().filter(ss -> ss.containsAllBusNodes(cell.getBusNodes()))
                                .map(subsections::indexOf).mapToInt(j -> side == LEFT ? j : -j).max()
                                .ifPresent(j -> moveExternCellToSubsection(cell, subsections.get(Math.abs(j)), subsections,
                                        side.getFlip()));
                    }
                });
    }

    private static void moveExternCellToSubsection(ExternCell c, Subsection ss, List<Subsection> subsections, Side side) {
        if (ss.externCells.contains(c) && side == Side.UNDEFINED) {
            return;
        }
        for (Subsection sub : subsections) {
            if (sub.externCells.contains(c)) {
                sub.externCells.remove(c);
                break;
            }
        }
        if (side == LEFT) {
            ss.externCells.addFirst(c);
        } else {
            ss.externCells.add(c);
        }
    }

    private static void alignMultiFeederShunt(Set<ShuntCell> shCells) {
        shCells.forEach(sc -> {
            for (Side side : Side.defined()) {
                ExternCell cell = sc.getSideCell(side);
                List<FeederNode> feeders = cell.getFeederNodes();
                if (feeders.size() > 1) {
                    Node shNode = sc.getSideShuntNode(side);
                    Set<Node> outsideNodes = new HashSet<>();
                    outsideNodes.add(shNode);
                    List<FeederNode> shuntSideFeederNodes = buildShuntSideFeederNodes(shNode, outsideNodes);
                    feeders.removeAll(shuntSideFeederNodes);
                    List<FeederNode> newlyOrderdFeeders;
                    if (side == RIGHT) {
                        newlyOrderdFeeders = shuntSideFeederNodes;
                        newlyOrderdFeeders.addAll(feeders);
                    } else {
                        newlyOrderdFeeders = feeders;
                        newlyOrderdFeeders.addAll(shuntSideFeederNodes);
                    }
                    for (int i = 0; i < newlyOrderdFeeders.size(); i++) {
                        newlyOrderdFeeders.get(i).setOrder(i);
                    }
                }
            }
        });
    }

    private static List<FeederNode> buildShuntSideFeederNodes(Node shNode, Set<Node> outsideNodes) {
        Objects.requireNonNull(shNode);
        return shNode.getAdjacentNodes().stream().flatMap(node -> {
            Set<Node> gtResult = new LinkedHashSet<>();
            if (GraphTraversal.run(node, node1 -> node1.getType() == Node.NodeType.FEEDER, node1 -> node1.getType() == Node.NodeType.BUS, gtResult, outsideNodes)) {
                return gtResult.stream().filter(n -> n.getType() == Node.NodeType.FEEDER).map(FeederNode.class::cast);
            } else {
                return Stream.empty();
            }
        }).collect(Collectors.toList());
    }

    private static void arrangeExternCellsOrders(List<Subsection> subsections) {
        subsections.forEach(ss -> {
            List<ExternCell> eCells = ss.getExternCells();
            for (int i = 1; i < eCells.size(); i++) {
                int prevIndex = eCells.get(i - 1).getOrder().orElse(-1);
                if (eCells.get(i).getOrder().orElse(-1) <= prevIndex) {
                    eCells.get(i).setOrder(prevIndex + 1);
                }
            }
        });
    }
}
