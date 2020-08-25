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
class BlockPositionner {

    void determineBlockPositions(Graph graph, List<Subsection> subsections) {
        int hPos = 0;
        int prevHPos = 0;
        int hSpace = 0;
        int maxV = graph.getMaxBusStructuralPosition().getV();
        List<InternCell> nonFlatCellsToClose = new ArrayList<>();

        Subsection prevSs = new Subsection(maxV);
        graph.getNodeBuses().forEach(nodeBus -> nodeBus.getPosition().setV(nodeBus.getStructuralPosition().getV()));

        for (Subsection ss : subsections) {
            updateNodeBuses(prevSs, ss, hPos, hSpace, Side.RIGHT); // close nodeBuses
            updateNodeBuses(prevSs, ss, hPos, hSpace, Side.LEFT); // open nodeBuses

            hPos = placeCrossOverInternCells(hPos, ss.getInternCells(InternCell.Shape.CROSSOVER, Side.RIGHT), Side.RIGHT, nonFlatCellsToClose);
            hPos = placeVerticalCells(hPos, new ArrayList<>(ss.getVerticalInternCells()));
            hPos = placeVerticalCells(hPos, new ArrayList<>(ss.getExternCells().stream()
                    .sorted(Comparator.comparingInt(ExternCell::getOrder)).collect(Collectors.toList())));
            hPos = placeCrossOverInternCells(hPos, ss.getInternCells(InternCell.Shape.CROSSOVER, Side.LEFT), Side.LEFT, nonFlatCellsToClose);
            if (hPos == prevHPos) {
                hPos++;
            }
            hSpace = placeFlatInternCells(hPos, ss.getInternCells(InternCell.Shape.FLAT, Side.LEFT)) - hPos;
            hPos += hSpace;
            prevHPos = hPos;
            prevSs = ss;
        }
        updateNodeBuses(prevSs, new Subsection(maxV), hPos, hSpace, Side.RIGHT); // close nodeBuses
        manageInternCellOverlaps(graph);
    }

    private void updateNodeBuses(Subsection prevSS, Subsection ss, int hPos, int hSpace, Side ssSide) {
        for (int v = 0; v < prevSS.getSize(); v++) {
            BusNode prevBusNode = prevSS.getBusNode(v);
            BusNode actualBusNode = ss.getBusNode(v);
            if (ssSide == Side.RIGHT && prevBusNode != null
                    && (actualBusNode == null || prevBusNode != actualBusNode)) {
                Position p = prevBusNode.getPosition();
                p.setHSpan(hPos - Math.max(p.getH(), 0) - hSpace);
            } else if (ssSide == Side.LEFT && actualBusNode != null &&
                    (prevBusNode == null || prevBusNode != actualBusNode)) {
                actualBusNode.getPosition().setH(hPos);
            }
        }
    }

    private int placeVerticalCells(int hPos, Collection<BusCell> busCells) {
        int hPosRes = hPos;
        for (BusCell cell : busCells) {
            hPosRes = cell.newHPosition(hPosRes);
        }
        return hPosRes;
    }

    private int placeFlatInternCells(int hPos, List<InternCell> cells) {
        int hPosRes = hPos;
        for (BusCell cell : cells) {
            hPosRes = Math.max(hPosRes, cell.newHPosition(hPos));
        }
        return hPosRes;
    }

    private int placeCrossOverInternCells(int hPos,
                                          List<InternCell> cells,
                                          Side side, List<InternCell> nonFlatCellsToClose) {
        // side, is the side from the InternCell standpoint. The left side of the internCell shall be on the right of the subsection
        int hPosRes = hPos;
        cells.sort(Comparator.comparingInt(c -> -nonFlatCellsToClose.indexOf(c)));
        for (InternCell cell : cells) {
            hPosRes = cell.newHPosition(hPosRes, side);
        }
        if (side == Side.LEFT) {
            nonFlatCellsToClose.addAll(cells);
        } else {
            nonFlatCellsToClose.removeAll(cells);
        }
        return hPosRes;
    }

