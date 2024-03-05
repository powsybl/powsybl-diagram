/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.nad.model.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class GeographicalLayoutFactory implements LayoutFactory {

    private Network network;

    private int scalingFactor = 100;
    private double repulsionFactor = 50;

    public GeographicalLayoutFactory(Network network) {
        Objects.requireNonNull(network);
        this.network = network;
    }

    public GeographicalLayoutFactory(Network network, int scalingFactor, double repulsionFactor) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(scalingFactor);
        Objects.requireNonNull(repulsionFactor);
        this.network = network;
        this.scalingFactor = scalingFactor;
        this.repulsionFactor = repulsionFactor;
    }

    @Override
    public Layout create() {
        AbstractLayout layout = new BasicForceLayout();
        layout.setFixedNodePositions(getFixedNodePosition());
        return layout;
    }

    private Map<String, Point> getFixedNodePosition() {
        Map<String, Point> fixedNodePositionMap = new HashMap<>();

        network.getSubstationStream().forEach(
                substation -> {
                    SubstationPosition substationPosition = substation.getExtension(SubstationPosition.class);
                    if (substationPosition != null) {
                        Coordinate coordinate = substationPosition.getCoordinate();
                        double latitude = coordinate.getLatitude();
                        double longitude = coordinate.getLongitude();

                        List<VoltageLevel> voltageLevelList = substation.getVoltageLevelStream().toList();
                        int voltageLevelListSize = voltageLevelList.size();

                        if (voltageLevelListSize == 1) {
                            String voltageLevelId = voltageLevelList.get(0).getId();
                            fixedNodePositionMap.put(voltageLevelId, new Point(longitude * scalingFactor, latitude * scalingFactor));
                        } else if (voltageLevelListSize > 1) {
                            //Deal with voltage levels within the same substation (and thus with the same coordinates)
                            double angle = 2 * Math.PI / voltageLevelListSize;
                            int i = 0;
                            for (VoltageLevel voltageLevel : voltageLevelList) {
                                double angleVoltageLevel = angle * i;
                                fixedNodePositionMap.put(voltageLevel.getId(), new Point(longitude * scalingFactor + repulsionFactor * Math.cos(angleVoltageLevel), latitude * scalingFactor + repulsionFactor * Math.cos(angleVoltageLevel)));
                                i++;
                            }
                        }
                    }
                }
        );

        return fixedNodePositionMap;
    }
}
