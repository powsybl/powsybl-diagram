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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractStyleProvider implements StyleProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractStyleProvider.class);

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
        return node instanceof BoundaryNode ? Collections.singletonList(BOUNDARY_NODE_CLASS) : Collections.emptyList();
    }

    @Override
    public List<String> getBusNodeStyleClasses(BusNode busNode) {
        return busNode == BusNode.UNKNOWN ? Collections.singletonList(UNKNOWN_BUSNODE_CLASS) : Collections.emptyList();
    }

    @Override
    public List<String> getBranchEdgeStyleClasses(BranchEdge branchEdge) {
        List<String> result = new ArrayList<>();
        if (isDisconnected(branchEdge)) {
            result.add(DISCONNECTED_CLASS);
        }
        getBranchTypeStyle(branchEdge).ifPresent(result::add);
        getBaseVoltageStyle(branchEdge).ifPresent(result::add);
        return result;
    }

    private static Optional<String> getBranchTypeStyle(Edge edge) {
        String edgeType = edge.getType();
        return switch (edgeType) {
            case BranchEdge.HVDC_LINE_EDGE -> Optional.of(HVDC_EDGE_CLASS);
            case BranchEdge.DANGLING_LINE_EDGE -> Optional.of(DANGLING_LINE_EDGE_CLASS);
            case BranchEdge.TIE_LINE_EDGE -> Optional.of(TIE_LINE_EDGE_CLASS);
            default -> Optional.empty();
        };
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
    public List<String> getEdgeInfoStyleClasses(EdgeInfo info) {
        List<String> styles = new LinkedList<>();
        String infoType = info.getInfoType();
        switch (infoType) {
            case EdgeInfo.ACTIVE_POWER:
                styles.add(CLASSES_PREFIX + "active");
                break;
            case EdgeInfo.REACTIVE_POWER:
                styles.add(CLASSES_PREFIX + "reactive");
                break;
            case EdgeInfo.CURRENT:
                styles.add(CLASSES_PREFIX + "current");
                break;
            default:
                LOGGER.warn("The \"{}\" type of information is not handled", infoType);
                break;
        }

        info.getDirection().ifPresent(direction -> styles.add(
                CLASSES_PREFIX + (direction == EdgeInfo.Direction.IN ? "state-in" : "state-out")));
        return styles;
    }

    @Override
    public List<String> getThreeWtEdgeStyleClasses(ThreeWtEdge threeWtEdge) {
        List<String> result = new ArrayList<>();
        if (isDisconnected(threeWtEdge)) {
            result.add(DISCONNECTED_CLASS);
        }
        getBaseVoltageStyle(threeWtEdge).ifPresent(result::add);
        return result;
    }

    protected abstract boolean isDisconnected(ThreeWtEdge threeWtEdge);

    protected abstract boolean isDisconnected(BranchEdge branchEdge);

    protected abstract boolean isDisconnected(BranchEdge edge, BranchEdge.Side side);

    protected abstract Optional<String> getBaseVoltageStyle(Edge edge);

    protected abstract Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side);

    protected abstract Optional<String> getBaseVoltageStyle(ThreeWtEdge threeWtEdge);

    protected Optional<String> getBaseVoltageStyle(double nominalV) {
        return baseVoltagesConfig.getBaseVoltageName(nominalV, baseVoltagesConfig.getDefaultProfile())
                    .map(bvName -> CLASSES_PREFIX + bvName);
    }

}
