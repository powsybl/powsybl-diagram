package com.powsybl.sld.svg;

public class SvgParameters {

    private String prefixId = "";
    private boolean showGrid = false;

    public SvgParameters() {
    }

    public SvgParameters(String prefixId) {
        this.prefixId = prefixId;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public SvgParameters setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        return this;
    }

    public SvgParameters(SvgParameters other) {
        this.prefixId = other.prefixId;
        this.showGrid = other.showGrid;
    }

    public String getPrefixId() {
        return prefixId;
    }
}
