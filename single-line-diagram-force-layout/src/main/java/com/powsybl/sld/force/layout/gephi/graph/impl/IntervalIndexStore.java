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

import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractIntervalMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalSet;

public class IntervalIndexStore<T extends Element> extends AbstractTimeIndexStore<T, Interval, IntervalSet, AbstractIntervalMap<?>> {

    public IntervalIndexStore(Class<T> type, GraphLock lock, boolean indexed) {
        super(type, lock, indexed, new Interval2IntTreeMap());
        mainIndex = indexed ? new IntervalIndexImpl(this, true) : null;
    }

    @Override
    protected double getLow(Interval interval) {
        return interval.getLow();
    }

    @Override
    protected void checkK(Interval k) {
        if (k == null) {
            throw new NullPointerException();
        }
    }

    @Override
    protected AbstractTimeIndexImpl createIndex(boolean main) {
        return new IntervalIndexImpl(this, main);
    }

    protected Interval2IntTreeMap getMap() {
        return (Interval2IntTreeMap) timeSortedMap;
    }
}
