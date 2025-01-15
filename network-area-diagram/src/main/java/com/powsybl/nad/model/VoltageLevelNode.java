/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class VoltageLevelNode extends AbstractNode {

    private final List<BusNode> busNodes = new ArrayList<>();
    private final boolean visible;
    private boolean hasUnknownBusNode = false;
    private String parentNetworkId;

    public VoltageLevelNode(String parentNetworkId, String diagramId, String equipmentId, String nameOrId, boolean fictitious) {
        this(parentNetworkId, diagramId, equipmentId, nameOrId, fictitious, true);
    }

    public VoltageLevelNode(String parentNetworkId, String diagramId, String equipmentId, String nameOrId, boolean fictitious, boolean visible) {
        super(diagramId, equipmentId, nameOrId, fictitious);
        this.visible = visible;
        this.parentNetworkId = parentNetworkId;
    }

    public String getParentNetworkId() {
        return parentNetworkId;
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
}
