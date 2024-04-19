/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.library.ComponentTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Jacques Borsenberger {@literal <jacques.borsenberger at rte-france.com>}
 */
public class LayoutParameters {

    private double verticalSpaceBus = 25;
    private double horizontalBusPadding = 20;
    private double cellWidth = 50;
    private double externCellHeight = 250;
    private double internCellHeight = 40;
    private double stackHeight = 30;
    private double horizontalSnakeLinePadding = 20;
    private double verticalSnakeLinePadding = 25;
    private double spaceForFeederInfos = 50;
    private boolean adaptCellHeightToContent = true;
    private double maxComponentHeight = 12;
    private double minSpaceBetweenComponents = 15;
    private double minExternCellHeight = 80;
    private Padding voltageLevelPadding = new Padding(20, 60, 20, 60);
    private Padding diagramPadding = new Padding(20);
    private Alignment busbarsAlignment = Alignment.FIRST;
    private List<String> componentsOnBusbars = List.of(ComponentTypeName.DISCONNECTOR); // Components which are displayed on busbars
    private boolean removeFictitiousSwitchNodes = false;
    private double cgmesScaleFactor = 1;
    private String cgmesDiagramName = null;
    private boolean cgmesUseNames = true;
    private int zoneLayoutSnakeLinePadding = 90;

    @JsonIgnore
    private Map<String, ComponentSize> componentsSize;

    @JsonCreator
    public LayoutParameters() {
    }

    @JsonCreator
    public LayoutParameters(@JsonProperty("verticalSpaceBus") double verticalSpaceBus,
                            @JsonProperty("horizontalBusPadding") double horizontalBusPadding,
                            @JsonProperty("cellWidth") double cellWidth,
                            @JsonProperty("externCellHeight") double externCellHeight,
                            @JsonProperty("internCellHeight") double internCellHeight,
                            @JsonProperty("stackHeight") double stackHeight,
                            @JsonProperty("horizontalSnakeLinePadding") double horizontalSnakeLinePadding,
                            @JsonProperty("verticalSnakeLinePadding") double verticalSnakeLinePadding,
                            @JsonProperty("spaceForFeederInfos") double spaceForFeederInfos,
                            @JsonProperty("adaptCellHeightToContent") boolean adaptCellHeightToContent,
                            @JsonProperty("maxComponentHeight") double maxComponentHeight,
                            @JsonProperty("minSpaceBetweenComponents") double minSpaceBetweenComponents,
                            @JsonProperty("minExternCellHeight") double minExternCellHeight,
                            @JsonProperty("voltageLevelPadding") Padding voltageLevelPadding,
                            @JsonProperty("diagramPadding") Padding diagramPadding,
                            @JsonProperty("busbarsAlignment") Alignment busbarsAlignment,
                            @JsonProperty("componentsOnBusbars") List<String> componentsOnBusbars,
                            @JsonProperty("removeFictitiousSwitchNodes") boolean removeFictitiousSwitchNodes,
                            @JsonProperty("cgmesScaleFactor") double cgmesScaleFactor,
                            @JsonProperty("cgmesDiagramName") String cgmesDiagramName,
                            @JsonProperty("cgmesUseNames") boolean cgmesUseNames,
                            @JsonProperty("zoneLayoutSnakeLinePadding") int zoneLayoutSnakeLinePadding) {

        this.verticalSpaceBus = verticalSpaceBus;
        this.horizontalBusPadding = horizontalBusPadding;
        this.cellWidth = cellWidth;
        this.externCellHeight = externCellHeight;
        this.internCellHeight = internCellHeight;
        this.stackHeight = stackHeight;
        this.horizontalSnakeLinePadding = horizontalSnakeLinePadding;
        this.verticalSnakeLinePadding = verticalSnakeLinePadding;
        this.spaceForFeederInfos = spaceForFeederInfos;
        this.adaptCellHeightToContent = adaptCellHeightToContent;
        this.maxComponentHeight = maxComponentHeight;
        this.minSpaceBetweenComponents = minSpaceBetweenComponents;
        this.minExternCellHeight = minExternCellHeight;
        this.voltageLevelPadding = voltageLevelPadding;
        this.diagramPadding = diagramPadding;
        this.busbarsAlignment = busbarsAlignment;
        this.componentsOnBusbars = new ArrayList<>(componentsOnBusbars);
        this.removeFictitiousSwitchNodes = removeFictitiousSwitchNodes;
        this.cgmesDiagramName = cgmesDiagramName;
        this.cgmesScaleFactor = cgmesScaleFactor;
        this.cgmesUseNames = cgmesUseNames;
        this.zoneLayoutSnakeLinePadding = zoneLayoutSnakeLinePadding;
    }

