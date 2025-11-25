/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.EdgeInfo;
import com.powsybl.nad.svg.StyleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultCountryStyleProvider implements StyleProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultCountryStyleProvider.class);

    public DefaultCountryStyleProvider(Network network) {
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("countriesStyle.css");
    }

    @Override
    public List<URL> getCssUrls() {
        return getCssFilenames().stream()
                .map(n -> getClass().getResource("/" + n))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getNodeStyleClasses(Node node) {
        return List.of();
    }

    @Override
    public List<String> getHighlightNodeStyleClasses(Node node) {
        return List.of();
    }

    @Override
    public List<String> getBusNodeStyleClasses(BusNode busNode) {
        return List.of();
    }

    @Override
    public List<String> getBranchEdgeStyleClasses(BranchEdge branchEdge) {
        return List.of();
    }

    @Override
    public List<String> getSideEdgeStyleClasses(BranchEdge edge, BranchEdge.Side side) {
        return List.of();
    }

    @Override
    public List<String> getHighlightSideEdgeStyleClasses(BranchEdge edge, BranchEdge.Side side) {
        return List.of();
    }

    @Override
    public List<String> getEdgeInfoStyleClasses(EdgeInfo info) {
        List<String> styles = new LinkedList<>();
        switch (info.getInfoType()) {
            case EdgeInfo.ACTIVE_POWER -> styles.add(CLASSES_PREFIX + "active");
            case EdgeInfo.REACTIVE_POWER -> styles.add(CLASSES_PREFIX + "reactive");
            case EdgeInfo.CURRENT -> styles.add(CLASSES_PREFIX + "current");
            default -> LOGGER.warn("The \"{}\" type of information is not handled", info.getInfoType());
        }
        return styles;
    }

    @Override
    public List<String> getThreeWtEdgeStyleClasses(ThreeWtEdge threeWtedge) {
        return List.of();
    }

    @Override
    public List<String> getInjectionStyleClasses(Injection injection) {
        return List.of();
    }

    @Override
    public List<String> getHighlightThreeWtEdgStyleClasses(ThreeWtEdge edge) {
        return List.of();
    }
}
