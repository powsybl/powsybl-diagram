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
        Optional<String> overloadStyle = getOverloadStyle(graph, edge);
        return overloadStyle.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private Optional<String> getOverloadStyle(Graph graph, Edge edge) {
        //return edge.isOverloaded() ? Optional.of(StyleClassConstants.OVERLOAD_STYLE_CLASS) : Optional.empty();
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        FeederNode n;
        if (n1 instanceof FeederNode || n2 instanceof FeederNode) {
            n = (FeederNode) (n1 instanceof FeederNode ? n1 : n2);
            return getHighlightFeederStateStyle(graph, n);
        }
        return Optional.empty();
    }

    protected Optional<String> getHighlightFeederStateStyle(Graph graph, FeederNode n) {
        if (n.getFeeder() instanceof FeederWithSides) {
            FeederWithSides feederWs = (FeederWithSides) n.getFeeder();
            Connectable<?> connectable = network.getConnectable(n.getEquipmentId());
            if (connectable instanceof Branch<?> branch) {
                if (branch.isOverloaded()) {
                    return Optional.of(OVERLOAD_STYLE_CLASS);
                }
            } else if (connectable instanceof ThreeWindingsTransformer transformer) {
                if (transformer.isOverloaded()) {
                    return Optional.of(OVERLOAD_STYLE_CLASS);
                }

            }
            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<String> getNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        BusNode busNode = null;
        if (node instanceof BusNode) {
            busNode = (BusNode) node;
        } else {
            return Collections.emptyList();
        }

        List<String> styles = new ArrayList<>();
        BusbarSection busbarSection = this.network.getBusbarSection(busNode.getEquipmentId());
        if (busbarSection != null) {
//            if(!LimitViolationUtil.detectBusViolations(busbarSection)){
//                return styles;
//            }
            if (busbarSection.getV() > busbarSection.getTerminal().getVoltageLevel().getHighVoltageLimit()) {
                styles.add(VL_OVERVOLTAGE_CLASS);
            } else if (busbarSection.getV() < busbarSection.getTerminal().getVoltageLevel().getLowVoltageLimit()) {
                styles.add(VL_UNDERVOLTAGE_CLASS);
            } else {
                System.out.println("Bus => " + busNode.getId());
            }
        }
        return styles;

    }


//    @Override
//    public List<String> getEdgeStyleClasses(Edge edge) {
//       // List<String> styleClasses = new ArrayList<>(super.getEdgeStyleClasses(edge));
////        if (IidmUtils.isIidmBranch(edge)) {
////            Branch<?> branch = network.getBranch(edge.getEquipmentId());
////            if (branch.isOverloaded()) {
////                styleClasses.add(StyleProvider.LINE_OVERLOADED_CLASS);
////            }
////        }
//        return List.of("");
//    }
}
