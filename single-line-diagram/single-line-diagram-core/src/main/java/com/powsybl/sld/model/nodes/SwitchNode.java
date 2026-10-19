/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.sld.model.coordinate.Direction.UNDEFINED;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer@rte-france.com>}
 */
public class SwitchNode extends EquipmentNode {

    public enum SwitchKind {
        BREAKER,
        DISCONNECTOR,
        LOAD_BREAK_SWITCH,
        GROUND_DISCONNECTOR
    }

    private boolean open = false;

    private final SwitchKind kind;

    private Integer iidmNode1;

    private Integer iidmNode2;

    public SwitchNode(String id, String name, String componentType, boolean fictitious, SwitchKind kind, boolean open) {
        super(NodeType.SWITCH, id, name, id, componentType, fictitious);
        this.kind = Objects.requireNonNull(kind);
        setOpen(open);
    }

    public SwitchKind getKind() {
        return kind;
    }

    public Node getOtherAdjNode(Node adj) {
        // a switch node has 2 and only 2 adjacent nodes.
        if (getAdjacentNodes().size() != 2) {
            throw new PowsyblException("Error switch node not having exactly 2 adjacent nodes " + getId());
        }
        return getAdjacentNodes().get(getAdjacentNodes().getFirst().equals(adj) ? 1 : 0);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * The first of the two nodes of the node/breaker view this switch connects.
     */
    public Optional<Integer> getIidmNode1() {
        return Optional.ofNullable(iidmNode1);
    }

    public void setIidmNode1(int iidmNode1) {
        this.iidmNode1 = iidmNode1;
    }

    /**
     * The second of the two nodes of the node/breaker view this switch connects.
     */
    public Optional<Integer> getIidmNode2() {
        return Optional.ofNullable(iidmNode2);
    }

    public void setIidmNode2(int iidmNode2) {
        this.iidmNode2 = iidmNode2;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        generator.writeBooleanField("open", open);
        generator.writeStringField("kind", kind.name());
        Optional<Integer> optOrder = getOrder();
        if (optOrder.isPresent()) {
            generator.writeNumberField("order", optOrder.get());
        }
        if (getDirection() != null && getDirection() != UNDEFINED) {
            generator.writeStringField("direction", getDirection().name());
        }
    }
}
