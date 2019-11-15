/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.Feeder3WTNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Node;
import org.apache.commons.lang3.StringUtils;

import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.CAPACITOR;
import static com.powsybl.sld.library.ComponentTypeName.DANGLING_LINE;
import static com.powsybl.sld.library.ComponentTypeName.DISCONNECTOR;
import static com.powsybl.sld.library.ComponentTypeName.GENERATOR;
import static com.powsybl.sld.library.ComponentTypeName.INDUCTOR;
import static com.powsybl.sld.library.ComponentTypeName.LINE;
import static com.powsybl.sld.library.ComponentTypeName.LOAD;
import static com.powsybl.sld.library.ComponentTypeName.LOAD_BREAK_SWITCH;
import static com.powsybl.sld.library.ComponentTypeName.STATIC_VAR_COMPENSATOR;
import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.VSC_CONVERTER_STATION;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at franck.lecuyer@rte-france.com>
 */
public class DefaultDiagramInitialValueProvider implements DiagramInitialValueProvider {

    private final Network network;

    public DefaultDiagramInitialValueProvider(Network net) {
        network = Objects.requireNonNull(net);
    }

    @Override
    public InitialValue getInitialValue(Node node) {
        Objects.requireNonNull(node);
        InitialValue initialValue = new InitialValue(null, null, null, null, null, null);

        if (node.getType() == Node.NodeType.BUS) {
            initialValue = new InitialValue(null, null, node.getLabel(), null, null, null);
        } else {
            String nodeId = node.getId();
            switch (node.getComponentType()) {
                case LINE:
                case TWO_WINDINGS_TRANSFORMER:
                case THREE_WINDINGS_TRANSFORMER:
                    initialValue = getBranchInitialValue(node);
                    break;

                case LOAD:
                    initialValue = getInjectionInitialValue(network.getLoad(nodeId));
                    break;

                case INDUCTOR:
                case CAPACITOR:
                    initialValue = getInjectionInitialValue(network.getShuntCompensator(nodeId));
                    break;

                case GENERATOR:
                    initialValue = getInjectionInitialValue(network.getGenerator(nodeId));
                    break;

                case STATIC_VAR_COMPENSATOR:
                    initialValue = getInjectionInitialValue(network.getStaticVarCompensator(nodeId));
                    break;

                case VSC_CONVERTER_STATION:
                    initialValue = getInjectionInitialValue(network.getVscConverterStation(nodeId));
                    break;

                case DANGLING_LINE:
                    initialValue = getInjectionInitialValue(network.getDanglingLine(nodeId));
                    break;

                case BUSBAR_SECTION:
                case BREAKER:
                case LOAD_BREAK_SWITCH:
                case DISCONNECTOR:
                default:
                    break;
            }
        }
        return initialValue;
    }

    private InitialValue getInjectionInitialValue(Injection<?> injection) {
        if (injection != null) {
            return buildInitialValue(injection);
        } else {
            return new InitialValue(null, null, null, null, null, null);
        }
    }

    private boolean isNodeTransformer(Node node) {
        return (node instanceof Feeder2WTNode
                && node.getComponentType().equals(LINE)
                && node.getGraph().isForVoltageLevelDiagram())
                || (node instanceof Feeder3WTNode && !node.getGraph().isForVoltageLevelDiagram());
    }

    private InitialValue getTransformerInitialValue(Node node) {
        String nodeId = node.getId();

        // special cases : branch of threeWindingsTransformer in voltageLevel graph
        //               : branch of threeWindingsTransformer in substation graph
        ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.ONE;
        ThreeWindingsTransformer transformer = null;

        String idTransformer = "";
        int posSide = StringUtils.lastOrdinalIndexOf(nodeId, "_", 1);
        if (posSide != -1) {
            side = ThreeWindingsTransformer.Side.valueOf(nodeId.substring(posSide + 1));
            if (node.getGraph().isForVoltageLevelDiagram()) {
                posSide = StringUtils.lastOrdinalIndexOf(nodeId, "_", 2);
                if (posSide != -1) {
                    idTransformer = nodeId.substring(0, posSide);
                }
            } else {
                idTransformer = nodeId.substring(0, posSide);
            }
            transformer = network.getThreeWindingsTransformer(idTransformer);
        }

        if (transformer != null) {
            return buildInitialValue(transformer, side);
        } else {
            return new InitialValue(null, null, null, null, null, null);
        }
    }

    private InitialValue getLineInitialValue(Node node) {
        String nodeId = node.getId();

        // Note : the nodeId is built with branch id, "_", and the side (ONE or TWO)
        Branch branch = network.getBranch(nodeId.substring(0, nodeId.length() - 4));
        if (branch != null) {
            return buildInitialValue(branch, Side.valueOf(nodeId.substring(nodeId.length() - 3)));
        } else {
            return new InitialValue(null, null, null, null, null, null);
        }
    }

    private InitialValue getBranchInitialValue(Node node) {
        if (isNodeTransformer(node)) {
            return getTransformerInitialValue(node);
        } else {
            return getLineInitialValue(node);
        }
    }

    @Override
    public List<String> getNodeLabelValue(Node node) {
        Objects.requireNonNull(node);

        List<String> res = new ArrayList<>();
        if (node instanceof FeederNode || node instanceof BusNode) {
            res.add(node.getLabel());
        }
        return res;
    }

    private InitialValue buildInitialValue(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        double p = transformer.getTerminal(side).getP();
        double q = transformer.getTerminal(side).getQ();
        String label1 = String.valueOf(Math.round(p));
        String label2 = String.valueOf(Math.round(q));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;

        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }

    private InitialValue buildInitialValue(Injection<?> injection) {
        Objects.requireNonNull(injection);
        double p = injection.getTerminal().getP();
        double q = injection.getTerminal().getQ();
        String label1 = String.valueOf(Math.round(p));
        String label2 = String.valueOf(Math.round(q));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;

        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }

    private InitialValue buildInitialValue(Branch<?> ln, Side side) {
        Objects.requireNonNull(ln);
        Objects.requireNonNull(side);
        double p = side.equals(Side.ONE) ? ln.getTerminal1().getP() : ln.getTerminal2().getP();
        double q = side.equals(Side.ONE) ? ln.getTerminal1().getQ() : ln.getTerminal2().getQ();
        String label1 = String.valueOf(Math.round(p));
        String label2 = String.valueOf(Math.round(q));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;

        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }
}
