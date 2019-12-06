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

import it.unimi.dsi.fastutil.doubles.Double2IntRBTreeMap;
import com.powsybl.sld.force.layout.gephi.graph.api.Element;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractTimestampMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampSet;

public class TimestampIndexStore<T extends Element> extends AbstractTimeIndexStore<T, Double, TimestampSet, AbstractTimestampMap<?>> {

    public TimestampIndexStore(Class<T> type, GraphLock lock, boolean indexed) {
        super(type, lock, indexed, new Double2IntRBTreeMap());
        mainIndex = indexed ? new TimestampIndexImpl(this, true) : null;
    }

    @Override
    protected double getLow(Double k) {
        return k;
    }

    @Override
    protected void checkK(Double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can't be NaN or infinity");
        }
    }

    @Override
    protected AbstractTimeIndexImpl createIndex(boolean main) {
        return new TimestampIndexImpl(this, main);
    }

    protected Double2IntRBTreeMap getMap() {
        return (Double2IntRBTreeMap) timeSortedMap;
    }
}
