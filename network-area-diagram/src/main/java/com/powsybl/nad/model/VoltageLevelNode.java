/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import com.powsybl.nad.build.iidm.IdProvider;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class VoltageLevelNode extends AbstractNode {

    private final List<BusNode> busNodes = new ArrayList<>();
    private final boolean visible;
    private boolean hasUnknownBusNode = false;
    private final String legendSvgId;
    private final String legendEdgeSvgId;
    private final List<String> legendHeader;
    private final List<String> legendFooter;

    public VoltageLevelNode(IdProvider idProvider, String equipmentId, String nameOrId, boolean fictitious, boolean visible,
                            List<String> legendHeader, List<String> legendFooter) {
        this(idProvider.createSvgId(equipmentId), equipmentId, nameOrId, fictitious, visible,
                idProvider.createSvgId(equipmentId), idProvider.createSvgId(equipmentId), legendHeader, legendFooter);
    }

    protected VoltageLevelNode(String svgId, String equipmentId, String nameOrId, boolean fictitious, boolean visible,
                               String legendSvgId, String legendEdgeSvgId, List<String> legendHeader, List<String> legendFooter) {
        super(svgId, equipmentId, nameOrId, fictitious);
        this.visible = visible;
        this.legendSvgId = legendSvgId;
        this.legendEdgeSvgId = legendEdgeSvgId;
        this.legendHeader = Objects.requireNonNull(legendHeader);
        this.legendFooter = Objects.requireNonNull(legendFooter);
    }

    public void addBusNode(BusNode busNode) {
        Objects.requireNonNull(busNode);
        busNodes.add(busNode);
    }

    public List<BusNode> getBusNodes() {
        return Collections.unmodifiableList(busNodes);
    }

    public Stream<BusNode> getBusNodeStream() {
        return busNodes.stream();
    }

    public boolean isVisible() {
        return visible;
    }

    public void sortBusNodes(Comparator<? super BusNode> c) {
        busNodes.sort(c);
    }

    public void setHasUnknownBusNode(boolean hasUnknownBusNode) {
        this.hasUnknownBusNode = hasUnknownBusNode;
    }

    public boolean hasUnknownBusNode() {
        return hasUnknownBusNode;
    }

    public String getLegendSvgId() {
        return legendSvgId;
    }

    public String getLegendEdgeSvgId() {
        return legendEdgeSvgId;
    }

    public List<String> getLegendHeader() {
        return legendHeader;
    }

    public List<String> getLegendFooter() {
        return legendFooter;
    }
}
