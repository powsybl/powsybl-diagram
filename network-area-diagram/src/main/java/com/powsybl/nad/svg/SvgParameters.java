/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SvgParameters {

    private Padding diagramPadding = new Padding(200);
    private boolean insertNameDesc = false;
    private boolean svgWidthAndHeightAdded = false;
    private CssLocation cssLocation = CssLocation.INSERTED_IN_SVG;
    private SizeConstraint sizeConstraint = SizeConstraint.FIXED_SCALE;
    private int fixedWidth = -1;
    private int fixedHeight = -1;
    private double fixedScale = 0.2;
    private double arrowShift = 30;
    private double arrowLabelShift = 19;
    private double converterStationWidth = 60;
    private double voltageLevelCircleRadius = 30;
    private double fictitiousVoltageLevelCircleRadius = 15;
    private double transformerCircleRadius = 20;
    private double nodeHollowWidth = 15;
    private double edgesForkLength = 80;
    private double edgesForkAperture = Math.toRadians(60);
    private double edgeStartShift = 0;
    private double unknownBusNodeExtraRadius = 10;
    private double loopDistance = 120;
    private double loopEdgesAperture = Math.toRadians(60);
    private double loopControlDistance = 40;
    private boolean textNodeBackground = true;
    private boolean edgeInfoAlongEdge = true;
    private double interAnnulusSpace = 5;
    private String svgPrefix = "";
    private boolean idDisplayed = false;
    private boolean substationDescriptionDisplayed;
    private double arrowHeight = 10;

    public enum CssLocation {
        INSERTED_IN_SVG, EXTERNAL_IMPORTED, EXTERNAL_NO_IMPORT
    }

    public enum SizeConstraint {
        NONE, FIXED_SCALE, FIXED_WIDTH, FIXED_HEIGHT
    }

    public SvgParameters() {
    }

    public SvgParameters(SvgParameters other) {
        this.diagramPadding = other.diagramPadding;
        this.insertNameDesc = other.insertNameDesc;
        this.svgWidthAndHeightAdded = other.svgWidthAndHeightAdded;
        this.cssLocation = other.cssLocation;
        this.sizeConstraint = other.sizeConstraint;
        this.fixedWidth = other.fixedWidth;
        this.fixedHeight = other.fixedHeight;
        this.fixedScale = other.fixedScale;
        this.arrowShift = other.arrowShift;
        this.arrowLabelShift = other.arrowLabelShift;
        this.converterStationWidth = other.converterStationWidth;
        this.voltageLevelCircleRadius = other.voltageLevelCircleRadius;
        this.fictitiousVoltageLevelCircleRadius = other.fictitiousVoltageLevelCircleRadius;
        this.transformerCircleRadius = other.transformerCircleRadius;
        this.nodeHollowWidth = other.nodeHollowWidth;
        this.edgesForkLength = other.edgesForkLength;
        this.edgesForkAperture = other.edgesForkAperture;
        this.edgeStartShift = other.edgeStartShift;
        this.unknownBusNodeExtraRadius = other.unknownBusNodeExtraRadius;
        this.loopDistance = other.loopDistance;
        this.loopEdgesAperture = other.loopEdgesAperture;
        this.loopControlDistance = other.loopControlDistance;
        this.textNodeBackground = other.textNodeBackground;
        this.edgeInfoAlongEdge = other.edgeInfoAlongEdge;
        this.interAnnulusSpace = other.interAnnulusSpace;
        this.svgPrefix = other.svgPrefix;
        this.idDisplayed = other.idDisplayed;
        this.substationDescriptionDisplayed = other.substationDescriptionDisplayed;
        this.arrowHeight = other.arrowHeight;
    }

    public Padding getDiagramPadding() {
        return diagramPadding;
    }

    public SvgParameters setDiagramPadding(Padding padding) {
        this.diagramPadding = Objects.requireNonNull(padding);
        return this;
    }

    public boolean isInsertNameDesc() {
        return insertNameDesc;
    }

    public SvgParameters setInsertNameDesc(boolean insertNameDesc) {
        this.insertNameDesc = insertNameDesc;
        return this;
    }

    public CssLocation getCssLocation() {
        return cssLocation;
    }

    public SvgParameters setCssLocation(CssLocation cssLocation) {
        this.cssLocation = Objects.requireNonNull(cssLocation);
        return this;
    }

    public int getFixedWidth() {
        return fixedWidth;
    }

    public SvgParameters setFixedWidth(int fixedWidth) {
        this.fixedWidth = fixedWidth;
        sizeConstraint = SizeConstraint.FIXED_WIDTH;
        return this;
    }

    public int getFixedHeight() {
        return fixedHeight;
    }

    public SvgParameters setFixedHeight(int fixedHeight) {
        this.fixedHeight = fixedHeight;
        sizeConstraint = SizeConstraint.FIXED_HEIGHT;
        return this;
    }

    public double getFixedScale() {
        return fixedScale;
    }

    public SvgParameters setFixedScale(double fixedScale) {
        this.fixedScale = fixedScale;
        sizeConstraint = SizeConstraint.FIXED_SCALE;
        return this;
    }

    public SizeConstraint getSizeConstraint() {
        return sizeConstraint;
    }

    public SvgParameters setSizeConstraint(SizeConstraint sizeConstraint) {
        this.sizeConstraint = sizeConstraint;
        return this;
    }

    public boolean isSvgWidthAndHeightAdded() {
        return svgWidthAndHeightAdded;
    }

    public SvgParameters setSvgWidthAndHeightAdded(boolean svgWidthAndHeightAdded) {
        this.svgWidthAndHeightAdded = svgWidthAndHeightAdded;
        return this;
    }

    public double getArrowShift() {
        return arrowShift;
    }

    public SvgParameters setArrowShift(double arrowShift) {
        this.arrowShift = arrowShift;
        return this;
    }

    public double getArrowLabelShift() {
        return arrowLabelShift;
    }

    public SvgParameters setArrowLabelShift(double arrowLabelShift) {
        this.arrowLabelShift = arrowLabelShift;
        return this;
    }

    public double getConverterStationWidth() {
        return converterStationWidth;
    }

    public SvgParameters setConverterStationWidth(double converterStationWidth) {
        this.converterStationWidth = converterStationWidth;
        return this;
    }

    public double getVoltageLevelCircleRadius() {
        return voltageLevelCircleRadius;
    }

    public SvgParameters setVoltageLevelCircleRadius(double voltageLevelCircleRadius) {
        this.voltageLevelCircleRadius = voltageLevelCircleRadius;
        return this;
    }

    public double getTransformerCircleRadius() {
        return transformerCircleRadius;
    }

    public SvgParameters setTransformerCircleRadius(double transformerCircleRadius) {
        this.transformerCircleRadius = transformerCircleRadius;
        return this;
    }

    public double getNodeHollowWidth() {
        return nodeHollowWidth;
    }

    public SvgParameters setNodeHollowWidth(double nodeHollowWidth) {
        this.nodeHollowWidth = nodeHollowWidth;
        return this;
    }

    public double getEdgesForkAperture() {
        return edgesForkAperture;
    }

    public SvgParameters setEdgesForkAperture(double edgesForkApertureDegrees) {
        this.edgesForkAperture = Math.toRadians(edgesForkApertureDegrees);
        return this;
    }

    public double getLoopEdgesAperture() {
        return loopEdgesAperture;
    }

    public SvgParameters setLoopEdgesAperture(double loopEdgesApertureDegrees) {
        this.loopEdgesAperture = Math.toRadians(loopEdgesApertureDegrees);
        return this;
    }

    public double getEdgesForkLength() {
        return edgesForkLength;
    }

    public SvgParameters setEdgesForkLength(double edgesForkLength) {
        this.edgesForkLength = edgesForkLength;
        return this;
    }

    public double getEdgeStartShift() {
        return edgeStartShift;
    }

    public SvgParameters setEdgeStartShift(double edgeStartShift) {
        this.edgeStartShift = edgeStartShift;
        return this;
    }

    public double getUnknownBusNodeExtraRadius() {
        return unknownBusNodeExtraRadius;
    }

    public SvgParameters setUnknownBusNodeExtraRadius(double unknownBusNodeExtraRadius) {
        this.unknownBusNodeExtraRadius = unknownBusNodeExtraRadius;
        return this;
    }

    public double getLoopDistance() {
        return loopDistance;
    }

    public SvgParameters setLoopDistance(double loopDistance) {
        this.loopDistance = loopDistance;
        return this;
    }

    public double getFictitiousVoltageLevelCircleRadius() {
        return fictitiousVoltageLevelCircleRadius;
    }

    public SvgParameters setFictitiousVoltageLevelCircleRadius(double fictitiousVoltageLevelCircleRadius) {
        this.fictitiousVoltageLevelCircleRadius = fictitiousVoltageLevelCircleRadius;
        return this;
    }

    public double getLoopControlDistance() {
        return loopControlDistance;
    }

    public SvgParameters setLoopControlDistance(double loopControlDistance) {
        this.loopControlDistance = loopControlDistance;
        return this;
    }

    public boolean isTextNodeBackground() {
        return textNodeBackground;
    }

    public SvgParameters setTextNodeBackground(boolean textNodeBackground) {
        this.textNodeBackground = textNodeBackground;
        return this;
    }

    public boolean isEdgeInfoAlongEdge() {
        return edgeInfoAlongEdge;
    }

    public SvgParameters setEdgeInfoAlongEdge(boolean edgeInfoAlongEdge) {
        this.edgeInfoAlongEdge = edgeInfoAlongEdge;
        return this;
    }

    public double getInterAnnulusSpace() {
        return interAnnulusSpace;
    }

    public SvgParameters setInterAnnulusSpace(double interAnnulusSpace) {
        this.interAnnulusSpace = interAnnulusSpace;
        return this;
    }

    public String getSvgPrefix() {
        return svgPrefix;
    }

    public SvgParameters setSvgPrefix(String svgPrefix) {
        this.svgPrefix = svgPrefix;
        return this;
    }

    public boolean isIdDisplayed() {
        return idDisplayed;
    }

    public SvgParameters setIdDisplayed(boolean idDisplayed) {
        this.idDisplayed = idDisplayed;
        return this;
    }

    public boolean isSubstationDescriptionDisplayed() {
        return substationDescriptionDisplayed;
    }

    public SvgParameters setSubstationDescriptionDisplayed(boolean substationDescriptionDisplayed) {
        this.substationDescriptionDisplayed = substationDescriptionDisplayed;
        return this;
    }

    public double getArrowHeight() {
        return arrowHeight;
    }

    public SvgParameters setArrowHeight(double arrowHeight) {
        this.arrowHeight = arrowHeight;
        return this;
    }
}
