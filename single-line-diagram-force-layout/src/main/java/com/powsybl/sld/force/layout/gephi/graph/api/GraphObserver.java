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
 * Observer over a graph to monitor changes and obtain the list of differences.
 * <p>
 * The graph observer is a mechanism used to monitor periodically changes made
 * to the graph. This scenario is common in multi-threaded application where a
 * thread is modifying the graph and one or multiple threads need to take action
 * when updates are made.
 * <p>
 * Graph observer users should periodically call the
 * <code>hasGraphChanged()</code> method to check the status. Each call resets
 * the observer so if the method returns true and the graph doesn't change after
 * that it will return false next time.
 * <p>
 * In addition of a boolean flag whether the graph has changed, an observer can
 * collect data about the differences such as nodes added or removed. Users
 * should call the <code>getDiff()</code> method after calling
 * <code>hasGraphChanged()</code> to obtain the diff.
 * <p>
 * Observers should be destroyed when not needed anymore. A new observer can be
 * obtained from the <code>GraphModel</code>.
 * <p>
 * Note that observer instances are not thread-safe and should not be called
 * from multiple threads simultaneously.
 *
 * @see GraphModel
 */
public interface GraphObserver {

    /**
     * Returns true if the graph has changed.
     *
     * @return true if changed, false otherwise
     */
    public boolean hasGraphChanged();

    /**
     * Gets the graph difference.
     *
     * @return the graph diff
     */
    public GraphDiff getDiff();

    /**
     * Gets the graph this observer is observing.
     *
     * @return the graph
     */
    public Graph getGraph();

    /**
     * Destroys this graph observer.
     */
    public void destroy();

    /**
     * Returns true if this observer has been destroyed.
     *
     * @return true if destroyed, false otherwise
     */
    public boolean isDestroyed();

    /**
     * Returns true if this observer has never got its
     * <em>hasGraphChanged()</em> method called.
     *
     * @return true if new observer, false otherwise
     */
    public boolean isNew();
}
