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

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

class Subsection {
    private int size;
    private BusNode[] busNodes;
    private Map<Side, List<InternCell>> flatCells = new EnumMap<>(Side.class);
    private Map<Side, List<InternCell>> crossOverCells = new EnumMap<>(Side.class);
    private List<InternCell> verticalCells = new ArrayList<>();
    private Set<ExternCell> externCells = new TreeSet<>(Comparator.comparingInt(ExternCell::getOrder));

    Subsection(int size) {
        this.size = size;
        initSideListMap(flatCells);
        initSideListMap(crossOverCells);
        busNodes = new BusNode[size];
    }

    private void initSideListMap(Map<Side, List<InternCell>> sideListMap) {
        sideListMap.put(Side.LEFT, new ArrayList<>());
        sideListMap.put(Side.RIGHT, new ArrayList<>());
    }

    boolean checkAbsorbability(LegBusSet lbs) {
        return lbs.getBusNodeSet().stream().noneMatch(busNode -> {
            int vIndex = busNode.getStructuralPosition().getV() - 1;
            return busNodes[vIndex] != null && busNodes[vIndex] != busNode;
        });
    }

    void addLegBusSet(LegBusSet lbs) {
        lbs.getBusNodeSet().forEach(bus -> busNodes[bus.getStructuralPosition().getV() - 1] = bus);
        addReverseInternToSide(lbs.getCandidateFlatCells(), flatCells);
        addReverseInternToSide(lbs.getCrossoverInternCell(), crossOverCells);
        verticalCells.addAll(lbs.getEmbeddedCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN)
                .map(InternCell.class::cast)
                .collect(Collectors.toList()));
        externCells.addAll(lbs.getEmbeddedCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .map(ExternCell.class::cast)
                .collect(Collectors.toList()));
    }

    private void addReverseInternToSide(Map<InternCell, Side> intern2side, Map<Side, List<InternCell>> sideToInterns) {
        // the right leg of an internCell is on the right side of the subsection and vice versa.
        intern2side.forEach((cell, side) -> {
            Side otherSide = side.getFlip();
            sideToInterns.get(otherSide).add(cell);
        });
    }

    public int getSize() {
        return size;
    }

    public BusNode[] getBusNodes() {
        return busNodes;
    }

    public BusNode getBusNode(int index) {
        return busNodes[index];
    }

    public Map<Side, List<InternCell>> getFlatCells() {
        return flatCells;
    }

    public Map<Side, List<InternCell>> getCrossOverCells() {
        return crossOverCells;
    }

    public List<InternCell> getVerticalInternCells() {
        return verticalCells;
    }

    public Set<ExternCell> getExternCells() {
        return externCells;
    }
}
