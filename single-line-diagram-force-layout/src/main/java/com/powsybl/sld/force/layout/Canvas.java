package com.powsybl.sld.force.layout;

public class Canvas {
    private int width;
    private int height;

    public Canvas(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Vector toScreen(BoundingBox boundingBox, Vector position) {
        Vector size = boundingBox.getTopRight().subtract(boundingBox.getBottomLeft());

        double screenX = position.subtract(boundingBox.getBottomLeft()).divide(size.getX()).getX() * this.width;
        double screenY = position.subtract(boundingBox.getBottomLeft()).divide(size.getY()).getY() * this.height;

        return new Vector(screenX, screenY);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
