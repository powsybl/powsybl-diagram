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
class Links<T extends Linkable> {
    private List<T> linkables;
    private TreeSet<Link<T>> linkSet = new TreeSet<>();

    Links(List<T> linkables) {
        this.linkables = linkables;
        for (int i = 0; i < linkables.size(); i++) {
            for (int j = i + 1; j < linkables.size(); j++) {
                buildNewLink(linkables.get(i), linkables.get(j));
            }
        }
    }

    Links() {
        this.linkables = new ArrayList<>();
    }

    void addLinkable(T linkable) {
        linkables.add(linkable);
        linkables.forEach(lk -> buildNewLink(lk, linkable));
    }

    private void buildNewLink(T linkable1, T linkable2) {
        if (!linkable1.hasSameRoot(linkable2)) {
            linkSet.add(new Link<>(linkable1, linkable2));
        }
    }

    Link<T> getStrongerLink() {
        return linkSet.last();
    }

    void removeLinkable(T linkable) {
        linkables.remove(linkable);
        removeLinksToLinkable(linkable);
    }

    private void removeLinksToLinkable(T linkable) {
        List<Link<T>> linksCopy = new ArrayList<>(linkable.getLinks());
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

    List<T> getLinkables() {
        return linkables;
    }
}
