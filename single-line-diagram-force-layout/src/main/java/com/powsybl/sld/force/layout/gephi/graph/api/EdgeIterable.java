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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An edge iterable.
 */
public interface EdgeIterable extends ElementIterable<Edge> {

    /**
     * The static empty iterable.
     */
    public static final EdgeIterable EMPTY = new EdgeIterableEmpty();

    /**
     * Returns an edge iterator.
     *
     * @return edge iterator
     */
    @Override
    public Iterator<Edge> iterator();

    /**
     * Returns the iterator content as an array.
     *
     * @return edge array
     */
    @Override
    public Edge[] toArray();

    /**
     * Returns the iterator content as a collection.
     *
     * @return edge collection
     */
    @Override
    public Collection<Edge> toCollection();

    /**
     * Empty edge iterable.
     */
    static final class EdgeIterableEmpty implements Iterator<Edge>, EdgeIterable {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Edge next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Iterator<Edge> iterator() {
            return this;
        }

        @Override
        public Edge[] toArray() {
            return new Edge[0];
        }

        @Override
        public Collection<Edge> toCollection() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void doBreak() {
        }
    }
}
