/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder3WTNode extends FeederNode {

    private ThreeWindingsTransformer transformer;
    private ThreeWindingsTransformer.Side side;

    protected Feeder3WTNode(String id, String name, String componentType,
                            boolean fictitious, Graph graph,
                            ThreeWindingsTransformer transformer,
                            ThreeWindingsTransformer.Side side) {
        super(id, name, componentType, fictitious, graph);
        this.transformer = transformer;
        this.side = side;
    }

    public static Feeder3WTNode create(Graph graph, ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(twt);
        Objects.requireNonNull(side);
        String id = twt.getId() + "_" + side.name();
        String name = twt.getName() + "_" + side.name();
        return new Feeder3WTNode(id, name, THREE_WINDINGS_TRANSFORMER,
                                            false, graph, twt, side);
    }

    public String getId2() {
        String ret = null;
        switch (side) {
            case ONE: ret = ThreeWindingsTransformer.Side.TWO.name(); break;
            case TWO: ret = ThreeWindingsTransformer.Side.ONE.name(); break;
            case THREE: ret = ThreeWindingsTransformer.Side.ONE.name(); break;
        }
        return ret;
    }

    public String getName2() {
        return getId2();
    }

    public VoltageLevel getVL2() {
        VoltageLevel ret = null;
        switch (side) {
            case ONE: ret = transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel(); break;
            case TWO: ret = transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel(); break;
            case THREE: ret = transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel(); break;
        }
        return ret;
    }

    public String getId3() {
        String ret = null;
        switch (side) {
            case ONE: ret = ThreeWindingsTransformer.Side.THREE.name(); break;
            case TWO: ret = ThreeWindingsTransformer.Side.THREE.name(); break;
            case THREE: ret = ThreeWindingsTransformer.Side.TWO.name(); break;
        }
        return ret;
    }

    public String getName3() {
        return getId3();
    }

    public VoltageLevel getVL3() {
        VoltageLevel ret = null;
        switch (side) {
            case ONE: ret = transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getVoltageLevel(); break;
            case TWO: ret = transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getVoltageLevel(); break;
            case THREE: ret = transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel(); break;
        }
        return ret;
    }

    public ThreeWindingsTransformer getTransformer() {
        return transformer;
    }
}
