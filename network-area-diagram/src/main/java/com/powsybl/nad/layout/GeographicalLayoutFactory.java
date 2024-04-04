/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.nad.model.Point;
import org.jgrapht.alg.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class GeographicalLayoutFactory extends FixedLayoutFactory implements LayoutFactory {

    private static final int SCALING_FACTOR = 100;
    private static final double RADIUS_FACTOR = 50;

    public GeographicalLayoutFactory(Network network) {
        this(network, SCALING_FACTOR, RADIUS_FACTOR, BasicForceLayout::new);
    }

    public GeographicalLayoutFactory(Network network, int scalingFactor, double radiusFactor, LayoutFactory layoutFactory) {
        super(getFixedNodePosition(network, scalingFactor, radiusFactor), layoutFactory);
    }

    private static Map<String, Point> getFixedNodePosition(Network network, int scalingFactor, double radiusFactor) {
        Map<String, Point> fixedNodePositionMap = new HashMap<>();
        network.getSubstationStream().forEach(substation -> fillPositionMap(substation, fixedNodePositionMap, scalingFactor, radiusFactor));
        return fixedNodePositionMap;
    }

    private static void fillPositionMap(Substation substation, Map<String, Point> fixedNodePositionMap, int scalingFactor, double radiusFactor) {
        SubstationPosition substationPosition = substation.getExtension(SubstationPosition.class);
        if (substationPosition != null) {
            Coordinate coordinate = substationPosition.getCoordinate();
            double latitude = coordinate.getLatitude();
            double longitude = coordinate.getLongitude();

            Pair<Double, Double> mercatorCoordinates = useMercatorLikeProjection(longitude, latitude);

            List<VoltageLevel> voltageLevelList = substation.getVoltageLevelStream().toList();
            int voltageLevelListSize = voltageLevelList.size();

            if (voltageLevelListSize == 1) {
                String voltageLevelId = voltageLevelList.get(0).getId();
                fixedNodePositionMap.put(voltageLevelId, new Point(scalingFactor * mercatorCoordinates.getFirst(), scalingFactor * mercatorCoordinates.getSecond()));
            } else if (voltageLevelListSize > 1) {
                //Deal with voltage levels within the same substation (and thus with the same coordinates)
                double angle = 2 * Math.PI / voltageLevelListSize;
                int i = 0;
                for (VoltageLevel voltageLevel : voltageLevelList) {
                    double angleVoltageLevel = angle * i;
                    fixedNodePositionMap.put(voltageLevel.getId(), new Point(scalingFactor * mercatorCoordinates.getFirst() + radiusFactor * Math.cos(angleVoltageLevel), scalingFactor * mercatorCoordinates.getSecond() + radiusFactor * Math.sin(angleVoltageLevel)));
                    i++;
                }
            }
        }
    }

    private static Pair<Double, Double> useMercatorLikeProjection(double longitude, double latitude) {
        double x = longitude * Math.PI / 180;
        double y = Math.log(Math.tan(Math.PI / 4 + latitude * Math.PI / 180 / 2));
        return new Pair(x, y);
    }

}
