/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class Links<T extends ClusterConnector> {
    private List<T> clusterConnectors;
    private TreeSet<Link<T>> linkSet = new TreeSet<>();

    Links(List<T> clusterConnectors) {
        this.clusterConnectors = clusterConnectors;
        for (int i = 0; i < clusterConnectors.size(); i++) {
            for (int j = i + 1; j < clusterConnectors.size(); j++) {
                buildNewLink(clusterConnectors.get(i), clusterConnectors.get(j));
            }
        }
    }

    Links() {
        this.clusterConnectors = new ArrayList<>();
    }

    void addClusterConnector(T clusterConnector) {
        clusterConnectors.add(clusterConnector);
        clusterConnectors.forEach(lk -> buildNewLink(lk, clusterConnector));
    }

    private void buildNewLink(T clusterConnector1, T clusterConnector2) {
        if (!clusterConnector1.hasSameRoot(clusterConnector2)) {
            linkSet.add(new Link<>(clusterConnector1, clusterConnector2));
        }
    }

    Link<T> getStrongerLink() {
        return linkSet.last();
    }

    void removeClusterConnector(T clusterConnector) {
        clusterConnectors.remove(clusterConnector);
        removeLinksToClusterConnector(clusterConnector);
    }

    private void removeLinksToClusterConnector(T clusterConnector) {
        List<Link<T>> linksCopy = new ArrayList<>(clusterConnector.getLinks());
        linksCopy.forEach(link -> {
            link.unregister();
            linkSet.remove(link);
        });
    }

    Set<Link<T>> getLinkSet() {
        return linkSet;
    }

    boolean isEmpty() {
        return linkSet.isEmpty();
    }

    List<T> getClusterConnectors() {
        return clusterConnectors;
    }
}
