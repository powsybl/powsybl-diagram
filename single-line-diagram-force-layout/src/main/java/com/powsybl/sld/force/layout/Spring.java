package com.powsybl.sld.force.layout;

import org.jgrapht.graph.DefaultEdge;

import java.io.PrintWriter;

public class Spring extends DefaultEdge {
    private static final double DEFAULT_LENGTH = 1.0;
    private static final double DEFAULT_STIFFNESS = 400.0;

    private final double length;
    private final double stiffness;

    public Spring() {
        super();
        length = DEFAULT_LENGTH;
        stiffness = DEFAULT_STIFFNESS;
    }

    public Point getNode1() {
        return (Point) this.getSource();
    }

    public Point getNode2() {
        return (Point) this.getTarget();
    }

    public double getLength() {
        return length;
    }

    public double getStiffness() {
        return stiffness;
    }

    public void printSVG(PrintWriter printWriter, Canvas canvas, BoundingBox boundingBox) {
        Vector position1 = this.getNode1().getPosition();
        Vector position2 = this.getNode2().getPosition();

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
