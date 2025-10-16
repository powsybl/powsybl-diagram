/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import com.powsybl.nad.svg.LabelProvider;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class VoltageLevelNode extends AbstractNode {

    private final List<BusNode> busNodes = new ArrayList<>();
    private final boolean visible;
    private boolean hasUnknownBusNode = false;
    private final String legendDiagramId;
    private final List<String> legendHeader;
    private final List<String> legendFooter;

    public VoltageLevelNode(String diagramId, String equipmentId, String nameOrId, boolean fictitious) {
        super(diagramId, equipmentId, nameOrId, fictitious);
        this.visible = true;
        this.legendDiagramId = null;
        this.legendHeader = List.of();
        this.legendFooter = List.of();
    }

    public VoltageLevelNode(String diagramId, String equipmentId, String nameOrId, boolean fictitious, boolean visible, String vlLegendId, LabelProvider labelProvider) {
        super(diagramId, equipmentId, nameOrId, fictitious);
        this.visible = visible;
        this.legendDiagramId = vlLegendId;
        this.legendHeader = labelProvider.getLegendHeader(equipmentId);
        this.legendFooter = labelProvider.getLegendFooter(equipmentId);
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

    public String getLegendDiagramId() {
        return legendDiagramId;
    }

    public List<String> getLegendHeader() {
        return legendHeader;
    }

    public List<String> getLegendFooter() {
        return legendFooter;
    }
}
