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

import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalByteMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalCharMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampStringMap;
import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Column;
import com.powsybl.sld.force.layout.gephi.graph.api.ColumnIterable;
import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.Estimator;
import com.powsybl.sld.force.layout.gephi.graph.api.GraphView;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeRepresentation;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalBooleanMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalFloatMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalIntegerMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalLongMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractIntervalMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalShortMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalStringMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampBooleanMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampByteMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampCharMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampFloatMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampIntegerMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampLongMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractTimestampMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampShortMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractElementImpl implements Element {

    // Reference to store
    protected final GraphStore graphStore;
    // Attributes
    protected Object[] attributes;

    public AbstractElementImpl(Object id, GraphStore graphStore) {
        if (id == null) {
            throw new NullPointerException();
        }
        this.graphStore = graphStore;
    }

    abstract ColumnStore getColumnStore();

    abstract AbstractTimeIndexStore getTimeIndexStore();

    abstract boolean isValid();

    @Override
    public Object getId() {
        return attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX];
    }

    @Override
    public String getLabel() {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_LABEL && attributes.length > GraphStoreConfiguration.ELEMENT_LABEL_INDEX) {
            return (String) attributes[GraphStoreConfiguration.ELEMENT_LABEL_INDEX];
        }
        return null;
    }

    @Override
    public Object getAttribute(String key) {
        return getAttribute(checkColumnExists(key));
    }

    @Override
    public Object getAttribute(Column column) {
        checkColumn(column);

        int index = column.getIndex();
        Object res = null;
        synchronized (this) {
            if (index < attributes.length) {
                res = attributes[index];
            }
        }

        if (res == null) {
            return column.getDefaultValue();
        }
        return res;
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        return getAttribute(checkColumnExists(key), timestamp);
    }

    @Override
    public Object getAttribute(Column column, double timestamp) {
        checkTimeRepresentationTimestamp();
        checkDouble(timestamp);
        return getTimeAttribute(column, timestamp);
    }

    @Override
    public Object getAttribute(String key, Interval interval) {
        return getAttribute(checkColumnExists(key), interval);
    }

    @Override
    public Object getAttribute(Column column, Interval interval) {
        checkTimeRepresentationInterval();
        return getTimeAttribute(column, interval);
    }

    private Object getTimeAttribute(Column column, Object timeObject) {
        checkColumn(column);
        checkColumnDynamic(column);

        int index = column.getIndex();
        synchronized (this) {
            TimeMap dynamicValue = null;
            if (index < attributes.length) {
                dynamicValue = (TimeMap) attributes[index];
            }
            if (dynamicValue != null) {
                return dynamicValue.get(timeObject, column.getDefaultValue());
            }
        }
        return null;
    }

    @Override
    public Object getAttribute(String key, GraphView view) {
        return getAttribute(checkColumnExists(key), view);
    }

    @Override
    public Object getAttribute(Column column, GraphView view) {
        checkColumn(column);

        if (!column.isDynamic()) {
            return getAttribute(column);
        } else {
            Interval interval = view.getTimeInterval();
            checkViewExist((GraphView) view);

            int index = column.getIndex();
            synchronized (this) {
                TimeMap dynamicValue = null;
                if (index < attributes.length) {
                    dynamicValue = (TimeMap) attributes[index];
                }
                if (dynamicValue != null && !dynamicValue.isEmpty()) {
                    Estimator estimator = column.getEstimator();
                    if (estimator == null) {
                        estimator = GraphStoreConfiguration.DEFAULT_ESTIMATOR;
                    }
                    return dynamicValue.get(interval, estimator);
                }
            }
        }

        return null;
    }

    @Override
    public Object[] getAttributes() {
        return attributes;
    }

    @Override
    public Set<String> getAttributeKeys() {
        return getColumnStore().getColumnKeys();
    }

    @Override
    public ColumnIterable getAttributeColumns() {
        return getColumnStore();
    }

    @Override
    public Object removeAttribute(String key) {
        return removeAttribute(checkColumnExists(key));
    }

    @Override
    public Object removeAttribute(Column column) {
        checkColumn(column);
        checkReadOnlyColumn(column);

        int index = column.getIndex();
        Object oldValue = null;
        synchronized (this) {
            if (index >= attributes.length) {
                Object[] newArray = new Object[index + 1];
                System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                attributes = newArray;
            } else {
                oldValue = attributes[index];
            }

            attributes[index] = null;
        }

        if (isValid()) {
            ColumnStore columnStore = getColumnStore();
            ColumnImpl columnImpl = (ColumnImpl) column;
            if (columnImpl.isDynamic() && oldValue != null) {
                AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
                if (timeIndexStore != null) {
                    if (TimeMap.class.isAssignableFrom(columnImpl.getTypeClass())) {
                        timeIndexStore.remove((TimeMap) oldValue);
                    } else if (TimeSet.class.isAssignableFrom(columnImpl.getTypeClass())) {
                        timeIndexStore.remove((TimeSet) oldValue);
                    }
                }
            } else if (column.isIndexed() && columnStore != null && isValid()) {
                columnStore.indexStore.set(column, oldValue, column.getDefaultValue(), this);
            }
            columnImpl.incrementVersion(this);
        }
        return oldValue;
    }

    @Override
    public Object removeAttribute(Column column, double timestamp) {
        checkTimeRepresentationTimestamp();
        checkDouble(timestamp);
        return removeTimeAttribute(column, timestamp);
    }

    @Override
    public Object removeAttribute(String key, double timestamp) {
        return removeAttribute(checkColumnExists(key), timestamp);
    }

    @Override
    public Object removeAttribute(Column column, Interval interval) {
        checkTimeRepresentationInterval();
        return removeTimeAttribute(column, interval);
    }

    @Override
    public Object removeAttribute(String key, Interval interval) {
        return removeAttribute(checkColumnExists(key), interval);
    }

    private Object removeTimeAttribute(Column column, Object timeObject) {
        checkColumn(column);
        checkColumnDynamic(column);
        checkReadOnlyColumn(column);

        int index = column.getIndex();
        Object oldValue = null;
        boolean res = false;
        synchronized (this) {
            TimeMap dynamicValue = (TimeMap) attributes[index];
            if (dynamicValue != null) {
                oldValue = dynamicValue.get(timeObject, null);

                res = dynamicValue.remove(timeObject);
            }
        }

        if (res && isValid()) {
            AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
            if (timeIndexStore != null) {
                timeIndexStore.remove(timeObject);
            }
            ((ColumnImpl) column).incrementVersion(this);
        }
        return oldValue;
    }

    @Override
    public void setLabel(String label) {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_LABEL) {
            int index = GraphStoreConfiguration.ELEMENT_LABEL_INDEX;
            synchronized (this) {
                if (index >= attributes.length) {
                    Object[] newArray = new Object[index + 1];
                    System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                    attributes = newArray;
                }
                attributes[index] = label;
            }
            ColumnStore columnStore = getColumnStore();
            if (columnStore != null && isValid()) {
                Column col = columnStore.getColumnByIndex(index);
                ((ColumnImpl) col).incrementVersion(this);
            }
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        setAttribute(checkColumnExists(key), value);
    }

    @Override
    public void setAttribute(Column column, Object valueParam) {
        checkColumn(column);
        checkReadOnlyColumn(column);

        Object value = AttributeUtils.standardizeValue(valueParam);
        checkType(column, value);

        int index = column.getIndex();
        ColumnStore columnStore = getColumnStore();
        Object oldValue = null;

        synchronized (this) {
            if (index >= attributes.length) {
                Object[] newArray = new Object[index + 1];
                System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                attributes = newArray;
            } else {
                oldValue = attributes[index];
            }

            if (column.isDynamic() && isValid()) {
                AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
                if (timeIndexStore != null) {
                    if (TimeMap.class.isAssignableFrom(column.getTypeClass())) {
                        if (oldValue != null && oldValue instanceof TimeMap) {
                            timeIndexStore.remove((TimeMap) oldValue);
                        }
                        if (value != null) {
                            timeIndexStore.add((TimeMap) value);
                        }
                    } else if (TimeSet.class.isAssignableFrom(column.getTypeClass()) && column.getIndex() == GraphStoreConfiguration.ELEMENT_TIMESET_INDEX) {
                        if (oldValue != null) {
                            timeIndexStore.remove((TimeSet) oldValue);
                        }
                        if (value != null) {
                            timeIndexStore.add((TimeSet) value);
                        }
                    }
                }
            } else if (column.isIndexed() && columnStore != null && isValid()) {
                value = columnStore.indexStore.set(column, oldValue, value, this);
            }
            attributes[index] = value;
        }
        if (isValid()) {
            ((ColumnImpl) column).incrementVersion(this);
        }
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        setAttribute(checkColumnExists(key), value, timestamp);
    }

    @Override
    public void setAttribute(Column column, Object value, double timestamp) {
        checkTimeRepresentationTimestamp();
        checkDouble(timestamp);
        setTimeAttribute(column, value, timestamp);
    }

    @Override
    public void setAttribute(String key, Object value, Interval interval) {
        setAttribute(checkColumnExists(key), value, interval);
    }

    @Override
    public void setAttribute(Column column, Object value, Interval interval) {
        checkTimeRepresentationInterval();
        setTimeAttribute(column, value, interval);
    }

    private void setTimeAttribute(Column column, Object value, Object timeObject) {
        checkColumn(column);
        checkColumnDynamic(column);
        checkReadOnlyColumn(column);
        checkType(column, value);

        int index = column.getIndex();
        Object oldValue = null;
        boolean res;
        synchronized (this) {
            if (index >= attributes.length) {
                Object[] newArray = new Object[index + 1];
                System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                attributes = newArray;
            } else {
                oldValue = attributes[index];
            }

            TimeMap dynamicValue = null;
            if (oldValue == null) {
                try {
                    attributes[index] = dynamicValue = (TimeMap) column.getTypeClass().newInstance();
                } catch (InstantiationException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                dynamicValue = (TimeMap) oldValue;
            }

            res = dynamicValue.put(timeObject, value);
        }

        if (res && isValid()) {
            AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
            if (timeIndexStore != null) {
                timeIndexStore.add(timeObject);
            }
        }
        if (isValid()) {
            ((ColumnImpl) column).incrementVersion(this);
        }
    }

    @Override
    public boolean addTimestamp(double timestamp) {
        checkDouble(timestamp);
        checkTimeRepresentationTimestamp();
        return addTime(timestamp);
    }

    @Override
    public boolean addInterval(Interval interval) {
        checkTimeRepresentationInterval();
        return addTime(interval);
    }

    private boolean addTime(Object timeObject) {
        checkEnabledTimeSet();

        boolean res;
        synchronized (this) {
            TimeSet timeSet = getTimeSet();
            if (timeSet == null) {
                TimeRepresentation timeRepresentation = getTimeRepresentation();
                switch (timeRepresentation) {
                    case INTERVAL:
                        timeSet = new IntervalSet();
                        break;
                    case TIMESTAMP:
                        timeSet = new TimestampSet();
                        break;
                    default:
                        throw new RuntimeException("Unrecognized time representation");
                }
                int index = GraphStoreConfiguration.ELEMENT_TIMESET_INDEX;
                if (index >= attributes.length) {
                    Object[] newArray = new Object[index + 1];
                    System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                    attributes = newArray;
                }
                attributes[index] = timeSet;
            }
            res = timeSet.add(timeObject);
        }

        if (res && isValid()) {
            AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
            if (timeIndexStore != null) {
                timeIndexStore.add(timeObject, this);
            }
            ColumnStore columnStore = getColumnStore();
            if (columnStore != null) {
                Column column = columnStore.getColumnByIndex(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
                ((ColumnImpl) column).incrementVersion(this);
            }
        }

        return res;
    }

    @Override
    public boolean removeTimestamp(double timestamp) {
        checkDouble(timestamp);
        checkTimeRepresentationTimestamp();
        return removeTime(timestamp);
    }

    @Override
    public boolean removeInterval(Interval interval) {
        checkTimeRepresentationInterval();
        return removeTime(interval);
    }

    private boolean removeTime(Object timeObject) {
        checkEnabledTimeSet();

        boolean res = false;
        synchronized (this) {
            TimeSet timeSet = getTimeSet();
            if (timeSet != null) {
                res = timeSet.remove(timeObject);
            }
        }

        if (res && isValid()) {
            AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
            if (timeIndexStore != null) {
                timeIndexStore.remove(timeObject, this);
            }
            ColumnStore columnStore = getColumnStore();
            if (columnStore != null) {
                Column column = columnStore.getColumnByIndex(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
                ((ColumnImpl) column).incrementVersion(this);
            }
        }

        return res;
    }

    @Override
    public double[] getTimestamps() {
        checkTimeRepresentationTimestamp();
        Object res = getTimeSetArray();
        if (res == null) {
            return new double[0];
        }
        return (double[]) res;
    }

    @Override
    public Interval[] getIntervals() {
        checkTimeRepresentationInterval();
        Object res = getTimeSetArray();
        if (res == null) {
            return new Interval[0];
        }
        return (Interval[]) res;
    }

    private Object getTimeSetArray() {
        checkEnabledTimeSet();

        synchronized (this) {
            TimeSet timeSet = getTimeSet();
            if (timeSet != null) {
                return timeSet.toPrimitiveArray();
            }
        }
        return null;
    }

    @Override
    public boolean hasTimestamp(double timestamp) {
        checkTimeRepresentationTimestamp();
        return hasTime(timestamp);
    }

    @Override
    public boolean hasInterval(Interval interval) {
        checkTimeRepresentationInterval();
        return hasTime(interval);
    }

    private boolean hasTime(Object timeObject) {
        checkEnabledTimeSet();

        synchronized (this) {
            TimeSet timeSet = getTimeSet();
            if (timeSet != null) {
                return timeSet.contains(timeObject);
            }
        }
        return false;
    }

    @Override
    public Iterable<Map.Entry> getAttributes(Column column) {
        checkColumn(column);
        checkColumnDynamic(column);

        int index = column.getIndex();
        TimeMap dynamicValue = null;
        synchronized (this) {
            if (index < attributes.length) {
                dynamicValue = (TimeMap) attributes[index];
            }
            if (dynamicValue != null) {
                Object[] values = dynamicValue.toValuesArray();
                if (dynamicValue instanceof AbstractTimestampMap) {
                    return new TimeAttributeIterable(((AbstractTimestampMap) dynamicValue).getTimestamps(), values);
                } else if (dynamicValue instanceof AbstractIntervalMap) {
                    return new TimeAttributeIterable(((AbstractIntervalMap) dynamicValue).toKeysArray(), values);
                }
            }

        }
        return TimeAttributeIterable.EMPTY_ITERABLE;
    }

    private TimeSet getTimeSet() {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET && GraphStoreConfiguration.ELEMENT_TIMESET_INDEX < attributes.length) {
            return (TimeSet) attributes[GraphStoreConfiguration.ELEMENT_TIMESET_INDEX];
        }
        return null;
    }

    protected void indexAttributes() {
        synchronized (this) {
            ColumnStore columnStore = getColumnStore();
            if (columnStore != null) {
                columnStore.indexStore.index(this);
            }

            AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
            if (timeIndexStore != null) {
                timeIndexStore.index(this);
            }
        }
    }

    @Override
    public void clearAttributes() {
        synchronized (this) {
            if (isValid()) {
                ColumnStore columnStore = getColumnStore();
                if (columnStore != null) {
                    columnStore.indexStore.clear(this);
                }
                AbstractTimeIndexStore timeIndexStore = getTimeIndexStore();
                if (timeIndexStore != null) {
                    timeIndexStore.clear(this);
                }
            }
            TimeSet timeSet = getTimeSet();
            if (timeSet != null) {
                timeSet.clear();
            }

            Object[] newAttributes = new Object[GraphStoreConfiguration.ELEMENT_ID_INDEX + 1];
            newAttributes[GraphStoreConfiguration.ELEMENT_ID_INDEX] = attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX];
            attributes = newAttributes;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.getId().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractElementImpl other = (AbstractElementImpl) obj;
        if (!this.getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    protected GraphStore getGraphStore() {
        return graphStore;
    }

    protected void checkTimeRepresentationTimestamp() {
        if (!getTimeRepresentation().equals(TimeRepresentation.TIMESTAMP)) {
            throw new RuntimeException("Can't use timestamps as the configuration is set to " + getTimeRepresentation());
        }
    }

    protected void checkTimeRepresentationInterval() {
        if (!getTimeRepresentation().equals(TimeRepresentation.INTERVAL)) {
            throw new RuntimeException("Can't use intervals as the configuration is set to " + getTimeRepresentation());
        }
    }

    void checkEnabledTimeSet() {
        if (!GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET) {
            throw new RuntimeException("Can't call timestamp or intervals methods if they are disabled");
        }
    }

    void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can't be NaN or infinity");
        }
    }

    Column checkColumnExists(String key) {
        Column col = getColumnStore().getColumn(key);
        if (col == null) {
            throw new IllegalArgumentException("The column '" + key + "' is not found");
        }
        return col;
    }

    void checkColumn(Column column) {
        if (column.getIndex() == ColumnStore.NULL_ID) {
            throw new IllegalArgumentException("The column does not exist");
        }
        ColumnStore columnStore = getColumnStore();
        if (columnStore != null && columnStore.getColumnByIndex(column.getIndex()) != column) {
            throw new IllegalArgumentException("The column does not belong to the right column store");
        }
    }

    void checkReadOnlyColumn(Column column) {
        if (column.isReadOnly()) {
            throw new RuntimeException("Can't modify the read-only '" + column.getId() + "' column");
        }
    }

    void checkColumnDynamic(Column column) {
        if (!((ColumnImpl) column).isDynamic()) {
            throw new IllegalArgumentException("The column is not dynamic");
        }
    }

    void checkType(Column column, Object value) {
        if (value != null) {
            Class typeClass = column.getTypeClass();
            if (AbstractTimestampMap.class.isAssignableFrom(typeClass)) {
                if ((value instanceof Double && (!typeClass.equals(TimestampDoubleMap.class))) || (value instanceof Float && !typeClass
                        .equals(TimestampFloatMap.class)) || (value instanceof Boolean && !typeClass
                        .equals(TimestampBooleanMap.class)) || (value instanceof Integer && !typeClass
                        .equals(TimestampIntegerMap.class)) || (value instanceof Long && !typeClass
                        .equals(TimestampLongMap.class)) || (value instanceof Short && !typeClass
                        .equals(TimestampShortMap.class)) || (value instanceof Byte && !typeClass
                        .equals(TimestampByteMap.class)) || (value instanceof String && !typeClass
                        .equals(TimestampStringMap.class)) || (value instanceof Character && !typeClass
                        .equals(TimestampCharMap.class))) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the dynamic type (" + typeClass.getName() + ")");
                }
            } else if (AbstractIntervalMap.class.isAssignableFrom(typeClass)) {
                if ((value instanceof Double && (!typeClass.equals(IntervalDoubleMap.class))) || (value instanceof Float && !typeClass
                        .equals(IntervalFloatMap.class)) || (value instanceof Boolean && !typeClass
                        .equals(IntervalBooleanMap.class)) || (value instanceof Integer && !typeClass
                        .equals(IntervalIntegerMap.class)) || (value instanceof Long && !typeClass
                        .equals(IntervalLongMap.class)) || (value instanceof Short && !typeClass
                        .equals(IntervalShortMap.class)) || (value instanceof Byte && !typeClass
                        .equals(IntervalByteMap.class)) || (value instanceof String && !typeClass
                        .equals(IntervalStringMap.class)) || (value instanceof Character && !typeClass
                        .equals(IntervalCharMap.class))) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the dynamic type (" + typeClass.getName() + ")");
                }
            } else if (List.class.isAssignableFrom(typeClass)) {
                if (!(value instanceof List)) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the list type (" + typeClass.getName() + ")");
                }
            } else if (Set.class.isAssignableFrom(typeClass)) {
                if (!(value instanceof Set)) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the set type (" + typeClass.getName() + ")");
                }
            } else if (Map.class.isAssignableFrom(typeClass)) {
                if (!(value instanceof Map)) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the map type (" + typeClass.getName() + ")");
                }
            } else if (!value.getClass().equals(typeClass)) {
                throw new IllegalArgumentException(
                        "The object class does not match with the column type (" + typeClass.getName() + ")");
            }
        }
    }

    void checkViewExist(final GraphView view) {
        graphStore.viewStore.checkNonNullViewObject(view);
        if (!view.isMainView()) {
            graphStore.viewStore.checkViewExist((GraphViewImpl) view);
        }
    }

    TimeRepresentation getTimeRepresentation() {
        if (graphStore != null) {
            return graphStore.configuration.getTimeRepresentation();
        }
        return GraphStoreConfiguration.DEFAULT_TIME_REPRESENTATION;
    }
}
