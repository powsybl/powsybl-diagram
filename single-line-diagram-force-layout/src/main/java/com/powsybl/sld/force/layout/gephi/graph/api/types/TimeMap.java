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
package com.powsybl.sld.force.layout.gephi.graph.api.types;

import com.powsybl.sld.force.layout.gephi.graph.api.Estimator;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeFormat;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import org.joda.time.DateTimeZone;

/**
 * Interface that defines the functionalities both timestamp and interval map
 * have.
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface TimeMap<K, V> {

    /**
     * Put the value at the given key.
     *
     * @param key key
     * @param value value
     * @return true if key is a new key, false otherwise
     */
    public boolean put(K key, V value);

    /**
     * Remove the value at the given key.
     *
     * @param key key
     * @return true if the key existed, false otherwise
     */
    public boolean remove(K key);

    /**
     * Get the estimated value for the given interval.
     * <p>
     * The estimator is used to determine the way multiple interval values are
     * merged together (e.g average, first, median).
     *
     * @param interval interval query
     * @param estimator estimator used
     * @return estimated value
     */
    public Object get(Interval interval, Estimator estimator);

    /**
     * Get the value for the given key.
     * <p>
     * Return <code>defaultValue</code> if the value is not found.
     *
     * @param key key
     * @param defaultValue default value
     * @return found value or the default value if not found
     */
    public V get(K key, V defaultValue);

    /**
     * Returns all the values as an array.
     *
     * @return values array
     */
    public V[] toValuesArray();

    /**
     * Returns all the keys as an array.
     *
     * @return keys array
     */
    public K[] toKeysArray();

    /**
     * Returns true if this map contains the given key.
     *
     * @param key key
     * @return true if contains, false otherwise
     */
    public boolean contains(K key);

    /**
     * Empties this map.
     */
    public void clear();

    /**
     * Returns the value type class.
     *
     * @return type class
     */
    public Class<V> getTypeClass();

    /**
     * Returns whether <code>estimator</code> is supported.
     *
     * @param estimator estimator
     * @return true if this map supports <code>estimator</code>
     */
    public boolean isSupported(Estimator estimator);

    /**
     * Returns the size.
     *
     * @return the number of elements in this map
     */
    public int size();

    /**
     * Returns true if this map is empty.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty();

    /**
     * Returns this map as a string.
     *
     * @param timeFormat time format
     * @return map as string
     */
    public String toString(TimeFormat timeFormat);

    /**
     * Returns this map as a string.
     *
     * @param timeFormat time format
     * @param timeZone time zone
     * @return map as string
     */
    public String toString(TimeFormat timeFormat, DateTimeZone timeZone);
}
