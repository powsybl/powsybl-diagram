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
class Link<T extends AbstractLinkable> implements Comparable {

    enum LinkCategory {
        COMMONBUSES, FLATCELLS, CROSSOVER//, SHUNT
    }

    private T linkable1;
    private T linkable2;
    private Map<LinkCategory, Integer> categoryToWeight = new EnumMap<>(LinkCategory.class);

    Link(T linkable1, T linkable2) {
        this.linkable1 = linkable1;
        this.linkable2 = linkable2;
        linkable1.addLink(this);
        linkable2.addLink(this);
        assessLink();
    }

    void assessLink() {
        HashSet<BusNode> nodeBusesIntersect = new HashSet<>(linkable1.getBusNodeSet());
        nodeBusesIntersect.retainAll(linkable2.getBusNodeSet());
        categoryToWeight.put(LinkCategory.COMMONBUSES, nodeBusesIntersect.size());

        HashSet<InternCell> flatCellIntersect = new HashSet<>(linkable1.getCandidateFlatCellList());
        flatCellIntersect.retainAll(linkable2.getCandidateFlatCellList());
        if (flatCellIntersect.isEmpty()) {
            categoryToWeight.put(LinkCategory.FLATCELLS, 0);
        } else {
            categoryToWeight.put(LinkCategory.FLATCELLS,
                    flatCellIntersect.size() * 100
                            - flatCellIntersect.stream()
                            .mapToInt(internCell -> linkable1.getDistanceToEdge(internCell)
                                    + linkable2.getDistanceToEdge(internCell)).sum());
        }

        HashSet<InternCell> commonInternCells = new HashSet<>(linkable1.getCrossOverCellList());
        commonInternCells.retainAll(linkable2.getCrossOverCellList());
        categoryToWeight.put(LinkCategory.CROSSOVER, (int) (commonInternCells
                .stream()
                .flatMap(internCell -> internCell.getBusNodes().stream())
                .distinct()
                .count()));
    }

    int getLinkCategoryWeight(LinkCategory cat) {
        return categoryToWeight.get(cat);
    }

    T getOtherLinkable(T linkable) {
        if (linkable == linkable1) {
            return linkable2;
        }
        if (linkable == linkable2) {
            return linkable1;
        }
        return null;
    }

    T getLinkable(int i) {
        if (i == 0) {
            return linkable1;
        } else if (i == 1) {
            return linkable2;
        }
        return null;
    }

    void mergeClusters() {
        if (linkable1.getCluster() == linkable2.getCluster()
                || linkable1.getMySidInCluster() == Side.UNDEFINED
                || linkable2.getMySidInCluster() == Side.UNDEFINED) {
            return;
        }
        linkable1.getCluster().merge(
                linkable1.getMySidInCluster(),
                linkable2.getCluster(),
                linkable2.getMySidInCluster());
    }

    boolean hasLink() {
        return categoryToWeight.values().stream().mapToInt(Integer::intValue).sum() != 0;
    }

    void unregister() {
        linkable1.removeLink(this);
        linkable2.removeLink(this);
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
