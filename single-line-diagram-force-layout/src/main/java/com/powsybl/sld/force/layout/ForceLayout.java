package com.powsybl.sld.force.layout;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ForceLayout<V, E extends Spring> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForceLayout.class);

    private final Random random = new Random();

    private static final int DEFAULT_MAX_STEPS = 2000;
    private static final double DEFAULT_MIN_ENERGY_THRESHOLD = 0.01;
    private static final double DEFAULT_DELTA_TIME = 0.05;
    private static final double DEFAULT_REPULSION = 400.0;
    private static final double DEFAULT_DAMPING = 0.5;
    private static final double DEFAULT_MAX_SPEED = Double.POSITIVE_INFINITY;

    private int maxSteps;
    private double minEnergyThreshold;
    private double deltaTime;
    private double repulsion;
    private double damping;
    private double maxSpeed;

    private Graph<V, E> graph;
    private Map<V, Point> points;

    public ForceLayout(Graph<V, E> graph) {
        this.maxSteps = DEFAULT_MAX_STEPS;
        this.minEnergyThreshold = DEFAULT_MIN_ENERGY_THRESHOLD;
        this.deltaTime = DEFAULT_DELTA_TIME;
        this.repulsion = DEFAULT_REPULSION;
        this.damping = DEFAULT_DAMPING;
        this.maxSpeed = DEFAULT_MAX_SPEED;

        this.graph = graph;
        this.points = new HashMap<>();
    }

    public ForceLayout<V, E> setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
        return this;
    }

    public ForceLayout<V, E> setMinEnergyThreshold(double minEnergyThreshold) {
        this.minEnergyThreshold = minEnergyThreshold;
        return this;
    }

    public ForceLayout<V, E> setDeltaTime(double deltaTime) {
        this.deltaTime = deltaTime;
        return this;
    }

    public ForceLayout<V, E> setRepulsion(double repulsion) {
        this.repulsion = repulsion;
        return this;
    }

    public ForceLayout<V, E> setDamping(double damping) {
        this.damping = damping;
        return this;
    }

    public ForceLayout<V, E> setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
        return this;
    }

    public ForceLayout<V, E> setInitialisationSeed(long seed) {
        random.setSeed(seed);
        return this;
    }

    // TODO: implement other initialisations methods
    public ForceLayout<V, E> initializePoints() {
        for (V vertex : this.graph.vertexSet()) {
            // TODO: not sure vertex is unique anymore if the user override the equals method
            //       maybe we should just use an id ?
            this.points.put(vertex, new Point(random.nextDouble(), random.nextDouble()));
        }

        return this;
    }

    public void execute() {
        int iterationCounter = 0;

        long start = System.nanoTime();

        for (int i = 0; i < this.maxSteps; i++) {
            this.applyCoulombsLaw();
            this.applyHookesLaw();
            this.attractToCenter();
            this.updateVelocity();
            this.updatePosition();

            iterationCounter = i;

            if (this.isStable()) {
                break;
            }
        }

        long elapsedTime = System.nanoTime() - start;

        LOGGER.info("Number of steps: {}", iterationCounter);
        LOGGER.info("Elapsed time: {}", (double) elapsedTime / 1000000000);
    }

    private void applyCoulombsLaw() {
        Collection<Point> points = this.points.values();
        for (Point point : points) {
            for (Point otherPoint : points) {
                if (!point.equals(otherPoint)) {
                    Vector distance = point.getPosition().subtract(otherPoint.getPosition());
                    double magnitude = distance.magnitude() + 0.1;
                    Vector direction = distance.normalize();

                    Vector force = direction.multiply(this.repulsion).divide(magnitude * magnitude * 0.5);
                    point.applyForce(force);
                    otherPoint.applyForce(force.multiply(-1));
                }
            }
        }
    }

    private void applyHookesLaw() {
        for (E edge : this.graph.edgeSet()) {
            V vertex1 = (V) edge.getNode1();
            V vertex2 = (V) edge.getNode2();

            Point point1 = this.points.get(vertex1);
            Point point2 = this.points.get(vertex2);

            Vector distance = point2.getPosition().subtract(point1.getPosition());
            double displacement = edge.getLength() - distance.magnitude();
            Vector direction = distance.normalize();

            Vector force = direction.multiply(edge.getStiffness() * displacement * 0.5);
            point1.applyForce(force.multiply(-1));
            point2.applyForce(force);
        }
    }

    private void attractToCenter() {
        for (Point point : this.points.values()) {
            Vector direction = point.getPosition().multiply(-1);

            point.applyForce(direction.multiply(this.repulsion / 50.0));
        }
    }

    private void updateVelocity() {
        for (Point point : this.points.values()) {
            Vector velocity = point.getVelocity().add(point.getAcceleration().multiply(this.deltaTime)).multiply(this.damping);
            point.setVelocity(velocity);

            if (point.getVelocity().magnitude() > this.maxSpeed) {
                velocity = point.getVelocity().normalize().multiply(this.maxSpeed);
                point.setVelocity(velocity);
            }

            point.setAcceleration(new Vector(0, 0));
        }
    }

    private void updatePosition() {
        for (Point point : this.points.values()) {
            Vector position = point.getPosition().add(point.getVelocity().multiply(this.deltaTime));
            point.setPosition(position);
        }
    }

    // TODO: make it a lambda ?
    private boolean isStable() {
        for (Point point : this.points.values()) {
            double speed = point.getVelocity().magnitude();
            double energy = 0.5 * point.getMass() * speed * speed;

            if (energy > this.minEnergyThreshold) {
                return false;
            }
        }

        return true;
    }

    public Vector getStablePosition(V vertex) {
        return points.get(vertex).getPosition();
    }

    public void renderToSVG(int width, int height) throws IOException {
        Canvas canvas = new Canvas(width, height);

        File tmpFile = File.createTempFile("springy", ".html");

        FileWriter fileWriter = new FileWriter(tmpFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println("<!DOCTYPE html>");
        printWriter.println("<html>");
        printWriter.println("<body>");
        printWriter.printf("<svg width=\"%d\" height=\"%d\">%n", canvas.getWidth(), canvas.getHeight());

        BoundingBox boundingBox = computeBoundingBox();

        for (Point point : this.points.values()) {
            point.printSVG(printWriter, canvas, boundingBox);
        }

        // TODO: find a solution for edges
        for (E edge : graph.edgeSet()) {
            edge.printSVG(printWriter, canvas, boundingBox, this.points);
        }

        printWriter.println("</svg>");
        printWriter.println("</body>");
        printWriter.println("</html>");

        printWriter.close();
        fileWriter.close();
    }

    private BoundingBox computeBoundingBox() {
        Vector topRight = new Vector(2, 2);
        Vector bottomLeft = new Vector(-2, -2);

        for (Point node : this.points.values()) {
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
