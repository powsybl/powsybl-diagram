/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.HorizontalBusLaneManager;
import com.powsybl.sld.layout.LBSCluster;
import com.powsybl.sld.model.Side;

import java.util.*;

/**
 * Manages the links between a list of lbsClusterSides.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class Links {

    private final List<LBSClusterSide> lbsClusterSides = new LinkedList<>();
    private final TreeSet<Link> linkSet = new TreeSet<>();
    private HorizontalBusLaneManager hblManager;

    private Links(HorizontalBusLaneManager hblManager) {
        this.hblManager = hblManager;
    }

    public static Links create(List<LBSCluster> lbsClusters, HorizontalBusLaneManager hblManager) {
        Links links = new Links(hblManager);
        lbsClusters.forEach(lbsCluster -> {
            links.addLBSClusterSide(new LBSClusterSide(lbsCluster, Side.LEFT));
            links.addLBSClusterSide(new LBSClusterSide(lbsCluster, Side.RIGHT));
        });
        return links;
    }

    private void addLBSClusterSide(LBSClusterSide lbsClusterSide) {
        lbsClusterSides.forEach(cc -> buildNewLink(cc, lbsClusterSide));
        lbsClusterSides.add(lbsClusterSide);
    }

    private void buildNewLink(LBSClusterSide lbsClusterSide1, LBSClusterSide lbsClusterSide2) {
        if (!lbsClusterSide1.hasSameRoot(lbsClusterSide2)) {
            Link linkToAdd = new Link(lbsClusterSide1, lbsClusterSide2);
            linkSet.add(linkToAdd);
        }
    }

    Link getStrongestLink() {
        return linkSet.last();
    }

    void mergeLink(Link link) {
        link.mergeClusters(hblManager);
        LBSCluster mergedCluster = link.getlbsClusterSide(0).getCluster();
        removeLBSClusterSide(link.getlbsClusterSide(0));
        removeLBSClusterSide(link.getlbsClusterSide(1));
        removeLBSClusterSide(link.getlbsClusterSide(0).getOtherSameRoot(lbsClusterSides));
        removeLBSClusterSide(link.getlbsClusterSide(1).getOtherSameRoot(lbsClusterSides));
        addLBSClusterSide(new LBSClusterSide(mergedCluster, Side.LEFT));
        addLBSClusterSide(new LBSClusterSide(mergedCluster, Side.RIGHT));
    }

    private void removeLBSClusterSide(LBSClusterSide lbsClusterSide) {
        lbsClusterSides.remove(lbsClusterSide);
        List<Link> linksCopy = new ArrayList<>(lbsClusterSide.getLinks());
        linksCopy.forEach(link -> {
            link.removeMe();
            linkSet.remove(link);
        });
    }

    boolean isEmpty() {
        return linkSet.isEmpty();
    }

    LBSCluster getFinalLBSCluster() {
        return lbsClusterSides.get(0).getCluster();
    }
}
