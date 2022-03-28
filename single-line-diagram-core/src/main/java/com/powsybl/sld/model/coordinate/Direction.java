package com.powsybl.sld.model.coordinate;

public enum Direction {
    TOP, BOTTOM, MIDDLE, UNDEFINED;

    public Orientation toOrientation() {
        switch (this) {
            case TOP:
                return Orientation.UP;
            case BOTTOM:
                return Orientation.DOWN;
            case MIDDLE:
                return Orientation.MIDDLE;
            default:
                return Orientation.UNDEFINED;
        }
    }
}
