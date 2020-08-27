/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

class Subsection {

    private int size;
    private BusNode[] busNodes;
    private Set<InternCellSide> internCellSides = new LinkedHashSet<>();
    private List<ExternCell> externCells = new LinkedList<>();

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
        externCells.sort(Comparator.comparingInt(ExternCell::getOrder));
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
                .filter(ics -> ics.getCell().checkShape(shape) && ics.getSide() == side)
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    List<InternCell> getVerticalInternCells() {
        return internCellSides.stream()
                .filter(ics -> ics.getCell().checkShape(InternCell.Shape.VERTICAL)
                        || ics.getCell().checkShape(InternCell.Shape.UNILEG))
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    List<ExternCell> getExternCells() {
        return externCells;
    }

    static List<Subsection> createSubsections(LBSCluster lbsCluster) {
        int vSize = lbsCluster.getLbsList().get(0).getBusNodeSet().iterator().next().getGraph().getMaxBusStructuralPosition().getV();
        List<Subsection> subsections = new ArrayList<>();
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

        internCellCoherence(lbsCluster.getLbsList(), subsections);
        return subsections;
    }

    private static void internCellCoherence(List<LegBusSet> lbsList, List<Subsection> subsections) {
        identifyVerticalInternCells(subsections);
        lbsList.stream()
                .flatMap(lbs -> lbs.getCandidateFlatCells().keySet().stream()).distinct()
                .forEach(InternCell::identifyIfFlat);
        identifyCrossOverAndCheckOrientation(subsections);
        slipInternCellSideToEdge(subsections);
    }

    private static void identifyVerticalInternCells(List<Subsection> subsections) {
        Optional<BusNode> oneNode = Arrays.stream(subsections.get(0).busNodes).filter(Objects::nonNull).findAny();
        if (!oneNode.isPresent()) {
            return;
        }
        Map<InternCell, Subsection> verticalCells = new HashMap<>();

        oneNode.get().getGraph().getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.INTERN
                        && ((InternCell) c).checkIsNotShape(InternCell.Shape.UNILEG, InternCell.Shape.UNDEFINED))
                .map(InternCell.class::cast)
                .forEach(c ->
                        subsections.stream()
                                .filter(subsection -> Arrays.asList(subsection.busNodes).containsAll(c.getBusNodes()))
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
                if (!cell.checkShape(InternCell.Shape.FLAT)) {
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
            ss.internCellSides.stream().filter(ics -> ics.getCell().checkShape(InternCell.Shape.FLAT)
                    || ics.getCell().checkShape(InternCell.Shape.CROSSOVER))
                    .forEach(ics -> {
                        List<BusNode> nodes = ics.getCell().getSideBusNodes(ics.getSide());
                        List<Subsection> candidateSss = subsections.stream().filter(ss2 -> Arrays.asList(ss2.busNodes)
                                .containsAll(nodes)).collect(Collectors.toList());
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
}
