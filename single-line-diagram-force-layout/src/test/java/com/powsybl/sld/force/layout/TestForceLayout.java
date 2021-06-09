package com.powsybl.sld.force.layout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TestForceLayout {
    private static final boolean ENABLE_SVG = true;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void toJson(String filename, Set<Spring> springs) throws IOException {
        File tmpFile = File.createTempFile(filename, ".json");

        MAPPER.writeValue(tmpFile, springs);
    }

    public String toString(String filename) {
        String filepath = TestForceLayout.class.getResource(filename).getPath();

        try {
            return new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return "";
    }

    public String toString(Set<Spring> springs) {
        try {
            return MAPPER.writeValueAsString(springs);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Before
    public void setUp() {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void test4Points() {
        String neoYokio = "NeoYokio";
        String tokyo = "Tokyo";
        String kyoto = "Kyoto";
        String osaka = "Osaka";

        Graph<String, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        graph.addVertex(neoYokio);
        graph.addVertex(tokyo);
        graph.addVertex(kyoto);
        graph.addVertex(osaka);

        graph.addEdge(neoYokio, tokyo);
        graph.addEdge(tokyo, kyoto);
        graph.addEdge(kyoto, osaka);
        graph.addEdge(osaka, tokyo);

        ForceLayout<String, DefaultEdge> forceLayout = new ForceLayout<>(graph).setInitialisationSeed(3);
        forceLayout.execute();

        if (ENABLE_SVG) {
            try {
                forceLayout.toSVG(s -> s);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        assertEquals(toString("/Graph4Points.json"), toString(forceLayout.getSprings()));
    }

    @Test
    public void test10Points() {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);

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

        ForceLayout<Integer, DefaultEdge> forceLayout = new ForceLayout<>(graph).setInitialisationSeed(3);
        forceLayout.execute();

        if (ENABLE_SVG) {
            try {
                forceLayout.toSVG(i -> Integer.toString(i));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        assertEquals(toString("/Graph10Points.json"), toString(forceLayout.getSprings()));
    }
}
