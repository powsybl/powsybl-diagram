/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

class Subsection {

    private int size;
    private BusNode[] busNodes;
    private Set<InternCellSide> internCellSides = new LinkedHashSet<>();
    private List<ExternCell> externCells = new LinkedList<>();
    private static Comparator<ExternCell> compareOrder = Comparator.comparingInt(ExternCell::getOrder);

    Subsection(int size) {
        this.size = size;
        busNodes = new BusNode[size];
    }

    private boolean checkAbsorbability(LegBusSet lbs) {
        return lbs.getExtendedNodeSet().stream().noneMatch(busNode -> {
            int vIndex = busNode.getStructuralPosition().getV() - 1;
            return busNodes[vIndex] != null && busNodes[vIndex] != busNode;
        });
    }

    private void addLegBusSet(LegBusSet lbs) {
        lbs.getExtendedNodeSet().forEach(bus -> busNodes[bus.getStructuralPosition().getV() - 1] = bus);
        externCells.addAll(lbs.getExternCells());
        externCells.sort(compareOrder);
        internCellSides.addAll(lbs.getInternCellSides());
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
                .filter(ics -> ics.getCell().checkisShape(shape) && ics.getSide() == side)
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    List<InternCell> getVerticalInternCells() {
        return internCellSides.stream()
                .filter(ics -> ics.getCell().checkisShape(InternCell.Shape.VERTICAL)
                        || ics.getCell().checkisShape(InternCell.Shape.UNILEG))
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    List<ExternCell> getExternCells() {
        return externCells;
    }

    private boolean containsAllBusNodes(List<BusNode> nodes) {
        return Arrays.asList(busNodes).containsAll(nodes);
    }

    static List<Subsection> createSubsections(LBSCluster lbsCluster, boolean slipShuntedCells) {
        List<Subsection> subsections = new ArrayList<>();
        Optional<Graph> oVLGraph = lbsCluster.getLbsList().get(0).getBusNodeSet().stream().filter(Objects::nonNull).findAny().map(BusNode::getGraph);
        if (!oVLGraph.isPresent()) {
            return subsections;
        }

        int vSize = oVLGraph.get().getMaxBusStructuralPosition().getV();
        Subsection currentSubsection = new Subsection(vSize);
        subsections.add(currentSubsection);
        int i = 0;
        for (LegBusSet lbs : lbsCluster.getLbsList()) {
            lbs.setExtendedNodeSet(lbsCluster.getVerticalBuseNodes(i));
            if (!currentSubsection.checkAbsorbability(lbs)) {
                currentSubsection = new Subsection(vSize);
                subsections.add(currentSubsection);
            }
            currentSubsection.addLegBusSet(lbs);
            i++;
        }

        internCellCoherence(oVLGraph.get(), lbsCluster.getLbsList(), subsections);

        if (slipShuntedCells) {
            shuntCellCoherence(oVLGraph.get(), subsections);
        }

        return subsections;
    }

    static List<Subsection> createSubsections(LBSCluster lbsCluster) {
        return createSubsections(lbsCluster, false);
    }

    private static void internCellCoherence(Graph vlGraph, List<LegBusSet> lbsList, List<Subsection> subsections) {
        identifyVerticalInternCells(vlGraph, subsections);
        lbsList.stream()
                .flatMap(lbs -> lbs.getCandidateFlatCells().keySet().stream()).distinct()
                .forEach(InternCell::identifyIfFlat);
        identifyCrossOverAndCheckOrientation(subsections);
        slipInternCellSideToEdge(subsections);
    }

    private static void identifyVerticalInternCells(Graph graph, List<Subsection> subsections) {
        Map<InternCell, Subsection> verticalCells = new HashMap<>();

        graph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.INTERN
                        && ((InternCell) c).checkIsNotShape(InternCell.Shape.UNILEG, InternCell.Shape.UNDEFINED, InternCell.Shape.UNHANDLEDPATTERN))
                .map(InternCell.class::cast)
                .forEach(c ->
                        subsections.stream()
                                .filter(subsection -> subsection.containsAllBusNodes(c.getBusNodes()))
                                .findAny().ifPresent(subsection -> verticalCells.putIfAbsent(c, subsection)));

        subsections.forEach(ss -> {
            List<InternCellSide> icsToRemove = ss.internCellSides.stream()
                    .filter(ics -> verticalCells.keySet().contains(ics.getCell())).collect(Collectors.toList());
            ss.internCellSides.removeAll(icsToRemove);
        });

        verticalCells.forEach((cell, sub) -> {
            cell.setShape(InternCell.Shape.VERTICAL);
            sub.internCellSides.add(new InternCellSide(cell, Side.UNDEFINED));
        });

    }

    private static void identifyCrossOverAndCheckOrientation(List<Subsection> subsections) {
        final class SideSs {
            private Side side;
            private Subsection ss;

            private SideSs(Side side, Subsection ss) {
                this.side = side;
                this.ss = ss;
            }
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
                if (!cell.checkisShape(InternCell.Shape.FLAT)) {
                    cell.setShape(InternCell.Shape.CROSSOVER);
                }
                if (sideSses.get(0).side == Side.RIGHT) {
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
            ss.internCellSides.stream().filter(ics -> ics.getCell().checkisShape(InternCell.Shape.FLAT)
                    || ics.getCell().checkisShape(InternCell.Shape.CROSSOVER))
                    .forEach(ics -> {
                        List<BusNode> nodes = ics.getCell().getSideBusNodes(ics.getSide());
                        List<Subsection> candidateSss = subsections.stream().filter(ss2 -> ss2.containsAllBusNodes(nodes)).collect(Collectors.toList());
                        if (!candidateSss.isEmpty()) {
                            Subsection candidateSs = ics.getSide() == Side.LEFT ? candidateSss.get(candidateSss.size() - 1) : candidateSss.get(0);
                            if (ss != candidateSs) {
                                cellToRemove.add(ics);
                                cellSideToMove.put(ics, candidateSs);
                            }
                        }
                    });
            ss.internCellSides.removeAll(cellToRemove);
        });
        cellSideToMove.forEach((cellSide, ss) -> ss.internCellSides.add(cellSide));
    }

    private static void shuntCellCoherence(Graph vlGraph, List<Subsection> subsections) {
        Map<ShuntCell, List<BusNode>> shuntCells2Buses = vlGraph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.SHUNT).map(ShuntCell.class::cast)
                .collect(Collectors.toMap(Function.identity(), ShuntCell::getParentBusNodes));
        if (shuntCells2Buses.isEmpty()) {
            return;
        }

        List<ExternCell> externCells = vlGraph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.EXTERN)
                .map(ExternCell.class::cast)
                .sorted(compareOrder)
                .collect(Collectors.toList());

