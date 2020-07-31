/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.HorizontalBusLaneManager;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A link is define between two lbsClusterSides (having the same implementation).
 * It implements Comparable that compares the strength of the link between two lbsClusterSides.
 * From the strongest to the weakest kind of link :
 * <ul>
 * <li>
 * having buses in common (the more there are the stronger is the link),
 * </li>
 * <li>
 * having flatcells in common (ie a cell with only two buses, one in each lbsClusterSide), the more there are,
 * the stronger is the link (for flatcell, the notion of distance to the edge is added
 * (in case of clustering by LBSClusterSide) to foster flatcell that are on the edge of the cluster)
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
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

// TODO implement SHUNT in the link assessment
class Link implements Comparable<Link> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Link.class);

    enum LinkCategory {
        COMMONBUSES, FLATCELLS, CROSSOVER //, SHUNT
    }

    private final LBSClusterSide lbsClusterSide1;
    private final LBSClusterSide lbsClusterSide2;
    private final Map<LinkCategory, Integer> categoryToWeight = new EnumMap<>(LinkCategory.class);
    private int nb;

    Link(LBSClusterSide lbsClusterSide1, LBSClusterSide lbsClusterSide2, int nb) {
        this.lbsClusterSide1 = lbsClusterSide1;
        this.lbsClusterSide2 = lbsClusterSide2;
        lbsClusterSide1.addLink(this);
        lbsClusterSide2.addLink(this);
        this.nb = nb;
        assessLink();
    }

    private void assessLink() {
        Set<BusNode> nodeBusesIntersect = new LinkedHashSet<>(lbsClusterSide1.getBusNodeSet());
        nodeBusesIntersect.retainAll(lbsClusterSide2.getBusNodeSet());
        categoryToWeight.put(LinkCategory.COMMONBUSES, nodeBusesIntersect.size());

        Set<InternCell> flatCellIntersect = new LinkedHashSet<>(lbsClusterSide1.getCandidateFlatCellList());
        flatCellIntersect.retainAll(lbsClusterSide2.getCandidateFlatCellList());
        if (flatCellIntersect.isEmpty()) {
            categoryToWeight.put(LinkCategory.FLATCELLS, 0);
        } else {
            categoryToWeight.put(LinkCategory.FLATCELLS,
                    flatCellIntersect.size() * 100
                            - flatCellIntersect.stream()
                            .mapToInt(internCell -> flatCellDistanceToEdges(internCell, lbsClusterSide1, lbsClusterSide2)).sum());
        }

        Set<InternCell> commonInternCells = new LinkedHashSet<>(lbsClusterSide1.getCrossOverCellList());
        commonInternCells.retainAll(lbsClusterSide2.getCrossOverCellList());
        categoryToWeight.put(LinkCategory.CROSSOVER, (int) (commonInternCells
                .stream()
                .flatMap(internCell -> internCell.getBusNodes().stream())
                .distinct()
                .count()));
    }

    static int flatCellDistanceToEdges(InternCell cell, LBSClusterSide lbsCS1, LBSClusterSide lbsCS2) {
        return lbsCS1.getCandidateFlatCellDistanceToEdge(cell) + lbsCS2.getCandidateFlatCellDistanceToEdge(cell);
    }

    private int getLinkCategoryWeight(LinkCategory cat) {
        return categoryToWeight.get(cat);
    }

    LBSClusterSide getOtherlbsClusterSide(LBSClusterSide lbsClusterSide) {
        if (lbsClusterSide == lbsClusterSide1) {
            return lbsClusterSide2;
        }
        if (lbsClusterSide == lbsClusterSide2) {
            return lbsClusterSide1;
        }
        return null;
    }

    LBSClusterSide getlbsClusterSide(int i) {
        if (i == 0) {
            return lbsClusterSide1;
        } else if (i == 1) {
            return lbsClusterSide2;
        }
        return null;
    }

    void mergeClusters(HorizontalBusLaneManager hblManager) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Merging Link: {}", this);
        }

        if (lbsClusterSide1.getCluster() == lbsClusterSide2.getCluster()
                || lbsClusterSide1.getMySideInCluster() == Side.UNDEFINED
                || lbsClusterSide2.getMySideInCluster() == Side.UNDEFINED) {
            return;
        }
        lbsClusterSide1.getCluster().merge(
                lbsClusterSide1.getMySideInCluster(),
                lbsClusterSide2.getCluster(),
                lbsClusterSide2.getMySideInCluster(), hblManager);
    }

    void removeMe() {
        lbsClusterSide1.removeLink(this);
        lbsClusterSide2.removeLink(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Link)) {
            return false;
        }
        return lbsClusterSide1.equals(((Link) obj).lbsClusterSide1)
                && lbsClusterSide2.equals(((Link) obj).lbsClusterSide2);
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
                + "\n\tlbsClusterSide1: " + lbsClusterSide1.toString()
                + "\n\tlbsClusterSide2: " + lbsClusterSide2.toString();
    }
}
