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
 * Observer over a table to monitor changes.
 * <p>
 * The table observer is a mechanism used to monitor periodically changes made
 * to the table. This scenario is common in multi-threaded application where a
 * thread is modifying the table and one or multiple threads need to take action
 * when updates are made.
 * <p>
 * Table observer users should periodically call the
 * <code>hasTableChanged()</code> method to check the status. Each call resets
 * the observer so if the method returns true and the table doesn't change after
 * that it will return false next time.
 * <p>
 * Observers should be destroyed when not needed anymore. A new observer can be
 * obtained from the <code>Table</code> instance.
 *
 * @see Table
 */
public interface TableObserver {

    /**
     * Returns true if the table has changed.
     *
     * @return true if changed, false otherwise
     */
    public boolean hasTableChanged();

    /**
     * Gets the table this observer belongs to.
     *
     * @return the table
     */
    public Table getTable();

    /**
     * Gets the table difference.
     *
     * @return the table diff
     */
    public TableDiff getDiff();

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
