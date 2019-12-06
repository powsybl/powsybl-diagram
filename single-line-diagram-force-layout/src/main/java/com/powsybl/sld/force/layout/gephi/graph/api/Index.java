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

import java.util.Collection;

/**
 * An index is associated with each table and keeps track of each unique value
 * in indexed columns.
 * <p>
 *
 * @param <T> Element class
 */
public interface Index<T extends Element> {

    /**
     * Counts the elements with <em>value</em> in the given <em>column</em>.
     *
     * @param column the column to count values
     * @param value the value
     * @return the number of elements in the index with <em>value</em> in
     *         <em>column</em>, or zero if none
     */
    public int count(Column column, Object value);

    /**
     * Gets an Iterable of all elements in the index with <em>value</em> in the
     * given <em>column</em>.
     *
     * @param column the column to get values
     * @param value the value
     * @return an iterable with element with <em>value</em> in <em>column</em>,
     *         or null if value not found
     */
    public Iterable<T> get(Column column, Object value);

    /**
     * Returns all unique values in the given column.
     *
     * @param column the column to get values from
     * @return a collection of all unique values
     */
    public Collection values(Column column);

    /**
     * Counts the unique values in the given column.
     *
     * @param column the column to count values
     * @return the number of distinct values in <em>column</em>
     */
    public int countValues(Column column);

    /**
     * Counts the elements in the given column.
     *
     * @param column the column to count elements
     * @return the number of elements in <em>column</em>
     */
    public int countElements(Column column);

    /**
     * Returns whether the column is numeric and sortable, and therefore methods
     * {@link #getMinValue(Column)} and
     * {@link #getMaxValue(Column)} are available for the
     * column.
     *
     * @param column the column
     * @return true if the column is sortable, false otherwise
     */
    public boolean isSortable(Column column);

    /**
     * Returns the minimum value in the given column.
     * <p>
     * Only applies for numerical columns.
     *
     * @param column the column
     * @return the minimum value in the column
     */
    public Number getMinValue(Column column);

    /**
     * Returns the maximum value in the given column.
     * <p>
     * Only applies for numerical columns.
     *
     * @param column the column
     * @return the maximum value in the column
     */
    public Number getMaxValue(Column column);

    /**
     * Returns the element type of this index.
     *
     * @return the index element class
     */
    public Class<T> getIndexClass();

    /**
     * Returns the name of this index.
     *
     * @return the index name
     */
    public String getIndexName();
}
