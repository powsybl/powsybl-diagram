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
import com.powsybl.sld.force.layout.gephi.graph.api.Estimator;
import com.powsybl.sld.force.layout.gephi.graph.api.Origin;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeMap;
import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Table;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeSet;

import java.util.ArrayList;
import java.util.List;

public class ColumnImpl implements Column {

    // Attributes
    protected final TableImpl table;
    protected final String id;
    protected final Class typeClass;
    protected final String title;
    protected final Object defaultValue;
    protected final Origin origin;
    protected final ColumnVersion version;
    protected final boolean indexed;
    protected final boolean dynamic;
    protected final boolean readOnly;
    protected Estimator estimator;
    // Observers
    protected final List<ColumnObserverImpl> observers;
    // Store Id
    protected int storeId = ColumnStore.NULL_ID;

    public ColumnImpl(TableImpl table, String id, Class typeClassParam, String title, Object defaultValue, Origin origin, boolean indexed, boolean readOnly) {
        Class typeClass = typeClassParam;
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("The column ID can't be null or empty");
        }
        if (typeClass == null) {
            throw new NullPointerException("The type class can't be null");
        }
        if (origin == null) {
            throw new NullPointerException("The origin can't be null");
        }

        typeClass = AttributeUtils.getStandardizedType(typeClass);
        this.table = table;
        this.id = id.toLowerCase(); // Make sure column has lowercase id from
                                   // wherever it's created
        this.typeClass = typeClass;
        this.title = title;
        this.defaultValue = defaultValue;
        this.version = new ColumnVersion(this);
        this.origin = origin;
        this.indexed = indexed;
        this.readOnly = readOnly;
        this.dynamic = TimeMap.class.isAssignableFrom(typeClass) || TimeSet.class.isAssignableFrom(typeClass);
        this.observers = GraphStoreConfiguration.ENABLE_OBSERVERS ? new ArrayList<ColumnObserverImpl>() : null;
        this.estimator = this.dynamic ? Estimator.FIRST : null;
    }

    public ColumnImpl(String id, Class typeClass, String title, Object defaultValue, Origin origin, boolean indexed, boolean readOnly) {
        this(null, id, typeClass, title, defaultValue, origin, indexed, readOnly);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getIndex() {
        return storeId;
    }

    @Override
    public Class getTypeClass() {
        return typeClass;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public boolean isIndexed() {
        return indexed;
    }

    @Override
    public boolean isArray() {
        return typeClass.isArray();
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean isNumber() {
        return AttributeUtils.isNumberType(typeClass);
    }

    @Override
    public boolean isProperty() {
        return origin.equals(Origin.PROPERTY);
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    @Override
    public String toString() {
        return title + " (" + typeClass.toString() + ")";
    }

    @Override
    public Estimator getEstimator() {
        return estimator;
    }

    @Override
    public void setEstimator(Estimator estimator) {
        if (!dynamic) {
            throw new IllegalStateException("The column must have a dynamic type");
        }
        if (TimeMap.class.isAssignableFrom(typeClass)) {
            TimeMap vs = null;
            try {
                vs = (TimeMap) typeClass.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            if (!vs.isSupported(estimator)) {
                throw new IllegalArgumentException("The column doesnt't support this estimator");
            }
            this.estimator = estimator;
        }
    }

    @Override
    public ColumnObserverImpl createColumnObserver(boolean withDiff) {
        if (observers != null) {
            ColumnObserverImpl observer = new ColumnObserverImpl(table.store.graphStore, this, withDiff);
            synchronized (observers) {
                observers.add(observer);
            }

            return observer;
        }
        return null;
    }

    protected void destroyColumnObserver(ColumnObserverImpl observer) {
        if (observers != null) {
            synchronized (observers) {
                observers.remove(observer);
            }
            observer.destroyObserver();
        }
    }

    protected void incrementVersion(AbstractElementImpl element) {
        version.incrementAndGetVersion();
        if (observers != null && !observers.isEmpty()) {
            synchronized (observers) {
                for (ColumnObserverImpl observer : observers) {
                    observer.setElement(element);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Column) {
            ColumnImpl o = (ColumnImpl) obj;
            return id.equals(o.id) && o.typeClass == typeClass;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.typeClass != null ? this.typeClass.hashCode() : 0);
        return hash;
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 31 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 31 * hash + (this.typeClass != null ? this.typeClass.hashCode() : 0);
        hash = 31 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 31 * hash + (this.defaultValue != null ? this.defaultValue.hashCode() : 0);
        hash = 31 * hash + (this.origin != null ? this.origin.hashCode() : 0);
        hash = 31 * hash + (this.estimator != null ? this.estimator.hashCode() : 0);
        return hash;
    }

    public boolean deepEquals(ColumnImpl col) {
        if (this == col) {
            return true;
        }
        if (col == null) {
            return false;
        }
        if ((this.id == null) ? (col.id != null) : !this.id.equals(col.id)) {
            return false;
        }
        if ((this.title == null) ? (col.title != null) : !this.title.equals(col.title)) {
            return false;
        }
        if (this.typeClass != col.typeClass && (this.typeClass == null || !this.typeClass.equals(col.typeClass))) {
            return false;
        }
        if (this.defaultValue != col.defaultValue && (this.defaultValue == null || !this.defaultValue
                .equals(col.defaultValue))) {
            return false;
        }
        if (this.origin != col.origin) {
            return false;
        }
        if (this.estimator != col.estimator) {
            return false;
        }
        return true;
    }

}
