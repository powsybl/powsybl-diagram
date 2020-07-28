/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

class Subsection {

    private int size;
    private BusNode[] busNodes;
    private Set<InternCellSide> internCellSides = new HashSet<>();
    private Set<ExternCell> externCells = new TreeSet<>(Comparator.comparingInt(ExternCell::getOrder));

    Subsection(int size) {
        this.size = size;
        busNodes = new BusNode[size];
    }

    boolean checkAbsorbability(LegBusSet lbs) {
        return lbs.getBusNodeSet().stream().noneMatch(busNode -> {
            int vIndex = busNode.getStructuralPosition().getV() - 1;
            return busNodes[vIndex] != null && busNodes[vIndex] != busNode;
        });
    }

    void addLegBusSet(LegBusSet lbs) {
        lbs.getBusNodeSet().forEach(bus -> busNodes[bus.getStructuralPosition().getV() - 1] = bus);
        externCells.addAll(lbs.getExternCells());
        internCellSides.addAll(lbs.getInternCellSides());
    }

    void internCellCoherence() {
        InternCellSide.identifyVerticalInternCells(internCellSides);
    }

    public Stream<InternCellSide> getNonEmbeddedCells() {
        return internCellSides.stream().filter(cellSide -> cellSide.getCell().getShape() == InternCell.Shape.FLAT
                || cellSide.getCell().getShape() == InternCell.Shape.CROSSOVER);
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

    public List<InternCell> getInternCells(InternCell.Shape shape, Side side) {
        return internCellSides.stream()
                .filter(ics -> ics.getCell().checkShape(shape) && ics.getSide() == side)
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    List<InternCell> getVerticalInternCells() {
        return internCellSides.stream()
                .filter(ics -> ics.getCell().checkShape(InternCell.Shape.VERTICAL))
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    Set<ExternCell> getExternCells() {
        return externCells;
    }

    public static List<Subsection> createSubsections(List<LegBusSet> lbsList) {
        int vSize = lbsList.get(0).getBusNodeSet().iterator().next().getGraph().getMaxBusStructuralPosition().getV();
        List<Subsection> subsections = new ArrayList<>();
        Subsection currentSubsection = new Subsection(vSize);
        subsections.add(currentSubsection);
        for (LegBusSet lbs : lbsList) {
            if (!currentSubsection.checkAbsorbability(lbs)) {
                currentSubsection = new Subsection(vSize);
                subsections.add(currentSubsection);
            }
            currentSubsection.addLegBusSet(lbs);
        }
        subsections.forEach(Subsection::internCellCoherence);
        ensureInternCellOrientation(subsections);
        return subsections;
    }

    private static void ensureInternCellOrientation(List<Subsection> subsections) {
        final class SideSs {
            private Side side;
            private Subsection ss;

            private SideSs(Side side, Subsection ss) {
                this.side = side;
                this.ss = ss;
            }
        }

        Map<InternCell, List<SideSs>> cellToSideSs = new HashMap<>();
        for (Subsection ss : subsections) {
            ss.getNonEmbeddedCells().forEach(ics -> {
                cellToSideSs.putIfAbsent(ics.getCell(), new ArrayList<>());
                cellToSideSs.get(ics.getCell()).add(new SideSs(ics.getSide(), ss));
            });
        }
        cellToSideSs.forEach((cell, sideSses) -> {
            if (sideSses.size() == 2 && sideSses.get(0).side == Side.RIGHT) {
                cell.reverseCell();
                sideSses.stream().flatMap(sss -> sss.ss.internCellSides.stream())
                        .filter(ics -> ics.getCell() == cell)
                        .forEach(InternCellSide::flipSide);
            }
        });
    }

}
