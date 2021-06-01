package com.powsybl.sld.force.layout;

import org.jgrapht.graph.DefaultEdge;

import java.io.PrintWriter;
import java.util.Map;

public class Spring<V> extends DefaultEdge {
    private static final double DEFAULT_LENGTH = 1.0;
    private static final double DEFAULT_STIFFNESS = 400.0;

    private final double length;
    private final double stiffness;

    public Spring() {
        super();
        length = DEFAULT_LENGTH;
        stiffness = DEFAULT_STIFFNESS;
    }

    public V getNode1() {
        return (V) this.getSource();
    }

    public V getNode2() {
        return (V) this.getTarget();
    }

    public double getLength() {
        return length;
    }

    public double getStiffness() {
        return stiffness;
    }

    public void printSVG(PrintWriter printWriter, Canvas canvas, BoundingBox boundingBox, Map<V, Point> points) {
        V vertex1 = this.getNode1();
        V vertex2 = this.getNode2();

        Vector position1 = points.get(vertex1).getPosition();
        Vector position2 = points.get(vertex2).getPosition();

        Vector screenPosition1 = canvas.toScreen(boundingBox, position1);
        Vector screenPosition2 = canvas.toScreen(boundingBox, position2);

        printWriter.println("<g>");
        printWriter.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"purple\" stroke-width=\"3\"/>%n",
                (int) Math.round(screenPosition1.getX()),
                (int) Math.round(screenPosition1.getY()),
                (int) Math.round(screenPosition2.getX()),
                (int) Math.round(screenPosition2.getY()));
        printWriter.println("</g>");
    }
}
