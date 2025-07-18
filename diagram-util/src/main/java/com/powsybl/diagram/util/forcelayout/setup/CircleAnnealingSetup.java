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
    // This should always be between 0 and 1, strictly, the initial probability of positive transition to be accepted
    private static final double STARTING_TRANSITION_PROBABILITY = 0.67;
    // The precision at which we want our STARTING_TRANSITION_PROBABILITY to be matched by the function calculating the initial temperature
    private static final double STARTING_PRECISION = 1e-3;
    // By how much the temperature will decrease at each step, increasing this means more intermediate temperature, a more stable system but also a longer runtime
    private static final double TEMPERATURE_DECREASE_RATIO = 0.62;
    // The coefficient that links the size of the network to the number of iteration at the initial temperature
    private static final double INITIAL_TRIES_MULTIPLIER = 1.5;
    // Increasing this will increase the number of iteration at a given temperature step, and increase the total number of steps
    // This will leave the system more time to stabilize at a given temperature, but will be slower
    private static final double TRIES_INCREMENT_MULTIPLIER = 1.35;
    // If the number of positive accepted transitions (ie transitions that increase the energy level) is under this value, we stop
    private static final double FINAL_ACCEPTANCE_VALUE = 0.015;

    @Override
    public void setup(ForceGraph<V, E> forceGraph, Random random) {
        initForceGraph(forceGraph);
        SetupTopologyData setupTopologyData = getPointsForRun(forceGraph);
        annealingProcess(setupTopologyData, random);
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

    private void putAllPointsInCircle(Point[] movablePoints, Point[] pointWithNoEdge) {
        if (movablePoints.length == 0) {
            throw new IllegalStateException("The graph does not contain any edge");
        } else {

            double firstCircleRadius = Math.max(1, movablePoints.length / 720); // ensure a separation of at least half a degree
            // Start by putting all the points who have edges on a circle
            double angleSeparation = 2 * Math.PI / movablePoints.length;
            for (int i = 0; i < movablePoints.length; ++i) {
                Vector2D position = new Vector2D(Math.cos(i * angleSeparation), Math.sin(i * angleSeparation));
                position.multiplyBy(firstCircleRadius);
                movablePoints[i].setPosition(position);
            }

            double secondCircleRadius = 2 * firstCircleRadius;
            angleSeparation = 2 * Math.PI / pointWithNoEdge.length;
            // Setup all the points with no edges, we won't move those for the remaining part of the setup
            for (int i = 0; i < pointWithNoEdge.length; ++i) {
                Vector2D position = new Vector2D(Math.cos(i * angleSeparation), Math.sin(i * angleSeparation));
                position.multiplyBy(secondCircleRadius);
                pointWithNoEdge[i].setPosition(position);
            }
        }
    }

    private SetupTopologyData getPointsForRun(ForceGraph<V, E> forceGraph) {
        List<Set<V>> neighborSetPerVertex = new ArrayList<>();
        SimpleGraph<V, DefaultEdge> graph = forceGraph.getSimpleGraph();
        List<V> vertexWithNoEdge = new ArrayList<>();
        List<V> vertexMovable = new ArrayList<>();
        @SuppressWarnings("unchecked")
        V[] allVertex = (V[]) graph.vertexSet().toArray();
        Point[] allPoints = new Point[allVertex.length];
        for (int i = 0; i < allVertex.length; ++i) {
            neighborSetPerVertex.add(Graphs.neighborSetOf(graph, allVertex[i]));
            allPoints[i] = getPoint(forceGraph, allVertex[i]);
        }

        List<Integer> skippedColumn = new ArrayList<>();

        // Find which points have edges or not
        for (int i = 0; i < neighborSetPerVertex.size(); ++i) {
            Set<V> setOfNeighbors = neighborSetPerVertex.get(i);
            if (setOfNeighbors.isEmpty()) {
                vertexWithNoEdge.add(allVertex[i]);
                skippedColumn.add(i);
            } else {
                V thisVertex = allVertex[i];
                if (forceGraph.getMovingPoints().containsKey(thisVertex)) {
                    vertexMovable.add(thisVertex);
                }
            }
        }

        // Convert from vertex to point
        Point[] pointWithNoEdge = new Point[vertexWithNoEdge.size()];
        Point[] pointMovable = new Point[vertexMovable.size()];

        for (int i = 0; i < vertexWithNoEdge.size(); ++i) {
            pointWithNoEdge[i] = getPoint(forceGraph, vertexWithNoEdge.get(i));
        }
        for (int i = 0; i < vertexMovable.size(); ++i) {
            pointMovable[i] = getPoint(forceGraph, vertexMovable.get(i));
        }

        putAllPointsInCircle(pointMovable, pointWithNoEdge);

        // we can't do the distanceMatrix in the previous loop since we didn't know yet how many points are movable
        double[][] distanceMatrixForPointsWithEdgeDistanceOneOrTwo = new double[pointMovable.length][pointMovable.length];
        int lineIndex = 0;

        for (int i = 0; i < neighborSetPerVertex.size(); ++i) {
            Set<V> setOfNeighbors = neighborSetPerVertex.get(i);
            if (!setOfNeighbors.isEmpty()) {
                distanceMatrixForPointsWithEdgeDistanceOneOrTwo[lineIndex][lineIndex] = 0;
                int columnIndex = lineIndex + 1;
                for (int k = i + 1; k < neighborSetPerVertex.size(); ++k) {
                    if (!skippedColumn.contains(k)) {
                        // we fill the matrix if two points are next to each other or at a distance of 2
                        // if two vertex are not next to each other, and they share a common neighbor, it means they are at a distance of 2
                        V otherVertex = allVertex[k];
                        if (setOfNeighbors.contains(otherVertex) || !Collections.disjoint(setOfNeighbors, neighborSetPerVertex.get(k))) {
                            double distance = getPoint(forceGraph, allVertex[i]).distanceTo(getPoint(forceGraph, allVertex[k]));
                            distanceMatrixForPointsWithEdgeDistanceOneOrTwo[lineIndex][columnIndex] = distance;
                            distanceMatrixForPointsWithEdgeDistanceOneOrTwo[columnIndex][lineIndex] = distance;
                        } else {
                            distanceMatrixForPointsWithEdgeDistanceOneOrTwo[lineIndex][columnIndex] = 0;
                        }
                        ++columnIndex;
                    }

                }
                // we only increase the line when we went over a point that had edges
                ++lineIndex;
            }
        }

        return new SetupTopologyData(
                pointWithNoEdge,
                pointMovable,
                allPoints,
                distanceMatrixForPointsWithEdgeDistanceOneOrTwo
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

    private record SetupTopologyData(
            Point[] pointWithNoEdge, // those points are put on an outer circle and do not move for the duration of the setup
            Point[] movablePoints, // points that are "movable" have at least one edge and are not fixed
            Point[] allPoints,
            double[][] distanceMatrixForPointsWithEdgeDistanceOneOrTwo
    ) { }

    private double calculateObjectiveFunction(SetupTopologyData setupTopologyData, double previousObjectiveValue, int firstChangedIndex, int secondChangedIndex) {
        double sum = previousObjectiveValue;
        double[] firstChangedLine = setupTopologyData.distanceMatrixForPointsWithEdgeDistanceOneOrTwo[firstChangedIndex];
        for (int i = 0; i < firstChangedLine.length; ++i) {
            // ignore if we are at the secondChangedIndex to prevent removing the value twice
            if (firstChangedLine[i] != 0 && i != secondChangedIndex) {
                sum -= firstChangedLine[i];
                double newDistance = setupTopologyData.movablePoints[firstChangedIndex].distanceTo(setupTopologyData.movablePoints[i]);
                sum += newDistance;
                firstChangedLine[i] = newDistance;
                setupTopologyData.distanceMatrixForPointsWithEdgeDistanceOneOrTwo[i][firstChangedIndex] = newDistance; // matrix is symmetric, this might not actually be useful to update and could be ignored
            }
        }
        double[] secondChangedLine = setupTopologyData.distanceMatrixForPointsWithEdgeDistanceOneOrTwo[secondChangedIndex];
        for (int i = 0; i < secondChangedLine.length; ++i) {
            if (secondChangedLine[i] != 0) {
                sum -= secondChangedLine[i];
                double newDistance = setupTopologyData.movablePoints[secondChangedIndex].distanceTo(setupTopologyData.movablePoints[i]);
                sum += newDistance;
                secondChangedLine[i] = newDistance;
                setupTopologyData.distanceMatrixForPointsWithEdgeDistanceOneOrTwo[i][secondChangedIndex] = newDistance;
            }
        }
        return sum;
    }

    private double calculateStartingObjectiveFunction(SetupTopologyData setupTopologyData) {
        double sum = 0;
        for (int i = 0; i < setupTopologyData.distanceMatrixForPointsWithEdgeDistanceOneOrTwo.length; ++i) {
            for (int k = i + 1; k < setupTopologyData.distanceMatrixForPointsWithEdgeDistanceOneOrTwo.length; ++k) {
                sum += setupTopologyData.distanceMatrixForPointsWithEdgeDistanceOneOrTwo[i][k];
            }
        }
        return sum;
    }

    private void annealingProcess(
        SetupTopologyData setupTopologyData,
        Random random
    ) {
        // Then start the process
        double temperature = computeInitialTemperature(setupTopologyData, random);
        LOGGER.trace("Initial temperature : {}", temperature);
        int neighborNumberTry = (int) (INITIAL_TRIES_MULTIPLIER * setupTopologyData.movablePoints.length);
        double previousEnergy = calculateStartingObjectiveFunction(setupTopologyData);
        double bestEnergy = previousEnergy;
        LOGGER.debug("Starting energy : {}", bestEnergy);

        double averagePositiveTransitionAcceptanceRatio = STARTING_TRANSITION_PROBABILITY;

        while (averagePositiveTransitionAcceptanceRatio > FINAL_ACCEPTANCE_VALUE) {
            averagePositiveTransitionAcceptanceRatio = 0;
            int numberOfPositiveTransition = 0;
            for (int neighborIteration = 0; neighborIteration < neighborNumberTry; ++neighborIteration) {
                int[] swapIndex = goToNeighborState(setupTopologyData.movablePoints, random);
                double newEnergy = calculateObjectiveFunction(setupTopologyData, previousEnergy, swapIndex[0], swapIndex[1]);

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
                    // update matrix back
                    calculateObjectiveFunction(setupTopologyData, newEnergy, swapIndex[1], swapIndex[0]);
                }
            }
            temperature *= TEMPERATURE_DECREASE_RATIO;
            neighborNumberTry = (int) (TRIES_INCREMENT_MULTIPLIER * neighborNumberTry);
            if (numberOfPositiveTransition == 0) {
                averagePositiveTransitionAcceptanceRatio = 1;
            } else {
                averagePositiveTransitionAcceptanceRatio /= numberOfPositiveTransition;
            }
        }
        LOGGER.debug("Final energy : {} | Best reached energy : {}", previousEnergy, bestEnergy);

    }

    private double computeInitialTemperature(
            SetupTopologyData setupTopologyData,
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
        double previousEnergy = calculateStartingObjectiveFunction(setupTopologyData);
        double sumOfAbsolute = 0;
        for (int i = 0; i < numberOfTransitions; ++i) {
            int[] swapIndex = goToNeighborState(setupTopologyData.movablePoints, random);
            double newEnergy = calculateObjectiveFunction(setupTopologyData, previousEnergy, swapIndex[0], swapIndex[1]);
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
                previousEnergy = calculateObjectiveFunction(setupTopologyData, newEnergy, swapIndex[1], swapIndex[0]);
            }
        }
        // See the paper for explanation
        double initialTemperature = -sumOfAbsolute / (numberOfTransitions * Math.log(STARTING_TRANSITION_PROBABILITY));
        if (initialTemperature <= 0) {
            LOGGER.warn("Calculated an initial annealing temperature that was less or equal to zero, returning default value");
            return 2d;
        }

        // Finally, use the iterative formula to determine the temperature such that the acceptance probability of a positive transition is equal to the target
        double power = 1;
        int powerChangeStep = 10;
        int maxConvergenceStep = powerChangeStep * powerChangeStep;
        int iterationStep = 0;
        while (iterationStep < maxConvergenceStep) {
            double probabilityEstimateNumerator = 0;
            double probabilityEstimateDenominator = 0;
            for (Double[] transition : positiveEnergyTransitions) {
                probabilityEstimateNumerator += Math.exp(-transition[1] / initialTemperature);
                probabilityEstimateDenominator += Math.exp(-transition[0] / initialTemperature);
            }
            if (probabilityEstimateDenominator == 0) {
                LOGGER.warn("The denominator of the probability estimator for the initial temperature of the annealing setup was zero");
                return initialTemperature;
            }
            if (iterationStep == powerChangeStep) {
                // prevent ping-pong between two or more values or getting stuck on a single value
                power *= 2;
                powerChangeStep += 2 * powerChangeStep;
            }

            double probabilityEstimate = probabilityEstimateNumerator / probabilityEstimateDenominator;
            if (Math.abs(probabilityEstimate - STARTING_TRANSITION_PROBABILITY) < STARTING_PRECISION) {
                // decrease temperature, because we did a round of annealing by searching the start temperature
                return initialTemperature * TEMPERATURE_DECREASE_RATIO;
            } else {
                initialTemperature *= Math.pow(Math.log(probabilityEstimate) / Math.log(STARTING_TRANSITION_PROBABILITY), 1 / power);
            }
            ++iterationStep;
        }
        return initialTemperature;
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

