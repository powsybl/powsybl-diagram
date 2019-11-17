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
 * A clusterConnector defines the extremity of a link between two clusters of LegBusSets.
 * A clusterConnector has links with other clusterConnectors (having the same implementation).
 * <p>
 * The different implementations enables to support different strategies of prioritization in the clusters merging
 * process.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
abstract class AbstractClusterConnector {
    abstract Set<BusNode> getBusNodeSet();

    /**
     * @param internCell a cell that is connected to the LBSCluster and to another
     * @return the distance of the leg of the internCell to the edge of the cluster. For implementation that
     * overrides it, taking into account the side of the connector - when assessing the strength of the Link held by the
     * internCell - enables to give more strength to the link for which the leg of the cell is closer to the its edge.
     */
    int getDistanceToEdge(InternCell internCell) {
        return 0;
    }

    abstract List<InternCell> getCandidateFlatCellList();

    abstract List<InternCell> getCrossOverCellList();

    abstract LBSCluster getCluster();

    abstract Side getMySideInCluster();

    abstract <T extends AbstractClusterConnector> void addLink(Link<T> link);

    abstract <T extends AbstractClusterConnector> void removeLink(Link<T> link);

    abstract <T extends AbstractClusterConnector> List<Link<T>> getLinks();

    /**
     * @param other object
     * @return true in case the other object has the same implementation and belongs to the same LBSCluster
     */

    abstract boolean hasSameLBSCluster(Object other);

    /**
     * @param clusterConnectors list of clusterConnectors
     * @param <T>               type extending AbstractClusterConnector
     * @return for implementation with 2 ClusterConnectors per LBSCluster, return the ClusterConnector on the opposite
     * side
     */
    abstract <T extends AbstractClusterConnector> T getOtherSameWithSameLBSCluster(List<T> clusterConnectors);

}