        identifySameSubsectionShuntCells(subsections, shuntCells2Buses);
        arrangeExternCellsOrders(externCells, subsections);
    }

    private static void identifySameSubsectionShuntCells(List<Subsection> subsections, Map<ShuntCell, List<BusNode>> shuntCells2Buses) {
        subsections.forEach(ss -> shuntCells2Buses.keySet().stream()
                .filter(sc -> ss.containsAllBusNodes(shuntCells2Buses.get(sc)))
                .forEach(sc -> {
                    sc.getCells().forEach(c -> moveExternCellToSubsection(c, ss, subsections));
                    int i1 = ss.externCells.indexOf(sc.getCell1());
                    int i2 = ss.externCells.indexOf(sc.getCell2());
                    if (Math.abs(i1 - 12) != 1) {
                        ss.externCells.remove(sc.getCell1());
                        ss.externCells.add(i2, sc.getCell1());
                    }
                })
        );
    }

    private static void moveExternCellToSubsection(ExternCell c, Subsection ss, List<Subsection> subsections) {
        if (ss.externCells.contains(c)) {
            return;
        }
        for (Subsection sub : subsections) {
            if (sub.externCells.contains(c)) {
                sub.externCells.remove(c);
                break;
            }
        }
        ss.externCells.add(c);
    }

    private static void arrangeExternCellsOrders(List<ExternCell> externCells, List<Subsection> subsections) {
        for (int i = 1; i < externCells.size() - 1; i++) {
            int prevIndex = externCells.get(i - 1).getOrder();
            if (externCells.get(i).getOrder() <= prevIndex) {
                externCells.get(i).setOrder(prevIndex + 1);
            }
        }
        subsections.stream().map(Subsection::getExternCells).forEach(cells -> cells.sort(compareOrder));
    }
}
