/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.powsybl.sld.force.layout.gephi.graph.impl;

import com.powsybl.sld.force.layout.gephi.graph.api.Column;
import com.powsybl.sld.force.layout.gephi.graph.api.TableObserver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import com.powsybl.sld.force.layout.gephi.graph.api.Table;
import com.powsybl.sld.force.layout.gephi.graph.api.TableDiff;

import java.util.Collections;
import java.util.List;

public class TableObserverImpl implements TableObserver {

    protected final TableImpl table;
    protected boolean destroyed;
    // Config
    protected final boolean withDiff;
    // Hashcodes
    protected int tableHash;
    // Cache
    protected TableDiffImpl tableDiff;
    protected Column[] columnCache;
    protected int[] columnHashCache;

    public TableObserverImpl(TableImpl table) {
        this(table, false);
    }

    public TableObserverImpl(TableImpl table, boolean withDiff) {
        this.table = table;
        this.withDiff = withDiff;
        this.columnCache = table.toArray();
        refreshColumnsHash();

        tableHash = table.deepHashCode();
    }

    @Override
    public synchronized boolean hasTableChanged() {
        int newHash = table.deepHashCode();
        boolean changed = newHash != tableHash;
        tableHash = newHash;
        if (changed && withDiff) {
            refreshDiff();
        }
        return changed;
    }

    @Override
    public synchronized TableDiff getDiff() {
        if (!withDiff) {
            throw new RuntimeException("This observer doesn't compute diffs, set diff setting to true");
        }
        if (tableDiff == null) {
            throw new IllegalStateException(
                    "The hasGraphChanged() method should be called first and getDiff() only once then");
        }
        TableDiff diff = tableDiff;
        tableDiff = null;
        return diff;
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public void destroy() {
        table.destroyTableObserver(this);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    protected void destroyObserver() {
        tableHash = 0;
        columnCache = null;
        columnHashCache = null;
        tableDiff = null;
        destroyed = true;
    }

    protected void refreshDiff() {
        Column[] currentColumns = table.toArray();
        tableDiff = new TableDiffImpl(currentColumns, columnCache, columnHashCache);
        columnCache = currentColumns;
        refreshColumnsHash();
    }

    private void refreshColumnsHash() {
        this.columnHashCache = new int[this.columnCache.length];
        for (int i = 0; i < columnCache.length; i++) {
            columnHashCache[i] = ((ColumnImpl) columnCache[i]).deepHashCode();
        }
    }

    protected static final class TableDiffImpl implements TableDiff {

        protected final ObjectList<Column> addedColumns;
        protected final ObjectList<Column> removedColumns;
        protected final ObjectList<Column> modifiedColumns;

        public TableDiffImpl(Column[] currentColumns, Column[] columnCache, int[] columnHashCache) {
            addedColumns = new ObjectArrayList<Column>();
            removedColumns = new ObjectArrayList<Column>();
            modifiedColumns = new ObjectArrayList<Column>();

            for (Column currentColumn : currentColumns) {
                if (!isContained(currentColumn, columnCache)) {
                    addedColumns.add(currentColumn);
                }
            }

            for (int i = 0; i < columnCache.length; i++) {
                Column cachedColumn = columnCache[i];
                if (!isContained(cachedColumn, currentColumns)) {
                    removedColumns.add(cachedColumn);
                } else if (((ColumnImpl) cachedColumn).deepHashCode() != columnHashCache[i]) {
                    modifiedColumns.add(cachedColumn);
                }
            }
        }

        private boolean isContained(Column searchedColumn, Column[] columns) {
            for (Column column : columns) {
                if (searchedColumn.equals(column)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<Column> getAddedColumns() {
            if (!addedColumns.isEmpty()) {
                return Collections.unmodifiableList(addedColumns);
            }
            return Collections.emptyList();
        }

        @Override
        public List<Column> getRemovedColumns() {
            if (!removedColumns.isEmpty()) {
                return Collections.unmodifiableList(removedColumns);
            }
            return Collections.emptyList();
        }

        @Override
        public List<Column> getModifiedColumns() {
            if (!modifiedColumns.isEmpty()) {
                return Collections.unmodifiableList(modifiedColumns);
            }
            return Collections.emptyList();
        }
    }
}
