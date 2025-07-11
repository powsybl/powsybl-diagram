/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util;

import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModel;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class IidmUtil {
    private IidmUtil() {
    }

    public static boolean isCapacitor(ShuntCompensator sc) {
        switch (sc.getModelType()) {
            case LINEAR:
                return ((ShuntCompensatorLinearModel) sc.getModel()).getBPerSection() >= 0;
            case NON_LINEAR:
                ShuntCompensatorNonLinearModel model = (ShuntCompensatorNonLinearModel) sc.getModel();
                double averageB = model.getAllSections().stream().mapToDouble(ShuntCompensatorNonLinearModel.Section::getB).average().orElse(0);
                return averageB >= 0;
            default:
                throw new IllegalStateException("Unknown shunt compensator model type: " + sc.getModelType());
        }
    }
}
