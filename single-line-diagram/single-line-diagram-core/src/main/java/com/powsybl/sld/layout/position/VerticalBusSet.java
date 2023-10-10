/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position;

import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A VerticalBusSet contains the set of BusNodes that shall be vertically presented, and the cells that have a pattern of
 * connection included in the busNodeSet. It is embedded into a BSCluster.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class VerticalBusSet {

    private final Set<BusNode> busNodeSet;
    private final Set<ExternCell> externCells = new LinkedHashSet<>();

    private final Set<InternCellSide> internCellSides = new LinkedHashSet<>();

    private VerticalBusSet(Map<BusNode, Integer> busToNb, List<BusNode> busNodes) {
        busNodeSet = new TreeSet<>(Comparator.comparingInt(busToNb::get));
        busNodeSet.addAll(busNodes);
    }

    private VerticalBusSet(Map<BusNode, Integer> busToNb, ExternCell cell) {
        this(busToNb, cell.getBusNodes());
        externCells.add(cell);
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

    Set<InternCellSide> getInternCellSides() {
        return internCellSides;
    }

    static List<VerticalBusSet> createVerticalBusSets(VoltageLevelGraph graph, Map<BusNode, Integer> busToNb) {
        List<VerticalBusSet> verticalBusSets = new ArrayList<>();

        List<ExternCell> externCells = graph.getExternCellStream()
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .collect(Collectors.toList());

        externCells.forEach(cell -> pushVbs(verticalBusSets, new VerticalBusSet(busToNb, cell)));

        graph.getInternCellStream()
                .filter(cell -> cell.checkIsShape(InternCell.Shape.ONE_LEG))
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .forEachOrdered(cell -> pushVbs(verticalBusSets, new VerticalBusSet(busToNb, cell, Side.UNDEFINED)));

        graph.getInternCellStream()
                .filter(cell -> cell.checkIsNotShape(InternCell.Shape.ONE_LEG, InternCell.Shape.UNHANDLED_PATTERN))
                .sorted(Comparator.comparing(cell -> -((InternCell) cell).getBusNodes().size())         // bigger first to identify encompassed InternCell at the end with the smaller one
                        .thenComparing(cell -> ((InternCell) cell).getFullId()))                        // avoid randomness
                .forEachOrdered(cell -> pushNonUnilegInternCell(verticalBusSets, busToNb, cell));

        // find orphan busNodes and build their VBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(verticalBusSets.stream().
                flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.stream()
                .sorted(Comparator.comparing(Node::getId))              //avoid randomness
                .forEach(busNode -> verticalBusSets.add(new VerticalBusSet(busToNb, busNode)));
        return verticalBusSets;
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

    private static void pushNonUnilegInternCell(List<VerticalBusSet> verticalBusSets, Map<BusNode, Integer> busToNb, InternCell internCell) {
        for (VerticalBusSet vbs : verticalBusSets) {
            if (vbs.contains(internCell.getBusNodes())) {
                vbs.addInternCell(internCell, Side.UNDEFINED);
                internCell.setShape(InternCell.Shape.VERTICAL);
                return;
            }
        }
        pushVbs(verticalBusSets, new VerticalBusSet(busToNb, internCell, Side.LEFT));
        pushVbs(verticalBusSets, new VerticalBusSet(busToNb, internCell, Side.RIGHT));
    }
}
