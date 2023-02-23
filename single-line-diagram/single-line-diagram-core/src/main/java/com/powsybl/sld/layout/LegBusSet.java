/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A LegBusSet contains the set of BusNodes that shall be vertically presented, and the cells that have a pattern of
 * connection included in the busNodeSet. It is embedded into a LBSCluster. It contains links to all the other
 * LegBusSet of the Graph.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class LegBusSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegBusSet.class);

    private final Set<BusNode> busNodeSet;
    private final Set<ExternCell> externCells = new LinkedHashSet<>();

    private final Set<InternCellSide> internCellSides = new LinkedHashSet<>();

    private LegBusSet(Map<BusNode, Integer> busToNb, List<BusNode> busNodes) {
        busNodeSet = new TreeSet<>(Comparator.comparingInt(busToNb::get));
        busNodeSet.addAll(busNodes);
    }

    private LegBusSet(Map<BusNode, Integer> busToNb, ExternCell cell) {
        this(busToNb, cell.getBusNodes());
        externCells.add(cell);
    }

    private LegBusSet(Map<BusNode, Integer> busToNb, ShuntCell cell) {
        this(busToNb, cell.getParentBusNodes());
        externCells.addAll(cell.getSideCells());
    }

    private LegBusSet(Map<BusNode, Integer> busToNb, InternCell internCell, Side side) {
        this(busToNb, internCell.getSideBusNodes(side));
        addInternCell(internCell, side);
    }

    private LegBusSet(Map<BusNode, Integer> busToNb, BusNode busNode) {
        this(busToNb, Collections.singletonList(busNode));
    }

    void addInternCell(InternCell internCell, Side side) {
        internCellSides.add(new InternCellSide(internCell, side));
    }

    private boolean contains(Collection<BusNode> busNodeCollection) {
        return busNodeSet.containsAll(busNodeCollection);
    }

    private boolean contains(LegBusSet lbs) {
        return contains(lbs.getBusNodeSet());
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

    static List<LegBusSet> createLegBusSets(VoltageLevelGraph graph, Map<BusNode, Integer> busToNb, boolean handleShunts) {
        List<LegBusSet> legBusSets = new ArrayList<>();

        List<ExternCell> externCells = graph.getExternCellStream()
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .collect(Collectors.toList());

        if (handleShunts) {
            manageShunts(graph, externCells, legBusSets, busToNb);
        }

        externCells.forEach(cell -> pushLBS(legBusSets, new LegBusSet(busToNb, cell)));

        graph.getInternCellStream()
                .filter(cell -> cell.checkIsShape(InternCell.Shape.ONE_LEG))
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .forEachOrdered(cell -> pushLBS(legBusSets, new LegBusSet(busToNb, cell, Side.UNDEFINED)));

        graph.getInternCellStream()
                .filter(cell -> cell.checkIsNotShape(InternCell.Shape.ONE_LEG, InternCell.Shape.UNHANDLED_PATTERN))
                .sorted(Comparator.comparing(cell -> -((InternCell) cell).getBusNodes().size())         // bigger first to identify encompassed InternCell at the end with the smaller one
                        .thenComparing(cell -> ((InternCell) cell).getFullId()))                        // avoid randomness
                .forEachOrdered(cell -> pushNonUnilegInternCell(legBusSets, busToNb, cell));

        // find orphan busNodes and build their LBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(legBusSets.stream().
                flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.stream()
                .sorted(Comparator.comparing(Node::getId))              //avoid randomness
                .forEach(busNode -> legBusSets.add(new LegBusSet(busToNb, busNode)));
        return legBusSets;
    }

    private static void manageShunts(VoltageLevelGraph graph, List<ExternCell> externCells, List<LegBusSet> legBusSets, Map<BusNode, Integer> busToNb) {
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
                    pushLBS(legBusSets, new LegBusSet(busToNb, sc));
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

    private static void pushNonUnilegInternCell(List<LegBusSet> legBusSets, Map<BusNode, Integer> busToNb, InternCell internCell) {
        for (LegBusSet lbs : legBusSets) {
            if (lbs.contains(internCell.getBusNodes())) {
                lbs.addInternCell(internCell, Side.UNDEFINED);
                internCell.setShape(InternCell.Shape.VERTICAL);
                return;
            }
        }
        pushLBS(legBusSets, new LegBusSet(busToNb, internCell, Side.LEFT));
        pushLBS(legBusSets, new LegBusSet(busToNb, internCell, Side.RIGHT));
    }
}
