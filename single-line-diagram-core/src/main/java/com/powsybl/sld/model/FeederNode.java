/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.CAPACITOR;
import static com.powsybl.sld.library.ComponentTypeName.DANGLING_LINE;
import static com.powsybl.sld.library.ComponentTypeName.GENERATOR;
import static com.powsybl.sld.library.ComponentTypeName.INDUCTOR;
import static com.powsybl.sld.library.ComponentTypeName.LINE;
import static com.powsybl.sld.library.ComponentTypeName.LOAD;
import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static com.powsybl.sld.library.ComponentTypeName.PHASE_SHIFT_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.STATIC_VAR_COMPENSATOR;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.VSC_CONVERTER_STATION;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederNode extends Node {

    private int order = -1;

    private BusCell.Direction direction = BusCell.Direction.UNDEFINED;

    protected FeederNode(String id, String name, String componentType, boolean fictitious, Graph graph) {
        super(NodeType.FEEDER, id, name, componentType, fictitious, graph);
    }

    public static FeederNode create(Graph graph, Injection injection) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(injection);
        String componentType;
        switch (injection.getType()) {
            case GENERATOR:
                componentType = GENERATOR;
                break;
            case LOAD:
                componentType = LOAD;
                break;
            case HVDC_CONVERTER_STATION:
                componentType = VSC_CONVERTER_STATION;
                break;
            case STATIC_VAR_COMPENSATOR:
                componentType = STATIC_VAR_COMPENSATOR;
                break;
            case SHUNT_COMPENSATOR:
                componentType = ((ShuntCompensator) injection).getbPerSection() >= 0 ? CAPACITOR : INDUCTOR;
                break;
            case DANGLING_LINE:
                componentType = DANGLING_LINE;
                break;
            default:
                throw new AssertionError();
        }
        return new FeederNode(injection.getId(), injection.getName(), componentType, false, graph);
    }

    public static FeederNode create(Graph graph, Branch branch, Branch.Side side) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(branch);
        String componentType;
        switch (branch.getType()) {
            case LINE:
                componentType = LINE;
                break;
            case TWO_WINDINGS_TRANSFORMER:
                if (((TwoWindingsTransformer) branch).getPhaseTapChanger() == null) {
                    componentType = TWO_WINDINGS_TRANSFORMER;
                } else {
                    componentType = PHASE_SHIFT_TRANSFORMER;
                }
                break;
            default:
                throw new AssertionError();
        }
        String id = branch.getId() + "_" + side.name();
        String name = branch.getName() + "_" + side.name();
        return new FeederNode(id, name, componentType, false, graph);
    }

    public static FeederNode createFictitious(Graph graph, String id) {
        return new FeederNode(id, id, NODE, true, graph);
    }

    public static FeederNode create(Graph graph, String id, String name, String componentType) {
        return new FeederNode(id, name, componentType, false, graph);
    }

    @Override
    public void setCell(Cell cell) {
        if (!(cell instanceof ExternCell)) {
            throw new PowsyblException("The Cell of a feeder node shall be an ExternCell");
        }
        super.setCell(cell);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public BusCell.Direction getDirection() {
        return direction;
    }

    public void setDirection(BusCell.Direction direction) {
        this.direction = direction;
    }
}
