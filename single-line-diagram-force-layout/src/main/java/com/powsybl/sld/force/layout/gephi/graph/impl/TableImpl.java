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

import com.powsybl.sld.force.layout.gephi.graph.api.TableObserver;
import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Column;
import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.Graph;
import com.powsybl.sld.force.layout.gephi.graph.api.Origin;
import com.powsybl.sld.force.layout.gephi.graph.api.Table;

import java.util.Iterator;
import java.util.List;

public class TableImpl<T extends Element> implements Table {

    // Store
    protected final ColumnStore<T> store;

    public TableImpl(Class<T> elementType, boolean indexed) {
        this(null, elementType, indexed);
    }

    public TableImpl(GraphStore graphStore, Class<T> elementType, boolean indexed) {
        store = new ColumnStore<T>(graphStore, elementType, indexed);
    }

    @Override
    public Column addColumn(String id, Class type) {
        return addColumn(id, null, type, Origin.DATA, null, true);
    }

    @Override
    public Column addColumn(String id, Class type, Origin origin) {
        return addColumn(id, null, type, origin, null, true);
    }

    @Override
    public Column addColumn(String id, String title, Class type, Object defaultValue) {
        return addColumn(id, title, type, Origin.DATA, defaultValue, true);
    }

    @Override
    public Column addColumn(String idParam, String titleParam, Class typeParam, Origin origin, Object defaultValueParam, boolean indexedParam) {
        checkValidId(idParam);
        checkSupportedTypes(typeParam);
        checkDefaultValue(defaultValueParam, typeParam);

        Class type = AttributeUtils.getStandardizedType(typeParam);
        Object defaultValue = defaultValueParam;
        if (defaultValue != null) {
            defaultValue = AttributeUtils.standardizeValue(defaultValue);
        }

        String title = titleParam;
        if (title == null || title.isEmpty()) {
            title = idParam;
        }

        String id = idParam.toLowerCase();

        boolean indexed = indexedParam;
        if (indexed && store.indexStore == null) {
            indexed = false;
        }

        ColumnImpl column = new ColumnImpl(this, id, type, title, defaultValue, origin, indexed, false);
        store.addColumn(column);

        return column;
    }

    @Override
    public int countColumns() {
        return store.size();
    }

    @Override
    public Iterator<Column> iterator() {
        return store.iterator();
    }

    @Override
    public void doBreak() {
        store.doBreak();
    }

    @Override
    public Column[] toArray() {
        return store.toArray();
    }

    @Override
    public List<Column> toList() {
        return store.toList();
    }

    @Override
    public Column getColumn(int index) {
        return store.getColumnByIndex(index);
    }

    @Override
    public Column getColumn(String id) {
        return store.getColumn(id.toLowerCase());
    }

    @Override
    public boolean hasColumn(String id) {
        return store.hasColumn(id.toLowerCase());
    }

    @Override
    public void removeColumn(Column column) {
        store.removeColumn(column);
    }

    @Override
    public void removeColumn(String id) {
        store.removeColumn(id.toLowerCase());
    }

    @Override
    public TableObserver createTableObserver(boolean withDiff) {
        return store.createTableObserver(this, withDiff);
    }

    @Override
    public Class getElementClass() {
        return store.elementType;
    }

    @Override
    public Graph getGraph() {
        return store.graphStore;
    }

    public void destroyTableObserver(TableObserver observer) {
        checkableTableObserver(observer);

        store.destroyTablesObserver((TableObserverImpl) observer);
    }

    public boolean deepEquals(TableImpl<T> obj) {
        if (obj == null) {
            return false;
        }
        return !(this.store != obj.store && (this.store == null || !this.store.deepEquals(obj.store)));
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 71 * hash + (this.store != null ? this.store.deepHashCode() : 0);
        return hash;
    }

    private void checkValidId(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("The column id can't be empty.");
        }
        if (store.hasColumn(id.toLowerCase())) {
            throw new IllegalArgumentException("The column '" + id + "' already existing in the table");
        }
    }

    private void checkSupportedTypes(Class type) {
        if (!AttributeUtils.isSupported(type)) {
            throw new IllegalArgumentException("Unknown type " + type.getName());
        }
    }

    private void checkDefaultValue(Object defaultValue, Class type) {
        if (defaultValue != null) {
            if (defaultValue.getClass() != type) {
                throw new IllegalArgumentException("The default value type cannot be cast to the type");
            }
        }
    }

    private void checkableTableObserver(TableObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (!(observer instanceof TableObserverImpl)) {
            throw new ClassCastException("The observer should be a TableObserverImpl instance");
        }
        if (((TableObserverImpl) observer).table != this) {
            throw new RuntimeException("The observer doesn't belong to this table");
        }
    }
}
