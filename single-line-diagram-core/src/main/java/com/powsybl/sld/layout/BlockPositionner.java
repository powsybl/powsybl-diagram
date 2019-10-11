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
public class BlockPositionner {

    void determineBlockPositions(Graph graph, SubSections subSections) {
        int hPos = 0;
        int prevHPos = 0;
        int hSpace = 0;
        int maxV = graph.getMaxBusStructuralPosition().getV();
        List<InternCell> nonFlatCellsToClose = new ArrayList<>();

        int[] previousIndexes = new int[maxV];
        graph.getNodeBuses().forEach(nodeBus -> nodeBus.getPosition().setV(nodeBus.getStructuralPosition().getV()));

        for (Map.Entry<SubSections.SubSectionIndexes, SubSections.HorizontalSubSection> entry :
                subSections.getSubsectionMap().entrySet()) {
            int[] ssIndexes = entry.getKey().getIndexes();
            SubSections.HorizontalSubSection hSs = entry.getValue();
            for (int vPos = 0; vPos < maxV; vPos++) {
                if (ssIndexes[vPos] != previousIndexes[vPos]) {
                    updateNodeBusPos(graph, vPos, hPos, hSpace, previousIndexes, Side.LEFT);
                    updateNodeBusPos(graph, vPos, hPos, 0, ssIndexes, Side.RIGHT);
                }
            }
            hPos = placeNonFlatInternCells(hPos, hSs, Side.LEFT, nonFlatCellsToClose);
            hPos = placeVerticalCells(hPos, new ArrayList<>(hSs.getSideInternCells(Side.UNDEFINED)));
            hPos = placeVerticalCells(hPos, hSs.getExternCells().stream()
                    .sorted(Comparator.comparingInt(ExternCell::getOrder))
                    .collect(Collectors.toList()));
            hPos = placeNonFlatInternCells(hPos, hSs, Side.RIGHT, nonFlatCellsToClose);
            if (hPos == prevHPos) {
                hPos++;
            }
            hSpace = placeFlatInternCells(hPos, hSs.getSideInternCells(Side.RIGHT).stream()
                    .filter(InternCell::isFlat)
                    .collect(Collectors.toList())) - hPos;
            hPos += hSpace;
            prevHPos = hPos;
            previousIndexes = ssIndexes;
        }
        for (int vPos = 0; vPos < maxV; vPos++) {
            updateNodeBusPos(graph, vPos, hPos, hSpace, previousIndexes, Side.LEFT);
        }
        manageInternCellOverlaps(graph);
    }

    private void updateNodeBusPos(Graph graph, int vPos, int hPos, int hSpace, int[] indexes, Side side) {
        if (indexes[vPos] != 0) {
            Position p = graph.getVHNodeBus(vPos + 1, indexes[vPos]).getPosition();
            if (side == Side.LEFT) {
                p.setHSpan(hPos - Math.max(p.getH(), 0) - hSpace);
            } else if (side == Side.RIGHT && (p.getH() == -1 || hPos == 0)) {
                p.setH(hPos);
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

    private int placeNonFlatInternCells(int hPos,
                                        SubSections.HorizontalSubSection hSs,
                                        Side side, List<InternCell> nonFlatCellsToClose) {
        int hPosRes = hPos;
        List<InternCell> cells = hSs.getSideInternCells(side).stream()
                .filter(internCell -> !internCell.isFlat())
                .sorted(Comparator.comparingInt(c -> -nonFlatCellsToClose.indexOf(c)))
                .collect(Collectors.toList());
        Side legSide = side.getFlip();
        for (InternCell cell : cells) {
            hPosRes = cell.newHPosition(hPosRes, legSide);
        }
        if (side == Side.RIGHT) {
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
                .filter(internCell -> internCell.getDirection() != BusCell.Direction.FLAT
                        && internCell.getBodyBlock() != null)
                .collect(Collectors.toList());
        Lane lane = new Lane(cellsToHandle);
        lane.run();
    }

    /**
     * The class lane manages the overlaps of internCells.
     * After bundleToCompatibleLanes each lane contents non overlapping cells
     * arrangeLane at this stage balance the lanes on TOP and BOTTOM this could be improved by having various VPos per lane
     */
    private class Lane {
        HashMap<InternCell, ArrayList<InternCell>> incompatibilities;
        Lane nextLane;
        List<Lane> lanes;

        Lane(List<InternCell> cells) {
            lanes = new ArrayList<>();
            lanes.add(this);
            incompatibilities = new HashMap<>();
            cells.forEach(this::addCell);
        }

        Lane(InternCell cell, List<Lane> lanes) {
            this.lanes = lanes;
            lanes.add(this);
            incompatibilities = new HashMap<>();
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
                int hAmin = internCellA.getSideHPos(Side.LEFT);
                int hAmax = internCellA.getSideHPos(Side.RIGHT);

                for (InternCell internCellB : incompatibilities.keySet()) {
                    if (!internCellA.equals(internCellB)) {
                        int hBmin = internCellB.getSideHPos(Side.LEFT);
                        int hBmax = internCellB.getSideHPos(Side.RIGHT);
                        if (hAmax > hBmin && hBmax > hAmin) {
                            entry.getValue().add(internCellB);
                            hasIncompatibility = true;
                        }
                    }
                }
            }
            return hasIncompatibility;
        }

        private void shiftIncompatibilities() {
            Map.Entry<InternCell, ArrayList<InternCell>> entry = incompatibilities.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .max(Comparator.comparingInt(e -> e.getValue().size())).orElse(null);

            if (entry != null) {
                InternCell cell = entry.getKey();
                incompatibilities.remove(cell);
                if (nextLane == null) {
                    nextLane = new Lane(cell, lanes);
                } else {
                    nextLane.addCell(cell);
                    nextLane.bundleToCompatibleLanes();
                }
            }
        }

        private void arrangeLanes() {
            int i = 0;
            for (Lane lane : lanes) {
                final int j = i % 2;
                final int newV = 1 + i / 2;
                lane.incompatibilities.keySet()
                        .forEach(c -> {
                            c.setDirection(j == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM);
                            c.getBodyBlock().getPosition().setV(newV);
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
