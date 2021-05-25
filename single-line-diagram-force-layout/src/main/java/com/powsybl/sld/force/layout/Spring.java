package com.powsybl.sld.force.layout;

import org.jgrapht.graph.DefaultEdge;

public class Spring extends DefaultEdge {
    private final static double DEFAULT_LENGTH = 1.0;
    private final static double DEFAULT_STIFFNESS = 400.0;

    private double length;
    private double stiffness;

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
}
