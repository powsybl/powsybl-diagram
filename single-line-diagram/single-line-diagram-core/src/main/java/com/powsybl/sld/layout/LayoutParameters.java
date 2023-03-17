/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.diagram.util.ValueFormatter;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.library.ComponentTypeName;

import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public class LayoutParameters {

    private double verticalSpaceBus = 25;
    private double horizontalBusPadding = 20;

    private double cellWidth = 50;

    private double externCellHeight = 250;
    private double internCellHeight = 40;

    private double stackHeight = 30;

    private boolean showGrid = false;

    private boolean showInternalNodes = false;

    private double scaleFactor = 1;

    private String diagramName = null;

    private boolean drawStraightWires = false;

    private double horizontalSnakeLinePadding = 20;
    private double verticalSnakeLinePadding = 25;
    private double feederInfosOuterMargin = 20;
    private double spaceForFeederInfos = 50;

    private boolean avoidSVGComponentsDuplication = false;

    private boolean adaptCellHeightToContent = true;
    private double maxComponentHeight = 12;
    private double minSpaceBetweenComponents = 15;
    private double minExternCellHeight = 80;

    private double angleLabelShift = 15.;
    private boolean labelCentered = false;
    private boolean labelDiagonal = false;

    private boolean highlightLineState = true;
    private boolean tooltipEnabled = false;

    private boolean addNodesInfos = false;

    private boolean feederInfoSymmetry = false;

    private CssLocation cssLocation = CssLocation.INSERTED_IN_SVG;

    private Padding voltageLevelPadding = new Padding(20, 60, 20, 60);

    private Padding diagramPadding = new Padding(20);

    private boolean svgWidthAndHeightAdded = false;

    private boolean useName = false;

    private double feederInfosIntraMargin = 10;

    private Alignment busbarsAlignment = Alignment.FIRST;

    /**
     * Can be used as horizontal shifting value for busInfo indicator.
     * Could be negative value.
     */
    private double busInfoMargin = 0.0;

    /** Components which are displayed on busbars */
    private List<String> componentsOnBusbars = List.of(ComponentTypeName.DISCONNECTOR);
    private String languageTag = "en";
    private int voltageValuePrecision = 1;
    private int powerValuePrecision = 0;
    private int angleValuePrecision = 1;
    private int currentValuePrecision = 0;

    /** em dash unicode for undefined value */
    private String undefinedValueSymbol = "\u2014";

    @JsonIgnore
    private Map<String, ComponentSize> componentsSize;
    private boolean displayCurrentFeederInfo;

    @JsonCreator
    public LayoutParameters() {
    }

    @JsonCreator
    public LayoutParameters(@JsonProperty("voltageLevelPadding") Padding voltageLevelPadding,
                            @JsonProperty("diagramPadding") Padding diagramPadding,
                            @JsonProperty("verticalSpaceBus") double verticalSpaceBus,
                            @JsonProperty("horizontalBusPadding") double horizontalBusPadding,
                            @JsonProperty("cellWidth") double cellWidth,
                            @JsonProperty("externCellHeight") double externCellHeight,
                            @JsonProperty("internCellHeight") double internCellHeight,
                            @JsonProperty("stackHeight") double stackHeight,
                            @JsonProperty("showGrid") boolean showGrid,
                            @JsonProperty("tooltipEnabled") boolean tooltipEnabled,
                            @JsonProperty("showInternalNodes") boolean showInternalNodes,
                            @JsonProperty("scaleFactor") double scaleFactor,
                            @JsonProperty("drawStraightWires") boolean drawStraightWires,
                            @JsonProperty("horizontalSnakeLinePadding") double horizontalSnakeLinePadding,
                            @JsonProperty("verticalSnakeLinePadding") double verticalSnakeLinePadding,
                            @JsonProperty("feederInfosOuterMargin") double feederInfosOuterMargin,
                            @JsonProperty("spaceForFeederInfos") double spaceForFeederInfos,
                            @JsonProperty("diagramName") String diagramName,
                            @JsonProperty("avoidSVGComponentsDuplication") boolean avoidSVGComponentsDuplication,
                            @JsonProperty("adaptCellHeightToContent") boolean adaptCellHeightToContent,
                            @JsonProperty("maxComponentHeight") double maxComponentHeight,
                            @JsonProperty("minSpaceBetweenComponents") double minSpaceBetweenComponents,
                            @JsonProperty("minExternCellHeight") double minExternCellHeight,
                            @JsonProperty("labelCentered") boolean labelCentered,
                            @JsonProperty("labelDiagonal") boolean labelDiagonal,
                            @JsonProperty("angleLabelShift") double angleLabelShift,
                            @JsonProperty("highlightLineState") boolean highlightLineState,
                            @JsonProperty("addNodesInfos") boolean addNodesInfos,
                            @JsonProperty("feederInfoSymmetry") boolean feederInfoSymmetry,
                            @JsonProperty("cssLocation") CssLocation cssLocation,
                            @JsonProperty("svgWidthAndHeightAdded") boolean svgWidthAndHeightAdded,
                            @JsonProperty("useName") boolean useName,
                            @JsonProperty("feederInfosIntraMargin") double feederInfosIntraMargin,
                            @JsonProperty("busInfoMargin") double busInfoMargin,
                            @JsonProperty("busbarsAlignment") Alignment busbarsAlignment,
                            @JsonProperty("componentsOnBusbars") List<String> componentsOnBusbars,
                            @JsonProperty("languageTag") String languageTag,
                            @JsonProperty("voltageValuePrecision") int voltageValuePrecision,
                            @JsonProperty("powerValuePrecision") int powerValuePrecision,
                            @JsonProperty("angleValuePrecision") int angleValuePrecision,
                            @JsonProperty("currentValuePrecision") int currentValuePrecision,
                            @JsonProperty("displayCurrentFeederInfo") boolean displayCurrentFeederInfo,
                            @JsonProperty("undefinedValueSymbol") String undefinedValueSymbol) {
        this.diagramPadding = diagramPadding;
        this.voltageLevelPadding = voltageLevelPadding;
        this.verticalSpaceBus = verticalSpaceBus;
        this.horizontalBusPadding = horizontalBusPadding;
        this.cellWidth = cellWidth;
        this.externCellHeight = externCellHeight;
        this.internCellHeight = internCellHeight;
        this.stackHeight = stackHeight;
        this.showGrid = showGrid;
        this.tooltipEnabled = tooltipEnabled;
        this.showInternalNodes = showInternalNodes;
        this.scaleFactor = scaleFactor;
        this.drawStraightWires = drawStraightWires;
        this.horizontalSnakeLinePadding = horizontalSnakeLinePadding;
        this.verticalSnakeLinePadding = verticalSnakeLinePadding;
        this.feederInfosOuterMargin = feederInfosOuterMargin;
        this.spaceForFeederInfos = spaceForFeederInfos;
        this.diagramName = diagramName;
        this.avoidSVGComponentsDuplication = avoidSVGComponentsDuplication;
        this.adaptCellHeightToContent = adaptCellHeightToContent;
        this.maxComponentHeight = maxComponentHeight;
        this.minSpaceBetweenComponents = minSpaceBetweenComponents;
        this.minExternCellHeight = minExternCellHeight;
        this.labelCentered = labelCentered;
        this.labelDiagonal = labelDiagonal;
        this.angleLabelShift = angleLabelShift;
        this.highlightLineState = highlightLineState;
        this.addNodesInfos = addNodesInfos;
        this.feederInfoSymmetry = feederInfoSymmetry;
        this.cssLocation = cssLocation;
        this.svgWidthAndHeightAdded = svgWidthAndHeightAdded;
        this.useName = useName;
        this.feederInfosIntraMargin = feederInfosIntraMargin;
        this.busInfoMargin = busInfoMargin;
        this.busbarsAlignment = busbarsAlignment;
        this.componentsOnBusbars = new ArrayList<>(componentsOnBusbars);
        this.languageTag = languageTag;
        this.voltageValuePrecision = voltageValuePrecision;
        this.powerValuePrecision = powerValuePrecision;
        this.angleValuePrecision = angleValuePrecision;
        this.currentValuePrecision = currentValuePrecision;
        this.displayCurrentFeederInfo = displayCurrentFeederInfo;
        this.undefinedValueSymbol = undefinedValueSymbol;
    }

    public LayoutParameters(LayoutParameters other) {
        Objects.requireNonNull(other);
        diagramPadding = new Padding(other.diagramPadding);
        voltageLevelPadding = new Padding(other.voltageLevelPadding);
        verticalSpaceBus = other.verticalSpaceBus;
        horizontalBusPadding = other.horizontalBusPadding;
        cellWidth = other.cellWidth;
        externCellHeight = other.externCellHeight;
        internCellHeight = other.internCellHeight;
        stackHeight = other.stackHeight;
        showGrid = other.showGrid;
        tooltipEnabled = other.tooltipEnabled;
        showInternalNodes = other.showInternalNodes;
        scaleFactor = other.scaleFactor;
        drawStraightWires = other.drawStraightWires;
        horizontalSnakeLinePadding = other.horizontalSnakeLinePadding;
        verticalSnakeLinePadding = other.verticalSnakeLinePadding;
        feederInfosOuterMargin = other.feederInfosOuterMargin;
        spaceForFeederInfos = other.spaceForFeederInfos;
        diagramName = other.diagramName;
        avoidSVGComponentsDuplication = other.avoidSVGComponentsDuplication;
        adaptCellHeightToContent = other.adaptCellHeightToContent;
        maxComponentHeight = other.maxComponentHeight;
        minSpaceBetweenComponents = other.minSpaceBetweenComponents;
        minExternCellHeight = other.minExternCellHeight;
        componentsSize = other.componentsSize;
        angleLabelShift = other.angleLabelShift;
        labelDiagonal = other.labelDiagonal;
        labelCentered = other.labelCentered;
        highlightLineState = other.highlightLineState;
        addNodesInfos = other.addNodesInfos;
        feederInfoSymmetry = other.feederInfoSymmetry;
        cssLocation = other.cssLocation;
        svgWidthAndHeightAdded = other.svgWidthAndHeightAdded;
        useName = other.useName;
        feederInfosIntraMargin = other.feederInfosIntraMargin;
        busInfoMargin = other.busInfoMargin;
        busbarsAlignment = other.busbarsAlignment;
        componentsOnBusbars = new ArrayList<>(other.componentsOnBusbars);
        languageTag = other.languageTag;
        voltageValuePrecision = other.voltageValuePrecision;
        powerValuePrecision = other.powerValuePrecision;
        angleValuePrecision = other.angleValuePrecision;
        currentValuePrecision = other.currentValuePrecision;
        displayCurrentFeederInfo = other.displayCurrentFeederInfo;
        undefinedValueSymbol = other.undefinedValueSymbol;
    }

    public double getVerticalSpaceBus() {
        return verticalSpaceBus;
    }

    public LayoutParameters setVerticalSpaceBus(double verticalSpaceBus) {
        this.verticalSpaceBus = verticalSpaceBus;
        return this;
    }

    public double getHorizontalBusPadding() {
        return horizontalBusPadding;
    }

    public LayoutParameters setHorizontalBusPadding(double horizontalSpaceBus) {
        this.horizontalBusPadding = horizontalSpaceBus;
        return this;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public LayoutParameters setCellWidth(double cellWidth) {
        this.cellWidth = cellWidth;
        return this;
    }

    public double getExternCellHeight() {
        return externCellHeight;
    }

    public LayoutParameters setExternCellHeight(double externCellHeight) {
        this.externCellHeight = externCellHeight;
        return this;
    }

    public double getInternCellHeight() {
        return internCellHeight;
    }

    public LayoutParameters setInternCellHeight(double internCellHeight) {
        this.internCellHeight = internCellHeight;
        return this;
    }

    public double getStackHeight() {
        return stackHeight;
    }

    public LayoutParameters setStackHeight(double stackHeight) {
        this.stackHeight = stackHeight;
        return this;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public LayoutParameters setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        return this;
    }

    public boolean isShowInternalNodes() {
        return showInternalNodes;
    }

    public LayoutParameters setShowInternalNodes(boolean showInternalNodes) {
        this.showInternalNodes = showInternalNodes;
        return this;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public LayoutParameters setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        return this;
    }

    public String getDiagramName() {
        return diagramName;
    }

    public LayoutParameters setDiagramName(String diagramName) {
        this.diagramName = diagramName;
        return this;
    }

    public boolean isDrawStraightWires() {
        return drawStraightWires;
    }

    public LayoutParameters setDrawStraightWires(boolean drawStraightWires) {
        this.drawStraightWires = drawStraightWires;
        return this;
    }

    public double getHorizontalSnakeLinePadding() {
        return horizontalSnakeLinePadding;
    }

    public LayoutParameters setHorizontalSnakeLinePadding(double horizontalSnakeLinePadding) {
        this.horizontalSnakeLinePadding = horizontalSnakeLinePadding;
        return this;
    }

    public double getVerticalSnakeLinePadding() {
        return verticalSnakeLinePadding;
    }

    public LayoutParameters setVerticalSnakeLinePadding(double verticalSnakeLinePadding) {
        this.verticalSnakeLinePadding = verticalSnakeLinePadding;
        return this;
    }

    public double getFeederInfosOuterMargin() {
        return feederInfosOuterMargin;
    }

    public LayoutParameters setFeederInfosOuterMargin(double feederInfosOuterMargin) {
        this.feederInfosOuterMargin = feederInfosOuterMargin;
        return this;
    }

    public boolean isAvoidSVGComponentsDuplication() {
        return avoidSVGComponentsDuplication;
    }

    public LayoutParameters setAvoidSVGComponentsDuplication(boolean avoidSVGComponentsDuplication) {
        this.avoidSVGComponentsDuplication = avoidSVGComponentsDuplication;
        return this;
    }

    public boolean isAdaptCellHeightToContent() {
        return adaptCellHeightToContent;
    }

    public LayoutParameters setAdaptCellHeightToContent(boolean adaptCellHeightToContent) {
        this.adaptCellHeightToContent = adaptCellHeightToContent;
        return this;
    }

    public double getMaxComponentHeight() {
        return maxComponentHeight;
    }

    public LayoutParameters setMaxComponentHeight(double maxComponentHeight) {
        this.maxComponentHeight = maxComponentHeight;
        return this;
    }

    public double getMinSpaceBetweenComponents() {
        return minSpaceBetweenComponents;
    }

    public LayoutParameters setMinSpaceBetweenComponents(double minSpaceBetweenComponents) {
        this.minSpaceBetweenComponents = minSpaceBetweenComponents;
        return this;
    }

    public double getMinExternCellHeight() {
        return minExternCellHeight;
    }

    public LayoutParameters setMinExternCellHeight(double minExternCellHeight) {
        this.minExternCellHeight = minExternCellHeight;
        return this;
    }

    public void setComponentsSize(Map<String, ComponentSize> componentsSize) {
        this.componentsSize = componentsSize;
    }

    public Map<String, ComponentSize> getComponentsSize() {
        return componentsSize;
    }

    public double getAngleLabelShift() {
        return angleLabelShift;
    }

    public LayoutParameters setAngleLabelShift(double angleLabelShift) {
        this.angleLabelShift = angleLabelShift;
        return this;
    }

    public boolean isLabelCentered() {
        return labelCentered;
    }

    public LayoutParameters setLabelCentered(boolean labelCentered) {
        this.labelCentered = labelCentered;
        return this;
    }

    public boolean isLabelDiagonal() {
        return labelDiagonal;
    }

    public LayoutParameters setLabelDiagonal(boolean labelDiagonal) {
        this.labelDiagonal = labelDiagonal;
        return this;
    }

    public boolean isHighlightLineState() {
        return highlightLineState;
    }

    public LayoutParameters setHighlightLineState(boolean highlightLineState) {
        this.highlightLineState = highlightLineState;
        return this;
    }

    public boolean isTooltipEnabled() {
        return tooltipEnabled;
    }

    public LayoutParameters setTooltipEnabled(boolean tooltipEnabled) {
        this.tooltipEnabled = tooltipEnabled;
        return this;
    }

    public boolean isAddNodesInfos() {
        return addNodesInfos;
    }

    public LayoutParameters setAddNodesInfos(boolean addNodesInfos) {
        this.addNodesInfos = addNodesInfos;
        return this;
    }

    public double getSpaceForFeederInfos() {
        return spaceForFeederInfos;
    }

    public LayoutParameters setSpaceForFeederInfos(double spaceForFeederInfos) {
        this.spaceForFeederInfos = spaceForFeederInfos;
        return this;
    }

    public boolean isFeederInfoSymmetry() {
        return feederInfoSymmetry;
    }

    public LayoutParameters setFeederInfoSymmetry(boolean feederInfoSymmetry) {
        this.feederInfoSymmetry = feederInfoSymmetry;
        return this;
    }

    public CssLocation getCssLocation() {
        return cssLocation;
    }

    public LayoutParameters setCssLocation(CssLocation cssLocation) {
        this.cssLocation = cssLocation;
        return this;
    }

    public Padding getVoltageLevelPadding() {
        return voltageLevelPadding;
    }

    public LayoutParameters setVoltageLevelPadding(double paddingLeft, double paddingTop, double paddingRight, double paddingBottom) {
        this.voltageLevelPadding = new Padding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        return this;
    }

    public Padding getDiagramPadding() {
        return diagramPadding;
    }

    public LayoutParameters setDiagrammPadding(double paddingLeft, double paddingTop, double paddingRight, double paddingBottom) {
        this.diagramPadding = new Padding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        return this;
    }

    public boolean isSvgWidthAndHeightAdded() {
        return svgWidthAndHeightAdded;
    }

    public LayoutParameters setSvgWidthAndHeightAdded(boolean svgWidthAndHeightAdded) {
        this.svgWidthAndHeightAdded = svgWidthAndHeightAdded;
        return this;
    }

    @JsonIgnore
    public double getBusPadding() {
        return getCellWidth() / 4;
    }

    public boolean isUseName() {
        return useName;
    }

    public LayoutParameters setUseName(boolean useName) {
        this.useName = useName;
        return this;
    }

    public double getFeederInfosIntraMargin() {
        return feederInfosIntraMargin;
    }

    public LayoutParameters setFeederInfosIntraMargin(double feederInfosIntraMargin) {
        this.feederInfosIntraMargin = feederInfosIntraMargin;
        return this;
    }

    public double getBusInfoMargin() {
        return busInfoMargin;
    }

    public LayoutParameters setBusInfoMargin(double busInfoMargin) {
        this.busInfoMargin = busInfoMargin;
        return this;
    }

    public Alignment getBusbarsAlignment() {
        return busbarsAlignment;
    }

    public LayoutParameters setBusbarsAlignment(Alignment busbarsAlignment) {
        this.busbarsAlignment = busbarsAlignment;
        return this;
    }

    public List<String> getComponentsOnBusbars() {
        return componentsOnBusbars;
    }

    public LayoutParameters setComponentsOnBusbars(List<String> componentsOnBusbars) {
        this.componentsOnBusbars = componentsOnBusbars;
        return this;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    /**
     * Sets the language tag string. This is used to format the value displayed according to the corresponding standards.
     * @param languageTag Specified IETF BCP 47 language tag string
     */
    public LayoutParameters setLanguageTag(String languageTag) {
        this.languageTag = languageTag;
        return this;
    }

    public int getVoltageValuePrecision() {
        return voltageValuePrecision;
    }

    public LayoutParameters setVoltageValuePrecision(int voltageValuePrecision) {
        this.voltageValuePrecision = voltageValuePrecision;
        return this;
    }

    public int getPowerValuePrecision() {
        return powerValuePrecision;
    }

    public LayoutParameters setPowerValuePrecision(int powerValuePrecision) {
        this.powerValuePrecision = powerValuePrecision;
        return this;
    }

    public int getAngleValuePrecision() {
        return angleValuePrecision;
    }

    public LayoutParameters setAngleValuePrecision(int angleValuePrecision) {
        this.angleValuePrecision = angleValuePrecision;
        return this;
    }

    public int getCurrentValuePrecision() {
        return currentValuePrecision;
    }

    public LayoutParameters setCurrentValuePrecision(int currentValuePrecision) {
        this.currentValuePrecision = currentValuePrecision;
        return this;
    }

    public ValueFormatter createValueFormatter() {
        return new ValueFormatter(powerValuePrecision, voltageValuePrecision, currentValuePrecision, angleValuePrecision, Locale.forLanguageTag(languageTag), undefinedValueSymbol);
    }

    public boolean isDisplayCurrentFeederInfo() {
        return this.displayCurrentFeederInfo;
    }

    public LayoutParameters setDisplayCurrentFeederInfo(boolean displayCurrentFeederInfo) {
        this.displayCurrentFeederInfo = displayCurrentFeederInfo;
        return this;
    }

    public enum Alignment {
        FIRST, LAST, MIDDLE, NONE;
    }

    public enum CssLocation {
        INSERTED_IN_SVG, EXTERNAL_IMPORTED, EXTERNAL_NO_IMPORT;
    }

    public static class Padding {
        private final double left;
        private final double top;
        private final double right;
        private final double bottom;

        @JsonCreator
        public Padding(@JsonProperty("left") double left,
                       @JsonProperty("top") double top,
                       @JsonProperty("right") double right,
                       @JsonProperty("bottom") double bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public Padding(int padding) {
            this(padding, padding, padding, padding);
        }

        public Padding(Padding padding) {
            this(padding.left, padding.top, padding.right, padding.bottom);
        }

        public double getLeft() {
            return left;
        }

        public double getRight() {
            return right;
        }

        public double getTop() {
            return top;
        }

        public double getBottom() {
            return bottom;
        }
    }

    @JsonIgnore
    public double getFeederSpan() {
        // The space needed between the feeder and the node connected to it corresponds to the space for feeder arrows
        // + half the height of the feeder component + half the height of that node component
        return getSpaceForFeederInfos() + getMaxComponentHeight();
    }

    public String getUndefinedValueSymbol() {
        return undefinedValueSymbol;
    }

    public LayoutParameters setUndefinedValueSymbol(String undefinedValueSymbol) {
        this.undefinedValueSymbol = undefinedValueSymbol;
        return this;
    }
}
