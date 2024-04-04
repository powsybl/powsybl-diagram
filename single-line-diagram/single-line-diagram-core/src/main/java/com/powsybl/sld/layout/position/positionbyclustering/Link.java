/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position.positionbyclustering;

import com.powsybl.sld.layout.position.HorizontalBusListManager;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A link is define between two VBSClusterSides (having the same implementation).
 * It implements Comparable that compares the strength of the link between two VBSClusterSides.
 * From the strongest to the weakest kind of link :
 * <ul>
 * <li>
 * having buses in common (the more there are the stronger is the link),
 * </li>
 * <li>
 * having flatcells in common (ie a cell with only two buses, one in each VBSClusterSide), the more there are,
 * the stronger is the link (for flatcell, the notion of distance to the edge is added
 * (in case of clustering by VBSClusterSide) to foster flatcell that are on the edge of the cluster)
 * </li>
 * <li>
 * having crossover cells in common defined as interncell that cannot be flatcell, and that potentially can span over
 * subsections
 * </li>
 * <li>
 * (TO BE IMPLEMENTED): having externCells bound by a SHUNT
 * </li>
 * </ul>
 * <p>
 * <p>
 * Sorting this way enables to foster merging of clusters according to the strength of the link.
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */

class Link implements Comparable<Link> {

    enum LinkCategory {
        COMMONBUSES, FLATCELLS, CROSSOVER, SHUNT
    }

    private final BSClusterSide bsClusterSide1;
    private final BSClusterSide bsClusterSide2;
    private final Map<LinkCategory, Integer> categoryToWeight = new EnumMap<>(LinkCategory.class);
    private int nb;

    Link(BSClusterSide bsClusterSide1, BSClusterSide bsClusterSide2, int nb) {
        this.bsClusterSide1 = bsClusterSide1;
        this.bsClusterSide2 = bsClusterSide2;
        this.nb = nb;
        assessLink();
    }

    private void assessLink() {
        categoryToWeight.put(LinkCategory.COMMONBUSES, assessCommonBusNodes());
        categoryToWeight.put(LinkCategory.FLATCELLS, assessFlatCell());
        categoryToWeight.put(LinkCategory.CROSSOVER, assessCrossOver());
        categoryToWeight.put(LinkCategory.SHUNT, assessShunt());
    }

    private int assessCommonBusNodes() {
        Set<BusNode> nodeBusesIntersect = new LinkedHashSet<>(bsClusterSide1.getBusNodeSet());
        nodeBusesIntersect.retainAll(bsClusterSide2.getBusNodeSet());
        return nodeBusesIntersect.size();
    }

    private int assessFlatCell() {
        Set<InternCell> flatCellIntersect = new LinkedHashSet<>(bsClusterSide1.getCandidateFlatCellList());
        flatCellIntersect.retainAll(bsClusterSide2.getCandidateFlatCellList());
        return flatCellIntersect.size() * 100
                - flatCellIntersect.stream()
                .mapToInt(internCell -> flatCellDistanceToEdges(internCell, bsClusterSide1, bsClusterSide2)).sum();
    }

    static int flatCellDistanceToEdges(InternCell cell, BSClusterSide bsCS1, BSClusterSide bsCS2) {
        return bsCS1.getCandidateFlatCellDistanceToEdge(cell) + bsCS2.getCandidateFlatCellDistanceToEdge(cell);
    }

    private int assessCrossOver() {
        Set<InternCell> commonInternCells = new LinkedHashSet<>(bsClusterSide1.getInternCellsFromShape(InternCell.Shape.UNDEFINED));
        commonInternCells.retainAll(bsClusterSide2.getInternCellsFromShape(InternCell.Shape.UNDEFINED));
        return (int) (commonInternCells.stream()
                .flatMap(internCell -> internCell.getBusNodes().stream()).distinct()
                .count());
    }

    private int assessShunt() {
        List<ExternCell> externCells1 = extractExternShuntedCells(bsClusterSide1);
        List<ExternCell> externCells2 = extractExternShuntedCells(bsClusterSide2);
        List<ShuntCell> shuntCells = extractShuntCells(externCells1);
        shuntCells.retainAll(extractShuntCells(externCells2));
        List<ExternCell> myShuntedExternCells = shuntCells.stream()
                .flatMap(sc -> sc.getSideCells().stream()).collect(Collectors.toList());
        externCells1.retainAll(myShuntedExternCells);
        externCells2.retainAll(myShuntedExternCells);
        return shuntAttractivity(externCells1, bsClusterSide1) + shuntAttractivity(externCells2, bsClusterSide2);
    }

    private List<ExternCell> extractExternShuntedCells(BSClusterSide bsClusterSide) {
        return bsClusterSide.getExternCells().stream().filter(ExternCell::isShunted).collect(Collectors.toList());
    }

    private List<ShuntCell> extractShuntCells(List<ExternCell> externCells) {
        return externCells.stream().map(ExternCell::getShuntCells).flatMap(List::stream).collect(Collectors.toList());
    }

    private int shuntAttractivity(List<ExternCell> cells, BSClusterSide bsClusterSide) {
        return cells.stream().mapToInt(bsClusterSide::getExternCellAttractionToEdge).sum();
    }

    private int getLinkCategoryWeight(LinkCategory cat) {
        return categoryToWeight.get(cat);
    }

    BSClusterSide getOtherBsClusterSide(BSClusterSide bsClusterSide) {
        if (bsClusterSide == bsClusterSide1) {
            return bsClusterSide2;
        }
        if (bsClusterSide == bsClusterSide2) {
            return bsClusterSide1;
        }
        return null;
    }

    BSClusterSide getBsClusterSide(int i) {
        if (i == 0) {
            return bsClusterSide1;
        } else if (i == 1) {
            return bsClusterSide2;
        }
        return null;
    }

    void mergeClusters(HorizontalBusListManager hblManager) {
        if (bsClusterSide1.getCluster() == bsClusterSide2.getCluster()
                || bsClusterSide1.getMySideInCluster() == Side.UNDEFINED
                || bsClusterSide2.getMySideInCluster() == Side.UNDEFINED) {
            return;
        }
        bsClusterSide1.getCluster().merge(
                bsClusterSide1.getMySideInCluster(),
                bsClusterSide2.getCluster(),
                bsClusterSide2.getMySideInCluster(), hblManager);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Link)) {
            return false;
        }
        return bsClusterSide1.equals(((Link) obj).bsClusterSide1)
                && bsClusterSide2.equals(((Link) obj).bsClusterSide2);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int compareTo(@Nonnull Link oLink) {
        for (LinkCategory category : LinkCategory.values()) {
            if (oLink.getLinkCategoryWeight(category) > getLinkCategoryWeight(category)) {
                return -1;
            }
            if (oLink.getLinkCategoryWeight(category) < getLinkCategoryWeight(category)) {
                return 1;
            }
        }
        return this.nb - oLink.nb;
    }

    @Override
    public String toString() {
        return "CommonBus: " + categoryToWeight.get(LinkCategory.COMMONBUSES)
                + " FlatCell: " + categoryToWeight.get(LinkCategory.FLATCELLS)
                + " CrossOver: " + categoryToWeight.get(LinkCategory.CROSSOVER)
                + " Shunt: " + categoryToWeight.get(LinkCategory.SHUNT)
                + "\n\tvbsClusterSide1: " + bsClusterSide1.toString()
                + "\n\tvbsClusterSide2: " + bsClusterSide2.toString();
    }
}
