/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionprocessor.positionbyclustering;

import com.powsybl.sld.layout.positionprocessor.BSCluster;
import com.powsybl.sld.layout.positionprocessor.HorizontalBusListManager;
import com.powsybl.sld.model.coordinate.Side;

import java.util.*;

/**
 * Manages the links between a list of VbsClusterSides.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
final class Links {

    private final List<VBSClusterSide> bsClusterSides = new LinkedList<>();
    private final TreeSet<Link> linkSet = new TreeSet<>();
    private final HorizontalBusListManager hblManager;
    private int linkCounter = 0;

    private Links(HorizontalBusListManager hblManager) {
        this.hblManager = hblManager;
    }

    public static Links create(List<BSCluster> bsClusters, HorizontalBusListManager hblManager) {
        Links links = new Links(hblManager);
        bsClusters.forEach(vbsCluster -> addClusterSidesTwins(links, vbsCluster));
        return links;
    }

    private static void addClusterSidesTwins(Links links, BSCluster bsCluster) {
        VBSClusterSide vbsSLeft = new VBSClusterSide(bsCluster, Side.LEFT);
        VBSClusterSide vbsSRight = new VBSClusterSide(bsCluster, Side.RIGHT);
        vbsSLeft.setOtherSameRoot(vbsSRight);
        vbsSRight.setOtherSameRoot(vbsSLeft);
        links.addVbsClusterSide(vbsSLeft);
        links.addVbsClusterSide(vbsSRight);
    }

    private void addVbsClusterSide(VBSClusterSide vbsClusterSide) {
        bsClusterSides.forEach(cc -> buildNewLink(cc, vbsClusterSide));
        bsClusterSides.add(vbsClusterSide);
    }

    private void buildNewLink(VBSClusterSide vbsClusterSide1, VBSClusterSide vbsClusterSide2) {
        if (!vbsClusterSide1.hasSameRoot(vbsClusterSide2)) {
            Link linkToAdd = new Link(vbsClusterSide1, vbsClusterSide2, linkCounter++);
            linkSet.add(linkToAdd);
        }
    }

    Link getStrongestLink() {
        return linkSet.last();
    }

    void mergeLink(Link link) {
        link.mergeClusters(hblManager);
        BSCluster mergedCluster = link.getBsClusterSide(0).getCluster();
        removeVbsClusterSide(link.getBsClusterSide(0));
        removeVbsClusterSide(link.getBsClusterSide(1));
        removeVbsClusterSide(link.getBsClusterSide(0).getOtherSameRoot());
        removeVbsClusterSide(link.getBsClusterSide(1).getOtherSameRoot());
        addClusterSidesTwins(this, mergedCluster);
    }

    private void removeVbsClusterSide(VBSClusterSide vbsClusterSide) {
        bsClusterSides.remove(vbsClusterSide);
        List<Link> linksCopy = new ArrayList<>(vbsClusterSide.getLinks());
        linksCopy.forEach(link -> {
            link.removeMe();
            linkSet.remove(link);
        });
    }

    boolean isEmpty() {
        return linkSet.isEmpty();
    }

    BSCluster getFinalBsCluster() {
        return bsClusterSides.get(0).getCluster();
    }
}
