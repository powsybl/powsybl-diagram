package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Middle3WTNode;
import com.powsybl.sld.model.nodes.SwitchNode;

public class LayoutGraphAdapter {
    private final boolean removeUnnecessaryFictitiousNodes;
    private final boolean substituteSingularFictitiousByFeederNode;

    public LayoutGraphAdapter(boolean removeUnnecessaryFictitiousNodes, boolean substituteSingularFictitiousByFeederNode) {
        this.removeUnnecessaryFictitiousNodes = removeUnnecessaryFictitiousNodes;
        this.substituteSingularFictitiousByFeederNode = substituteSingularFictitiousByFeederNode;
    }

    void run(VoltageLevelGraph graph) {
        graph.substituteFictitiousNodesMirroringBusNodes();
        if (removeUnnecessaryFictitiousNodes) {
            graph.removeUnnecessaryFictitiousNodes();
        }
        if (substituteSingularFictitiousByFeederNode) {
            graph.substituteSingularFictitiousByFeederNode();
        }
        graph.insertFictitiousNodesAtFeeders();
        graph.extendNodeConnectedToBus(node -> node instanceof SwitchNode && ((SwitchNode) node).getKind() != SwitchNode.SwitchKind.DISCONNECTOR);
        graph.extendNodeConnectedToBus(Middle3WTNode.class::isInstance);
        graph.extendSwitchBetweenBuses();
        graph.extendFirstOutsideNode();
        graph.extendBusConnectedToBus();
    }
}
