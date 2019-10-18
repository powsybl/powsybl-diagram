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

import java.util.List;
import java.util.Set;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
abstract class AbstractLinkable {
    abstract Set<BusNode> getBusNodeSet();

    abstract List<InternCell> getCandidateFlatCellList();

    abstract List<InternCell> getCrossOverCellList();

    abstract LBSCluster getCluster();

    abstract Side getMySidInCluster();

    abstract boolean hasSameRoot(Object other);

    abstract <T extends AbstractLinkable> void addLink(Link<T> link);

    abstract <T extends AbstractLinkable> void removeLink(Link<T> link);

    abstract <T extends AbstractLinkable> List<Link<T>> getLinks();

    <T extends AbstractLinkable> T getOtherSameRoot(List<T> linkables) {
        return null;
    }

}
