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

import com.powsybl.sld.force.layout.gephi.graph.api.TimeFormat;
import org.joda.time.DateTimeZone;

/**
 * Interface that defines the functionalities both timestamp and interval set
 * have.
 *
 * @param <K> key type
 */
public interface TimeSet<K> {

    /**
     * Adds key to this set.
     *
     * @param key key
     * @return true if added, false otherwise
     */
    public boolean add(K key);

    /**
     * Removes key from this set.
     *
     * @param key key
     * @return true if removed, false otherwise
     */
    public boolean remove(K key);

    /**
     * Returns the size of this set.
     *
     * @return the number of elements in this set
     */
    public int size();

    /**
     * Returns true if this set is empty.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty();

    /**
     * Returns true if this set contains <code>key</code>.
     *
     * @param key key
     * @return true if contains, false otherwise
     */
    public boolean contains(K key);

    /**
     * Returns an array of all keys in this set.
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all keys
     */
    public K[] toArray();

    /**
     * Returns the same result as {@link #toArray() } but in a primitive array if
     * the underlying storage is in a primtive form.
     *
     * @return array of all keys
     */
    public Object toPrimitiveArray();

    /**
     * Empties this set.
     */
    public void clear();

    /**
     * Returns this set as a string.
     *
     * @param timeFormat time format
     * @return set as string
     */
    public String toString(TimeFormat timeFormat);

    /**
     * Returns this set as a string.
     *
     * @param timeFormat time format
     * @param timeZone time zone
     * @return set as string
     */
    public String toString(TimeFormat timeFormat, DateTimeZone timeZone);
}
