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
import com.powsybl.sld.library.ComponentSize;

import java.util.Map;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class LayoutParameters {

    private double translateX = 20;
    private double translateY = 50;
    private double initialXBus = 0;
    private double initialYBus = 260;
    private double verticalSpaceBus = 25;
    private double horizontalBusPadding = 20;

    private double cellWidth = 50;

    private double externCellHeight = 250;
    private double internCellHeight = 40;

    private double stackHeight = 30;

    private boolean showGrid = false;

    private boolean showInternalNodes = false;

    private double scaleFactor = 1;

    private double horizontalSubstationPadding = 50;
    private double verticalSubstationPadding = 50;

    private String diagramName = null;

    private boolean drawStraightWires = false;

    private double horizontalSnakeLinePadding = 20;
    private double verticalSnakeLinePadding = 25;
    private double arrowDistance = 20;

    private boolean showInductorFor3WT = false;

    private boolean shiftFeedersPosition = false;

    private double scaleShiftFeedersPosition = 1;
    private boolean avoidSVGComponentsDuplication = false;

    private boolean adaptCellHeightToContent = false;
    private double maxComponentHeight = 12;
    private double minSpaceBetweenComponents = 15;
    private double minExternCellHeight = 80;

    @JsonIgnore
    private Map<String, ComponentSize> componentsSize;

    @JsonCreator
    public LayoutParameters() {
    }

    @JsonCreator
    public LayoutParameters(@JsonProperty("translateX") double translateX,
                            @JsonProperty("translateY") double translateY,
                            @JsonProperty("initialXBus") double initialXBus,
                            @JsonProperty("initialYBus") double initialYBus,
                            @JsonProperty("verticalSpaceBus") double verticalSpaceBus,
                            @JsonProperty("horizontalBusPadding") double horizontalBusPadding,
                            @JsonProperty("cellWidth") double cellWidth,
                            @JsonProperty("externCellHeight") double externCellHeight,
                            @JsonProperty("internCellHeight") double internCellHeight,
                            @JsonProperty("stackHeight") double stackHeight,
                            @JsonProperty("showGrid") boolean showGrid,
                            @JsonProperty("showInternalNodes") boolean showInternalNodes,
                            @JsonProperty("scaleFactor") double scaleFactor,
                            @JsonProperty("horizontalSubstationPadding") double horizontalSubstationPadding,
                            @JsonProperty("verticalSubstationPadding") double verticalSubstationPadding,
                            @JsonProperty("drawStraightWires") boolean drawStraightWires,
                            @JsonProperty("horizontalSnakeLinePadding") double horizontalSnakeLinePadding,
                            @JsonProperty("verticalSnakeLinePadding") double verticalSnakeLinePadding,
                            @JsonProperty("arrowDistance") double arrowDistance,
                            @JsonProperty("showInductorFor3WT") boolean showInductorFor3WT,
                            @JsonProperty("diagramName") String diagramName,
                            @JsonProperty("shiftFeedersPosition") boolean shiftFeedersPosition,
                            @JsonProperty("scaleShiftFeedersPosition") double scaleShiftFeedersPosition,
                            @JsonProperty("avoidSVGComponentsDuplication") boolean avoidSVGComponentsDuplication,
                            @JsonProperty("adaptCellHeightToContent") boolean adaptCellHeightToContent,
                            @JsonProperty("maxComponentHeight") double maxComponentHeight,
                            @JsonProperty("minSpaceBetweenComponents") double minSpaceBetweenComponents,
                            @JsonProperty("minExternCellHeight") double minExternCellHeight) {
        this.translateX = translateX;
        this.translateY = translateY;
        this.initialXBus = initialXBus;
        this.initialYBus = initialYBus;
        this.verticalSpaceBus = verticalSpaceBus;
        this.horizontalBusPadding = horizontalBusPadding;
        this.cellWidth = cellWidth;
        this.externCellHeight = externCellHeight;
        this.internCellHeight = internCellHeight;
        this.stackHeight = stackHeight;
        this.showGrid = showGrid;
        this.showInternalNodes = showInternalNodes;
        this.scaleFactor = scaleFactor;
        this.horizontalSubstationPadding = horizontalSubstationPadding;
        this.verticalSubstationPadding = verticalSubstationPadding;
        this.drawStraightWires = drawStraightWires;
        this.horizontalSnakeLinePadding = horizontalSnakeLinePadding;
        this.verticalSnakeLinePadding = verticalSnakeLinePadding;
        this.arrowDistance = arrowDistance;
        this.showInductorFor3WT = showInductorFor3WT;
        this.diagramName = diagramName;
        this.shiftFeedersPosition = shiftFeedersPosition;
        this.scaleShiftFeedersPosition = scaleShiftFeedersPosition;
        this.avoidSVGComponentsDuplication = avoidSVGComponentsDuplication;
        this.adaptCellHeightToContent = adaptCellHeightToContent;
        this.maxComponentHeight = maxComponentHeight;
        this.minSpaceBetweenComponents = minSpaceBetweenComponents;
        this.minExternCellHeight = minExternCellHeight;
    }

    public LayoutParameters(LayoutParameters other) {
        Objects.requireNonNull(other);
        translateX = other.translateX;
        translateY = other.translateY;
        initialXBus = other.initialXBus;
        initialYBus = other.initialYBus;
        verticalSpaceBus = other.verticalSpaceBus;
        horizontalBusPadding = other.horizontalBusPadding;
        cellWidth = other.cellWidth;
        externCellHeight = other.externCellHeight;
        internCellHeight = other.internCellHeight;
        stackHeight = other.stackHeight;
        showGrid = other.showGrid;
        showInternalNodes = other.showInternalNodes;
        scaleFactor = other.scaleFactor;
        horizontalSubstationPadding = other.horizontalSubstationPadding;
        verticalSubstationPadding = other.verticalSubstationPadding;
        drawStraightWires = other.drawStraightWires;
        horizontalSnakeLinePadding = other.horizontalSnakeLinePadding;
        verticalSnakeLinePadding = other.verticalSnakeLinePadding;
        arrowDistance = other.arrowDistance;
        showInductorFor3WT = other.showInductorFor3WT;
        diagramName = other.diagramName;
        shiftFeedersPosition = other.shiftFeedersPosition;
        scaleShiftFeedersPosition = other.scaleShiftFeedersPosition;
        avoidSVGComponentsDuplication = other.avoidSVGComponentsDuplication;
        adaptCellHeightToContent = other.adaptCellHeightToContent;
        maxComponentHeight = other.maxComponentHeight;
        minSpaceBetweenComponents = other.minSpaceBetweenComponents;
        minExternCellHeight = other.minExternCellHeight;
        componentsSize = other.componentsSize;
    }

    public double getTranslateX() {
        return translateX;
    }

    public LayoutParameters setTranslateX(double translateX) {
        this.translateX = translateX;
        return this;
    }

    public double getTranslateY() {
        return translateY;
    }

    public LayoutParameters setTranslateY(double translateY) {
        this.translateY = translateY;
        return this;
    }

    public double getInitialXBus() {
        return initialXBus;
    }

    public LayoutParameters setInitialXBus(double initialXBus) {
        this.initialXBus = initialXBus;
        return this;
    }

    public double getInitialYBus() {
        return initialYBus;
    }

    public LayoutParameters setInitialYBus(double initialYBus) {
        this.initialYBus = initialYBus;
        return this;
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

    public double getHorizontalSubstationPadding() {
        return horizontalSubstationPadding;
    }

    public LayoutParameters setHorizontalSubstationPadding(double padding) {
        this.horizontalSubstationPadding = padding;
        return this;
    }

    public double getVerticalSubstationPadding() {
        return verticalSubstationPadding;
    }

    public LayoutParameters setVerticalSubstationPadding(double padding) {
        this.verticalSubstationPadding = padding;
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

    public double getArrowDistance() {
        return arrowDistance;
    }

    public LayoutParameters setArrowDistance(double arrowDistance) {
        this.arrowDistance = arrowDistance;
        return this;
    }

    public boolean isShowInductorFor3WT() {
        return showInductorFor3WT;
    }

    public LayoutParameters setShowInductorFor3WT(boolean showInductorFor3WT) {
        this.showInductorFor3WT = showInductorFor3WT;
        return this;
    }

    public boolean isShiftFeedersPosition() {
        return shiftFeedersPosition;
    }

    public LayoutParameters setShiftFeedersPosition(boolean shiftFeedersPosition) {
        this.shiftFeedersPosition = shiftFeedersPosition;
        return this;
    }

    public double getScaleShiftFeedersPosition() {
        return scaleShiftFeedersPosition;
    }

    public LayoutParameters setScaleShiftFeedersPosition(double scaleShiftFeedersPosition) {
        this.scaleShiftFeedersPosition = scaleShiftFeedersPosition;
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
}
