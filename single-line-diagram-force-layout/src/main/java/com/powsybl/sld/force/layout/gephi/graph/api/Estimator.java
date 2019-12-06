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
 * Estimators specify the strategy to merge attribute values over time.
 * <p>
 * Estimators are associated with actions that require to transform a sorted set
 * of values over time into a single value.
 */
public enum Estimator {

    /**
     * Average value.
     */
    AVERAGE,
    /**
     * Median value.
     */
    MEDIAN,
    /**
     * Minimum value.
     */
    MIN,
    /**
     * Maximum value.
     */
    MAX,
    /**
     * First value.
     */
    FIRST,
    /**
     * Last value.
     */
    LAST;

    /**
     * Returns true if this estimator is equals to <code>estimator</code>.
     *
     * @param estimator estimator to test equality
     * @return true if <code>estimator</code> is equal to this instance
     */
    public boolean is(Estimator estimator) {
        return estimator.equals(this);
    }

    /**
     * Returns true if this estimator is any of the given
     * <code>estimators</code>.
     *
     * @param estimators estimators to test equality
     * @return true if <code>estimators</code> contains this estimator
     */
    public boolean is(Estimator... estimators) {
        for (Estimator e : estimators) {
            if (e.equals(this)) {
                return true;
            }
        }
        return false;
    }
}
