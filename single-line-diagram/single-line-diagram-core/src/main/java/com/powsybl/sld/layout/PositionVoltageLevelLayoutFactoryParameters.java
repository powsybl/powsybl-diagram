/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Side;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */

public class PositionVoltageLevelLayoutFactoryParameters {
    private boolean feederStacked = true;
    private boolean removeUnnecessaryFictitiousNodes = true;
    private boolean substituteSingularFictitiousByFeederNode = true;
    private boolean exceptionIfPatternNotHandled = false;
    private boolean handleShunts = false;
    private Map<String, Side> busInfoMap = new HashMap<>();

    public boolean isFeederStacked() {
        return feederStacked;
    }

    public PositionVoltageLevelLayoutFactoryParameters setFeederStacked(boolean feederStacked) {
        this.feederStacked = feederStacked;
        return this;
    }

    public boolean isExceptionIfPatternNotHandled() {
        return exceptionIfPatternNotHandled;
    }

    public PositionVoltageLevelLayoutFactoryParameters setExceptionIfPatternNotHandled(boolean exceptionIfPatternNotHandled) {
        this.exceptionIfPatternNotHandled = exceptionIfPatternNotHandled;
        return this;
    }

    public boolean isRemoveUnnecessaryFictitiousNodes() {
        return removeUnnecessaryFictitiousNodes;
    }

    public PositionVoltageLevelLayoutFactoryParameters setRemoveUnnecessaryFictitiousNodes(boolean removeUnnecessaryFictitiousNodes) {
        this.removeUnnecessaryFictitiousNodes = removeUnnecessaryFictitiousNodes;
        return this;
    }

    public boolean isSubstituteSingularFictitiousByFeederNode() {
        return substituteSingularFictitiousByFeederNode;
    }

    public PositionVoltageLevelLayoutFactoryParameters setSubstituteSingularFictitiousByFeederNode(boolean substituteSingularFictitiousByFeederNode) {
        this.substituteSingularFictitiousByFeederNode = substituteSingularFictitiousByFeederNode;
        return this;
    }

    public boolean isHandleShunts() {
        return handleShunts;
    }

    public PositionVoltageLevelLayoutFactoryParameters setHandleShunts(boolean handleShunts) {
        this.handleShunts = handleShunts;
        return this;
    }

    public Map<String, Side> getBusInfoMap() {
        return busInfoMap;
    }

    public PositionVoltageLevelLayoutFactoryParameters setBusInfoMap(Map<String, Side> busInfoMap) {
        this.busInfoMap = busInfoMap;
        return this;
    }
}
