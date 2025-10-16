/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface LabelProvider {
    Optional<EdgeInfo> getBranchEdgeInfo(String branchId, BranchEdge.Side side, String branchType);

    Optional<EdgeInfo> getThreeWindingTransformerEdgeInfo(String threeWindingTransformerId, ThreeWtEdge.Side side);

    Optional<EdgeInfo> getInjectionEdgeInfo(String injectionId);

    String getBranchLabel(String branchId);

    List<String> getLegendHeader(String voltageLevelId);

    String getLegend(String busId);

    List<String> getLegendFooter(String voltageLevelId);
}
