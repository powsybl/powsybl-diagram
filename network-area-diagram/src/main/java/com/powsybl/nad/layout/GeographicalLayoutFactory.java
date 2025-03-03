/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.nad.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class GeographicalLayoutFactory extends ForceLayoutFactory implements LayoutFactory {

    protected static final Logger LOGGER = LoggerFactory.getLogger(GeographicalLayoutFactory.class);

    private static final int SCALING_FACTOR = 150000;
    private static final double RADIUS_FACTOR = 150;

    public GeographicalLayoutFactory(Network network) {
        this(network, SCALING_FACTOR, RADIUS_FACTOR, () -> new BasicForceLayout(false, false));
    }

    public GeographicalLayoutFactory(Network network, int scalingFactor, double radiusFactor, LayoutFactory layoutFactory) {
        super(getFixedNodePosition(network, scalingFactor, radiusFactor),
                network.getVoltageLevelStream().filter(v -> v.getNominalV() > 340 && v.getSubstation().flatMap(Substation::getCountry).filter(c -> c == Country.FR).isPresent()).map(VoltageLevel::getId).collect(Collectors.toSet()));
    }

    private static Map<String, Point> getFixedNodePosition(Network network, int scalingFactor, double radiusFactor) {
        Map<String, Point> fixedNodePositionMap = new HashMap<>();
        EnumMap<Country, Integer> countryMap = new EnumMap<>(Country.class);
        network.getSubstationStream().forEach(substation -> fillPositionMap(substation, fixedNodePositionMap, scalingFactor, radiusFactor));
        network.getSubstationStream()
                .filter(s -> s.getExtension(SubstationPosition.class) == null)
                .forEach(s -> s.getCountry()
                        .flatMap(c -> getCountryPosition(c, countryMap))
                        .ifPresent(coordinate -> fillPositionMap(s, fixedNodePositionMap, scalingFactor, radiusFactor, coordinate)));

//        String countries = network.getSubstationStream()
//                .filter(s -> s.getExtension(SubstationPosition.class) == null)
//                .map(Substation::getCountry)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .filter(c -> getCountryPosition(c).isEmpty())
//                .distinct()
//                .map(Objects::toString)
//                .collect(Collectors.joining("/"));
//        System.out.println(countries);

        int voltageLevelCount = network.getVoltageLevelCount();
        int missingPositions = voltageLevelCount - fixedNodePositionMap.size();
        double missingPositionsRatio = (double) missingPositions / voltageLevelCount;
        LOGGER.atLevel(missingPositionsRatio > 0.3 ? Level.WARN : Level.INFO)
                .log("{} missing voltage level positions out of {} voltage levels", missingPositions, voltageLevelCount);

        return fixedNodePositionMap;
    }

    private static Optional<Coordinate> getCountryPosition(Country c, EnumMap<Country, Integer> countryMap) {
        double shift = countryMap.merge(c, 1, (v1, v2) -> v1 + 1) / 100.;
        return Optional.ofNullable(switch (c) {
            case ES -> new Coordinate(40.4168, -3.7038 + shift);
            case IT -> new Coordinate(41.9028, 12.4964 + shift);
            case DE -> new Coordinate(48.7833, 9.1833 + shift);
            case CH -> new Coordinate(46.9481, 7.4474 + shift);
            case BE -> new Coordinate(50.8503, 4.3517 + shift);
            case LU -> new Coordinate(49.6117, 6.13 + shift);
            case GB -> new Coordinate(51.5074, -0.1278 + shift);
            case NL -> new Coordinate(52.3779, 4.8971 + shift);
            default -> null;
        });
    }

    private static void fillPositionMap(Substation substation, Map<String, Point> fixedNodePositionMap, int scalingFactor, double radiusFactor) {
        SubstationPosition substationPosition = substation.getExtension(SubstationPosition.class);
        if (substationPosition != null) {
            fillPositionMap(substation, fixedNodePositionMap, scalingFactor, radiusFactor, substationPosition.getCoordinate());
        }
    }

    private static void fillPositionMap(Substation substation, Map<String, Point> fixedNodePositionMap, int scalingFactor, double radiusFactor, Coordinate substationCoordinate) {
        Point mercatorCoordinates = useMercatorLikeProjection(scalingFactor, substationCoordinate);
        long voltageLevelListSize = substation.getVoltageLevelStream().count();

        if (voltageLevelListSize == 1) {
            String voltageLevelId = substation.getVoltageLevels().iterator().next().getId();
            fixedNodePositionMap.put(voltageLevelId, mercatorCoordinates);
        } else if (voltageLevelListSize > 1) {
            //Deal with voltage levels within the same substation (and thus with the same coordinates)
            double angle = 2 * Math.PI / voltageLevelListSize;
            int i = 0;
            for (VoltageLevel voltageLevel : substation.getVoltageLevels()) {
                double angleVoltageLevel = angle * i;
                Point pointOnCircle = mercatorCoordinates.shift(radiusFactor * Math.cos(angleVoltageLevel), radiusFactor * Math.sin(angleVoltageLevel));
                fixedNodePositionMap.put(voltageLevel.getId(), pointOnCircle);
                i++;
            }
        }
    }

    private static Point useMercatorLikeProjection(double scalingFactor, Coordinate coordinate) {
        double x = coordinate.getLongitude() * Math.PI / 180;
        double y = -Math.log(Math.tan(Math.PI / 4 + coordinate.getLatitude() * Math.PI / 180 / 2));
        return new Point(scalingFactor * x, scalingFactor * y);
    }

}
