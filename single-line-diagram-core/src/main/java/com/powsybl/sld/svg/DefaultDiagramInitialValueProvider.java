/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.sld.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        switch (node.getType()) {
            case BUS:
                return new InitialValue(null, null, node.getLabel(), null, null, null);
            case FEEDER:
                switch (((FeederNode) node).getFeederType()) {
                    case INJECTION:
                        return getInjectionInitialValue((FeederInjectionNode) node);
                    case BRANCH:
                        return getBranchInitialValue((FeederBranchNode) node);
                    case THREE_WINDINGS_TRANSFORMER:
                        return get3wtInitialValue((Feeder3WTNode) node);
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return new InitialValue(null, null, null, null, null, null);
    }

    private InitialValue getInjectionInitialValue(FeederInjectionNode node) {
        Injection injection = (Injection) network.getIdentifiable(node.getId());
        if (injection != null) {
            return buildInitialValue(injection);
        } else {
            return new InitialValue(null, null, null, null, null, null);
        }
    }

    private InitialValue get3wtInitialValue(Feeder3WTNode node) {
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.valueOf(node.getSide().name());
            return buildInitialValue(transformer, side);
        } else {
            return new InitialValue(null, null, null, null, null, null);
        }
    }

    private InitialValue getBranchInitialValue(FeederBranchNode node) {
        Branch branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            return buildInitialValue(branch, side);
        } else {
            return new InitialValue(null, null, null, null, null, null);
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

    private InitialValue buildInitialValue(Injection injection) {
        Objects.requireNonNull(injection);
        double p = injection.getTerminal().getP();
        double q = injection.getTerminal().getQ();
        String label1 = String.valueOf(Math.round(p));
        String label2 = String.valueOf(Math.round(q));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;

        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }

    private InitialValue buildInitialValue(Branch branch, Branch.Side side) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);
        double p = branch.getTerminal(side).getP();
        double q = branch.getTerminal(side).getQ();
        String label1 = String.valueOf(Math.round(p));
        String label2 = String.valueOf(Math.round(q));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;

        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }
}
