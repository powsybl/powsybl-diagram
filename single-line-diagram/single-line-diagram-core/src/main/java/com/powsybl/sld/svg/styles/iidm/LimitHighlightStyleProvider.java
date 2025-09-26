/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg.styles.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import com.powsybl.sld.svg.styles.EmptyStyleProvider;
import java.util.*;

import static com.powsybl.sld.svg.styles.StyleClassConstants.*;

/**
 * @author Jamal KHEYYAD {@literal <jamal.kheyyad at rte-international.com>}
 */
public class LimitHighlightStyleProvider extends EmptyStyleProvider {
    private final Network network;
    private Map<String, String> limitViolationStyles = Map.of();

    public LimitHighlightStyleProvider(Network network) {
        this(network, Collections.emptyMap());
    }

    public LimitHighlightStyleProvider(Network network, Map<String, String> limitViolationStyles) {
        this.network = network;
        this.limitViolationStyles = limitViolationStyles;
    }

    @Override
    public List<String> getEdgeStyles(Graph graph, Edge edge) {
        // Check custom violations first
        if (!limitViolationStyles.isEmpty()) {
            Optional<String> customStyle = getCustomViolationStyle(edge);
            if (customStyle.isPresent()) {
                return List.of(customStyle.get());
            }
        }
        // Fallback to default overload detection
        Optional<String> overloadStyle = getOverloadStyle(edge);
        return overloadStyle.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private Optional<String> getCustomViolationStyle(Edge edge) {
        for (Node node : edge.getNodes()) {
            if (node instanceof FeederNode feederNode) {
                String style = limitViolationStyles.get(feederNode.getEquipmentId());
                if (style != null && !style.isBlank()) {
                    return Optional.of(style);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> getOverloadStyle(Edge edge) {
        List<Node> nodes = edge.getNodes();
        for (Node node : nodes) {
            if (node instanceof FeederNode feederNode) {
                return isOverloaded(feederNode) ? Optional.of(OVERLOAD_STYLE_CLASS) : Optional.empty();
            }
        }
        return Optional.empty();
    }

    private boolean isOverloaded(FeederNode n) {
        if (!(n.getFeeder() instanceof FeederWithSides)) {
            return false;
        }
        Branch<?> branch = network.getBranch(n.getEquipmentId());
        if (branch != null) {
            return branch.isOverloaded();
        } else {
            ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(n.getEquipmentId());
            return transformer != null && transformer.isOverloaded();
        }
    }

    @Override
    public List<String> getNodeStyles(VoltageLevelGraph graph, Node node, SldComponentLibrary componentLibrary, boolean showInternalNodes) {
        if (!(node instanceof BusNode busNode)) {
            return Collections.emptyList();
        }
        BusbarSection busbarSection = this.network.getBusbarSection(busNode.getEquipmentId());
        if (busbarSection != null) {
            if (busbarSection.getV() > busbarSection.getTerminal().getVoltageLevel().getHighVoltageLimit()) {
                return List.of(VL_OVERVOLTAGE_CLASS);
            } else if (busbarSection.getV() < busbarSection.getTerminal().getVoltageLevel().getLowVoltageLimit()) {
                return List.of(VL_UNDERVOLTAGE_CLASS);
            }
        }
        return List.of();
    }

}
