package com.powsybl.sld.force.layout;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class ForceLayout {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForceLayout.class);

    private static final double DEFAULT_REPULSION = 400.0;
    private static final double DEFAULT_DAMPING = 0.5;
    private static final double DEFAULT_MAX_SPEED = Double.POSITIVE_INFINITY;
    private static final double MIN_ENERGY_THRESHOLD = 0.01;
    private static final int MAX_STEPS = 10000;

    private int maxSteps;
    private double minEnergyThreshold;
    private double repulsion;
    private double damping;
    private double maxSpeed;

    public ForceLayout() {
        this.maxSteps = MAX_STEPS;
        this.minEnergyThreshold = MIN_ENERGY_THRESHOLD;
        this.repulsion = DEFAULT_REPULSION;
        this.damping = DEFAULT_DAMPING;
        this.maxSpeed = DEFAULT_MAX_SPEED;
    }

    public ForceLayout(int maxSteps) {
        this();
        this.maxSteps = maxSteps;
    }

    public ForceLayout(int maxSteps, double minEnergyThreshold) {
        this();
        this.maxSteps = maxSteps;
        this.minEnergyThreshold = minEnergyThreshold;
    }

    public ForceLayout setRepulsion(double repulsion) {
        this.repulsion = repulsion;
        return this;
    }

    public ForceLayout setDamping(double damping) {
        this.damping = damping;
        return this;
    }

    public ForceLayout setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
        return this;
    }

    // TODO: make graph’s node and edge generics instead of point and spring type?
    public void execute(Graph<Point, Spring> graph) {
        double deltaTime = 0.01;
        int iterationCounter = 0;

        long start = System.nanoTime();

        for (int i = 0; i < this.maxSteps; i++) {
            this.applyCoulombsLaw(graph);
            this.applyHookesLaw(graph);
            this.attractToCenter(graph);
            this.updateVelocity(graph, deltaTime);
            this.updatePosition(graph, deltaTime);

            iterationCounter = i;

            if (this.isStable(graph)) {
                break;
            }
        }

        long elapsedTime = System.nanoTime() - start;

        LOGGER.info("Number of steps: {}", iterationCounter);
        LOGGER.info("Elapsed time: {}", (double) elapsedTime / 1000000000);
    }

    private void applyCoulombsLaw(Graph<Point, Spring> graph) {
        Set<Point> nodes = graph.vertexSet();
        for (Point node : nodes) {
            for (Point otherNode : nodes) {
                if (!node.equals(otherNode)) {
                    Vector distance = node.getPosition().subtract(otherNode.getPosition());
                    double magnitude = distance.magnitude() + 0.1; // avoid massive forces at small distances (and divide by zero)
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

            Vector force = direction.multiply(edge.getStiffness() * displacement * 0.5);
            edge.getNode1().applyForce(force.multiply(-1));
            edge.getNode2().applyForce(force);
        }
    }

    private void attractToCenter(Graph<Point, Spring> graph) {
        for (Point node : graph.vertexSet()) {
            Vector direction = node.getPosition().multiply(-1);

            node.applyForce(direction.multiply(this.repulsion / 50.0));
        }
    }

    private void updateVelocity(Graph<Point, Spring> graph, double deltaTime) {
        for (Point node : graph.vertexSet()) {
            Vector velocity = node.getVelocity().add(node.getAcceleration().multiply(deltaTime)).multiply(this.damping);
            node.setVelocity(velocity);

            if (node.getVelocity().magnitude() > this.maxSpeed) {
                LOGGER.debug("Velocity is superior to max speed");
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

    private boolean isStable(Graph<Point, Spring> graph) {
        Set<Point> nodes = graph.vertexSet();

        for (Point node : nodes) {
            double speed = node.getVelocity().magnitude();
            double energy = 0.5 * node.getMass() * speed * speed;

            if (energy > this.minEnergyThreshold) {
                return false;
            }
        }

        return true;
    }

    public void renderToSVG(Graph<Point, Spring> graph, Canvas canvas) throws IOException {
        File tmpFile = File.createTempFile("springy", ".html");

        FileWriter fileWriter = new FileWriter(tmpFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println("<!DOCTYPE html>");
        printWriter.println("<html>");
        printWriter.println("<body>");
        printWriter.printf("<svg width=\"%d\" height=\"%d\">%n", canvas.getWidth(), canvas.getHeight());

        BoundingBox boundingBox = computeBoundingBox(graph);

        for (Point node : graph.vertexSet()) {
            node.printSVG(printWriter, canvas, boundingBox);
        }

        for (Spring edge : graph.edgeSet()) {
            edge.printSVG(printWriter, canvas, boundingBox);
        }

        printWriter.println("</svg>");
        printWriter.println("</body>");
        printWriter.println("</html>");

        printWriter.close();
        fileWriter.close();
    }

    private BoundingBox computeBoundingBox(Graph<Point, Spring> graph) {
        Vector topRight = new Vector(2, 2);
        Vector bottomLeft = new Vector(-2, -2);

        for (Point node : graph.vertexSet()) {
            Vector position = node.getPosition();
            if (position.getX() < bottomLeft.getX())  {
                bottomLeft.setX(position.getX());
            }
            if (position.getY() < bottomLeft.getY()) {
                bottomLeft.setY(position.getY());
            }
            if (position.getX() > topRight.getX()) {
                topRight.setX(position.getX());
            }
            if (position.getY() > topRight.getY()) {
                topRight.setY(position.getY());
            }
        }

        Vector padding = topRight.subtract(bottomLeft).multiply(0.07); // to give 5% of padding, can be removed if needed

        return new BoundingBox(topRight.add(padding), bottomLeft.subtract(padding));
    }
}
