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

import com.powsybl.sld.force.layout.gephi.graph.api.NodeProperties;
import com.powsybl.sld.force.layout.gephi.graph.api.Table;
import com.powsybl.sld.force.layout.gephi.graph.spi.LayoutData;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;

import java.awt.Color;

public class NodeImpl extends AbstractElementImpl implements Node {

    protected int storeId = NodeStore.NULL_ID;
    protected EdgeImpl[] headOut = new EdgeImpl[GraphStoreConfiguration.EDGESTORE_DEFAULT_TYPE_COUNT];
    protected EdgeImpl[] headIn = new EdgeImpl[GraphStoreConfiguration.EDGESTORE_DEFAULT_TYPE_COUNT];
    // Degree
    protected int inDegree;
    protected int outDegree;
    protected int mutualDegree;
    // Props
    protected final NodePropertiesImpl properties;

    public NodeImpl(Object id, GraphStore graphStore) {
        super(id, graphStore);
        checkIdType(id);
        this.properties = GraphStoreConfiguration.ENABLE_NODE_PROPERTIES ? new NodePropertiesImpl() : null;
        this.attributes = new Object[GraphStoreConfiguration.ELEMENT_ID_INDEX + 1];
        this.attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX] = id;
    }

    public NodeImpl(Object id) {
        this(id, null);
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int id) {
        this.storeId = id;
    }

    public int getDegree() {
        return inDegree + outDegree;
    }

    public int getInDegree() {
        return inDegree;
    }

    public int getOutDegree() {
        return outDegree;
    }

    public int getUndirectedDegree() {
        return inDegree + outDegree - mutualDegree;
    }

    @Override
    ColumnStore getColumnStore() {
        if (graphStore != null) {
            return graphStore.nodeTable.store;
        }
        return null;
    }

    @Override
    public Table getTable() {
        if (graphStore != null) {
            return graphStore.nodeTable;
        }
        return null;
    }

    @Override
    AbstractTimeIndexStore getTimeIndexStore() {
        if (graphStore != null) {
            return graphStore.timeStore.nodeIndexStore;
        }
        return null;
    }

    @Override
    boolean isValid() {
        return storeId != NodeStore.NULL_ID;
    }

    @Override
    public float x() {
        return properties.x;
    }

    @Override
    public float y() {
        return properties.y;
    }

    @Override
    public float z() {
        return properties.z;
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
    public int getRGBa() {
        return properties.rgba;
    }

    @Override
    public Color getColor() {
        return properties.getColor();
    }

    @Override
    public float size() {
        return properties.size;
    }

    @Override
    public boolean isFixed() {
        return properties.isFixed();
    }

    @Override
    public <T extends LayoutData> T getLayoutData() {
        return properties.getLayoutData();
    }

    @Override
    public TextPropertiesImpl getTextProperties() {
        return properties.getTextProperties();
    }

    protected void setNodeProperties(NodePropertiesImpl nodeProperties) {
        properties.x = nodeProperties.x;
        properties.y = nodeProperties.y;
        properties.z = nodeProperties.z;
        properties.rgba = nodeProperties.rgba;
        properties.size = nodeProperties.size;
        properties.fixed = nodeProperties.fixed;
        if (properties.textProperties != null) {
            properties.setTextProperties(nodeProperties.textProperties);
        }
    }

    @Override
    public void setX(float x) {
        properties.setX(x);
    }

    @Override
    public void setY(float y) {
        properties.setY(y);
    }

    @Override
    public void setZ(float z) {
        properties.setZ(z);
    }

    @Override
    public void setPosition(float x, float y) {
        properties.setPosition(x, y);
    }

    @Override
    public void setPosition(float x, float y, float z) {
        properties.setPosition(x, y, z);
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

    @Override
    public void setSize(float size) {
        properties.setSize(size);
    }

    @Override
    public void setFixed(boolean fixed) {
        properties.setFixed(fixed);
    }

    @Override
    public void setLayoutData(LayoutData layoutData) {
        properties.setLayoutData(layoutData);
    }

    final void checkIdType(Object id) {
        if (graphStore != null && !id.getClass().equals(graphStore.configuration.getNodeIdType())) {
            throw new IllegalArgumentException(
                    "The id class does not match with the expected type (" + graphStore.configuration.getNodeIdType()
                            .getName() + ")");
        }
    }

    protected static class NodePropertiesImpl implements NodeProperties {

        protected final TextPropertiesImpl textProperties;
        protected float x;
        protected float y;
        protected float z;
        protected int rgba;
        protected float size;
        protected boolean fixed;
        protected LayoutData layoutData;

        public NodePropertiesImpl() {
            this.textProperties = new TextPropertiesImpl();
            this.rgba = 255 << 24; // Alpha set to 1
        }

        @Override
        public float x() {
            return x;
        }

        @Override
        public float y() {
            return y;
        }

        @Override
        public float z() {
            return z;
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
        public Color getColor() {
            return new Color(rgba, true);
        }

        @Override
        public float size() {
            return size;
        }

        @Override
        public boolean isFixed() {
            return fixed;
        }

        @Override
        public <T extends LayoutData> T getLayoutData() {
            return (T) layoutData;
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
        public void setX(float x) {
            this.x = x;
        }

        @Override
        public void setY(float y) {
            this.y = y;
        }

        @Override
        public void setZ(float z) {
            this.z = z;
        }

        @Override
        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void setPosition(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
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
            this.rgba = (color.getAlpha() << 24) | color.getRGB();
        }

        @Override
        public void setSize(float size) {
            this.size = size;
        }

        @Override
        public void setFixed(boolean fixed) {
            this.fixed = fixed;
        }

        @Override
        public void setLayoutData(LayoutData layoutData) {
            this.layoutData = layoutData;
        }

        public int deepHashCode() {
            int hash = 3;
            hash = 53 * hash + Float.floatToIntBits(this.x);
            hash = 53 * hash + Float.floatToIntBits(this.y);
            hash = 53 * hash + Float.floatToIntBits(this.z);
            hash = 53 * hash + this.rgba;
            hash = 53 * hash + Float.floatToIntBits(this.size);
            hash = 53 * hash + (this.fixed ? 1 : 0);
            hash = 53 * hash + (this.layoutData != null ? this.layoutData.hashCode() : 0);
            hash = 53 * hash + (this.textProperties != null ? this.textProperties.deepHashCode() : 0);
            return hash;
        }

        public boolean deepEquals(NodePropertiesImpl obj) {
            if (obj == null) {
                return false;
            }
            if (Float.floatToIntBits(this.x) != Float.floatToIntBits(obj.x)) {
                return false;
            }
            if (Float.floatToIntBits(this.y) != Float.floatToIntBits(obj.y)) {
                return false;
            }
            if (Float.floatToIntBits(this.z) != Float.floatToIntBits(obj.z)) {
                return false;
            }
            if (this.rgba != obj.rgba) {
                return false;
            }
            if (Float.floatToIntBits(this.size) != Float.floatToIntBits(obj.size)) {
                return false;
            }
            if (this.fixed != obj.fixed) {
                return false;
            }
            if (this.layoutData != obj.layoutData && (this.layoutData == null || !this.layoutData
                    .equals(obj.layoutData))) {
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
