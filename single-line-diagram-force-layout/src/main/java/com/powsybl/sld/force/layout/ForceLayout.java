package com.powsybl.sld.force.layout;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ForceLayout {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForceLayout.class);

    private static final double DEFAULT_REPULSION = 400.0;
    private static final double DEFAULT_DAMPING = 0.5;
    private static final double DEFAULT_MAX_SPEED = Double.POSITIVE_INFINITY;
    private static final double  MIN_ENERGY_THRESHOLD = 0.00001;

    private final double repulsion;
    private final double damping;
    private final double maxSpeed;

    public ForceLayout() {
        repulsion = DEFAULT_REPULSION;
        damping = DEFAULT_DAMPING;
        maxSpeed = DEFAULT_MAX_SPEED;
    }

    // TODO: make graph’s node and edge generics?
    public void execute(Graph<Point, Spring> graph, int maxSteps) {
        double deltaTime = 0.01;
        int iterationCounter = 0;
        boolean isStopped = false;

        while (!isStopped) {
            this.applyCoulombsLaw(graph);
            this.applyHookesLaw(graph);
            this.attractToCenter(graph);
            this.updateVelocity(graph, deltaTime);
            this.updatePosition(graph, deltaTime);

            iterationCounter++;
            if (this.totalEnergy(graph) <= MIN_ENERGY_THRESHOLD) {
                isStopped = true;
            }
            if (iterationCounter >= maxSteps) {
                isStopped = true;
            }
        }

        LOGGER.info("Number of steps: {}", iterationCounter);
    }

    private void applyCoulombsLaw(Graph<Point, Spring> graph) {
        Set<Point> nodes = graph.vertexSet();
        for (Point node : nodes) {
            for (Point otherNode : nodes) {
                if (!node.equals(otherNode)) {
                    Vector distance = node.getPosition().subtract(otherNode.getPosition());
                    double magnitude = distance.magnitude() + 0.1; // avoid massive forces at small distances (and divide by zero) // TODO: remove magic number?
                    Vector direction = distance.normalize();

                    Vector force = direction.multiply(this.repulsion).divide(magnitude * magnitude * 0.5);
                    node.applyForce(force);
                    otherNode.applyForce(force.multiply(-1));
                }
            }
        }
    }

    private void applyHookesLaw(Graph<Point, Spring> graph) {
        for (Spring edge : graph.edgeSet()) {
            Vector distance = edge.getNode2().getPosition().subtract(edge.getNode1().getPosition());
            double displacement = edge.getLength() - distance.magnitude();
            Vector direction = distance.normalize();

            Vector force = direction.multiply(edge.getStiffness() * displacement * 0.5); // TODO: remove magic number?
            edge.getNode1().applyForce(force.multiply(-1));
            edge.getNode2().applyForce(force);
        }
    }

    private void attractToCenter(Graph<Point, Spring> graph) {
        for (Point node : graph.vertexSet()) {
            Vector direction = node.getPosition().multiply(-1);

            node.applyForce(direction.multiply(this.repulsion / 50.0)); // TODO: remove magic number?
        }
    }

    private void updateVelocity(Graph<Point, Spring> graph, double deltaTime) {
        for (Point node : graph.vertexSet()) {
            Vector velocity = node.getVelocity().add(node.getAcceleration().multiply(deltaTime)).multiply(this.damping);
            node.setVelocity(velocity);

            if (node.getVelocity().magnitude() > this.maxSpeed) {
                System.out.println("velocity is superior to max max speed");
                velocity = node.getVelocity().normalize().multiply(this.maxSpeed);
                node.setVelocity(velocity);
            }

            node.setAcceleration(new Vector(0, 0));
        }
    }

    private void updatePosition(Graph<Point, Spring> graph, double deltaTime) {
        for (Point node : graph.vertexSet()) {
            Vector position = node.getPosition().add(node.getVelocity().multiply(deltaTime));
            node.setPosition(position);
        }
    }

    private double totalEnergy(Graph<Point, Spring> graph) {
        Set<Point> nodes = graph.vertexSet();
        double energy = 0.0;

        for (Point node : nodes) {
            double speed = node.getVelocity().magnitude();
            energy += 0.5 * node.getMass() * speed * speed;
        }

        return energy;
    }
}
