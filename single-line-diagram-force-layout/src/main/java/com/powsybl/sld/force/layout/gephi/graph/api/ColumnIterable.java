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

import java.util.Iterator;
import java.util.List;

/**
 * Column iterable.
 */
public interface ColumnIterable extends Iterable<Column> {

    /**
     * Returns the element iterator.
     *
     * @return the iterator.
     */
    @Override
    public Iterator<Column> iterator();

    /**
     * Returns the iterator content as an array.
     *
     * @return column array
     */
    public Column[] toArray();

    /**
     * Returns the iterator content as a list.
     *
     * @return column list
     */
    public List<Column> toList();

    /**
     * Break the iterator and release read lock (if any).
     */
    public void doBreak();
}
