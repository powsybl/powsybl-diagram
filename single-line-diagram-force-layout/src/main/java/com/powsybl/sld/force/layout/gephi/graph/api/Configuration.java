/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.powsybl.sld.force.layout.gephi.graph.api;

import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.impl.GraphStoreConfiguration;

/**
 * Global configuration set at initialization.
 * <p>
 * This class can be passed as a parameter to
 * {@link GraphModel.Factory#newInstance(Configuration)} to
 * create a <em>GraphModel</em> with custom configuration.
 * <p>
 * Note that setting configurations after the <em>GraphModel</em> has been
 * created won't have any effect.
 * <p>
 * By default, both node and edge id types are <code>String.class</code> and the
 * time representation is <code>TIMESTAMP</code>.
 *
 * @see GraphModel
 */
public class Configuration {

    private Class nodeIdType;
    private Class edgeIdType;
    private Class edgeLabelType;
    private Class edgeWeightType;
    private TimeRepresentation timeRepresentation;
    private Boolean edgeWeightColumn;

    /**
     * Default constructor.
     */
    public Configuration() {
        nodeIdType = GraphStoreConfiguration.DEFAULT_NODE_ID_TYPE;
        edgeIdType = GraphStoreConfiguration.DEFAULT_EDGE_ID_TYPE;
        edgeLabelType = GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE;
        edgeWeightType = GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT_TYPE;
        timeRepresentation = GraphStoreConfiguration.DEFAULT_TIME_REPRESENTATION;
        edgeWeightColumn = true;
    }

    /**
     * Returns the node id type.
     *
     * @return node id type
     */
    public Class getNodeIdType() {
        return nodeIdType;
    }

    /**
     * Sets the node id type.
     * <p>
     * Only simple types such as primitives, wrappers and String are supported.
     *
     * @param nodeIdType node id type
     * @throws IllegalArgumentException if the type isn't supported
     */
    public void setNodeIdType(Class nodeIdType) {
        if (!AttributeUtils.isSimpleType(nodeIdType)) {
            throw new IllegalArgumentException("Unsupported type " + nodeIdType.getClass().getCanonicalName());
        }
        this.nodeIdType = nodeIdType;
    }

    /**
     * Returns the edge id type.
     *
     * @return edge id type
     */
    public Class getEdgeIdType() {
        return edgeIdType;
    }

    /**
     * Sets the edge id type.
     * <p>
     * Only simple types such as primitives, wrappers and String are supported.
     *
     * @param edgeIdType edge id type
     * @throws IllegalArgumentException if the type isn't supported
     */
    public void setEdgeIdType(Class edgeIdType) {
        if (!AttributeUtils.isSimpleType(edgeIdType)) {
            throw new IllegalArgumentException("Unsupported type " + edgeIdType.getClass().getCanonicalName());
        }
        this.edgeIdType = edgeIdType;
    }

    /**
     * Returns the edge label type.
     *
     * @return edge label type
     */
    public Class getEdgeLabelType() {
        return edgeLabelType;
    }

    /**
     * Sets the edge label type.
     *
     * @param edgeLabelType edge label type
     * @throws IllegalArgumentException if the type isn't supported
     */
    public void setEdgeLabelType(Class edgeLabelType) {
        if (!AttributeUtils.isSimpleType(edgeLabelType)) {
            throw new IllegalArgumentException("Unsupported type " + edgeLabelType.getClass().getCanonicalName());
        }
        this.edgeLabelType = edgeLabelType;
    }

    /**
     * Returns the edge weight type.
     *
     * @return edge weight type
     */
    public Class getEdgeWeightType() {
        return edgeWeightType;
    }

    /**
     * Sets the edge weight type.
     *
     * @param edgeWeightType edge weight type
     * @throws IllegalArgumentException if the type isn't supported
     */
    public void setEdgeWeightType(Class edgeWeightType) {
        if (Double.class.equals(edgeWeightType) || TimestampDoubleMap.class.equals(edgeWeightType) || IntervalDoubleMap.class
                .equals(edgeWeightType)) {
            this.edgeWeightType = edgeWeightType;
        } else {
            throw new IllegalArgumentException("Unsupported type " + edgeWeightType.getClass().getCanonicalName());
        }
    }

    /**
     * Returns the time representation.
     *
     * @return time representation
     */
    public TimeRepresentation getTimeRepresentation() {
        return timeRepresentation;
    }

    /**
     * Sets the time representation.
     *
     * @param timeRepresentation time representation
     */
    public void setTimeRepresentation(TimeRepresentation timeRepresentation) {
        if (timeRepresentation == null) {
            throw new IllegalArgumentException("timeRepresentation cannot be null");
        }
        this.timeRepresentation = timeRepresentation;
    }

    /**
     * Returns whether an edge weight column is created.
     *
     * @return edge weight column
     */
    public Boolean getEdgeWeightColumn() {
        return edgeWeightColumn;
    }

    /**
     * Sets whether to create an edge weight column.
     *
     * @param edgeWeightColumn edge weight column
     */
    public void setEdgeWeightColumn(Boolean edgeWeightColumn) {
        this.edgeWeightColumn = edgeWeightColumn;
    }

    /**
     * Copy this configuration.
     *
     * @return a copy of this configuration
     */
    public Configuration copy() {
        Configuration copy = new Configuration();
        copy.nodeIdType = nodeIdType;
        copy.edgeIdType = edgeIdType;
        copy.edgeLabelType = edgeLabelType;
        copy.edgeWeightType = edgeWeightType;
        copy.timeRepresentation = timeRepresentation;
        copy.edgeWeightColumn = edgeWeightColumn;
        return copy;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.nodeIdType != null ? this.nodeIdType.hashCode() : 0);
        hash = 19 * hash + (this.edgeIdType != null ? this.edgeIdType.hashCode() : 0);
        hash = 19 * hash + (this.edgeLabelType != null ? this.edgeLabelType.hashCode() : 0);
        hash = 19 * hash + (this.edgeWeightType != null ? this.edgeWeightType.hashCode() : 0);
        hash = 19 * hash + (this.timeRepresentation != null ? this.timeRepresentation.hashCode() : 0);
        hash = 19 * hash + (this.edgeWeightColumn != null ? this.edgeWeightColumn.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Configuration other = (Configuration) obj;
        if (this.nodeIdType != other.nodeIdType && (this.nodeIdType == null || !this.nodeIdType
                .equals(other.nodeIdType))) {
            return false;
        }
        if (this.edgeIdType != other.edgeIdType && (this.edgeIdType == null || !this.edgeIdType
                .equals(other.edgeIdType))) {
            return false;
        }
        if (this.edgeLabelType != other.edgeLabelType && (this.edgeLabelType == null || !this.edgeLabelType
                .equals(other.edgeLabelType))) {
            return false;
        }
        if (this.edgeWeightType != other.edgeWeightType && (this.edgeWeightType == null || !this.edgeWeightType
                .equals(other.edgeWeightType))) {
            return false;
        }
        if (this.timeRepresentation != other.timeRepresentation && (this.timeRepresentation == null || !this.timeRepresentation
                .equals(other.timeRepresentation))) {
            return false;
        }
        if (this.edgeWeightColumn != other.edgeWeightColumn && (this.edgeWeightColumn == null || !this.edgeWeightColumn
                .equals(other.edgeWeightColumn))) {
            return false;
        }
        return true;
    }
}
