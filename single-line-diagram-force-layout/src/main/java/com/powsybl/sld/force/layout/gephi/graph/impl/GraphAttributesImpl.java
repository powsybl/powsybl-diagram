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

import com.powsybl.sld.force.layout.gephi.graph.api.AttributeUtils;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimeMap;
import com.powsybl.sld.force.layout.gephi.graph.impl.utils.MapDeepEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphAttributesImpl {

    protected final Map<String, Object> attributes = new HashMap<String, Object>();

    public synchronized Set<String> getKeys() {
        return attributes.keySet();
    }

    public synchronized void setValue(String key, Object value) {
        if (value != null) {
            checkSupportedTypes(value.getClass());
        }
        attributes.put(key, AttributeUtils.standardizeValue(value));
    }

    public synchronized void removeValue(String key) {
        attributes.remove(key);
    }

    public synchronized Object getValue(String key) {
        return attributes.get(key);
    }

    public synchronized Object getValue(String key, double timestamp) {
        return getValueInternal(key, timestamp);
    }

    public synchronized Object getValue(String key, Interval interval) {
        return getValueInternal(key, interval);
    }

    private Object getValueInternal(String key, Object timeObj) {
        TimeMap valueSet = (TimeMap) attributes.get(key);
        if (valueSet != null) {
            return valueSet.get(timeObj, null);
        }
        return null;
    }

    public synchronized void removeValue(String key, double timestamp) {
        removeValueInternal(key, timestamp);

    }

    public synchronized void removeValue(String key, Interval interval) {
        removeValueInternal(key, interval);
    }

    private void removeValueInternal(String key, Object timeObj) {
        TimeMap timeMap = (TimeMap) attributes.get(key);
        if (timeMap != null) {
            if (timeMap.remove(timeObj)) {
                if (timeMap.isEmpty()) {
                    attributes.remove(key);
                }
            }
        }
    }

    public synchronized void setValue(String key, Object value, double timestamp) {
        setValueInternal(key, value, timestamp);
    }

    public synchronized void setValue(String key, Object value, Interval interval) {
        setValueInternal(key, value, interval);
    }

    private void setValueInternal(String key, Object value, Object timeObj) {
        if (value == null) {
            throw new NullPointerException("The value can't be null for the key '" + key + "'");
        }
        TimeMap valueSet = null;
        if (attributes.containsKey(key)) {
            valueSet = (TimeMap) attributes.get(key);

            if (!value.getClass().equals(valueSet.getTypeClass())) {
                throw new IllegalArgumentException(
                        "The value type " + value.getClass().getName() + " doesn't match with the expected type " + valueSet
                                .getTypeClass().getName());
            }
        } else {
            if (timeObj instanceof Interval) {
                checkSupportedIntervalTypes(value.getClass());
            } else {
                checkSupportedTimestampTypes(value.getClass());
            }
            try {
                if (timeObj instanceof Interval) {
                    valueSet = AttributeUtils.getIntervalMapType(value.getClass()).newInstance();
                } else {
                    valueSet = AttributeUtils.getTimestampMapType(value.getClass()).newInstance();
                }
                attributes.put(key, valueSet);
            } catch (Exception ex) {
                throw new RuntimeException("The dynamic type can't be created", ex);
            }
        }

        valueSet.put(timeObj, value);
    }

    protected void setGraphAttributes(GraphAttributesImpl graphAttributes) {
        attributes.putAll(graphAttributes.attributes);
    }

    private void checkSupportedTypes(Class type) {
        if (!AttributeUtils.isSupported(type)) {
            throw new IllegalArgumentException("Unknown type " + type.getName());
        }
    }

    private void checkSupportedTimestampTypes(Class type) {
        try {
            AttributeUtils.getTimestampMapType(type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported dynamic type " + type.getName());
        }
    }

    private void checkSupportedIntervalTypes(Class type) {
        try {
            AttributeUtils.getIntervalMapType(type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported dynamic type " + type.getName());
        }
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 47 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
        return hash;
    }

    public boolean deepEquals(GraphAttributesImpl obj) {
        if (obj == null) {
            return false;
        }
        if (!MapDeepEquals.mapDeepEquals(attributes, obj.attributes)) {
            return false;
        }
        return true;
    }
}