    private void manageInternCellOverlaps(Graph graph) {
        List<InternCell> cellsToHandle = graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN)
                .map(InternCell.class::cast)
                .filter(internCell -> internCell.checkIsNotShape(InternCell.Shape.FLAT, InternCell.Shape.UNDEFINED))
                .collect(Collectors.toList());
        InternCellsLane lane = new InternCellsLane(cellsToHandle);
        lane.run();
    }

    /**
     * The class lane manages the overlaps of internCells.
     * After bundleToCompatibleLanes each lane contents non overlapping cells
     * arrangeLane at this stage balance the lanes on TOP and BOTTOM this could be improved by having various VPos per lane
     */
    private class InternCellsLane {
        Map<InternCell, ArrayList<InternCell>> incompatibilities;
        InternCellsLane nextLane;
        List<InternCellsLane> lanes;

        InternCellsLane(List<InternCell> cells) {
            lanes = new ArrayList<>();
            lanes.add(this);
            incompatibilities = new LinkedHashMap<>();
            cells.forEach(this::addCell);
        }

        InternCellsLane(InternCell cell, List<InternCellsLane> lanes) {
            this.lanes = lanes;
            lanes.add(this);
            incompatibilities = new LinkedHashMap<>();
            addCell(cell);
        }

        void run() {
            bundleToCompatibleLanes();
            arrangeLanes();
        }

        private void addCell(InternCell cell) {
            incompatibilities.put(cell, new ArrayList<>());
        }

        private void bundleToCompatibleLanes() {
            while (identifyIncompatibilities()) {
                shiftIncompatibilities();
            }
        }

        private boolean identifyIncompatibilities() {
            boolean hasIncompatibility = false;
            for (Map.Entry<InternCell, ArrayList<InternCell>> entry : incompatibilities.entrySet()) {
                InternCell internCellA = entry.getKey();
                entry.getValue().clear();
                int hAmin = getHfromSide(internCellA, Side.LEFT);
                int hAmax = getHfromSide(internCellA, Side.RIGHT);

                for (InternCell internCellB : incompatibilities.keySet()) {
                    if (!internCellA.equals(internCellB)) {
                        int hBmin = getHfromSide(internCellB, Side.LEFT);
                        int hBmax = getHfromSide(internCellB, Side.RIGHT);
                        if (hAmin < hBmax && hBmin < hAmax) {
                            entry.getValue().add(internCellB);
                            hasIncompatibility = true;
                        }
                    }
                }
            }
            return hasIncompatibility;
        }

        private int getHfromSide(InternCell cell, Side side) {
            if (cell.checkShape(InternCell.Shape.UNILEG)) {
                return cell.getSideHPos(Side.UNDEFINED);
            }
            return cell.getSideHPos(side);
        }

        private void shiftIncompatibilities() {
            Map.Entry<InternCell, ArrayList<InternCell>> entry = incompatibilities.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .max(Comparator.comparingInt(e -> e.getValue().size())).orElse(null);

            if (entry != null) {
                InternCell cell = entry.getKey();
                incompatibilities.remove(cell);
                if (nextLane == null) {
                    nextLane = new InternCellsLane(cell, lanes);
                } else {
                    nextLane.addCell(cell);
                    nextLane.bundleToCompatibleLanes();
                }
            }
        }

        private void arrangeLanes() {
            int i = 0;
            for (InternCellsLane lane : lanes) {
                final int j = i % 2;
                final int newV = 1 + i / 2;
                lane.incompatibilities.keySet()
                        .forEach(c -> {
                            c.setDirection(j == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM);
                            if (!c.checkShape(InternCell.Shape.UNILEG)) {
                                c.getBodyBlock().getPosition().setV(newV);
                            }
                        });
                i++;
            }
        }

        public String toString() {
            StringBuilder str = new StringBuilder(incompatibilities.toString() + "\n\n");
            if (nextLane != null) {
                str.append(nextLane.toString());
            }
            return new String(str);
        }

        public int size() {
            return incompatibilities.size();
        }
    }
}
