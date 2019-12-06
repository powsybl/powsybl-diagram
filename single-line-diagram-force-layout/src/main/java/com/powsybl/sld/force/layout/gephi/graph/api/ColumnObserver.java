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
 * Observer over a column to monitor changes in the attributes values.
 * <p>
 * Column observer can be used to periodically monitor changes made to a column.
 * This scenario is common is multi-threaded applications where a thread is
 * responsible to take action when something has changed in the column's data.
 * <p>
 * Column observer users should periodically call the
 * <code>hasColumnChanged()</code> method to check the status. Each call resets
 * the observer so if the method returns true and the table doesn't change after
 * that it will return false next time.
 * <p>
 * This observer monitors all the rows for this column and consider something
 * has changed when an element's value for this column has been changed.
 * <p>
 * Observers should be destroyed when not needed anymore. A new observer can be
 * obtained from the <code>Column</code>.
 *
 * @see Column
 */
public interface ColumnObserver {

    /**
     * Returns true if the column has changed.
     *
     * @return true if changed, false otherwise
     */
    public boolean hasColumnChanged();

    /**
     * Gets the column difference.
     *
     * @return the column diff
     */
    public ColumnDiff getDiff();

    /**
     * Gets the column this observer belongs to.
     *
     * @return the table
     */
    public Column getColumn();

    /**
     * Destroys this observer.
     */
    public void destroy();

    /**
     * Returns true if this observer has been destroyed.
     *
     * @return true if destroyed, false otherwise
     */
    public boolean isDestroyed();
}
