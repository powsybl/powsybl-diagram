/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.util.ValueFormatter;

import java.util.Locale;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class SvgParameters {

    private String prefixId = "";
    private String undefinedValueSymbol = "\u2014"; //em dash unicode for undefined value
    private String languageTag = "en";
    private int voltageValuePrecision = 1;
    private int powerValuePrecision = 0;
    private int angleValuePrecision = 1;
    private double busInfoMargin = 0.0; //Can be used as horizontal shifting value for busInfo indicator. Can be negative value
    private double feederInfosIntraMargin = 10;
    private boolean useName = false;
    private boolean svgWidthAndHeightAdded = false;
    private SvgParameters.CssLocation cssLocation = SvgParameters.CssLocation.INSERTED_IN_SVG;
    private boolean feederInfoSymmetry = false;
    private boolean addNodesInfos = false;
    private boolean tooltipEnabled = false;
    private boolean highlightLineState = true;
    private double angleLabelShift = 15.;
    private boolean labelCentered = false;
    private boolean labelDiagonal = false;
    private boolean avoidSVGComponentsDuplication = false;
    private double feederInfosOuterMargin = 20;
    private String diagramName = null;
    private boolean drawStraightWires = false;
    private boolean showGrid = false;
    private boolean showInternalNodes = false;

    public SvgParameters() {
    }

    public enum CssLocation {
        INSERTED_IN_SVG, EXTERNAL_IMPORTED, EXTERNAL_NO_IMPORT
    }

    public SvgParameters(SvgParameters other) {
        this.prefixId = other.prefixId;
        this.undefinedValueSymbol = other.undefinedValueSymbol;
        this.languageTag = other.languageTag;
        this.voltageValuePrecision = other.voltageValuePrecision;
        this.powerValuePrecision = other.powerValuePrecision;
        this.angleValuePrecision = other.angleValuePrecision;
        this.busInfoMargin = other.busInfoMargin;
        this.feederInfosIntraMargin = other.feederInfosIntraMargin;
        this.useName = other.useName;
        this.svgWidthAndHeightAdded = other.svgWidthAndHeightAdded;
        this.cssLocation = other.cssLocation;
        this.feederInfoSymmetry = other.feederInfoSymmetry;
        this.addNodesInfos = other.addNodesInfos;
        this.tooltipEnabled = other.tooltipEnabled;
        this.highlightLineState = other.highlightLineState;
        this.angleLabelShift = other.angleLabelShift;
        this.labelCentered = other.labelCentered;
        this.labelDiagonal = other.labelDiagonal;
        this.avoidSVGComponentsDuplication = other.avoidSVGComponentsDuplication;
        this.feederInfosOuterMargin = other.feederInfosOuterMargin;
        this.diagramName = other.diagramName;
        this.drawStraightWires = other.drawStraightWires;
        this.showGrid = other.showGrid;
        this.showInternalNodes = other.showInternalNodes;
    }

    public ValueFormatter createValueFormatter() {
        return new ValueFormatter(powerValuePrecision, voltageValuePrecision, angleValuePrecision, Locale.forLanguageTag(languageTag), undefinedValueSymbol);
    }

    public String getPrefixId() {
        return prefixId;
    }

    public SvgParameters setPrefixId(String prefixId) {
        this.prefixId = prefixId;
        return this;
    }

    public String getUndefinedValueSymbol() {
        return undefinedValueSymbol;
    }

    public SvgParameters setUndefinedValueSymbol(String undefinedValueSymbol) {
        this.undefinedValueSymbol = undefinedValueSymbol;
        return this;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    /**
     * Sets the language tag string. This is used to format the value displayed according to the corresponding standards.
     * @param languageTag Specified IETF BCP 47 language tag string
     */
    public SvgParameters setLanguageTag(String languageTag) {
        this.languageTag = languageTag;
        return this;
    }

    public int getVoltageValuePrecision() {
        return voltageValuePrecision;
    }

    public SvgParameters setVoltageValuePrecision(int voltageValuePrecision) {
        this.voltageValuePrecision = voltageValuePrecision;
        return this;
    }

    public int getPowerValuePrecision() {
        return powerValuePrecision;
    }

    public SvgParameters setPowerValuePrecision(int powerValuePrecision) {
        this.powerValuePrecision = powerValuePrecision;
        return this;
    }

    public int getAngleValuePrecision() {
        return angleValuePrecision;
    }

    public SvgParameters setAngleValuePrecision(int angleValuePrecision) {
        this.angleValuePrecision = angleValuePrecision;
        return this;
    }

    public double getBusInfoMargin() {
        return busInfoMargin;
    }

    public SvgParameters setBusInfoMargin(double busInfoMargin) {
        this.busInfoMargin = busInfoMargin;
        return this;
    }

    public double getFeederInfosIntraMargin() {
        return feederInfosIntraMargin;
    }

    public SvgParameters setFeederInfosIntraMargin(double feederInfosIntraMargin) {
        this.feederInfosIntraMargin = feederInfosIntraMargin;
        return this;
    }

    public boolean isUseName() {
        return useName;
    }

    public SvgParameters setUseName(boolean useName) {
        this.useName = useName;
        return this;
    }

    public boolean isSvgWidthAndHeightAdded() {
        return svgWidthAndHeightAdded;
    }

    public SvgParameters setSvgWidthAndHeightAdded(boolean svgWidthAndHeightAdded) {
        this.svgWidthAndHeightAdded = svgWidthAndHeightAdded;
        return this;
    }

    public CssLocation getCssLocation() {
        return cssLocation;
    }

    public SvgParameters setCssLocation(CssLocation cssLocation) {
        this.cssLocation = cssLocation;
        return this;
    }

    public boolean isFeederInfoSymmetry() {
        return feederInfoSymmetry;
    }

    public SvgParameters setFeederInfoSymmetry(boolean feederInfoSymmetry) {
        this.feederInfoSymmetry = feederInfoSymmetry;
        return this;
    }

    public boolean isAddNodesInfos() {
        return addNodesInfos;
    }

    public SvgParameters setAddNodesInfos(boolean addNodesInfos) {
        this.addNodesInfos = addNodesInfos;
        return this;
    }

    public boolean isTooltipEnabled() {
        return tooltipEnabled;
    }

    public SvgParameters setTooltipEnabled(boolean tooltipEnabled) {
        this.tooltipEnabled = tooltipEnabled;
        return this;
    }

    public boolean isHighlightLineState() {
        return highlightLineState;
    }

    public SvgParameters setHighlightLineState(boolean highlightLineState) {
        this.highlightLineState = highlightLineState;
        return this;
    }

    public double getAngleLabelShift() {
        return angleLabelShift;
    }

    public SvgParameters setAngleLabelShift(double angleLabelShift) {
        this.angleLabelShift = angleLabelShift;
        return this;
    }

    public boolean isLabelCentered() {
        return labelCentered;
    }

    public SvgParameters setLabelCentered(boolean labelCentered) {
        this.labelCentered = labelCentered;
        return this;
    }

    public boolean isLabelDiagonal() {
        return labelDiagonal;
    }

    public SvgParameters setLabelDiagonal(boolean labelDiagonal) {
        this.labelDiagonal = labelDiagonal;
        return this;
    }

    public boolean isAvoidSVGComponentsDuplication() {
        return avoidSVGComponentsDuplication;
    }

    public SvgParameters setAvoidSVGComponentsDuplication(boolean avoidSVGComponentsDuplication) {
        this.avoidSVGComponentsDuplication = avoidSVGComponentsDuplication;
        return this;
    }

    public double getFeederInfosOuterMargin() {
        return feederInfosOuterMargin;
    }

    public SvgParameters setFeederInfosOuterMargin(double feederInfosOuterMargin) {
        this.feederInfosOuterMargin = feederInfosOuterMargin;
        return this;
    }

    public String getDiagramName() {
        return diagramName;
    }

    public SvgParameters setDiagramName(String diagramName) {
        this.diagramName = diagramName;
        return this;
    }

    public boolean isDrawStraightWires() {
        return drawStraightWires;
    }

    public SvgParameters setDrawStraightWires(boolean drawStraightWires) {
        this.drawStraightWires = drawStraightWires;
        return this;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public SvgParameters setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        return this;
    }

    public boolean isShowInternalNodes() {
        return showInternalNodes;
    }

    public SvgParameters setShowInternalNodes(boolean showInternalNodes) {
        this.showInternalNodes = showInternalNodes;
        return this;
    }
}
