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
package com.powsybl.sld.force.layout.gephi.graph.impl;

import com.powsybl.sld.force.layout.gephi.graph.api.Column;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.EdgeProperties;
import com.powsybl.sld.force.layout.gephi.graph.api.Estimator;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphView;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.Table;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractIntervalMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractTimestampMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeMap;

import java.awt.Color;
import java.util.Map;

public class EdgeImpl extends AbstractElementImpl implements Edge {

    // Const
    protected static final byte DIRECTED_BYTE = 1;
    protected static final byte MUTUAL_BYTE = 1 << 1;
    // Final Data
    protected final NodeImpl source;
    protected final NodeImpl target;
    protected final int type;
    // Pointers
    protected int storeId = EdgeStore.NULL_ID;
    protected int nextOutEdge = EdgeStore.NULL_ID;
    protected int nextInEdge = EdgeStore.NULL_ID;
    protected int previousOutEdge = EdgeStore.NULL_ID;
    protected int previousInEdge = EdgeStore.NULL_ID;
    // Flags
    protected byte flags;
    // Props
    protected final EdgePropertiesImpl properties;

    public EdgeImpl(Object id, GraphStore graphStore, NodeImpl source, NodeImpl target, int type, double weight, boolean directed) {
        super(id, graphStore);
        checkIdType(id);
        this.source = source;
        this.target = target;
        this.flags = (byte) (directed ? 1 : 0);
        this.type = type;
        this.properties = GraphStoreConfiguration.ENABLE_EDGE_PROPERTIES ? new EdgePropertiesImpl() : null;
        this.attributes = new Object[GraphStoreConfiguration.EDGE_WEIGHT_INDEX + 1];
        this.attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX] = id;
        if (graphStore == null || graphStore.configuration.getEdgeWeightType().equals(Double.class)) {
            this.attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX] = weight;
        }
    }

    public EdgeImpl(Object id, NodeImpl source, NodeImpl target, int type, double weight, boolean directed) {
        this(id, null, source, target, type, weight, directed);
    }

    @Override
    public NodeImpl getSource() {
        return source;
    }

    @Override
    public NodeImpl getTarget() {
        return target;
    }

    @Override
    public double getWeight() {
        synchronized (this) {
            Object weightObject = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
            if (weightObject instanceof Double) {
                return (Double) weightObject;
            } else {
                return getWeight(graphStore.getView());
            }
        }
    }

    @Override
    public boolean hasDynamicWeight() {
        return !Double.class.equals(graphStore.configuration.getEdgeWeightType());
    }

    @Override
    public void setWeight(double weight, double timestamp) {
        checkTimeRepresentationTimestamp();
        setTimeWeight(weight, timestamp);
    }

    @Override
    public void setWeight(double weight, Interval interval) {
        checkTimeRepresentationInterval();
        setTimeWeight(weight, interval);
    }

    private void setTimeWeight(double weight, Object timeObject) {
        checkWeightDynamicType();

        boolean res;
        synchronized (this) {
            Object oldValue = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
            TimeMap dynamicValue = null;
            if (oldValue == null) {
                try {
                    attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX] = dynamicValue = (TimeMap) graphStore.configuration
                            .getEdgeWeightType().newInstance();
                } catch (InstantiationException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                dynamicValue = (TimeMap) oldValue;
            }
            res = dynamicValue.put(timeObject, weight);
        }
        AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
        if (res && timeIndexStore != null && isValid()) {
            timeIndexStore.add(timeObject);
        }
        ColumnStore columnStore = getColumnStore();
        if (res && columnStore != null && isValid()) {
            Column column = columnStore.getColumnByIndex(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
            ((ColumnImpl) column).incrementVersion(this);
        }
    }

    @Override
    public double getWeight(double timestamp) {
        synchronized (this) {
            Object weightValue = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
            if (weightValue instanceof Double) {
                throw new IllegalStateException("The weight is static, call getWeight() instead");
            }

            TimeMap dynamicValue = (TimeMap) weightValue;
            if (dynamicValue == null) {
                return GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING;
            }

            if (dynamicValue instanceof AbstractIntervalMap) {
                return (Double) ((AbstractIntervalMap) dynamicValue)
                        .get(new Interval(timestamp, timestamp), GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
            } else {
                return (Double) dynamicValue.get(timestamp, GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
            }
        }
    }

    @Override
    public double getWeight(Interval interval) {
        synchronized (this) {
            Object weightValue = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
            if (weightValue instanceof Double) {
                throw new IllegalStateException("The weight is static, call getWeight() instead");
            }

            TimeMap dynamicValue = (TimeMap) weightValue;
            if (dynamicValue == null) {
                return GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING;
            }

            if (dynamicValue instanceof AbstractTimestampMap) {
                Double doubleVal = (Double) dynamicValue.get(interval, GraphStoreConfiguration.DEFAULT_ESTIMATOR);
                return doubleVal != null ? doubleVal : GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING;
            } else {
                return (Double) dynamicValue.get(interval, GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
            }
        }
    }

    @Override
    public double getWeight(GraphView view) {
        synchronized (this) {
            Object value = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
            if (value instanceof TimeMap) {
                Interval interval = view.getTimeInterval();
                checkViewExist((GraphView) view);

                TimeMap dynamicValue = (TimeMap) value;
                Estimator estimator = getColumnStore().getColumnByIndex(GraphStoreConfiguration.EDGE_WEIGHT_INDEX)
                        .getEstimator();
                if (estimator == null) {
                    estimator = GraphStoreConfiguration.DEFAULT_ESTIMATOR;
                }

                Double doubleVal = (Double) dynamicValue.get(interval, estimator);
                return doubleVal != null ? doubleVal : GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING;
            } else if (value == null) {
                return GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING;
            } else {
                // Must be double
                return (Double) value;
            }
        }
    }

    @Override
    public Iterable<Map.Entry> getWeights() {
        synchronized (this) {
            Object weightValue = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
            if (weightValue instanceof Double) {
                throw new IllegalStateException("The weight is static, call getWeight() instead");
            }
            TimeMap dynamicValue = (TimeMap) weightValue;
            Object[] values = dynamicValue.toValuesArray();
            if (dynamicValue instanceof AbstractTimestampMap) {
                return new TimeAttributeIterable(((AbstractTimestampMap) dynamicValue).getTimestamps(), values);
            } else if (dynamicValue instanceof AbstractIntervalMap) {
                return new TimeAttributeIterable(((AbstractIntervalMap) dynamicValue).toKeysArray(), values);
            }
        }
        return TimeAttributeIterable.EMPTY_ITERABLE;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Object getTypeLabel() {
        graphStore.autoReadLock();
        try {
            return graphStore.edgeTypeStore.getLabel(type);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public void setWeight(double weight) {
        checkWeightStaticType();

        synchronized (this) {
            attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX] = weight;
        }
        ColumnStore columnStore = getColumnStore();
        if (columnStore != null && isValid()) {
            Column column = columnStore.getColumnByIndex(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
            ((ColumnImpl) column).incrementVersion(this);
        }
    }

    public int getNextOutEdge() {
        return nextOutEdge;
    }

    public int getNextInEdge() {
        return nextInEdge;
    }

    public int getPreviousOutEdge() {
        return previousOutEdge;
    }

    public int getPreviousInEdge() {
        return previousInEdge;
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int id) {
        this.storeId = id;
    }

    public long getLongId() {
        return EdgeStore.getLongId(source, target, isDirected());
    }

    @Override
    public boolean isDirected() {
        return (flags & DIRECTED_BYTE) == 1;
    }

    protected void setMutual(boolean mutual) {
        if (isDirected()) {
            if (mutual) {
                flags |= MUTUAL_BYTE;
            } else {
                flags &= ~MUTUAL_BYTE;
            }
        }
    }

    protected boolean isMutual() {
        return (flags & MUTUAL_BYTE) == MUTUAL_BYTE;
    }

    @Override
    public boolean isSelfLoop() {
        return source == target;
    }

    @Override
    public Table getTable() {
        if (graphStore != null) {
            return graphStore.edgeTable;
        }
        return null;
    }

    @Override
    ColumnStore getColumnStore() {
        if (graphStore != null) {
            return graphStore.edgeTable.store;
        }
        return null;
    }

    @Override
    AbstractTimeIndexStore getTimeIndexStore() {
        if (graphStore != null) {
            return graphStore.timeStore.edgeIndexStore;
        }
        return null;
    }

    @Override
    boolean isValid() {
        return storeId != EdgeStore.NULL_ID;
    }

    @Override
    public float r() {
        return properties.r();
    }

    @Override
    public float g() {
        return properties.g();
    }

    @Override
    public float b() {
        return properties.b();
    }

    @Override
    public float alpha() {
        return properties.alpha();
    }

    @Override
    public TextPropertiesImpl getTextProperties() {
        return properties.getTextProperties();
    }

    protected void setEdgeProperties(EdgePropertiesImpl edgeProperties) {
        properties.rgba = edgeProperties.rgba;
        if (properties.textProperties != null) {
            properties.setTextProperties(edgeProperties.textProperties);
        }
    }

    @Override
    public int getRGBa() {
        return properties.rgba;
    }

    @Override
    public Color getColor() {
        return properties.getColor();
    }

    @Override
    public void setR(float r) {
        properties.setR(r);
    }

    @Override
    public void setG(float g) {
        properties.setG(g);
    }

    @Override
    public void setB(float b) {
        properties.setB(b);
    }

    @Override
    public void setAlpha(float a) {
        properties.setAlpha(a);
    }

    @Override
    public void setColor(Color color) {
        properties.setColor(color);
    }

    final void checkIdType(Object id) {
        if (graphStore != null && !id.getClass().equals(graphStore.configuration.getEdgeIdType())) {
            throw new IllegalArgumentException(
                    "The id class does not match with the expected type (" + graphStore.configuration.getEdgeIdType()
                            .getName() + ")");
        }
    }

    final void checkWeightStaticType() {
        if (graphStore != null && !Double.class.equals(graphStore.configuration.getEdgeWeightType())) {
            throw new IllegalArgumentException(
                    "The weight class does not match with the expected type (" + graphStore.configuration
                            .getEdgeWeightType().getName() + ")");
        }
    }

    final void checkWeightDynamicType() {
        if (graphStore != null && Double.class.equals(graphStore.configuration.getEdgeWeightType())) {
            throw new IllegalArgumentException(
                    "The weight class does not match with the expected type (" + graphStore.configuration
                            .getEdgeWeightType().getName() + ")");
        }
    }

    protected static class EdgePropertiesImpl implements EdgeProperties {

        protected final TextPropertiesImpl textProperties;
        protected int rgba;

        public EdgePropertiesImpl() {
            textProperties = new TextPropertiesImpl();
            this.rgba = 255 << 24; // Alpha set to 1
        }

        @Override
        public float r() {
            return ((rgba >> 16) & 0xFF) / 255f;
        }

        @Override
        public float g() {
            return ((rgba >> 8) & 0xFF) / 255f;
        }

        @Override
        public float b() {
            return (rgba & 0xFF) / 255f;
        }

        @Override
        public float alpha() {
            return ((rgba >> 24) & 0xFF) / 255f;
        }

        @Override
        public int getRGBa() {
            return rgba;
        }

        @Override
        public TextPropertiesImpl getTextProperties() {
            return textProperties;
        }

        protected void setTextProperties(TextPropertiesImpl textProperties) {
            this.textProperties.rgba = textProperties.rgba;
            this.textProperties.size = textProperties.size;
            this.textProperties.text = textProperties.text;
            this.textProperties.visible = textProperties.visible;
        }

        @Override
        public Color getColor() {
            return new Color(rgba, true);
        }

        @Override
        public void setR(float r) {
            rgba = (rgba & 0xFF00FFFF) | (((int) (r * 255f)) << 16);
        }

        @Override
        public void setG(float g) {
            rgba = (rgba & 0xFFFF00FF) | ((int) (g * 255f)) << 8;
        }

        @Override
        public void setB(float b) {
            rgba = (rgba & 0xFFFFFF00) | ((int) (b * 255f));
        }

        @Override
        public void setAlpha(float a) {
            rgba = (rgba & 0xFFFFFF) | ((int) (a * 255f)) << 24;
        }

        @Override
        public void setColor(Color color) {
            rgba = (color.getAlpha() << 24) | color.getRGB();
        }

        public int deepHashCode() {
            int hash = 3;
            hash = 29 * hash + this.rgba;
            hash = 29 * hash + (this.textProperties != null ? this.textProperties.deepHashCode() : 0);
            return hash;
        }

        public boolean deepEquals(EdgePropertiesImpl obj) {
            if (obj == null) {
                return false;
            }
            if (this.rgba != obj.rgba) {
                return false;
            }
            if (this.textProperties != obj.textProperties && (this.textProperties == null || !this.textProperties
                    .deepEquals(obj.textProperties))) {
                return false;
            }
            return true;
        }
    }
}
