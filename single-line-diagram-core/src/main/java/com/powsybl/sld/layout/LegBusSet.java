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
 * A LegBusSet contains the set of BusNodes that shall be vertically presented, and the cells that have a pattern of
 * connection included in the busNodeSet. It is embedded into a LBSCluster. It contains links to all the other
 * LegBusSet of the Graph.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class LegBusSet {

    private final Set<BusNode> busNodeSet;
    private final Set<ExternCell> externCells = new LinkedHashSet<>();

    private final Set<InternCellSide> internCellSides = new LinkedHashSet<>();

    private LegBusSet(Map<BusNode, Integer> nodeToNb, List<BusNode> busNodes) {
        busNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
        busNodeSet.addAll(busNodes);
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, ExternCell cell) {
        this(nodeToNb, cell.getBusNodes());
        externCells.add(cell);
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, InternCell internCell, Side side) {
        this(nodeToNb, internCell.getSideBusNodes(side));
        addInternCell(internCell, side);
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, BusNode busNode) {
        this(nodeToNb, Collections.singletonList(busNode));
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

    public Map<InternCell, Side> getCellSideMapFromShape(InternCell.Shape shape) {
        return internCellSides.stream().filter(ics -> ics.getCell().checkShape(shape))
                .collect(Collectors.toMap(InternCellSide::getCell, InternCellSide::getSide));
    }

    public Map<InternCell, Side> getCandidateFlatCells() {
        return getCellSideMapFromShape(InternCell.Shape.MAYBEFLAT);
    }

    public Map<InternCell, Side> getCrossoverInternCell() {
        return getCellSideMapFromShape(InternCell.Shape.CROSSOVER);
    }

    public Set<BusNode> getBusNodeSet() {
        return busNodeSet;
    }

    public Set<ExternCell> getExternCells() {
        return externCells;
    }

    public Set<InternCellSide> getInternCellSides() {
        return internCellSides;
    }


    // TODO : to be clarified / strengthened
    static List<LegBusSet> createLegBusSets(Graph graph, Map<BusNode, Integer> nodeToNb) {
        List<LegBusSet> legBusSets = new ArrayList<>();
        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .map(ExternCell.class::cast)
                .sorted(Comparator.comparing(ExternCell::getOrder)
                        .thenComparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .forEach(cell -> pushNewLBS(legBusSets, nodeToNb, cell, Side.UNDEFINED));

        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN && ((InternCell) cell).checkShape(InternCell.Shape.UNILEG))
                .map(InternCell.class::cast)
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .forEach(cell -> pushNewLBS(legBusSets, nodeToNb, cell, Side.UNDEFINED));

        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN && !((InternCell) cell).checkShape(InternCell.Shape.UNILEG))
                .map(InternCell.class::cast)
                .sorted(Comparator.comparing(cell -> -((InternCell) cell).getBusNodes().size())         // bigger first to identify encompassed InternCell at the end with the smaller one
                        .thenComparing(cell -> ((InternCell) cell).getFullId()))                        // avoid randomness
                .forEach(cell -> pushNonUnilegInternCell(legBusSets, nodeToNb, cell));

        legBusSets.forEach(lbs -> InternCellSide.identifyVerticalInternCells(lbs.getInternCellSides()));

        // find orphan busNodes and build their LBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(legBusSets.stream().
                flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.stream()
                .sorted(Comparator.comparing(Node::getId))              //avoid randomness
                .forEach(busNode -> legBusSets.add(new LegBusSet(nodeToNb, busNode)));
        return legBusSets;
    }

    private static void pushNewLBS(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, BusCell busCell, Side side) {
        LegBusSet legBusSet = busCell instanceof ExternCell ?
                new LegBusSet(nodeToNb, (ExternCell) busCell) :
                new LegBusSet(nodeToNb, (InternCell) busCell, side);

        for (LegBusSet lbs : legBusSets) {
            if (lbs.contains(legBusSet)) {
                lbs.absorbs(legBusSet);
                return;
            }
        }
        List<LegBusSet> absorbedLBS = new ArrayList<>();
        legBusSets.forEach(lbs -> {
            if (legBusSet.contains(lbs)) {
                absorbedLBS.add(lbs);
                legBusSet.absorbs(lbs);
            }
        });
        legBusSets.removeAll(absorbedLBS);
        legBusSets.add(legBusSet);
    }

    private static void pushNonUnilegInternCell(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, InternCell internCell) {
        for (LegBusSet lbs : legBusSets) {
            if (lbs.contains(internCell.getBusNodes())) {
                lbs.addInternCell(internCell, Side.UNDEFINED);
                internCell.setShape(InternCell.Shape.VERTICAL);
                return;
            }
        }
        pushNewLBS(legBusSets, nodeToNb, internCell, Side.LEFT);
        pushNewLBS(legBusSets, nodeToNb, internCell, Side.RIGHT);
    }
}
