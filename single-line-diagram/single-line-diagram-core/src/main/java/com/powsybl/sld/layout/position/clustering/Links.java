/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position.clustering;

import com.powsybl.sld.layout.position.BSCluster;
import com.powsybl.sld.model.coordinate.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Manages the links between a list of BSClusterSides.
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
final class Links {

    private final List<BSClusterSide> bsClusterSides = new LinkedList<>();
    private final TreeSet<Link> linkSet = new TreeSet<>();
    private final Map<BSClusterSide, List<Link>> bsClusterSideToLink = new HashMap<>();
    private int linkCounter = 0;

    public Links(List<BSCluster> bsClusters) {
        bsClusters.forEach(this::addClusterSidesTwins);
    }

    private void addClusterSidesTwins(BSCluster bsCluster) {
        BSClusterSide bsSLeft = new BSClusterSide(bsCluster, Side.LEFT);
        BSClusterSide bsSRight = new BSClusterSide(bsCluster, Side.RIGHT);
        bsSLeft.setOtherSameRoot(bsSRight);
        bsSRight.setOtherSameRoot(bsSLeft);
        addBsClusterSide(bsSLeft);
        addBsClusterSide(bsSRight);
    }

    private void addBsClusterSide(BSClusterSide bsClusterSide) {
        bsClusterSideToLink.put(bsClusterSide, new ArrayList<>());
        bsClusterSides.forEach(cc -> buildNewLink(cc, bsClusterSide));
        bsClusterSides.add(bsClusterSide);
    }

    private void buildNewLink(BSClusterSide bsClusterSide1, BSClusterSide bsClusterSide2) {
        if (bsClusterSide1.getCluster() != bsClusterSide2.getCluster()) {
            Link linkToAdd = new Link(bsClusterSide1, bsClusterSide2, linkCounter++);
            linkSet.add(linkToAdd);
            bsClusterSideToLink.get(bsClusterSide1).add(linkToAdd);
            bsClusterSideToLink.get(bsClusterSide2).add(linkToAdd);
        }
    }

    Link getStrongestLink() {
        return linkSet.last();
    }

    void mergeLink(Link link) {
        link.mergeClusters();
        BSCluster mergedCluster = link.getBsClusterSide(0).getCluster();
        removeBsClusterSide(link.getBsClusterSide(0));
        removeBsClusterSide(link.getBsClusterSide(1));
        removeBsClusterSide(link.getBsClusterSide(0).getOtherSameRoot());
        removeBsClusterSide(link.getBsClusterSide(1).getOtherSameRoot());
        addClusterSidesTwins(mergedCluster);
    }

    private void removeBsClusterSide(BSClusterSide bsClusterSide) {
        bsClusterSides.remove(bsClusterSide);
        bsClusterSideToLink.get(bsClusterSide).forEach(linkSet::remove);
    }

    boolean isEmpty() {
        return linkSet.isEmpty();
    }

    BSCluster getFinalBsCluster() {
        return bsClusterSides.getFirst().getCluster();
    }
}
