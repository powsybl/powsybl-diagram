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
 * A node iterable.
 */
public interface NodeIterable extends ElementIterable<Node> {

    /**
     * The static empty iterable.
     */
    static final NodeIterable EMPTY = new NodeIterableEmpty();

    /**
     * Returns a node iterator.
     *
     * @return node iterator
     */
    @Override
    public Iterator<Node> iterator();

    /**
     * Returns the iterator content as an array.
     *
     * @return node array
     */
    @Override
    public Node[] toArray();

    /**
     * Returns the iterator content as a collection.
     *
     * @return node collection
     */
    @Override
    public Collection<Node> toCollection();

    /**
     * Empty node iterable.
     */
    static final class NodeIterableEmpty implements Iterator<Node>, NodeIterable {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Node next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Iterator<Node> iterator() {
            return this;
        }

        @Override
        public Node[] toArray() {
            return new Node[0];
        }

        @Override
        public Collection<Node> toCollection() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void doBreak() {
        }
    }
}
