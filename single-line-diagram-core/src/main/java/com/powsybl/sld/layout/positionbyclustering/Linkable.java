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
interface Linkable {
    Set<BusNode> getBusNodeSet();

    int getDistanceToEdge(InternCell internCell);

    List<InternCell> getCandidateFlatCellList();

    List<InternCell> getCrossOverCellList();

    LBSCluster getCluster();

    Side getMySidInCluster();

    boolean hasSameRoot(Object other);

    <T extends Linkable> void addLink(Link<T> link);

    <T extends Linkable> void removeLink(Link<T> link);

    <T extends Linkable> List<Link<T>> getLinks();

    <T extends Linkable> T getOtherSameRoot(List<T> linkables);

}
