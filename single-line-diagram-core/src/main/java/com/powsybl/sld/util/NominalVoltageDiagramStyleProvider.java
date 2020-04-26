/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.sld.color.BaseVoltageColor;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.VoltageLevelInfos;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageDiagramStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    public NominalVoltageDiagramStyleProvider() {
        super(BaseVoltageColor.fromPlatformConfig());
    }

    public NominalVoltageDiagramStyleProvider(BaseVoltageColor baseVoltageColor) {
        super(baseVoltageColor);
    }

    @Override
    protected String getNodeColor(VoltageLevelInfos voltageLevelInfos, Node node) {
        return getBaseColor(voltageLevelInfos.getNominalVoltage());
    }

    @Override
    protected String getEdgeColor(VoltageLevelInfos voltageLevelInfos1, Node node1, VoltageLevelInfos voltageLevelInfos2, Node node2) {
        return voltageLevelInfos1 != null ? getNodeColor(voltageLevelInfos1, node1)
                                          : getNodeColor(voltageLevelInfos2, node2);
    }
}
