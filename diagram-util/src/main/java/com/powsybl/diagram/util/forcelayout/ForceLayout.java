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

import com.powsybl.diagram.util.layout.Layout;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import com.powsybl.diagram.util.layout.algorithms.BasicForceLayoutAlgorithm;
import com.powsybl.diagram.util.layout.algorithms.parameters.BasicForceLayoutParameters;
import com.powsybl.diagram.util.layout.setup.SquareRandomBarycenterSetup;
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
 * The algorithm is inspired from: https://github.com/dhotson/springy
 * @deprecated Use {@link Layout} instead <br>
 * The equivalent of: <br>
 * {@code new ForceLayout(graph).execute();}<br>
 * would be <br>
 * {@code Layout.createBasicForceLayout().run(new LayoutContext<>(graph));}
 *
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
@Deprecated(since = "5.0.0", forRemoval = true)
public class ForceLayout<V, E> {

    private final LayoutContext<V, E> layoutContext;
    private final BasicForceLayoutParameters.Builder basicParametersBuilder = new BasicForceLayoutParameters.Builder();
    @java.lang.SuppressWarnings("java:S2245")
    private final Random random = new Random();

    public ForceLayout(Graph<V, E> graph) {
        this.layoutContext = new LayoutContext<>(Objects.requireNonNull(graph));
        this.random.setSeed(3L);
    }

    public ForceLayout(LayoutContext<V, E> layoutContext) {
        this.layoutContext = Objects.requireNonNull(layoutContext);
        this.random.setSeed(3L);
    }

    public ForceLayout<V, E> setAttractToCenterForce(boolean attractToCenterForce) {
        this.basicParametersBuilder.withAttractToCenterForce(attractToCenterForce);
        return this;
    }

    public ForceLayout<V, E> setRepulsionForceFromFixedPoints(boolean repulsionForceFromFixedPoints) {
        this.basicParametersBuilder.withRepulsionForceFromFixedPoints(repulsionForceFromFixedPoints);
        return this;
    }

    public ForceLayout<V, E> setMaxSteps(int maxSteps) {
        this.basicParametersBuilder.withMaxSteps(maxSteps);
        return this;
    }

    public ForceLayout<V, E> setMinEnergyThreshold(double minEnergyThreshold) {
        this.basicParametersBuilder.withMinEnergyThreshold(minEnergyThreshold);
        return this;
    }

    public ForceLayout<V, E> setDeltaTime(double deltaTime) {
        this.basicParametersBuilder.withDeltaTime(deltaTime);
        return this;
    }

    public ForceLayout<V, E> setRepulsion(double repulsion) {
        this.basicParametersBuilder.withRepulsion(repulsion);
        return this;
    }

    public ForceLayout<V, E> setFriction(double friction) {
        this.basicParametersBuilder.withFriction(friction);
        return this;
    }

    public ForceLayout<V, E> setMaxSpeed(double maxSpeed) {
        this.basicParametersBuilder.withMaxSpeed(maxSpeed);
        return this;
    }

    public ForceLayout<V, E> setInitialPoints(Map<V, Point> initialPoints) {
        this.layoutContext.setInitialPoints(initialPoints);
        return this;
    }

    public ForceLayout<V, E> setFixedPoints(Map<V, Point> fixedPoints) {
        this.layoutContext.setFixedPoints(fixedPoints);
        return this;
    }

    public ForceLayout<V, E> setFixedNodes(Set<V> fixedNodes) {
        this.layoutContext.setFixedNodes(fixedNodes);
        return this;
    }

    public void execute() {
        Layout<V, E> algorithmRunner = new Layout<>(
                new SquareRandomBarycenterSetup<>(random),
                new BasicForceLayoutAlgorithm<>(basicParametersBuilder.build())
        );
        algorithmRunner.run(layoutContext);
    }

    public Vector2D getStablePosition(V vertex) {
        return layoutContext.getStablePosition(vertex);
    }

    /**
     * @deprecated
     * This method now returns an empty Set until its removal
     */
    @Deprecated(since = "4.9.0", forRemoval = true)
    public Set<Spring> getSprings() {
        return Collections.emptySet();
    }

    public void toSVG(Function<V, String> tooltip, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            toSVG(tooltip, writer);
        }
    }

    public void toSVG(Function<V, String> tooltip, Writer writer) {
        if (layoutContext != null) {
            layoutContext.toSVG(tooltip, writer);
        }
    }

    public void setCenter(Vector2D center) {
        layoutContext.setCenter(center);
    }

    public Vector2D getCenter() {
        return layoutContext.getCenter();
    }
}
