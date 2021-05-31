package com.powsybl.sld.force.layout;

import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestForceLayout {
    private final Random random = new Random();

    @Test
    public void test4Points() {
        // Create a graph
        Point neoYokio = new Point("NeoYokio", random.nextDouble(), random.nextDouble());
        Point tokyo = new Point("Tokyo", random.nextDouble(), random.nextDouble());
        Point kyoto = new Point("Kyoto", random.nextDouble(), random.nextDouble());
        Point osaka = new Point("Osaka", random.nextDouble(), random.nextDouble());

        Graph<Point, Spring> graph = new Pseudograph<>(Spring.class);
        graph.addVertex(neoYokio);
        graph.addVertex(tokyo);
        graph.addVertex(kyoto);
        graph.addVertex(osaka);

        graph.addEdge(neoYokio, tokyo);
        graph.addEdge(tokyo, kyoto);
        graph.addEdge(kyoto, osaka);
        graph.addEdge(osaka, tokyo);

        ForceLayout forceLayout = new ForceLayout();
        forceLayout.execute(graph);

        try {
            forceLayout.renderToSVG(graph, new Canvas(600, 600));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    public void test10Points() {
        Graph<Point, Spring> graph = new Pseudograph<>(Spring.class);
        List<Point> points = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Point point = new Point("p" + i, random.nextDouble(), random.nextDouble());
            points.add(point);
            graph.addVertex(point);
        }

        graph.addEdge(points.get(0), points.get(1));
        graph.addEdge(points.get(0), points.get(3));
        graph.addEdge(points.get(3), points.get(4));
        graph.addEdge(points.get(3), points.get(6));
        graph.addEdge(points.get(7), points.get(9));
        graph.addEdge(points.get(5), points.get(9));
        graph.addEdge(points.get(8), points.get(3));
        graph.addEdge(points.get(1), points.get(9));
        graph.addEdge(points.get(1), points.get(2));
        graph.addEdge(points.get(4), points.get(2));
        graph.addEdge(points.get(7), points.get(2));
        // graph.addEdge(points.get(2), points.get(2)); // does not seem to end if we set self-loop node

        ForceLayout forceLayout = new ForceLayout();
        forceLayout.execute(graph);

        try {
            forceLayout.renderToSVG(graph, new Canvas(600, 600));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
