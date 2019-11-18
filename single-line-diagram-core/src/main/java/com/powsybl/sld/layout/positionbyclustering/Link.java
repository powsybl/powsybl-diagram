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

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class Link<T extends ClusterConnector> implements Comparable {

    enum LinkCategory {
        COMMONBUSES, FLATCELLS, CROSSOVER//, SHUNT
    }

    private T clusterConnector1;
    private T clusterConnector2;
    private Map<LinkCategory, Integer> categoryToWeight = new EnumMap<>(LinkCategory.class);

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
        if (clusterConnector1.getCluster() == clusterConnector2.getCluster()
                || clusterConnector1.getMySidInCluster() == Side.UNDEFINED
                || clusterConnector2.getMySidInCluster() == Side.UNDEFINED) {
            return;
        }
        clusterConnector1.getCluster().merge(
                clusterConnector1.getMySidInCluster(),
                clusterConnector2.getCluster(),
                clusterConnector2.getMySidInCluster());
    }

    boolean hasLink() {
        return categoryToWeight.values().stream().mapToInt(Integer::intValue).sum() != 0;
    }

    void unregister() {
        clusterConnector1.removeLink(this);
        clusterConnector2.removeLink(this);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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
                + " CrossOver: " + categoryToWeight.get(LinkCategory.CROSSOVER);
    }
}
