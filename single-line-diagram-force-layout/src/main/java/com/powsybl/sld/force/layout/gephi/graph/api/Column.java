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

/**
 * A column belongs to a table and represent a dimension in the data.
 * <p>
 * A column has primarily a unique identifier and a type, which both are set at
 * the creation time.
 *
 * @see Table
 */
public interface Column {

    /**
     * Returns the column id, a unique identifier.
     *
     * @return the column's identifier
     */
    public String getId();

    /**
     * Returns the column's integer index, which is the position of the column
     * in the store.
     *
     * @return the column's index
     */
    public int getIndex();

    /**
     * Returns the column's title.
     *
     * @return the column title
     */
    public String getTitle();

    /**
     * Returns the column's type.
     *
     * @return the column type
     */
    public Class getTypeClass();

    /**
     * Returns the column's data origin.
     *
     * @return the column origin
     */
    public Origin getOrigin();

    /**
     * Returns the estimator for this column
     *
     * @return the estimator, or null if not set
     */
    public Estimator getEstimator();

    /**
     * Returns the table this column belong to.
     *
     * @return the table
     */
    public Table getTable();

    /**
     * Returns true if this column is indexed.
     *
     * @return true if indexed, false otherwise
     */
    public boolean isIndexed();

    /**
     * Returns true if this column has an array type.
     *
     * @return true if array type, false otherwise
     */
    public boolean isArray();

    /**
     * Returns true if this column has a dynamic type.
     *
     * @return true if dynamic type, false otherwise
     */
    public boolean isDynamic();

    /**
     * Returns true if this column has a number type.
     *
     * @return true if number type, false otherwise
     */
    public boolean isNumber();

    /**
     * Returns true if this column is a property.
     * <p>
     * This is equivalent to test if the column's origin is
     * <em>Origin.PROPERTY</em>
     *
     * @return true if property, false otherwise
     */
    public boolean isProperty();

    /**
     * Returns true if this column is read-only.
     *
     * @return true if read-only, false otherwise
     */
    public boolean isReadOnly();

    /**
     * Returns the column's default value, or null if not set.
     *
     * @return the default value, or null
     */
    public Object getDefaultValue();

    /**
     * Sets the estimator for this column.
     *
     * @param estimator the estimator to set
     */
    public void setEstimator(Estimator estimator);

    /**
     * Create a new column observer.
     *
     * @param withDiff true if column observer should provide column differences
     * @return the column observer
     */
    public ColumnObserver createColumnObserver(boolean withDiff);
}
