/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A link is define between two clusterConnectors (having the same implementation).
 * It implements Comparable that compares the strength of the link between two clusterConnectors.
 * From the strongest to the weakest kind of link :
 * <ul>
 * <li>
 * having buses in common (the more there are the stronger is the link),
 * </li>
 * <li>
 * having flatcells in common (ie a cell with only two buses, one in each clusterConnector), the more there are,
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
class Link<T extends ClusterConnector> implements Comparable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Link.class);

    enum LinkCategory {
        COMMONBUSES, FLATCELLS, CROSSOVER//, SHUNT
    }

    private final T clusterConnector1;
    private final T clusterConnector2;
    private final Map<LinkCategory, Integer> categoryToWeight = new EnumMap<>(LinkCategory.class);

    Link(T clusterConnector1, T clusterConnector2) {
        this.clusterConnector1 = clusterConnector1;
        this.clusterConnector2 = clusterConnector2;
        clusterConnector1.addLink(this);
        clusterConnector2.addLink(this);
        assessLink();
    }

    private void assessLink() {
        HashSet<BusNode> nodeBusesIntersect = new HashSet<>(clusterConnector1.getBusNodeSet());
        nodeBusesIntersect.retainAll(clusterConnector2.getBusNodeSet());
        categoryToWeight.put(LinkCategory.COMMONBUSES, nodeBusesIntersect.size());

        HashSet<InternCell> flatCellIntersect = new HashSet<>(clusterConnector1.getCandidateFlatCellList());
        flatCellIntersect.retainAll(clusterConnector2.getCandidateFlatCellList());
        if (flatCellIntersect.isEmpty()) {
            categoryToWeight.put(LinkCategory.FLATCELLS, 0);
        } else {
            categoryToWeight.put(LinkCategory.FLATCELLS,
                    flatCellIntersect.size() * 100
                            - flatCellIntersect.stream()
                            .mapToInt(internCell -> clusterConnector1.getDistanceToEdge(internCell)
                                    + clusterConnector2.getDistanceToEdge(internCell)).sum());
        }

        HashSet<InternCell> commonInternCells = new HashSet<>(clusterConnector1.getCrossOverCellList());
        commonInternCells.retainAll(clusterConnector2.getCrossOverCellList());
        categoryToWeight.put(LinkCategory.CROSSOVER, (int) (commonInternCells
                .stream()
                .flatMap(internCell -> internCell.getBusNodes().stream())
                .distinct()
                .count()));
    }

    private int getLinkCategoryWeight(LinkCategory cat) {
        return categoryToWeight.get(cat);
    }

    T getOtherClusterConnector(T clusterConnector) {
        if (clusterConnector == clusterConnector1) {
            return clusterConnector2;
        }
        if (clusterConnector == clusterConnector2) {
            return clusterConnector1;
        }
        return null;
    }

    T getClusterConnector(int i) {
        if (i == 0) {
            return clusterConnector1;
        } else if (i == 1) {
            return clusterConnector2;
        }
        return null;
    }

    void mergeClusters() {
        LOGGER.info("Merging Link: " + toString());

        if (clusterConnector1.getCluster() == clusterConnector2.getCluster()
                || clusterConnector1.getMySideInCluster() == Side.UNDEFINED
                || clusterConnector2.getMySideInCluster() == Side.UNDEFINED) {
            return;
        }
        clusterConnector1.getCluster().merge(
                clusterConnector1.getMySideInCluster(),
                clusterConnector2.getCluster(),
                clusterConnector2.getMySideInCluster());
    }

    boolean hasLink() {
        return categoryToWeight.values().stream().mapToInt(Integer::intValue).sum() != 0;
    }

    void removeMe() {
        clusterConnector1.removeLink(this);
        clusterConnector2.removeLink(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Link)) return false;
        return clusterConnector1.equals(((Link<T>) obj).clusterConnector1)
                && clusterConnector2.equals(((Link<T>) obj).clusterConnector2);
    }

    @Override
    public int hashCode() {
        return clusterConnector1.hashCode() + 2027 * clusterConnector2.hashCode();
    }

    @Override
    public int compareTo(@Nonnull Object o) {
        if (!(o instanceof Link)) {
            return 0;
        }
        Link link = (Link) o;
        for (LinkCategory category : LinkCategory.values()) {
            if (link.getLinkCategoryWeight(category) > getLinkCategoryWeight(category)) {
                return -1;
            }
            if (link.getLinkCategoryWeight(category) < getLinkCategoryWeight(category)) {
                return 1;
            }
        }
        return this.hashCode() - o.hashCode();
    }

    @Override
    public String toString() {
        return "CommonBus: " + categoryToWeight.get(LinkCategory.COMMONBUSES)
                + " FlatCell: " + categoryToWeight.get(LinkCategory.FLATCELLS)
                + " CrossOver: " + categoryToWeight.get(LinkCategory.CROSSOVER)
                + "\n\tConnector1: " + clusterConnector1.toString()
                + "\n\tConnector2: " + clusterConnector2.toString();
    }
}