    public LayoutParameters(LayoutParameters other) {
        Objects.requireNonNull(other);
        verticalSpaceBus = other.verticalSpaceBus;
        horizontalBusPadding = other.horizontalBusPadding;
        cellWidth = other.cellWidth;
        externCellHeight = other.externCellHeight;
        internCellHeight = other.internCellHeight;
        stackHeight = other.stackHeight;
        horizontalSnakeLinePadding = other.horizontalSnakeLinePadding;
        verticalSnakeLinePadding = other.verticalSnakeLinePadding;
        spaceForFeederInfos = other.spaceForFeederInfos;
        adaptCellHeightToContent = other.adaptCellHeightToContent;
        maxComponentHeight = other.maxComponentHeight;
        minSpaceBetweenComponents = other.minSpaceBetweenComponents;
        minExternCellHeight = other.minExternCellHeight;
        voltageLevelPadding = new Padding(other.voltageLevelPadding);
        diagramPadding = new Padding(other.diagramPadding);
        busbarsAlignment = other.busbarsAlignment;
        componentsOnBusbars = new ArrayList<>(other.componentsOnBusbars);
        removeFictitiousSwitchNodes = other.removeFictitiousSwitchNodes;
        componentsSize = other.componentsSize;
        cgmesScaleFactor = other.cgmesScaleFactor;
        cgmesDiagramName = other.cgmesDiagramName;
        cgmesUseNames = other.cgmesUseNames;
        zoneLayoutSnakeLinePadding = other.zoneLayoutSnakeLinePadding;
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

    public double getSpaceForFeederInfos() {
        return spaceForFeederInfos;
    }

    public LayoutParameters setSpaceForFeederInfos(double spaceForFeederInfos) {
        this.spaceForFeederInfos = spaceForFeederInfos;
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

    public Map<String, ComponentSize> getComponentsSize() {
        return componentsSize;
    }

    public void setComponentsSize(Map<String, ComponentSize> componentsSize) {
        this.componentsSize = componentsSize;
    }

    public boolean isRemoveFictitiousSwitchNodes() {
        return removeFictitiousSwitchNodes;
    }

    public LayoutParameters setRemoveFictitiousSwitchNodes(boolean removeFictitiousSwitchNodes) {
        this.removeFictitiousSwitchNodes = removeFictitiousSwitchNodes;
        return this;
    }

    @JsonIgnore
    public double getBusPadding() {
        return getCellWidth() / 4;
    }

    public double getCgmesScaleFactor() {
        return cgmesScaleFactor;
    }

    public LayoutParameters setCgmesScaleFactor(double cgmesScaleFactor) {
        this.cgmesScaleFactor = cgmesScaleFactor;
        return this;
    }

    public String getCgmesDiagramName() {
        return cgmesDiagramName;
    }

    public LayoutParameters setCgmesDiagramName(String cgmesDiagramName) {
        this.cgmesDiagramName = cgmesDiagramName;
        return this;
    }

    public boolean isCgmesUseNames() {
        return cgmesUseNames;
    }

    public LayoutParameters setCgmesUseNames(boolean cgmesUseNames) {
        this.cgmesUseNames = cgmesUseNames;
        return this;
    }

    public int getZoneLayoutSnakeLinePadding() {
        return zoneLayoutSnakeLinePadding;
    }

    public LayoutParameters setZoneLayoutSnakeLinePadding(int zoneLayoutSnakeLinePadding) {
        this.zoneLayoutSnakeLinePadding = zoneLayoutSnakeLinePadding;
        return this;
    }

    public enum Alignment {
        FIRST, LAST, MIDDLE, NONE
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

}
