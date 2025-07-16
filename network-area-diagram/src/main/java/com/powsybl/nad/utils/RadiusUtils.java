/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.utils;

import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.VoltageLevelNode;
import com.powsybl.nad.svg.SvgParameters;

public final class RadiusUtils {

    private RadiusUtils() {
    }

    public static double getVoltageLevelCircleRadius(VoltageLevelNode vlNode, SvgParameters svgParameters) {
        if (vlNode.isFictitious()) {
            return svgParameters.getFictitiousVoltageLevelCircleRadius();
        }
        int nbBuses = vlNode.getBusNodes().size();
        return Math.min(Math.max(nbBuses, 1), 2) * svgParameters.getVoltageLevelCircleRadius();
    }

    public static double getBusAnnulusInnerRadius(BusNode node, VoltageLevelNode vlNode, SvgParameters svgParameters) {
        if (node.getRingIndex() == 0) {
            return 0;
        }
        int nbNeighbours = node.getNbNeighbouringBusNodes();
        double unitaryRadius = getVoltageLevelCircleRadius(vlNode, svgParameters) / (nbNeighbours + 1);
        return node.getRingIndex() * unitaryRadius + svgParameters.getInterAnnulusSpace() / 2;
    }

    public static double getBusAnnulusOuterRadius(BusNode node, VoltageLevelNode vlNode, SvgParameters svgParameters) {
        int nbNeighbours = node.getNbNeighbouringBusNodes();
        double unitaryRadius = getVoltageLevelCircleRadius(vlNode, svgParameters) / (nbNeighbours + 1);
        return (node.getRingIndex() + 1) * unitaryRadius - svgParameters.getInterAnnulusSpace() / 2;
    }
}
