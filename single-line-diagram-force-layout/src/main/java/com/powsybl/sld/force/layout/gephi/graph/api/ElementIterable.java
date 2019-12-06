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
 * Element iterable.
 *
 * @param <T> the element class
 */
public interface ElementIterable<T extends Element> extends Iterable<T> {

    /**
     * Empty iterable.
     */
    final ElementIterable EMPTY = new ElementIterable.ElementIterableEmpty();

    /**
     * Returns the element iterator.
     *
     * @return the iterator.
     */
    @Override
    public Iterator<T> iterator();

    /**
     * Returns the iterator content as an array.
     *
     * @return element array
     */
    public T[] toArray();

    /**
     * Returns the iterator content as a collection.
     *
     * @return element collection
     */
    public Collection<T> toCollection();

    /**
     * Break the iterator and release read lock (if any).
     */
    public void doBreak();

    /**
     * Empty element iterable.
     */
    static final class ElementIterableEmpty implements Iterator<Element>, ElementIterable {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Element next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Iterator<Element> iterator() {
            return this;
        }

        @Override
        public Element[] toArray() {
            return new Node[0];
        }

        @Override
        public Collection<Element> toCollection() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void doBreak() {
        }
    }
}
