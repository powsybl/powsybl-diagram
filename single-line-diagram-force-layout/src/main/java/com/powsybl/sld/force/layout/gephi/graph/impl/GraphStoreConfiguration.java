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

import com.powsybl.sld.force.layout.gephi.graph.api.Estimator;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeFormat;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeRepresentation;
import org.joda.time.DateTimeZone;

public final class GraphStoreConfiguration {

    // Features
    public static final boolean ENABLE_AUTO_LOCKING = true;
    public static final boolean ENABLE_AUTO_TYPE_REGISTRATION = true;
    public static final boolean ENABLE_INDEX_NODES = true;
    public static final boolean ENABLE_INDEX_EDGES = true;
    public static final boolean ENABLE_INDEX_TIMESTAMP = true;
    public static final boolean ENABLE_OBSERVERS = true;
    public static final boolean ENABLE_NODE_PROPERTIES = true;
    public static final boolean ENABLE_EDGE_PROPERTIES = true;
    public static final boolean ENABLE_PARALLEL_EDGES = true;
    // NodeStore
    public static final int NODESTORE_BLOCK_SIZE = 5000;
    public static final int NODESTORE_DEFAULT_BLOCKS = 10;
    public static final int NODESTORE_DEFAULT_DICTIONARY_SIZE = 1000;
    public static final float NODESTORE_DICTIONARY_LOAD_FACTOR = .7f;
    // EdgeStore
    public static final int EDGESTORE_BLOCK_SIZE = 8192;
    public static final int EDGESTORE_DEFAULT_BLOCKS = 10;
    public static final int EDGESTORE_DEFAULT_TYPE_COUNT = 1;
    public static final int EDGESTORE_DEFAULT_DICTIONARY_SIZE = 1000;
    public static final float EDGESTORE_DICTIONARY_LOAD_FACTOR = .7f;
    // GraphView
    public static final int VIEW_DEFAULT_TYPE_COUNT = 1;
    public static final double VIEW_GROWING_FACTOR = 1.1;
    // Diff
    public static final double COLUMNDIFF_GROWING_FACTOR = 1.1;
    // Properties
    public static final boolean ENABLE_ELEMENT_LABEL = true;
    public static final boolean ENABLE_ELEMENT_TIME_SET = true;
    public static final Class DEFAULT_NODE_ID_TYPE = String.class;
    public static final Class DEFAULT_EDGE_ID_TYPE = String.class;
    public static final Class DEFAULT_EDGE_LABEL_TYPE = String.class;
    public static final Class DEFAULT_EDGE_WEIGHT_TYPE = Double.class;
    public static final Double DEFAULT_EDGE_WEIGHT = 1.0;
    public static final Double DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING = 0.0;
    // Properties name
    public static final String ELEMENT_ID_COLUMN_ID = "id";
    public static final String ELEMENT_LABEL_COLUMN_ID = "label";
    public static final String ELEMENT_TIMESET_COLUMN_ID = "timeset";
    public static final String EDGE_WEIGHT_COLUMN_ID = "weight";
    // Properties index
    public static final int ELEMENT_ID_INDEX = 0;
    public static final int ELEMENT_LABEL_INDEX = 1;
    public static final int ELEMENT_TIMESET_INDEX = ENABLE_ELEMENT_LABEL ? 2 : 1;
    public static final int EDGE_WEIGHT_INDEX = ENABLE_ELEMENT_TIME_SET ? ELEMENT_TIMESET_INDEX + 1
            : ELEMENT_TIMESET_INDEX;
    // TimeFormat
    public static final TimeFormat DEFAULT_TIME_FORMAT = TimeFormat.DOUBLE;
    // Time zone
    public static final DateTimeZone DEFAULT_TIME_ZONE = DateTimeZone.UTC;
    // Dynamics
    public static final Estimator DEFAULT_ESTIMATOR = Estimator.FIRST;
    public static final TimeRepresentation DEFAULT_TIME_REPRESENTATION = TimeRepresentation.TIMESTAMP;
    // Miscellaneous
    public static final double TIMESTAMP_STORE_GROWING_FACTOR = 1.1;
    public static final double INTERVAL_STORE_GROWING_FACTOR = 1.1;
    public static final int NODE_DEFAULT_COLUMNS = 1 + (ENABLE_ELEMENT_LABEL ? 1 : 0) + (ENABLE_ELEMENT_TIME_SET ? 1
            : 0);
    public static final int EDGE_DEFAULT_COLUMNS = 2 + (ENABLE_ELEMENT_LABEL ? 1 : 0) + (ENABLE_ELEMENT_TIME_SET ? 1
            : 0);

    private GraphStoreConfiguration() {
    }
}
