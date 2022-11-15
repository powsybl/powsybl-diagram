/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.nad.model.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractStyleProvider implements StyleProvider {

    private final BaseVoltagesConfig baseVoltagesConfig;

    protected AbstractStyleProvider() {
        this(BaseVoltagesConfig.fromPlatformConfig());
    }

    protected AbstractStyleProvider(BaseVoltagesConfig baseVoltagesConfig) {
        this.baseVoltagesConfig = Objects.requireNonNull(baseVoltagesConfig);
    }

    @Override
    public String getStyleDefs() {
        StringBuilder styleSheetBuilder = new StringBuilder("\n");
        for (URL cssUrl : getCssUrls()) {
            try {
                styleSheetBuilder.append(new String(IOUtils.toByteArray(cssUrl), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException("Can't read css file " + cssUrl.getPath(), e);
            }
        }
        return styleSheetBuilder.toString()
                .replace("\r\n", "\n") // workaround for https://bugs.openjdk.java.net/browse/JDK-8133452
                .replace("\r", "\n");
    }

    protected List<URL> getCssUrls() {
        return getCssFilenames().stream()
                .map(n -> getClass().getResource("/" + n))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getNodeStyleClasses(Node node) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNodeStyleClasses(BusNode busNode) {
        return busNode == BusNode.UNKNOWN ? Collections.singletonList(UNKNOWN_BUSNODE_CLASS) : Collections.emptyList();
    }

    @Override
    public List<String> getEdgeStyleClasses(Edge edge) {
        List<String> result = new ArrayList<>();
        if (isDisconnected(edge)) {
            result.add(DISCONNECTED_CLASS);
        }
        getBaseVoltageStyle(edge).ifPresent(result::add);
        return result;
    }

    @Override
    public List<String> getSideEdgeStyleClasses(BranchEdge edge, BranchEdge.Side side) {
        Objects.requireNonNull(side);
        List<String> result = new ArrayList<>();
        if (isDisconnected(edge, side)) {
            result.add(DISCONNECTED_CLASS);
        }
        getBaseVoltageStyle(edge, side).ifPresent(result::add);
        return result;
    }

    @Override
    public List<String> getEdgeInfoStyles(EdgeInfo info) {
        List<String> styles = new LinkedList<>();
        if (info.getInfoType().equals(EdgeInfo.ACTIVE_POWER)) {
            styles.add(CLASSES_PREFIX + "active");
        } else if (info.getInfoType().equals(EdgeInfo.REACTIVE_POWER)) {
            styles.add(CLASSES_PREFIX + "reactive");
        }
        info.getDirection().ifPresent(direction -> styles.add(
                CLASSES_PREFIX + (direction == EdgeInfo.Direction.IN ? "state-in" : "state-out")));
        return styles;
    }

    @Override
    public List<String> getThreeWtNodeStyle(ThreeWtNode threeWtNode, ThreeWtEdge.Side side) {
        Objects.requireNonNull(side);
        List<String> result = new ArrayList<>();
        if (isDisconnected(threeWtNode, side)) {
            result.add(DISCONNECTED_CLASS);
        }
        getBaseVoltageStyle(threeWtNode, side).ifPresent(result::add);
        return result;
    }

    protected abstract boolean isDisconnected(Edge edge);

    protected abstract boolean isDisconnected(BranchEdge edge, BranchEdge.Side side);

    protected abstract boolean isDisconnected(ThreeWtNode threeWtNode, ThreeWtEdge.Side side);

    protected abstract Optional<String> getBaseVoltageStyle(Edge edge);

    protected abstract Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side);

    protected abstract Optional<String> getBaseVoltageStyle(ThreeWtNode threeWtNode, ThreeWtEdge.Side side);

    protected Optional<String> getBaseVoltageStyle(double nominalV) {
        return baseVoltagesConfig.getBaseVoltageName(nominalV, baseVoltagesConfig.getDefaultProfile())
                    .map(bvName -> CLASSES_PREFIX + bvName);
    }

}
