/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.postprocessing.parameters;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class OverlapPreventionPostProcessingParameters {
    private static final double DEFAULT_POINT_SIZE_SCALE = 0.008;
    private static final double DEFAULT_POINT_SIZE_OFFSET = 4;
    private static final double DEFAULT_EDGE_ATTRACTION_INTENSITY = 1;
    private static final double DEFAULT_REPULSION_NO_OVERLAP = 4;
    private static final double DEFAULT_REPULSION_WITH_OVERLAP = 100;
    private static final double DEFAULT_REPULSION_ZONE_RATIO = 10;
    private static final double DEFAULT_ATTRACT_TO_CENTER_INTENSITY = 0.001;

    private final double pointSizeScale;
    private final double pointSizeOffset;
    private final double edgeAttractionIntensity;
    private final double repulsionNoOverlap;
    private final double repulsionWithOverlap;
    private final double repulsionZoneRatio;
    private final double attractToCenterIntensity;

    /**
     * @param pointSizeScale scaling coefficient for the size of the point given the number of nodes of the graph, get the size of a point via scale * graph size + offset
     * @param pointSizeOffset offset for the size of the point, get the size of a point via scale * graph size + offset
     * @param edgeAttractionIntensity coefficient for the attraction force on the edges
     * @param repulsionNoOverlap coefficient for the repulsion force when points do not overlap, should be no more than repulsionWithOverlap / 10 to work correctly
     * @param repulsionWithOverlap coefficient for the repulsion force when points overlap, should be no less than repulsionWithOverlap * 10 to work correctly
     * @param repulsionZoneRatio ratio for the area in which the repulsion force is calculated around a point, the area is of radius pointSize * repulsionZoneRatio
     * @param attractToCenterIntensity coefficient for the intensity of the force to attract all points to the center
     */
    private OverlapPreventionPostProcessingParameters(
            double pointSizeScale,
            double pointSizeOffset,
            double edgeAttractionIntensity,
            double repulsionNoOverlap,
            double repulsionWithOverlap,
            double repulsionZoneRatio,
            double attractToCenterIntensity
    ) {
        this.pointSizeScale = pointSizeScale;
        this.pointSizeOffset = pointSizeOffset;
        this.edgeAttractionIntensity = edgeAttractionIntensity;
        this.repulsionNoOverlap = repulsionNoOverlap;
        this.repulsionWithOverlap = repulsionWithOverlap;
        this.repulsionZoneRatio = repulsionZoneRatio;
        this.attractToCenterIntensity = attractToCenterIntensity;
    }

    public static class Builder {
        private double pointSizeScale = DEFAULT_POINT_SIZE_SCALE;
        private double pointSizeOffset = DEFAULT_POINT_SIZE_OFFSET;
        private double edgeAttractionIntensity = DEFAULT_EDGE_ATTRACTION_INTENSITY;
        private double repulsionNoOverlap = DEFAULT_REPULSION_NO_OVERLAP;
        private double repulsionWithOverlap = DEFAULT_REPULSION_WITH_OVERLAP;
        private double repulsionZoneRatio = DEFAULT_REPULSION_ZONE_RATIO;
        private double attractToCenterIntensity = DEFAULT_ATTRACT_TO_CENTER_INTENSITY;

        /**
         * @param pointSizeScale The scale for the size we want our point to take on the screen (akin to a size in pixel), the size of the point is calculated with pointSizeScale * graphSize + pointSizeOffset<br>
         *                  Default is {@value DEFAULT_POINT_SIZE_SCALE}, be careful, this parameter is very sensitive. With default values, the size for a graph of 100 nodes is 5, 12 for 1000 nodes and 28 for a graph of 3000 nodes
         * @return the instance of this Builder with the pointSizeScale parameter changed
         */
        public Builder withPointSizeScale(double pointSizeScale) {
            this.pointSizeScale = pointSizeScale;
            return this;
        }

        /**
         * @param pointSizeOffset The offset for the size we want our point to take on the screen (akin to a size in pixel), the size of the point is calculated with pointSizeScale * graphSize + pointSizeOffset<br>
         *                  Default is {@value DEFAULT_POINT_SIZE_OFFSET}, With default values, the size for a graph of 100 nodes is 5, 12 for 1000 nodes and 28 zfor a graph of 3000 nodes
         * @return the instance of this Builder with the pointSizeOffset parameter changed
         */
        public Builder withPointSizeOffset(double pointSizeOffset) {
            this.pointSizeOffset = pointSizeOffset;
            return this;
        }

        /**
         * @param edgeAttractionIntensity the coefficient for the force that attracts nodes that share an edge<br>
         *                                Default is {@value DEFAULT_EDGE_ATTRACTION_INTENSITY}
         * @return the instance of this Builder with the  edgeAttractionIntensity parameter changed
         */
        public Builder withEdgeAttractionIntensity(double edgeAttractionIntensity) {
            this.edgeAttractionIntensity = edgeAttractionIntensity;
            return this;
        }

        /**
         * @param repulsionNoOverlap the coefficient for the repulsion force when two points do not overlap, should be no greater than repulsionWithOverlap / 10<br>
         *                           Default is {@value DEFAULT_REPULSION_NO_OVERLAP}
         * @return the instance of this Builder with the repulsionNoOverlap parameter changed
         */
        public Builder withRepulsionNoOverlap(double repulsionNoOverlap) {
            this.repulsionNoOverlap = repulsionNoOverlap;
            return this;
        }

        /**
         * @param repulsionWithOverlap the coefficient for the repulsion force when two points overlap, should be at least 10 times the value of the repulsion without overlap<br>
         *                             Default is {@value DEFAULT_REPULSION_WITH_OVERLAP}
         * @return the instance of this Builder with the repulsionWithOverlap parameter changed
         */
        public Builder withRepulsionWithOverlap(double repulsionWithOverlap) {
            this.repulsionWithOverlap = repulsionWithOverlap;
            return this;
        }

        /**
         * @param repulsionZoneRatio the ratio between the area we check repulsion in and the size of the point, that is to say calculate repulsion forces around a point in a radius of ratio * point size<br>
         *                           Default is {@value DEFAULT_REPULSION_ZONE_RATIO}
         * @return the instance of this Builder with the repulsionZoneRatio parameter changed
         */
        public Builder withRepulsionZoneRatio(double repulsionZoneRatio) {
            this.repulsionZoneRatio = repulsionZoneRatio;
            return this;
        }

        /**
         * @param attractToCenterIntensity the coefficient for the force that attracts all points to the center<br>
         *                                 Default is {@value DEFAULT_ATTRACT_TO_CENTER_INTENSITY}
         * @return the instance of this Builder with the attractToCenterIntensity parameter changed
         */
        public Builder withAttractToCenterIntensity(double attractToCenterIntensity) {
            this.attractToCenterIntensity = attractToCenterIntensity;
            return this;
        }

        public OverlapPreventionPostProcessingParameters build() {
            return new OverlapPreventionPostProcessingParameters(
                    pointSizeScale,
                    pointSizeOffset,
                    edgeAttractionIntensity,
                    repulsionNoOverlap,
                    repulsionWithOverlap,
                    repulsionZoneRatio,
                    attractToCenterIntensity
            );
        }
    }

    public double getPointSizeScale() {
        return pointSizeScale;
    }

    public double getPointSizeOffset() {
        return pointSizeOffset;
    }

    public double getEdgeAttractionIntensity() {
        return edgeAttractionIntensity;
    }

    public double getRepulsionNoOverlap() {
        return repulsionNoOverlap;
    }

    public double getRepulsionWithOverlap() {
        return repulsionWithOverlap;
    }

    public double getRepulsionZoneRatio() {
        return repulsionZoneRatio;
    }

    public double getAttractToCenterIntensity() {
        return attractToCenterIntensity;
    }
}

