package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;

import java.util.List;
import java.util.Set;

abstract class AbstractLinkable {
    abstract Set<BusNode> getBusNodeSet();

    int getDistanceToEdge(InternCell internCell) {
        return 0;
    }

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
