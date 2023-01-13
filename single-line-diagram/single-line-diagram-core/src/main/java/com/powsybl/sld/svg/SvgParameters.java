package com.powsybl.sld.svg;

public class SvgParameters {

    private String prefixId = "";

    public SvgParameters() {
    }

    public SvgParameters(String prefixId) {
        this.prefixId = prefixId;
    }

    public SvgParameters(SvgParameters other) {
        this.prefixId = other.prefixId;
    }

    public String getPrefixId() {
        return prefixId;
    }
}
