/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.util.ValueFormatter;

import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class SvgParameters {

    private String prefixId = "";
    private String undefinedValueSymbol = "\u2014"; //em dash unicode for undefined value
    private String languageTag = "en";
    private int voltageValuePrecision = 1;
    private int powerValuePrecision = 0;
    private int angleValuePrecision = 1;
    private int currentValuePrecision = 0;
    private int percentageValuePrecision = 0;
    private String activePowerUnit = "";
    private String reactivePowerUnit = "";
    private String currentUnit = "";
    private double busInfoMargin = 0.0; //Can be used as horizontal shifting value for busInfo indicator. Can be negative value
    private double feederInfosIntraMargin = 10;
    private double feederInfosOuterMargin = 20;
    private boolean feederInfoSymmetry = false;
    private boolean busesLegendAdded = false;
    private boolean useName = false;
    private double angleLabelShift = 15.;
    private boolean labelCentered = false;
    private boolean labelDiagonal = false;
    private boolean tooltipEnabled = false;
    private boolean svgWidthAndHeightAdded = false;
    private CssLocation cssLocation = CssLocation.INSERTED_IN_SVG;
    private boolean avoidSVGComponentsDuplication = false;
    private String diagramName = null;
    private boolean drawStraightWires = false;
    private boolean showGrid = false;
    private boolean showInternalNodes = false;
    private boolean displayCurrentFeederInfo = false;
    private boolean displayEquipmentNodesLabel;
    private boolean displayConnectivityNodesId;
    private boolean unifyVoltageLevelColors = false;

    public SvgParameters() {
    }

    public enum CssLocation {
        INSERTED_IN_SVG, EXTERNAL_IMPORTED, EXTERNAL_NO_IMPORT
    }

    public SvgParameters(SvgParameters other) {
        Objects.requireNonNull(other);
        this.prefixId = other.prefixId;
        this.undefinedValueSymbol = other.undefinedValueSymbol;
        this.languageTag = other.languageTag;
        this.voltageValuePrecision = other.voltageValuePrecision;
        this.powerValuePrecision = other.powerValuePrecision;
        this.angleValuePrecision = other.angleValuePrecision;
        this.currentValuePrecision = other.currentValuePrecision;
        this.percentageValuePrecision = other.percentageValuePrecision;
        this.activePowerUnit = other.activePowerUnit;
        this.reactivePowerUnit = other.reactivePowerUnit;
        this.currentUnit = other.currentUnit;
        this.busInfoMargin = other.busInfoMargin;
        this.feederInfosIntraMargin = other.feederInfosIntraMargin;
        this.feederInfosOuterMargin = other.feederInfosOuterMargin;
        this.feederInfoSymmetry = other.feederInfoSymmetry;
        this.busesLegendAdded = other.busesLegendAdded;
        this.useName = other.useName;
        this.angleLabelShift = other.angleLabelShift;
        this.labelCentered = other.labelCentered;
        this.labelDiagonal = other.labelDiagonal;
        this.tooltipEnabled = other.tooltipEnabled;
        this.svgWidthAndHeightAdded = other.svgWidthAndHeightAdded;
        this.cssLocation = other.cssLocation;
        this.avoidSVGComponentsDuplication = other.avoidSVGComponentsDuplication;
        this.diagramName = other.diagramName;
        this.drawStraightWires = other.drawStraightWires;
        this.showGrid = other.showGrid;
        this.showInternalNodes = other.showInternalNodes;
        this.displayCurrentFeederInfo = other.displayCurrentFeederInfo;
        this.displayEquipmentNodesLabel = other.displayEquipmentNodesLabel;
        this.displayConnectivityNodesId = other.displayConnectivityNodesId;
        this.unifyVoltageLevelColors = other.unifyVoltageLevelColors;
    }

    public ValueFormatter createValueFormatter() {
        return new ValueFormatter(powerValuePrecision, voltageValuePrecision, currentValuePrecision, angleValuePrecision,
            percentageValuePrecision, Locale.forLanguageTag(languageTag), undefinedValueSymbol);
    }

    public String getPrefixId() {
        return prefixId;
    }

    public SvgParameters setPrefixId(String prefixId) {
        Objects.requireNonNull(prefixId);
        this.prefixId = prefixId;
        return this;
    }

    public String getUndefinedValueSymbol() {
        return undefinedValueSymbol;
    }

    public SvgParameters setUndefinedValueSymbol(String undefinedValueSymbol) {
        Objects.requireNonNull(undefinedValueSymbol);
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
        Objects.requireNonNull(languageTag);
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

    public int getCurrentValuePrecision() {
        return currentValuePrecision;
    }

    public SvgParameters setCurrentValuePrecision(int currentValuePrecision) {
        this.currentValuePrecision = currentValuePrecision;
        return this;
    }

    public int getPercentageValuePrecision() {
        return percentageValuePrecision;
    }

    public SvgParameters setPercentageValuePrecision(int percentageValuePrecision) {
        this.percentageValuePrecision = percentageValuePrecision;
        return this;
    }

    public String getActivePowerUnit() {
        return activePowerUnit;
    }

    public SvgParameters setActivePowerUnit(String activePowerUnit) {
        this.activePowerUnit = activePowerUnit;
        return this;
    }

    public String getReactivePowerUnit() {
        return reactivePowerUnit;
    }

    public SvgParameters setReactivePowerUnit(String reactivePowerUnit) {
        this.reactivePowerUnit = reactivePowerUnit;
        return this;
    }

    public String getCurrentUnit() {
        return currentUnit;
    }

    public SvgParameters setCurrentUnit(String currentUnit) {
        this.currentUnit = currentUnit;
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

    public double getFeederInfosOuterMargin() {
        return feederInfosOuterMargin;
    }

    public SvgParameters setFeederInfosOuterMargin(double feederInfosOuterMargin) {
        this.feederInfosOuterMargin = feederInfosOuterMargin;
        return this;
    }

    public boolean isFeederInfoSymmetry() {
        return feederInfoSymmetry;
    }

    public SvgParameters setFeederInfoSymmetry(boolean feederInfoSymmetry) {
        this.feederInfoSymmetry = feederInfoSymmetry;
        return this;
    }

    public boolean isBusesLegendAdded() {
        return busesLegendAdded;
    }

    public SvgParameters setBusesLegendAdded(boolean busesLegendAdded) {
        this.busesLegendAdded = busesLegendAdded;
        return this;
    }

    public boolean isUseName() {
        return useName;
    }

    public SvgParameters setUseName(boolean useName) {
        this.useName = useName;
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

    public boolean isTooltipEnabled() {
        return tooltipEnabled;
    }

    public SvgParameters setTooltipEnabled(boolean tooltipEnabled) {
        this.tooltipEnabled = tooltipEnabled;
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
        Objects.requireNonNull(cssLocation);
        this.cssLocation = cssLocation;
        return this;
    }

    public boolean isAvoidSVGComponentsDuplication() {
        return avoidSVGComponentsDuplication;
    }

    public SvgParameters setAvoidSVGComponentsDuplication(boolean avoidSVGComponentsDuplication) {
        this.avoidSVGComponentsDuplication = avoidSVGComponentsDuplication;
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

    public boolean isDisplayCurrentFeederInfo() {
        return this.displayCurrentFeederInfo;
    }

    public SvgParameters setDisplayCurrentFeederInfo(boolean displayCurrentFeederInfo) {
        this.displayCurrentFeederInfo = displayCurrentFeederInfo;
        return this;
    }

    public boolean isDisplayEquipmentNodesLabel() {
        return displayEquipmentNodesLabel;
    }

    public SvgParameters setDisplayEquipmentNodesLabel(boolean displayEquipmentNodesLabel) {
        this.displayEquipmentNodesLabel = displayEquipmentNodesLabel;
        return this;
    }

    public boolean isDisplayConnectivityNodesId() {
        return displayConnectivityNodesId;
    }

    public SvgParameters setDisplayConnectivityNodesId(boolean displayConnectivityNodesId) {
        this.displayConnectivityNodesId = displayConnectivityNodesId;
        return this;
    }

    public boolean isUnifyVoltageLevelColors() {
        return this.unifyVoltageLevelColors;
    }

    public SvgParameters setUnifyVoltageLevelColors(boolean unifyVoltageLevelColors) {
        this.unifyVoltageLevelColors = unifyVoltageLevelColors;
        return this;
    }
}
