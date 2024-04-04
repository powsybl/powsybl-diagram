/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position.clustering;

import com.powsybl.sld.layout.position.BSCluster;
import com.powsybl.sld.layout.position.HorizontalBusListManager;
import com.powsybl.sld.model.coordinate.Side;

import java.util.*;

/**
 * Manages the links between a list of BSClusterSides.
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
final class Links {

    private final List<BSClusterSide> bsClusterSides = new LinkedList<>();
    private final TreeSet<Link> linkSet = new TreeSet<>();
    private final Map<BSClusterSide, List<Link>> bsCs2Link = new HashMap<>();
    private final HorizontalBusListManager hblManager;
    private int linkCounter = 0;

    public Links(List<BSCluster> bsClusters, HorizontalBusListManager hblManager) {
        this.hblManager = hblManager;
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
        bsCs2Link.put(bsClusterSide, new ArrayList<>());
        bsClusterSides.forEach(cc -> buildNewLink(cc, bsClusterSide));
        bsClusterSides.add(bsClusterSide);
    }

    private void buildNewLink(BSClusterSide bsClusterSide1, BSClusterSide bsClusterSide2) {
        if (bsClusterSide1.getCluster() != bsClusterSide2.getCluster()) {
            Link linkToAdd = new Link(bsClusterSide1, bsClusterSide2, linkCounter++);
            linkSet.add(linkToAdd);
            bsCs2Link.get(bsClusterSide1).add(linkToAdd);
            bsCs2Link.get(bsClusterSide2).add(linkToAdd);
        }
    }

    Link getStrongestLink() {
        return linkSet.last();
    }

    void mergeLink(Link link) {
        link.mergeClusters(hblManager);
        BSCluster mergedCluster = link.getBsClusterSide(0).getCluster();
        removeBsClusterSide(link.getBsClusterSide(0));
        removeBsClusterSide(link.getBsClusterSide(1));
        removeBsClusterSide(link.getBsClusterSide(0).getOtherSameRoot());
        removeBsClusterSide(link.getBsClusterSide(1).getOtherSameRoot());
        addClusterSidesTwins(mergedCluster);
    }

    private void removeBsClusterSide(BSClusterSide bsClusterSide) {
        bsClusterSides.remove(bsClusterSide);
        bsCs2Link.get(bsClusterSide).forEach(linkSet::remove);
    }

    boolean isEmpty() {
        return linkSet.isEmpty();
    }

    BSCluster getFinalBsCluster() {
        return bsClusterSides.get(0).getCluster();
    }
}
