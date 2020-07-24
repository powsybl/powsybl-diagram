/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OldSubSections splits the horizontal organisation of the busBars to cope with the case parallelism is not respected
 * This solves the case of a busbar spanning over many busbars at another vertical structural position. e.g.:
 * 1.1 ---*--- / ---*--- 1.2
 * 2   ---*---------*--- 2
 * One SubSection is define for each horizontal part having the same vertical busbar organization.
 * In the example, 2 subsections will be defined: (1.1, 2) and (1.2, 2).
 * A OldSubSections contains a Set of ordered (left to right) Subsection.
 * This assumes that the orders of the ExternCell in the given graph are coherent with the structural horizontal
 * positions of the busBars.
 * This must be ensured by the PositionFinder
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class OldSubSections {

    private Graph graph;
    private static final Logger LOGGER = LoggerFactory.getLogger(OldSubSections.class);
    private Set<SubSection> subsectionSet = new TreeSet<>();
    private static final String STR_SIDE = "\t side ";

    OldSubSections(Graph graph) {
        this.graph = graph;
        buildSubSections();
        checkInternCellLaterality();
        if (!checkCellOrderConsistencyWithSubsSections()) {
            LOGGER.warn("Cells order not coherent with Subsections order");
        }
    }

    private void checkInternCellLaterality() {
        Map<InternCell, List<SubSection>> cellToIndex = new HashMap<>();

        subsectionSet.forEach(ss -> ss.getInternCells()
                .forEach(c -> {
                    cellToIndex.putIfAbsent(c, new ArrayList<>());
                    cellToIndex.get(c).add(ss);
                }));

        cellToIndex.forEach((c, ssList) -> {
            if (ssList.size() == 2) {
                SubSection leftSs = ssList.get(0);
                SubSection rightSs = ssList.get(1);
                if (leftSs.getSideInternCells(Side.LEFT).contains(c)
                        && rightSs.getSideInternCells(Side.RIGHT).contains(c)) {
                    c.reverseCell();
                    leftSs.setSide(c, Side.RIGHT);
                    rightSs.setSide(c, Side.LEFT);
                }
            }
        });
    }

    //TODO: inappropriate criteria
    private boolean verticalInternCell(InternCell cell) {
        if (cell.isUniLeg()) {
            return true;
        }
        SubSectionIndexes ssi = busNodesToSubSectionIndexes(cell.getBusNodes());
        return cell.getBusNodes().stream()
                .map(bus -> bus.getStructuralPosition().getH())
                .distinct().count() == 1;
    }

    private void buildSubSections() {
        graph.getCells().stream().filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .map(ExternCell.class::cast)
                .forEach(cell -> allocateCellToSubsection(cell, cell.getBusNodes(), Side.UNDEFINED));

        Set<InternCell> internCells = graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN)
                .map(InternCell.class::cast)
                .collect(Collectors.toSet());

        Set<InternCell> verticalInternCells = internCells.stream().filter(this::verticalInternCell)
                .collect(Collectors.toSet());
        verticalInternCells.forEach(cell -> allocateCellToSubsection(cell, cell.getBusNodes(), Side.UNDEFINED));

        internCells.removeAll(verticalInternCells);

        internCells.stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN)
                .forEach(internCell -> {
                    allocateCellToSubsection(internCell, internCell.getSideBusNodes(Side.LEFT), Side.LEFT);
                    allocateCellToSubsection(internCell, internCell.getSideBusNodes(Side.RIGHT), Side.RIGHT);
                });
        mergeSimilarSubsections();
    }

    private void allocateCellToSubsection(BusCell busCell, List<BusNode> busNodes, Side side) {
        SubSectionIndexes indexes = busNodesToSubSectionIndexes(busNodes);

        SubSection subSection = subsectionSet.stream()
                .filter(i -> i.getSsIndexes().equals(indexes))
                .findAny()
                .orElse(null);
        if (subSection == null) {
            subSection = new SubSection(indexes);
            subsectionSet.add(subSection);
        }

        if (side == Side.UNDEFINED) {
            if (busCell.getType() == Cell.CellType.INTERN) {
                subSection.add((InternCell) busCell, Side.UNDEFINED);
            } else {
                subSection.add(busCell);
            }
        } else {
            List<SubSection> candidateSubsections = subsectionSet.stream()
                    .filter(ss -> ss.getSsIndexes().hasSameNonZeroIndexes(indexes))
                    .collect(Collectors.toList());
            SubSection ss;
            if (candidateSubsections.isEmpty()) {
                ss = new SubSection(indexes);
            } else {
                if (side == Side.LEFT) {
                    ss = candidateSubsections.get(candidateSubsections.size() - 1);
                } else {
                    ss = candidateSubsections.get(0);
                }
            }
            subsectionSet.add(ss);
        }
    }

    private SubSectionIndexes busNodesToSubSectionIndexes(List<BusNode> busNodes) {
        SubSectionIndexes indexes = new SubSectionIndexes(graph.getMaxBusStructuralPosition().getV());
        busNodes.stream().map(BusNode::getStructuralPosition)
                .forEach(position -> indexes.setIndexI(position.getV() - 1, position.getH()));
        return indexes;
    }

    private void mergeSimilarSubsections() {
        boolean change = true;
        while (change) {
            change = false;
            List<SubSection> ssList = new ArrayList<>(subsectionSet);
            for (int i = 0; i < ssList.size() && !change; i++) {
                SubSection ss1 = ssList.get(i);
                for (int j = i + 1; j < ssList.size() && !change; j++) {
                    SubSection ss2 = ssList.get(j);
                    if (ss1.getSsIndexes().isMergeableWith(ss2.getSsIndexes())) {
                        change = true;
                        subsectionSet.remove(ss1);
                        subsectionSet.remove(ss2);
                        ss1.merge(ss2);
                        subsectionSet.add(ss1);
                    }
                }
            }
        }
    }

    private boolean checkCellOrderConsistencyWithSubsSections() {
        int previousMax = 0;
        boolean checkOK = true;
        for (SubSection subSection : subsectionSet) {
            Set<ExternCell> externCells = subSection.getExternCells();
            int minOrder = externCells.stream().mapToInt(ExternCell::getOrder).max().orElse(previousMax);
            checkOK &= minOrder >= previousMax;
            previousMax = externCells.stream().mapToInt(ExternCell::getOrder).max().orElse(previousMax);
        }
        return checkOK;
    }

    @Override
    public String toString() {
        StringBuilder stBdr = new StringBuilder();
        getSubsectionMap().forEach((indexes, subsection) -> {
            stBdr.append(Arrays.toString(indexes.getIndexes())).append(":\n");
            stBdr.append(subsection.toString()).append("\n");
        });
        return stBdr.toString();
    }

    Map<SubSectionIndexes, SubSection> getSubsectionMap() {
        Map<SubSectionIndexes, SubSection> subsectionMap = new TreeMap<>();
        subsectionSet.forEach(subSection -> subsectionMap.put(subSection.getSsIndexes(), subSection));
        return subsectionMap;
    }

    class ExternCellComparator implements Comparator<ExternCell> {
        public int compare(ExternCell extCell1, ExternCell extCell2) {
            if (extCell1 == extCell2) {
                return 0;
            }
            if (extCell1.getOrder() == extCell2.getOrder()) {
                return Comparator.comparingInt(ExternCell::getNumber).compare(extCell1, extCell2);
            }
            return Comparator.comparingInt(ExternCell::getOrder).compare(extCell1, extCell2);
        }
    }

    class SubSection implements Comparable<SubSection> {
        SubSectionIndexes ssIndexes;
        private Set<Cell> cells;
        private Set<ExternCell> externCells;
        private Set<InternCell> internCells;
        private Set<BusNode> busNodes;
        private Map<InternCell, Side> cellToSideMap;

        SubSection(SubSectionIndexes ssIndexes) {
            this.ssIndexes = ssIndexes;
            cells = new HashSet<>();
            externCells = new TreeSet<>(new ExternCellComparator());

            internCells = new HashSet<>();
            busNodes = new HashSet<>();
            cellToSideMap = new HashMap<>();
        }

        void add(BusCell busCell) {
            cells.add(busCell);
            if (busCell.getType() == Cell.CellType.EXTERN) {
                externCells.add((ExternCell) busCell);
            }
            busNodes.addAll(busCell.getBusNodes());
        }

        void add(InternCell cell, Side side) {
            cells.add(cell);
            internCells.add(cell);
            if (cellToSideMap.containsKey(cell) || side == Side.UNDEFINED) {
                cellToSideMap.put(cell, Side.UNDEFINED); // vertical coupling
            } else {
                cellToSideMap.put(cell,
                        side == Side.RIGHT ? Side.LEFT : Side.RIGHT); //inversion, the left leg of an interncell, is on the right side of the subsection
            }
            busNodes.addAll(cell.getBusNodes());
        }

        void merge(SubSection ss) {
            ssIndexes.mergeWith(ss.ssIndexes);
            cells.addAll(ss.cells);
            externCells.addAll(ss.externCells);
            internCells.addAll(ss.internCells);
            busNodes.addAll(ss.busNodes);
            cellToSideMap.putAll(ss.cellToSideMap);
        }

        public SubSectionIndexes getSsIndexes() {
            return ssIndexes;
        }

        public int compareTo(@Nonnull SubSection ss) {
            return getSsIndexes().compareTo(ss.getSsIndexes());
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder strBd = new StringBuilder();

            Set<InternCell> leftCells = getSideInternCells(Side.LEFT);
            if (!leftCells.isEmpty()) {
                strBd.append("internCells Left: ").append(leftCells.size()).append("\n");
                leftCells.forEach(cell -> strBd.append(STR_SIDE).append(" ").append(cell.toString()).append("\n"));
            }

            if (!externCells.isEmpty()) {
                strBd.append("externCells: ").append(externCells.size()).append("\n");
                externCells.forEach(cell -> strBd.append("\t").append(cell.toString()).append("\n"));
            }

            Set<InternCell> undefinedCells = getSideInternCells(Side.UNDEFINED);
            if (!undefinedCells.isEmpty()) {
                strBd.append("undefined internCells: ").append(undefinedCells.size()).append("\n");
                undefinedCells.forEach(cell -> strBd.append(STR_SIDE).append(" ").append(cell.toString()).append("\n"));
            }

            Set<InternCell> rightCells = getSideInternCells(Side.RIGHT);
            if (!rightCells.isEmpty()) {
                strBd.append("internCells Right: ").append(rightCells.size()).append("\n");
                rightCells.forEach(cell -> strBd.append(STR_SIDE).append(" ").append(cell).append("\n"));
            }

            strBd.append("busNodes: ").append(busNodes.size()).append("\n");
            busNodes.forEach(node -> strBd.append("\t").append(node.toString()).append("\n"));

            return strBd.toString();
        }

        Set<ExternCell> getExternCells() {
            TreeSet<ExternCell> externCellsCopy = new TreeSet<>(new ExternCellComparator());
            externCellsCopy.addAll(this.externCells);
            return externCellsCopy;
        }

        Set<Cell> getCells() {
            return new HashSet<>(cells);
        }

        Set<InternCell> getInternCells() {
            return new HashSet<>(internCells);
        }

        Set<InternCell> getSideInternCells(Side side) {
            return internCells.stream().filter(cell -> cellToSideMap.get(cell) == side)
                    .collect(Collectors.toSet());
        }

        void setSide(InternCell c, Side side) {
            cellToSideMap.put(c, side);
        }

    }

    class SubSectionIndexes implements Comparable<SubSectionIndexes> {
        private int size;
        private int[] indexes;

        SubSectionIndexes(int size) {
            this.size = size;
            indexes = new int[size];
        }

        void setIndexI(int i, int val) {
            indexes[i] = val;
        }

        int[] getIndexes() {
            return indexes.clone();
        }

        //TODO: suspicious check!!!
        boolean hasSameNonZeroIndexes(SubSectionIndexes ssI) {
            for (int i = 0; i < size; i++) {
                int index = ssI.getIndexes()[i];
                if (index != 0 && index != indexes[i]) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SubSectionIndexes) {
                return Arrays.equals(((SubSectionIndexes) o).getIndexes(), this.indexes);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public int compareTo(@Nonnull SubSectionIndexes o) {
            boolean noNotNullIntersection = false;
            for (int i = 0; i < size; i++) {
                int index = o.getIndexes()[i];
                if (indexes[i] != 0 && index != 0) {
                    if (indexes[i] != index) {
                        return indexes[i] - index;
                    }
                } else {
                    noNotNullIntersection = true;
                }
            }
            if (noNotNullIntersection) {
                return notObviousComp(o.getIndexes());
            }
            return 0;
        }

        private int notObviousComp(int[] indexes2) {
            int compMax = Arrays.stream(indexes).max().orElse(0) - Arrays.stream(indexes2).max().orElse(0);
            if (compMax != 0) {
                return compMax;
            }
            for (int i = 0; i < size; i++) {
                int index = indexes2[i];
                if (indexes[i] != index) {
                    return indexes[i] - index;
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            return Arrays.toString(indexes);
        }

        boolean isMergeableWith(SubSectionIndexes ssi) {
            for (int i = 0; i < size; i++) {
                if (ssi.indexes[i] != 0 && indexes[i] != 0 && ssi.indexes[i] != indexes[i]) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Warning: the merge can be achieve only if the isMergeableWith test is successful.
         *
         * @param ssi : SubSectionsIndexes to merge with
         */
        void mergeWith(SubSectionIndexes ssi) {
            if (!isMergeableWith(ssi)) {
                throw new PowsyblException("SubSectionIndexes not mergeable");
            }
            for (int i = 0; i < size; i++) {
                if (indexes[i] == 0) {
                    indexes[i] = ssi.indexes[i];
                }
            }
        }
    }
}
