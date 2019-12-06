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

import java.util.List;

/**
 * Interface to retrieve added, removed and modified columns from the table.
 * <p>
 * This interface is associated with a {@link TableObserver} and provides an
 * easy access to the columns added or removed.
 */
public interface TableDiff {

    /**
     * Gets all added columns.
     *
     * @return a list of added columns
     */
    public List<Column> getAddedColumns();

    /**
     * Gets all removed columns.
     *
     * @return a list of removed columns
     */
    public List<Column> getRemovedColumns();

    /**
     * Returns all columns that have been modified.
     * <p>
     * It excludes columns that have been added or removed.
     *
     * @return a list of modified columns
     */
    public List<Column> getModifiedColumns();
}
