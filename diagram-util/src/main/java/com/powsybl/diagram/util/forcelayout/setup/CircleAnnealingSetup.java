/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/// This is the implementation of the method described in the paper:
/// Simulated Annealing as a Pre-Processing Step for Force-Directed Graph Drawing
/// Farshad Ghassemi Toosi, Nikola S. Nikolov, Malachy Eaton
/// July 2016
/// This also takes elements from:
/// Drawing Graphs Nicely Using Simulated Annealing - RON DAVIDSON and DAVID HAREL
/// Computing the initial temperature of simulated annealing - Walid Ben-Ameur

/// Modifications compared to the paper:
/// Make the size of the starting circle depending on the number of nodes in the graph, to prevent a density that is too high
/// Treat points that are not connected separately by putting those on another circle, larger than the first one

/// Note : this setup will ignore initial positions for points that are not fixed

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class CircleAnnealingSetup<V, E> implements Setup<V, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CircleAnnealingSetup.class);
    // This should always be between 0 and 1, strictly
    private static final double STARTING_TRANSITION_PROBABILITY = 0.8;
    private static final double STARTING_PRECISION = 1e-3;
    private static final double TEMPERATURE_DECREASE_RATIO = 0.8;

    @Override
    public void setup(ForceGraph<V, E> forceGraph, Random random) {
        initForceGraph(forceGraph);
        SetupListData setupTopologyData = getPointsForRun(forceGraph);

        int pointNumberOnFirstCircle = forceGraph.getAllPoints().size() - setupTopologyData.pointWithNoEdge.length;
        if (pointNumberOnFirstCircle <= 0) {
            throw new IllegalStateException("There are more vertex with no edge than there are vertex in the graph, or the graph does not contain any edge");
        } else {
            double firstCircleRadius = Math.max(1, pointNumberOnFirstCircle / 720); // ensure a separation of at least half a degree
            double secondCircleRadius = 2 * firstCircleRadius;
            double angleSeparation = 2 * Math.PI / setupTopologyData.pointWithNoEdge.length;
            // Setup all the points with no edges, we won't move those for the remaining part of the setup
            for (int i = 0; i < setupTopologyData.pointWithNoEdge.length; ++i) {
                Vector2D position = new Vector2D(Math.cos(i * angleSeparation), Math.sin(i * angleSeparation));
                position.multiplyBy(secondCircleRadius);
                setupTopologyData.pointWithNoEdge[i].setPosition(position);
            }

            annealingProcess(setupTopologyData, firstCircleRadius, random);

        }
    }

    private void initForceGraph(ForceGraph<V, E> forceGraph) {
        for (V vertex : forceGraph.getSimpleGraph().vertexSet()) {
            if (forceGraph.getFixedNodes().contains(vertex)) {
                forceGraph.getFixedPoints().put(vertex, forceGraph.getInitialPoints().get(vertex));
            } else {
                forceGraph.getMovingPoints().put(vertex, new Point(0, 0));
            }
        }
        forceGraph.getAllPoints().putAll(forceGraph.getMovingPoints());
        forceGraph.getAllPoints().putAll(forceGraph.getFixedPoints());
    }

    private SetupListData getPointsForRun(ForceGraph<V, E> forceGraph) {
        List<Set<V>> neighborSetPerVertex = new ArrayList<>();
        SimpleGraph<V, DefaultEdge> graph = forceGraph.getSimpleGraph();
        List<V> vertexWithNoEdge = new ArrayList<>();
        List<V> vertexMovable = new ArrayList<>();
        List<VertexPair<V>> vertexWithDistanceTwo = new ArrayList<>();
        @SuppressWarnings("unchecked")
        V[] allVertex = (V[]) graph.vertexSet().toArray();
        for (V vertex : allVertex) {
            neighborSetPerVertex.add(Graphs.neighborSetOf(graph, vertex));
        }
        for (int i = 0; i < neighborSetPerVertex.size(); ++i) {
            Set<V> setOfNeighbors = neighborSetPerVertex.get(i);
            if (setOfNeighbors.isEmpty()) {
                vertexWithNoEdge.add(allVertex[i]);
            } else {
                V thisVertex = allVertex[i];
                if (forceGraph.getMovingPoints().containsKey(thisVertex)) {
                    vertexMovable.add(thisVertex);
                }
                for (int k = i + 1; k < neighborSetPerVertex.size(); ++k) {
                    // if two vertex are not next to each other, and they share a common neighbor, it means they are at a distance of 2
                    if (
                            !setOfNeighbors.contains(allVertex[k])
                                    && !Collections.disjoint(setOfNeighbors, neighborSetPerVertex.get(k))
                    ) {
                        vertexWithDistanceTwo.add(new VertexPair<>(thisVertex, allVertex[k]));
                    }
                }
            }
        }
        // Now convert from vertex to point and return
        List<Point> pointWithNoEdge = new ArrayList<>();
        List<Point> pointMovable = new ArrayList<>();
        List<PointPair> pointsWithDistanceTwo = new ArrayList<>();
        for (V vertex : vertexWithNoEdge) {
            pointWithNoEdge.add(getPoint(forceGraph, vertex));
        }
        for (VertexPair<V> vertexPair : vertexWithDistanceTwo) {
            pointsWithDistanceTwo.add(new PointPair(
                    getPoint(forceGraph, vertexPair.first),
                    getPoint(forceGraph, vertexPair.second)
                )
            );
        }
        for (V vertex : vertexMovable) {
            pointMovable.add(getPoint(forceGraph, vertex));
        }
        Set<DefaultEdge> allEdges = forceGraph.getSimpleGraph().edgeSet();
        List<PointPair> pointsWithDistanceOne = new ArrayList<>();
        for (DefaultEdge edge : allEdges) {
            pointsWithDistanceOne.add(new PointPair(
                            getPoint(forceGraph, forceGraph.getSimpleGraph().getEdgeSource(edge)),
                            getPoint(forceGraph, forceGraph.getSimpleGraph().getEdgeTarget(edge))
                    )
            );
        }

        return new SetupListData(
                pointWithNoEdge.toArray(new Point[0]),
                pointMovable.toArray(new Point[0]),
                pointsWithDistanceTwo.toArray(new PointPair[0]),
                pointsWithDistanceOne.toArray(new PointPair[0])
        );
    }

    private Point getPoint(ForceGraph<V, E> forceGraph, V vertex) {
        return Objects.requireNonNull(
            forceGraph.getAllPoints().get(vertex),
            String.format("No point corresponding to the given vertex %s", vertex.toString())
            );

    }

    private record PointPair(Point first, Point second) { }

    private record VertexPair<V>(V first, V second) { }

    private record SetupListData(
            Point[] pointWithNoEdge, // those points are put on an outer circle and do not move for the duration of the setup
            Point[] movablePoints, // points that are "movable" have at least one edge and are not fixed
            PointPair[] pointsWithDistanceTwo, // used to calculate the metric on the network
            PointPair[] pointsWithDistanceOne // used to calculate the metric on the network
    ) { }

    private double calculateObjectiveFunction(SetupListData setupTopologyData) {
        double sum = 0;
        for (PointPair edgePoints : setupTopologyData.pointsWithDistanceOne) {
            sum += edgePoints.first.distanceTo(edgePoints.second);
        }
        for (PointPair twoDistancePoints : setupTopologyData.pointsWithDistanceTwo) {
            sum += twoDistancePoints.first.distanceTo(twoDistancePoints.second);
        }
        return sum;
    }

    private void annealingProcess(
        SetupListData setupTopologyData,
        double circleRadius,
        Random random
    ) {
        // Start by putting all the points on a circle
        double angleSeparation = 2 * Math.PI / setupTopologyData.movablePoints.length;
        for (int i = 0; i < setupTopologyData.movablePoints.length; ++i) {
            Vector2D position = new Vector2D(Math.cos(i * angleSeparation), Math.sin(i * angleSeparation));
            position.multiplyBy(circleRadius);
            setupTopologyData.movablePoints[i].setPosition(position);
        }
        // Then start the process
        double temperature = computeInitialTemperature(setupTopologyData, random);
        LOGGER.trace("Initial temperature : {}", temperature);
        int neighborNumberTry = 30 * setupTopologyData.movablePoints.length;
        double previousEnergy = calculateObjectiveFunction(setupTopologyData);
        double bestEnergy = previousEnergy;
        LOGGER.debug("Starting energy : {}", bestEnergy);

        double averagePositiveTransitionAcceptanceRatio = STARTING_TRANSITION_PROBABILITY;

        while (averagePositiveTransitionAcceptanceRatio > 0.03) {
            averagePositiveTransitionAcceptanceRatio = 0;
            int numberOfPositiveTransition = 0;
            for (int neighborIteration = 0; neighborIteration < neighborNumberTry; ++neighborIteration) {
                int[] swapIndex = goToNeighborState(setupTopologyData.movablePoints, random);
                double newEnergy = calculateObjectiveFunction(setupTopologyData);

                double acceptanceRatio = Math.exp((previousEnergy - newEnergy) / temperature);
                if (acceptanceRatio < 1 && acceptanceRatio > 0) {
                    averagePositiveTransitionAcceptanceRatio += acceptanceRatio;
                    ++numberOfPositiveTransition;
                }

                // if the energy is lower, just update the energy value and keep going
                // if it's not lower, randomly choose if this higher energy value is accepted, if it's not revert the transformation
                if (newEnergy < previousEnergy || random.nextDouble() < acceptanceRatio) {
                    previousEnergy = newEnergy;
                    if (newEnergy < bestEnergy) {
                        bestEnergy = newEnergy;
                    }
                } else {
                    // swap back
                    swapPositions(setupTopologyData.movablePoints, swapIndex[1], swapIndex[0]);
                }
            }
            temperature *= TEMPERATURE_DECREASE_RATIO;
            if (numberOfPositiveTransition == 0) {
                averagePositiveTransitionAcceptanceRatio = 1;
            } else {
                averagePositiveTransitionAcceptanceRatio /= numberOfPositiveTransition;
            }
        }
        LOGGER.debug("Final energy : {} | Best reached energy : {}", previousEnergy, bestEnergy);

    }

    private double computeInitialTemperature(
            SetupListData setupTopologyData,
            Random random
    ) {
        // Computed using the algorithm described in Walid Ben-Ameur : Computing the initial temperature of simulated annealing
        // Start by estimating the number of transitions needed for a good estimation
        // The values for the number of transition are based on data taken from "Computing the Initial Temperature of Simulated Annealing"
        // Estimation : problem size 100 -> 62500 transitions
        // size 11 -> 2500 transitions
        // regression was made to fit a curve to that so that it makes sense
        int numberOfTransitions = getNumberOfTransitions(setupTopologyData.movablePoints);
        // Make that number of transitions on the points, calculate the corresponding energy level difference
        // the first energy is always lower than the second energy value
        List<Double[]> positiveEnergyTransitions = new ArrayList<>();
        double previousEnergy = calculateObjectiveFunction(setupTopologyData);
        double sumOfAbsolute = 0;
        for (int i = 0; i < numberOfTransitions; ++i) {
            int[] swapIndex = goToNeighborState(setupTopologyData.movablePoints, random);
            double newEnergy = calculateObjectiveFunction(setupTopologyData);
            // We can use the absolute difference to make every transition positive, because transitions are symmetric
            // That means if we have a negative energy transition, we can say we could have done the swap in the other direction, and it is positive that way
            if (newEnergy > previousEnergy) {
                positiveEnergyTransitions.add(new Double[]{previousEnergy, newEnergy});
                sumOfAbsolute += newEnergy - previousEnergy;
            } else {
                positiveEnergyTransitions.add(new Double[]{newEnergy, previousEnergy});
                sumOfAbsolute += previousEnergy - newEnergy;
            }
            // if the energy is lower, just update the energy value and keep going
            // if it's not lower, randomly choose if this higher energy value is accepted, if it's not revert the transformation
            if (newEnergy < previousEnergy || random.nextDouble() < STARTING_TRANSITION_PROBABILITY) {
                previousEnergy = newEnergy;
            } else {
                // swap back
                swapPositions(setupTopologyData.movablePoints, swapIndex[1], swapIndex[0]);
            }
            goToNeighborState(setupTopologyData.movablePoints, random);
        }
        // See the paper for explanation
        double initialTemperature = -sumOfAbsolute / (numberOfTransitions * Math.log(STARTING_TRANSITION_PROBABILITY));
        if (initialTemperature <= 0) {
            LOGGER.warn("Calculated an initial annealing temperature that was less or equal to zero, returning default value");
            return 2d;
        }
        double power = 1;

        // Finally, use the iterative formula to determine the temperature such that the acceptance probability of a positive transition is equal to the target
        while (true) {
            double probabilityEstimateNumerator = 0;
            double probabilityEstimateDenominator = 0;
            for (Double[] transition : positiveEnergyTransitions) {
                probabilityEstimateNumerator += Math.exp(-transition[1] / initialTemperature);
                probabilityEstimateDenominator += Math.exp(-transition[0] / initialTemperature);
            }
            if (probabilityEstimateDenominator == 0) {
                LOGGER.warn("The denominator of the probability estimator for the initial temperature of the annealing setup was zero, returning default value");
                return 2d;
            }
            double probabilityEstimate = probabilityEstimateNumerator / probabilityEstimateDenominator;
            if (Math.abs(probabilityEstimate - STARTING_TRANSITION_PROBABILITY) < STARTING_PRECISION) {
                // decrease temperature, because we did a round of annealing by searching the start temperature
                return initialTemperature * TEMPERATURE_DECREASE_RATIO;
            } else {
                initialTemperature *= Math.pow(Math.log(probabilityEstimate) / Math.log(STARTING_TRANSITION_PROBABILITY), power);
            }
        }
    }

    private static int getNumberOfTransitions(Point[] movablePoints) {
        if (movablePoints.length <= 9) {
            return 300;
        } else if (movablePoints.length > 9 && movablePoints.length <= 47) {
            // use linear estimation for lower values to not make too many test compared to what's needed
            return 1100 * movablePoints.length - 9600;
        } else {
            // use log estimation to not make too many test with higher values
            return -59165 + (int) (26390 * Math.log(movablePoints.length));
        }
    }

    private int[] goToNeighborState(Point[] points, Random random) {
        int firstIndex = random.nextInt(points.length);
        int secondIndex = firstIndex;
        while (secondIndex == firstIndex) {
            // this could fail in the case of a graph with a single moving point
            secondIndex = random.nextInt(points.length);
        }
        swapPositions(points, firstIndex, secondIndex);
        return new int[] {firstIndex, secondIndex};
    }

    private void swapPositions(Point[] points, int firstIndex, int secondIndex) {
        Vector2D temp = new Vector2D(points[firstIndex].getPosition());
        points[firstIndex].setPosition(points[secondIndex].getPosition());
        points[secondIndex].setPosition(temp);
    }
}

