package com.powsybl.sld.model.nodes.feeders;

import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.NodeSide;

public class FeederTeePointLeg extends FeederTwLeg {
    public FeederTeePointLeg(FeederType feederType, NodeSide side, VoltageLevelInfos myVoltageLevelInfos, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(feederType, side, myVoltageLevelInfos, otherSideVoltageLevelInfos);
    }
}
