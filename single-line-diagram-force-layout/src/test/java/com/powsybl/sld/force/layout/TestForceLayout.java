package com.powsybl.sld.force.layout;

import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;

import java.io.IOException;

public class TestForceLayout {

    @Test
    public void test4Points() {
        String neoYokio = "NeoYokio";
        String tokyo = "Tokyo";
        String kyoto = "Kyoto";
        String osaka = "Osaka";

        Graph<String, Spring> graph = new Pseudograph<>(Spring.class);
        graph.addVertex(neoYokio);
        graph.addVertex(tokyo);
        graph.addVertex(kyoto);
        graph.addVertex(osaka);

        graph.addEdge(neoYokio, tokyo);
        graph.addEdge(tokyo, kyoto);
        graph.addEdge(kyoto, osaka);
        graph.addEdge(osaka, tokyo);

        ForceLayout forceLayout = new ForceLayout(graph).setInitialisationSeed(0).initializePoints();
        forceLayout.execute();

        try {
            forceLayout.renderToSVG(600, 600);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    public void test10Points() {
        Graph<Integer, Spring> graph = new Pseudograph<>(Spring.class);

        for (int i = 0; i < 10; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(0, 3);
        graph.addEdge(3, 4);
        graph.addEdge(3, 6);
        graph.addEdge(7, 9);
        graph.addEdge(5, 9);
        graph.addEdge(8, 3);
        graph.addEdge(1, 9);
        graph.addEdge(1, 2);
        graph.addEdge(4, 2);
        graph.addEdge(7, 2);
        // graph.addEdge(2, 2); // does not seem to end if we set self-loop node

        ForceLayout forceLayout = new ForceLayout(graph).setInitialisationSeed(0).initializePoints();
        forceLayout.execute();

        try {
            forceLayout.renderToSVG(600, 600);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
