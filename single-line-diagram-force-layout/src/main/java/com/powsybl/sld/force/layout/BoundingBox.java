package com.powsybl.sld.force.layout;

public class BoundingBox {
    private Vector topRight;
    private Vector bottomLeft;

    public BoundingBox(Vector topRight, Vector bottomLeft) {
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
    }

    public Vector getTopRight() {
        return topRight;
    }

    public Vector getBottomLeft() {
        return bottomLeft;
    }
}
