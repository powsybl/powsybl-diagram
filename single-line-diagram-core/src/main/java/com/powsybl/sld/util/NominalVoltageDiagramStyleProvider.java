/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.color.BaseVoltageColor;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.VoltageLevelInfos;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageDiagramStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    public NominalVoltageDiagramStyleProvider(Network network) {
        this(BaseVoltageColor.fromPlatformConfig(), network);
    }

    public NominalVoltageDiagramStyleProvider(BaseVoltageColor baseVoltageColor, Network network) {
        super(baseVoltageColor, network);
    }

    @Override
    protected String getNodeColor(VoltageLevelInfos voltageLevelInfos, Node node) {
        return getBaseColor(voltageLevelInfos.getNominalVoltage());
    }
}
