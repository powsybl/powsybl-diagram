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
interface ClusterConnector {
    Set<BusNode> getBusNodeSet();

    int getDistanceToEdge(InternCell internCell);

    List<InternCell> getCandidateFlatCellList();

    List<InternCell> getCrossOverCellList();

    LBSCluster getCluster();

    Side getMySidInCluster();

    boolean hasSameRoot(Object other);

    <T extends ClusterConnector> void addLink(Link<T> link);

    <T extends ClusterConnector> void removeLink(Link<T> link);

    <T extends ClusterConnector> List<Link<T>> getLinks();

    <T extends ClusterConnector> T getOtherSameRoot(List<T> clusterConnectors);

}
