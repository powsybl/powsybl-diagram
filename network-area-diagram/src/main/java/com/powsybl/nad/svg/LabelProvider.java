/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.ThreeWtEdge;

import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface LabelProvider {
    /**
     * Returns a list of edge info for the given branch side. Maximum 2 elements allowed.
     */
    List<EdgeInfo> getBranchEdgeInfos(String branchId, BranchEdge.Side side, String branchType);

    List<EdgeInfo> getThreeWindingTransformerEdgeInfos(String threeWindingTransformerId, ThreeWtEdge.Side side);

    List<EdgeInfo> getInjectionEdgeInfos(String injectionId);

    List<EdgeInfo> getBranchEdgeInfos(String branchId, String branchType);

    VoltageLevelLegend getVoltageLevelLegend(String voltageLevelId);
}
