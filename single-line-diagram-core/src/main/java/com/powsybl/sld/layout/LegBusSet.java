/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
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
    private final Set<BusCell> embeddedCells;
    private final Map<InternCell, Side> candidateFlatCells;
    private final Map<InternCell, Side> crossoverInternCells;

    private LegBusSet(Map<BusNode, Integer> nodeToNb, List<BusNode> busNodes) {
        busNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
        busNodeSet.addAll(busNodes);
        embeddedCells = new LinkedHashSet<>();
        candidateFlatCells = new LinkedHashMap<>();
        crossoverInternCells = new LinkedHashMap<>();
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, BusCell cell) {
        this(nodeToNb, cell.getBusNodes());
        embeddedCells.add(cell);
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, InternCell internCell, Side side) {
        this(nodeToNb, internCell.getSideBusNodes(side));
        if (internCell.getBusNodes().size() == 1) {
            embeddedCells.add(internCell);
        } else if (internCell.getBusNodes().size() == 2) {
            candidateFlatCells.put(internCell, side);
        } else {
            crossoverInternCells.put(internCell, side);
        }
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, BusNode busNode) {
        this(nodeToNb, Collections.singletonList(busNode));
    }

    private boolean contains(Collection<BusNode> busNodeCollection) {
        return busNodeSet.containsAll(busNodeCollection);
    }

    private boolean contains(LegBusSet lbs) {
        return contains(lbs.getBusNodeSet());
    }

    private void addEmbededCell(BusCell busCell) {
        embeddedCells.add(busCell);
    }

    private void absorbs(LegBusSet lbsToAbsorb) {
        busNodeSet.addAll(lbsToAbsorb.getBusNodeSet());
        embeddedCells.addAll(lbsToAbsorb.getEmbeddedCells());
        absorbMap(candidateFlatCells, lbsToAbsorb.getCandidateFlatCells());
        absorbMap(crossoverInternCells, lbsToAbsorb.getCrossoverInternCell());
    }

    private void absorbMap(Map<InternCell, Side> myMap, Map<InternCell, Side> map) {
        Set<InternCell> commonCells = new LinkedHashSet<>(myMap.keySet());
        Set<InternCell> cellToAbsorb = map.keySet();
        commonCells.retainAll(cellToAbsorb);
        for (InternCell commonCell : commonCells) {
            if (myMap.get(commonCell) == Side.RIGHT && map.get(commonCell) == Side.LEFT
                    || myMap.get(commonCell) == Side.LEFT && map.get(commonCell) == Side.RIGHT) {
                embeddedCells.add(commonCell);
                myMap.remove(commonCell);
            } else {
                throw new PowsyblException("Absorption of InternCell in a LegBusSet should concern both side of the InternCell");
            }
        }
        cellToAbsorb.removeAll(commonCells);
        cellToAbsorb.forEach(internCell -> myMap.put(internCell, map.get(internCell)));
    }

    private void identifyEmbeddedCells() {
        identifyEmbeddedCells(candidateFlatCells);
        identifyEmbeddedCells(crossoverInternCells);
    }

    private void identifyEmbeddedCells(Map<InternCell, Side> cells) {
        List<InternCell> cellActuallyEmbeded = new ArrayList<>();
        cells.forEach((internCell, side) -> {
            List<BusNode> otherLegBusNodes = internCell
                    .getSideBusNodes(side.getFlip());
            if (busNodeSet.containsAll(otherLegBusNodes)) {
                cellActuallyEmbeded.add(internCell);
            }
        });
        cellActuallyEmbeded.forEach(cells::remove);
        embeddedCells.addAll(cellActuallyEmbeded);
    }

    public Map<InternCell, Side> getCandidateFlatCells() {
        return candidateFlatCells;
    }

    public List<InternCell> getCandidateFlatCellList() {
        return new ArrayList<>(candidateFlatCells.keySet());
    }

    public Map<InternCell, Side> getCrossoverInternCell() {
        return crossoverInternCells;
    }

    public List<InternCell> getCrossOverCellList() {
        return new ArrayList<>(crossoverInternCells.keySet());
    }

    public Set<BusNode> getBusNodeSet() {
        return busNodeSet;
    }

    public Set<BusCell> getEmbeddedCells() {
        return embeddedCells;
    }

    public Map<InternCell, Side> getNonEmbeddedInternCells() {
        HashMap<InternCell, Side> nonEmbeddedInternCells = new HashMap<>(candidateFlatCells);
        nonEmbeddedInternCells.putAll(crossoverInternCells);
        return nonEmbeddedInternCells;
    }

    public List<ExternCell> getExternCells() {
        return embeddedCells.stream().filter(c -> c.getType() == Cell.CellType.EXTERN)
                .map(ExternCell.class::cast).collect(Collectors.toList());
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
                .filter(cell -> cell.getType() == Cell.CellType.INTERN && ((InternCell) cell).isUniLeg())
                .map(InternCell.class::cast)
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .forEach(cell -> pushNewLBS(legBusSets, nodeToNb, cell, Side.UNDEFINED));

        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN && !((InternCell) cell).isUniLeg())
                .map(InternCell.class::cast)
                .sorted(Comparator.comparing(cell -> -((InternCell) cell).getBusNodes().size())         // bigger first to identify encompassed InternCell at the end with the smaller one
                        .thenComparing(cell -> ((InternCell) cell).getFullId()))                        // avoid randomness
                .forEach(cell -> pushNonUnilegInternCell(legBusSets, nodeToNb, cell));

        // find orphan busNodes and build their LBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(legBusSets.stream().
                flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.stream()
                .sorted(Comparator.comparing(Node::getId))              //avoid randomness
                .forEach(busNode -> legBusSets.add(new LegBusSet(nodeToNb, busNode)));
        legBusSets.forEach(LegBusSet::identifyEmbeddedCells);
        return legBusSets;
    }

    private static void pushNewLBS(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, BusCell busCell, Side side) {
        LegBusSet legBusSet = side == Side.UNDEFINED ?
                new LegBusSet(nodeToNb, busCell) :
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
                lbs.addEmbededCell(internCell);
                return;
            }
        }
        pushNewLBS(legBusSets, nodeToNb, internCell, Side.LEFT);
        pushNewLBS(legBusSets, nodeToNb, internCell, Side.RIGHT);
    }
}
