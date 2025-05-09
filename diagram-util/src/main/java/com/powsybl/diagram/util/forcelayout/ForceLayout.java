/**
 * Java transcription of Springy v2.8.0
 *
 * Copyright (c) 2010-2018 Dennis Hotson
 * Copyright (c) 2021-2025 RTE (https://www.rte-france.com)
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.layouts.layoutsparameters.SpringyParameters;
import com.powsybl.diagram.util.forcelayout.setup.SetupEnum;
import org.jgrapht.Graph;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 * The following algorithm is a force layout algorithm.
 * It seeks to place the nodes of a graph in such a way that the nodes are well spaced and that there are no unnecessary crossings.
 * The algorithm uses an analogy with physics where the nodes of the graph are particles with mass and the edges are springs.
 * Force calculations are used to place the nodes.
 *
 * The algorithm is taken from: https://github.com/dhotson/springy
 *
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 */
public class ForceLayout<V, E> {
    private final Set<Spring> springs = new LinkedHashSet<>();

    private final ForceGraph<V, E> forceGraph;
    private final SpringyParameters<V, E> springyParameters = new SpringyParameters<>();
    private final LayoutAlgorithmRunner<V, E> algorithmRunner;

    public ForceLayout(Graph<V, E> graph) {
        this.forceGraph = new ForceGraph<>(Objects.requireNonNull(graph));
        this.algorithmRunner = new LayoutAlgorithmRunner<>(
                SetupEnum.SPRINGY,
                this.springyParameters
        );
    }

    public ForceLayout<V, E> setAttractToCenterForce(boolean attractToCenterForce) {
        this.springyParameters.setAttractToCenterForce(attractToCenterForce);
        return this;
    }

    public ForceLayout<V, E> setRepulsionForceFromFixedPoints(boolean repulsionForceFromFixedPoints) {
        this.springyParameters.setRepulsionForceFromFixedPoints(repulsionForceFromFixedPoints);
        return this;
    }

    public ForceLayout<V, E> setMaxSteps(int maxSteps) {
        this.springyParameters.setMaxSteps(maxSteps);
        return this;
    }

    public ForceLayout<V, E> setMinEnergyThreshold(double minEnergyThreshold) {
        this.springyParameters.setMinEnergyThreshold(minEnergyThreshold);
        return this;
    }

    public ForceLayout<V, E> setDeltaTime(double deltaTime) {
        this.springyParameters.setDeltaTime(deltaTime);
        return this;
    }

    public ForceLayout<V, E> setRepulsion(double repulsion) {
        this.springyParameters.setRepulsion(repulsion);
        return this;
    }

    public ForceLayout<V, E> setFriction(double friction) {
        this.springyParameters.setFriction(friction);
        return this;
    }

    public ForceLayout<V, E> setMaxSpeed(double maxSpeed) {
        this.springyParameters.setMaxSpeed(maxSpeed);
        return this;
    }

    public ForceLayout<V, E> setInitialPoints(Map<V, Point> initialPoints) {
        this.forceGraph.setInitialPoints(initialPoints);
        return this;
    }

    public ForceLayout<V, E> setFixedPoints(Map<V, Point> fixedPoints) {
        this.forceGraph.setFixedPoints(fixedPoints);
        return this;
    }

    public ForceLayout<V, E> setFixedNodes(Set<V> fixedNodes) {
        this.forceGraph.setFixedNodes(fixedNodes);
        return this;
    }

    public void execute() {
        algorithmRunner.run(forceGraph);
    }

    public Vector2D getStablePosition(V vertex) {
        return algorithmRunner.getStablePosition(vertex);
    }

    public Set<Spring> getSprings() {
        return springs;
    }

    public void toSVG(Function<V, String> tooltip, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            toSVG(tooltip, writer);
        }
    }

    public void toSVG(Function<V, String> tooltip, Writer writer) {
        algorithmRunner.toSVG(tooltip, writer);
    }

    public void setCenter(Vector2D center) {
        algorithmRunner.setCenter(center);
    }

    public Vector2D getCenter() {
        return algorithmRunner.getCenter();
    }
}
