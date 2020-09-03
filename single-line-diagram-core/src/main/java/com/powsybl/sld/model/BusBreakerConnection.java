package com.powsybl.sld.model;

import static com.powsybl.sld.library.ComponentTypeName.BUSBREAKER_CONNECTION;

public class BusBreakerConnection extends FictitiousNode {
    public BusBreakerConnection(Graph graph, String id) {
        super(graph, id, BUSBREAKER_CONNECTION);
    }
}
