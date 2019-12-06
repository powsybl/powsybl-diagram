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
 * Different time representations.
 * <p>
 * Both the elements (i.e nodes and edges) existence in time and the attributes'
 * values in time can be represented in two different ways: using timestamps or
 * using intervals. They can be mixed thought and therefore need to be
 * configured by the user.
 * <p>
 * Each representation has its advantages and disadvantages. For instance,
 * timestamps are great when observations are made at fixed periods. On the
 * other hand, intervals are great when the time is arbitrary and elements or
 * attributes have long continuous existence.
 *
 * @see Configuration
 */
public enum TimeRepresentation {
    /**
     * Timestamp representation (fixed).
     * <p>
     * Time is represented using timestamps. Timestamps are single value and
     * represent a single moment in time.
     */
    TIMESTAMP,
    /**
     * Interval representation (continuous).
     * <p>
     * Time is represented using intervals, with a beginning and an end.
     * Intervals are always included on both bounds but allows an infinite
     * bound.
     */
    INTERVAL;
}
