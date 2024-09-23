package com.powsybl.sld.svg.styles.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.library.ComponentLibrary;
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
    Network network;

    public LimitHighlightStyleProvider(Network network) {
        this.network = network;
    }

    @Override
    public List<String> getEdgeStyles(Graph graph, Edge edge) {
        Optional<String> overloadStyle = getOverloadStyle(edge);
        return overloadStyle.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private Optional<String> getOverloadStyle(Edge edge) {
        List<Node> nodes = edge.getNodes();
        for (Node node : nodes) {
            if (node instanceof FeederNode feederNode) {
                return getHighlightFeederStateStyle(feederNode);
            } else if (node instanceof ConnectivityNode connectivityNode && hasOverloadedAdjacentNode(connectivityNode)) {
                return Optional.of(OVERLOAD_STYLE_CLASS);
            }
        }
        return Optional.empty();
    }

    boolean hasOverloadedAdjacentNode(ConnectivityNode connectivityNode) {
        return connectivityNode.getAdjacentNodes().stream().anyMatch(node -> {
            if (node instanceof FeederNode feederNode) {
                return isOverloaded(feederNode);
            }
            return false;
        });
    }

    protected Optional<String> getHighlightFeederStateStyle(FeederNode n) {
        if (isOverloaded(n)) {
            return Optional.of(OVERLOAD_STYLE_CLASS);
        }
        return Optional.empty();
    }

    private boolean isOverloaded(FeederNode n) {
        if (!(n.getFeeder() instanceof FeederWithSides)) {
            return false;
        }
        Connectable<?> connectable = network.getConnectable(n.getEquipmentId());
        if (connectable instanceof Branch<?> branch && branch.isOverloaded()) {
            return true;
        } else {
            return connectable instanceof ThreeWindingsTransformer transformer && transformer.isOverloaded();
        }
    }

    @Override
    public List<String> getNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        if (!(node instanceof BusNode busNode)) {
            return Collections.emptyList();
        }
        List<String> styles = new ArrayList<>();
        BusbarSection busbarSection = this.network.getBusbarSection(busNode.getEquipmentId());
        if (busbarSection != null) {

            if (busbarSection.getV() > busbarSection.getTerminal().getVoltageLevel().getHighVoltageLimit()) {
                styles.add(VL_OVERVOLTAGE_CLASS);
            } else if (busbarSection.getV() < busbarSection.getTerminal().getVoltageLevel().getLowVoltageLimit()) {
                styles.add(VL_UNDERVOLTAGE_CLASS);
            }
        }
        return styles;

    }

}
