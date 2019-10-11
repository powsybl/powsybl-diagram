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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Node;
import org.apache.commons.lang3.StringUtils;

import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.CAPACITOR;
import static com.powsybl.sld.library.ComponentTypeName.DISCONNECTOR;
import static com.powsybl.sld.library.ComponentTypeName.GENERATOR;
import static com.powsybl.sld.library.ComponentTypeName.INDUCTOR;
import static com.powsybl.sld.library.ComponentTypeName.LINE;
import static com.powsybl.sld.library.ComponentTypeName.LOAD;
import static com.powsybl.sld.library.ComponentTypeName.LOAD_BREAK_SWITCH;
import static com.powsybl.sld.library.ComponentTypeName.STATIC_VAR_COMPENSATOR;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.VSC_CONVERTER_STATION;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at franck.lecuyer@rte-france.com>
 */
public class DefaultSubstationDiagramInitialValueProvider implements SubstationDiagramInitialValueProvider {

    private final Network network;

    public DefaultSubstationDiagramInitialValueProvider(Network net) {
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
                    initialValue = getBranchInitialValue(node);
                    break;

                case LOAD:
                    initialValue = getLoadInitialValue(network.getLoad(nodeId));
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
            return new InitialValue(injection);
        } else {
            return new InitialValue(null, null, null, null, null, null);
        }
    }

    private InitialValue getLoadInitialValue(Load load) {
        if (load != null) {
            return new InitialValue(load);
        } else {
            return new InitialValue(null, null, null, null, null, null);
        }
    }

    private InitialValue getBranchInitialValue(Node node) {
        String nodeId = node.getId();
        if (node instanceof Feeder2WTNode && node.getComponentType().equals(LINE)) {
            // special case : branch of threeWindingsTransformer
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.ONE;
            ThreeWindingsTransformer transformer = null;

            int posSide = StringUtils.lastOrdinalIndexOf(nodeId, "_", 1);
            if (posSide != -1) {
                side = ThreeWindingsTransformer.Side.valueOf(nodeId.substring(posSide + 1));
                posSide = StringUtils.lastOrdinalIndexOf(nodeId, "_", 2);
                if (posSide != -1) {
                    String idTransformer = nodeId.substring(0, posSide);
                    transformer = network.getThreeWindingsTransformer(idTransformer);
                }
            }

            if (transformer != null) {
                return new InitialValue(transformer, side);
            } else {
                return new InitialValue(null, null, null, null, null, null);
            }
        } else {
            Branch branch = network.getBranch(nodeId.substring(0, nodeId.length() - 4));
            if (branch != null) {
                return new InitialValue(branch, Side.valueOf(nodeId.substring(nodeId.length() - 3)));
            } else {
                return new InitialValue(null, null, null, null, null, null);
            }
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
}
