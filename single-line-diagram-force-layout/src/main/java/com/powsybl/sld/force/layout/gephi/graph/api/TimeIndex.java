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
 * Holds an index based on time attributes.
 *
 * @param <T> Element
 */
public interface TimeIndex<T extends Element> {

    /**
     * Returns the minimum timestamp in this index.
     *
     * @return the minimum timestamp, or -inf if not defined
     */
    public double getMinTimestamp();

    /**
     * Returns the maximum timestamp in this index.
     *
     * @return the maximum timestamp, or +inf if not defined
     */
    public double getMaxTimestamp();

    /**
     * Returns all elements at the given timestamp.
     *
     * @param timestamp timestamp
     * @return elements at this timestamp
     */
    public ElementIterable<T> get(double timestamp);

    /**
     * Returns all elements between the given interval (bounds included).
     *
     * @param interval interval
     * @return elements at this interval
     */
    public ElementIterable<T> get(Interval interval);
}
