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

import cern.colt.bitvector.BitVector;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalCharMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampStringMap;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanOpenHashSet;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import com.powsybl.sld.force.layout.gephi.graph.api.Configuration;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Estimator;
import com.powsybl.sld.force.layout.gephi.graph.api.Interval;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;
import com.powsybl.sld.force.layout.gephi.graph.api.Origin;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeFormat;
import com.powsybl.sld.force.layout.gephi.graph.api.TimeRepresentation;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalBooleanMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalByteMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalFloatMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalIntegerMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalLongMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractIntervalMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalShortMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.IntervalStringMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampBooleanMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampByteMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampCharMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampDoubleMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampFloatMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampIntegerMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampLongMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.AbstractTimestampMap;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampSet;
import com.powsybl.sld.force.layout.gephi.graph.api.types.TimestampShortMap;
import com.powsybl.sld.force.layout.gephi.graph.impl.utils.DataInputOutput;
import com.powsybl.sld.force.layout.gephi.graph.impl.utils.LongPacker;
import org.joda.time.DateTimeZone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Greatly inspired from JDBM https://github.com/jankotek/JDBM3
public class Serialization {

    static final float VERSION = 0.5f;
    static final int NULL_ID = -1;
    static final int NULL = 0;
    static final int NORMAL = 1;
    static final int BOOLEAN_TRUE = 2;
    static final int BOOLEAN_FALSE = 3;
    static final int INTEGER_MINUS_1 = 4;
    static final int INTEGER_0 = 5;
    static final int INTEGER_1 = 6;
    static final int INTEGER_2 = 7;
    static final int INTEGER_3 = 8;
    static final int INTEGER_4 = 9;
    static final int INTEGER_5 = 10;
    static final int INTEGER_6 = 11;
    static final int INTEGER_7 = 12;
    static final int INTEGER_8 = 13;
    static final int INTEGER_255 = 14;
    static final int INTEGER_PACK_NEG = 15;
    static final int INTEGER_PACK = 16;
    static final int LONG_MINUS_1 = 17;
    static final int LONG_0 = 18;
    static final int LONG_1 = 19;
    static final int LONG_2 = 20;
    static final int LONG_3 = 21;
    static final int LONG_4 = 22;
    static final int LONG_5 = 23;
    static final int LONG_6 = 24;
    static final int LONG_7 = 25;
    static final int LONG_8 = 26;
    static final int LONG_PACK_NEG = 27;
    static final int LONG_PACK = 28;
    static final int LONG_255 = 29;
    static final int LONG_MINUS_MAX = 30;
    static final int SHORT_MINUS_1 = 31;
    static final int SHORT_0 = 32;
    static final int SHORT_1 = 33;
    static final int SHORT_255 = 34;
    static final int SHORT_FULL = 35;
    static final int BYTE_MINUS_1 = 36;
    static final int BYTE_0 = 37;
    static final int BYTE_1 = 38;
    static final int BYTE_FULL = 39;
    static final int CHAR = 40;
    static final int FLOAT_MINUS_1 = 41;
    static final int FLOAT_0 = 42;
    static final int FLOAT_1 = 43;
    static final int FLOAT_255 = 44;
    static final int FLOAT_SHORT = 45;
    static final int FLOAT_FULL = 46;
    static final int DOUBLE_MINUS_1 = 47;
    static final int DOUBLE_0 = 48;
    static final int DOUBLE_1 = 49;
    static final int DOUBLE_255 = 50;
    static final int DOUBLE_SHORT = 51;
    static final int DOUBLE_FULL = 52;
    static final int DOUBLE_ARRAY = 53;
    static final int BIGDECIMAL = 54;
    static final int BIGINTEGER = 55;
    static final int FLOAT_ARRAY = 56;
    static final int INTEGER_MINUS_MAX = 57;
    static final int SHORT_ARRAY = 58;
    static final int BOOLEAN_ARRAY = 59;
    static final int ARRAY_INT_B_255 = 60;
    static final int ARRAY_INT_B_INT = 61;
    static final int ARRAY_INT_S = 62;
    static final int ARRAY_INT_I = 63;
    static final int ARRAY_INT_PACKED = 64;
    static final int ARRAY_LONG_B = 65;
    static final int ARRAY_LONG_S = 66;
    static final int ARRAY_LONG_I = 67;
    static final int ARRAY_LONG_L = 68;
    static final int ARRAY_LONG_PACKED = 69;
    static final int CHAR_ARRAY = 70;
    static final int ARRAY_BYTE_INT = 71;
    static final int NOTUSED_ARRAY_OBJECT_255 = 72;
    static final int ARRAY_OBJECT = 73;
    static final int STRING_ARRAY = 74;
    static final int STRING_EMPTY = 101;
    static final int NOTUSED_STRING_255 = 102;
    static final int STRING = 103;
    static final int LOCALE = 124;
    static final int PROPERTIES = 125;
    static final int CLASS = 126;
    static final int DATE = 127;
    static final String EMPTY_STRING = "";
    // Specifics
    static final int NODE = 200;
    static final int EDGE = 201;
    static final int EDGETYPE_STORE = 202;
    static final int COLUMN_ORIGIN = 203;
    static final int TABLE = 204;
    static final int CONFIGURATION = 205;
    static final int GRAPH_STORE = 206;
    static final int GRAPH_FACTORY = 207;
    static final int GRAPH_VIEW_STORE = 208;
    static final int GRAPH_VIEW = 209;
    static final int BIT_VECTOR = 210;
    static final int GRAPH_STORE_CONFIGURATION = 211;
    static final int TIME_REPRESENTATION = 212;
    static final int GRAPH_VERSION = 213;
    static final int NODE_PROPERTIES = 214;
    static final int EDGE_PROPERTIES = 215;
    static final int TEXT_PROPERTIES = 216;
    static final int ESTIMATOR = 217;
    static final int GRAPH_ATTRIBUTES = 218;
    static final int TIMESTAMP_INDEX_STORE = 219;
    static final int INTERVAL_INDEX_STORE = 220;
    static final int TIME_FORMAT = 221;
    static final int TIME_STORE = 222;
    static final int TIMESTAMP_SET = 223;
    static final int INTERVAL_SET = 224;
    static final int TIMESTAMP_MAP = 225;
    static final int INTERVAL_MAP = 226;
    static final int TIME_ZONE = 227;
    static final int INTERVAL = 228;
    static final int LIST = 229;
    static final int SET = 230;
    static final int MAP = 231;
    // Store
    protected final Int2IntMap idMap;
    protected GraphModelImpl model;
    protected float readVersion = VERSION;
    // Deserialized configuration
    protected GraphStoreConfigurationVersion graphStoreConfigurationVersion;

    public Serialization() {
        this(null);
    }

    public Serialization(GraphModelImpl graphModel) {
        model = graphModel;
        idMap = new Int2IntOpenHashMap();
        idMap.defaultReturnValue(NULL_ID);
    }

    public void serializeGraphModel(DataOutput out, GraphModelImpl model) throws IOException {
        this.model = model;
        serialize(out, VERSION);
        serialize(out, model.configuration);
        serialize(out, model.store);
    }

    public GraphModelImpl deserializeGraphModel(DataInput is) throws IOException, ClassNotFoundException {
        readVersion = (Float) deserialize(is);
        Configuration config = (Configuration) deserialize(is);
        model = new GraphModelImpl(config);
        deserialize(is);
        return model;
    }

    public GraphModelImpl deserializeGraphModelWithoutVersionPrefix(DataInput is, float version) throws IOException, ClassNotFoundException {
        readVersion = version;
        Configuration config = (Configuration) deserialize(is);
        model = new GraphModelImpl(config);
        deserialize(is);
        return model;
    }

    public void serializeGraphStore(DataOutput out, GraphStore store) throws IOException {
        // Configuration
        serializeGraphStoreConfiguration(out);

        // GraphVersion
        serialize(out, store.version);

        // Edge types
        EdgeTypeStore edgeTypeStore = store.edgeTypeStore;
        serialize(out, edgeTypeStore);

        // Column
        serialize(out, store.nodeTable);
        serialize(out, store.edgeTable);

        // Time store
        serialize(out, store.timeStore);

        // Factory
        serialize(out, store.factory);

        // Atts
        serialize(out, store.attributes);

        // TimeFormat
        serialize(out, store.timeFormat);

        // Time zone
        serialize(out, store.timeZone);

        // Nodes + Edges
        int nodesAndEdges = store.nodeStore.size() + store.edgeStore.size();
        serialize(out, nodesAndEdges);

        for (Node node : store.nodeStore) {
            serialize(out, node);
        }
        for (Edge edge : store.edgeStore) {
            serialize(out, edge);
        }

        // Views
        serialize(out, store.viewStore);
    }

    public GraphStore deserializeGraphStore(DataInput is) throws IOException, ClassNotFoundException {
        if (!model.store.nodeStore.isEmpty()) { // TODO test other stores
            throw new IOException("The store is not empty");
        }

        // Store Configuration
        deserialize(is);

        // Graph Version
        GraphVersion version = (GraphVersion) deserialize(is);
        model.store.version.nodeVersion = version.nodeVersion;
        model.store.version.edgeVersion = version.edgeVersion;

        // Edge types
        deserialize(is);

        // Columns
        deserialize(is);
        deserialize(is);

        // Time store
        deserialize(is);

        // Factory
        deserialize(is);

        // Atts
        GraphAttributesImpl attributes = (GraphAttributesImpl) deserialize(is);
        model.store.attributes.setGraphAttributes(attributes);

        // TimeFormat
        deserialize(is);

        // Time zone
        deserialize(is);

        // Nodes and edges
        int nodesAndEdges = (Integer) deserialize(is);
        for (int i = 0; i < nodesAndEdges; i++) {
            deserialize(is);
        }

        // ViewStore
        deserialize(is);

        return model.store;
    }

    private void serializeNode(DataOutput out, NodeImpl node) throws IOException {
        serialize(out, node.getId());
        serialize(out, node.storeId);
        serialize(out, node.attributes);
        serialize(out, node.properties);
    }

    private void serializeEdge(DataOutput out, EdgeImpl edge) throws IOException {
        serialize(out, edge.getId());
        serialize(out, edge.source.storeId);
        serialize(out, edge.target.storeId);
        serialize(out, edge.type);
        if (edge.graphStore != null && edge.hasDynamicWeight()) {
            serialize(out, edge.getWeight());
        } else {
            serialize(out, GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT);
        }
        serialize(out, edge.isDirected());
        serialize(out, edge.attributes);
        serialize(out, edge.properties);
    }

    private NodeImpl deserializeNode(DataInput is) throws IOException, ClassNotFoundException {
        Object id = deserialize(is);
        int storeId = (Integer) deserialize(is);
        Object[] attributes = (Object[]) deserialize(is);
        NodeImpl.NodePropertiesImpl properties = (NodeImpl.NodePropertiesImpl) deserialize(is);

        NodeImpl node = (NodeImpl) model.store.factory.newNode(id);
        node.attributes = attributes;
        if (node.properties != null) {
            node.setNodeProperties(properties);
        }
        model.store.nodeStore.add(node);

        idMap.put(storeId, node.storeId);

        return node;
    }

    private EdgeImpl deserializeEdge(DataInput is) throws IOException, ClassNotFoundException {
        Object id = deserialize(is);
        int sourceId = (Integer) deserialize(is);
        int targetId = (Integer) deserialize(is);
        int type = (Integer) deserialize(is);
        double weight = (Double) deserialize(is);
        boolean directed = (Boolean) deserialize(is);
        Object[] attributes = (Object[]) deserialize(is);
        EdgeImpl.EdgePropertiesImpl properties = (EdgeImpl.EdgePropertiesImpl) deserialize(is);

        int sourceNewId = idMap.get(sourceId);
        int targetNewId = idMap.get(targetId);

        if (sourceId == NULL_ID || targetId == NULL_ID) {
            throw new IOException("The edge source of target can't be found");
        }

        NodeImpl source = model.store.nodeStore.get(sourceNewId);
        NodeImpl target = model.store.nodeStore.get(targetNewId);

        EdgeImpl edge = (EdgeImpl) model.store.factory.newEdge(id, source, target, type, weight, directed);
        edge.attributes = attributes;
        if (edge.properties != null) {
            edge.setEdgeProperties(properties);
        }

        model.store.edgeStore.add(edge);

        return edge;
    }

    private void serializeEdgeTypeStore(final DataOutput out) throws IOException {
        EdgeTypeStore edgeTypeStore = model.store.edgeTypeStore;
        int length = edgeTypeStore.length;
        serialize(out, length);
        short[] ids = edgeTypeStore.getIds();
        serialize(out, ids);
        Object[] labels = edgeTypeStore.getLabels();
        serialize(out, labels);
        short[] garbage = edgeTypeStore.getGarbage();
        serialize(out, garbage);
    }

    private EdgeTypeStore deserializeEdgeTypeStore(final DataInput is) throws IOException, ClassNotFoundException {
        int length = (Integer) deserialize(is);
        short[] ids = (short[]) deserialize(is);
        Object[] labels = (Object[]) deserialize(is);
        short[] garbage = (short[]) deserialize(is);

        EdgeTypeStore edgeTypeStore = model.store.edgeTypeStore;
        edgeTypeStore.length = length;
        for (int i = 0; i < ids.length; i++) {
            short id = ids[i];
            Object label = labels[i];
            edgeTypeStore.idMap.put(id, label);
            edgeTypeStore.labelMap.put(label, id);
        }
        for (int i = 0; i < garbage.length; i++) {
            edgeTypeStore.garbageQueue.add(garbage[i]);
        }
        return edgeTypeStore;
    }

    private void serializeTable(final DataOutput out, final TableImpl table) throws IOException {
        serialize(out, table.store.elementType);

        serializeColumnStore(out, table.store);
    }

    private TableImpl deserializeTable(final DataInput is) throws IOException, ClassNotFoundException {
        Class elementType = (Class) deserialize(is);

        TableImpl table = null;

        if (elementType.equals(Node.class)) {
            table = model.store.nodeTable;
        } else if (elementType.equals(Edge.class)) {
            table = model.store.edgeTable;
        } else {
            throw new RuntimeException("Not recognized column store");
        }

        deserializeColumnStore(is, table);

        return table;
    }

    private void serializeColumnStore(final DataOutput out, final ColumnStore columnStore) throws IOException {
        int length = columnStore.length;
        serialize(out, length);

        for (int i = 0; i < length; i++) {
            ColumnImpl col = columnStore.columns[i];
            serializeColumn(out, col);
        }

        serialize(out, columnStore.garbageQueue.toShortArray());
    }

    private ColumnStore deserializeColumnStore(final DataInput is, final TableImpl table) throws IOException, ClassNotFoundException {
        ColumnStore columnStore = table.store;
        int length = (Integer) deserialize(is);
        columnStore.length = length;

        for (int i = 0; i < length; i++) {
            ColumnImpl col = (ColumnImpl) deserializeColumn(is, table);
            if (col != null) {
                columnStore.columns[col.storeId] = col;
                columnStore.idMap.put(col.id, columnStore.intToShort(col.storeId));
                if (columnStore.indexStore != null) {
                    columnStore.indexStore.addColumn(col);
                }
            }
        }

        short[] garbage = (short[]) deserialize(is);
        for (int i = 0; i < garbage.length; i++) {
            columnStore.garbageQueue.add(garbage[i]);
        }
        return columnStore;
    }

    private void serializeColumn(final DataOutput out, final ColumnImpl column) throws IOException {
        if (column == null) {
            serialize(out, null);
            return;
        }
        serialize(out, column.id);
        serialize(out, column.title);
        serialize(out, column.origin);
        serialize(out, column.storeId);
        serialize(out, column.typeClass);
        serialize(out, column.defaultValue);
        serialize(out, column.indexed);
        serialize(out, column.readOnly);
        serialize(out, column.estimator);
    }

    private ColumnImpl deserializeColumn(final DataInput is, TableImpl table) throws IOException, ClassNotFoundException {
        String id = (String) deserialize(is);
        if (id == null) {
            return null;
        }
        String title = (String) deserialize(is);
        Origin origin = (Origin) deserialize(is);
        int storeId = (Integer) deserialize(is);
        Class typeClass = (Class) deserialize(is);
        Object defaultValue = deserialize(is);
        boolean indexed = (Boolean) deserialize(is);
        boolean readOnly = (Boolean) deserialize(is);
        Estimator estimator = (Estimator) deserialize(is);

        ColumnImpl column = new ColumnImpl(table, (String) id, typeClass, title, defaultValue, origin, indexed,
                readOnly);
        column.storeId = storeId;
        if (estimator != null) {
            column.setEstimator(estimator);
        }

        // Make sure configured types match the deserialized column types:
        if (Edge.class.equals(table.getElementClass())) {
            if (id.equals(GraphStoreConfiguration.EDGE_WEIGHT_COLUMN_ID)) {
                table.store.configuration.setEdgeWeightType(typeClass);
            } else if (id.equals(GraphStoreConfiguration.ELEMENT_ID_COLUMN_ID)) {
                table.store.configuration.setEdgeIdType(typeClass);
            }
        } else if (Node.class.equals(table.getElementClass())) {
            if (id.equals(GraphStoreConfiguration.ELEMENT_ID_COLUMN_ID)) {
                table.store.configuration.setNodeIdType(typeClass);
            }
        }

        return column;
    }

    private void serializeGraphFactory(final DataOutput out) throws IOException {
        GraphFactoryImpl factory = model.store.factory;

        serialize(out, factory.getNodeCounter());
        serialize(out, factory.getEdgeCounter());
    }

    private GraphFactoryImpl deserializeGraphFactory(final DataInput is) throws IOException, ClassNotFoundException {
        GraphFactoryImpl graphFactory = model.store.factory;

        int nodeCounter = (Integer) deserialize(is);
        int edgeCounter = (Integer) deserialize(is);

        graphFactory.setNodeCounter(nodeCounter);
        graphFactory.setEdgeCounter(edgeCounter);

        return graphFactory;
    }

    private void serializeViewStore(final DataOutput out) throws IOException {
        GraphViewStore viewStore = model.store.viewStore;

        serialize(out, viewStore.length);
        serialize(out, viewStore.views);
        serialize(out, viewStore.garbageQueue.toIntArray());
    }

    private GraphViewStore deserializeViewStore(final DataInput is) throws IOException, ClassNotFoundException {
        GraphViewStore viewStore = model.store.viewStore;

        int length = (Integer) deserialize(is);
        Object[] views = (Object[]) deserialize(is);
        int[] garbages = (int[]) deserialize(is);

        viewStore.length = length;
        viewStore.views = new GraphViewImpl[views.length];
        System.arraycopy(views, 0, viewStore.views, 0, views.length);
        for (int i = 0; i < garbages.length; i++) {
            viewStore.garbageQueue.add(garbages[i]);
        }
        return viewStore;
    }

    private void serializeGraphView(final DataOutput out, final GraphViewImpl view) throws IOException {
        serialize(out, view.nodeView);
        serialize(out, view.edgeView);
        serialize(out, view.storeId);
        serialize(out, view.nodeCount);
        serialize(out, view.edgeCount);

        serialize(out, view.nodeBitVector);
        serialize(out, view.edgeBitVector);

        serialize(out, view.typeCounts);
        serialize(out, view.mutualEdgeTypeCounts);
        serialize(out, view.mutualEdgesCount);

        serialize(out, view.version);

        serialize(out, view.attributes);
        serialize(out, view.interval);
    }

    private GraphViewImpl deserializeGraphView(final DataInput is) throws IOException, ClassNotFoundException {
        boolean nodeView = (Boolean) deserialize(is);
        boolean edgeView = (Boolean) deserialize(is);
        GraphViewImpl view = new GraphViewImpl(model.store, nodeView, edgeView);

        int storeId = (Integer) deserialize(is);
        int nodeCount = (Integer) deserialize(is);
        int edgeCount = (Integer) deserialize(is);
        BitVector nodeCountVector = (BitVector) deserialize(is);
        BitVector edgeCountVector = (BitVector) deserialize(is);
        int[] typeCounts = (int[]) deserialize(is);
        int[] mutualEdgeTypeCounts = (int[]) deserialize(is);
        int mutualEdgesCount = (Integer) deserialize(is);
        GraphVersion version = (GraphVersion) deserialize(is);
        GraphAttributesImpl atts = (GraphAttributesImpl) deserialize(is);
        Interval interval = (Interval) deserialize(is);

        view.nodeCount = nodeCount;
        view.edgeCount = edgeCount;
        view.nodeBitVector = nodeCountVector;
        view.edgeBitVector = edgeCountVector;
        view.storeId = storeId;

        view.typeCounts = typeCounts;
        view.mutualEdgesCount = mutualEdgesCount;
        view.mutualEdgeTypeCounts = mutualEdgeTypeCounts;

        view.version.nodeVersion = version.nodeVersion;
        view.version.edgeVersion = version.edgeVersion;

        view.attributes.setGraphAttributes(atts);
        view.interval = interval;

        return view;
    }

    private void serializeBitVector(final DataOutput out, final BitVector bitVector) throws IOException {
        serialize(out, bitVector.size());
        serialize(out, bitVector.elements());
    }

    private BitVector deserializeBitVector(final DataInput is) throws IOException, ClassNotFoundException {
        int size = (Integer) deserialize(is);
        long[] elements = (long[]) deserialize(is);
        return new BitVector(elements, size);
    }

    private void serializeGraphStoreConfiguration(final DataOutput out) throws IOException {
        out.write(GRAPH_STORE_CONFIGURATION);
        serialize(out, GraphStoreConfiguration.ENABLE_ELEMENT_LABEL);
        serialize(out, GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET);
        serialize(out, GraphStoreConfiguration.ENABLE_NODE_PROPERTIES);
        serialize(out, GraphStoreConfiguration.ENABLE_EDGE_PROPERTIES);
    }

    private GraphStoreConfigurationVersion deserializeGraphStoreConfiguration(final DataInput is) throws IOException, ClassNotFoundException {
        boolean enableElementLabel = (Boolean) deserialize(is);
        boolean enableElementTimestamp = (Boolean) deserialize(is);
        boolean enableNodeProperties = (Boolean) deserialize(is);
        boolean enableEdgeProperties = (Boolean) deserialize(is);

        graphStoreConfigurationVersion = new GraphStoreConfigurationVersion(enableElementLabel, enableElementTimestamp,
                enableNodeProperties, enableEdgeProperties);
        return graphStoreConfigurationVersion;
    }

    private void serializeGraphVersion(final DataOutput out, final GraphVersion graphVersion) throws IOException {
        serialize(out, graphVersion.nodeVersion);
        serialize(out, graphVersion.edgeVersion);
    }

    private GraphVersion deserializeGraphVersion(final DataInput is) throws IOException, ClassNotFoundException {
        GraphVersion graphVersion = new GraphVersion(null);

        int nodeVersion = (Integer) deserialize(is);
        int edgeVersion = (Integer) deserialize(is);

        graphVersion.nodeVersion = nodeVersion;
        graphVersion.edgeVersion = edgeVersion;

        return graphVersion;
    }

    private void serializeNodeProperties(final DataOutput out, final NodeImpl.NodePropertiesImpl nodeProperties) throws IOException {
        serialize(out, nodeProperties.x);
        serialize(out, nodeProperties.y);
        serialize(out, nodeProperties.z);
        serialize(out, nodeProperties.rgba);
        serialize(out, nodeProperties.size);
        serialize(out, nodeProperties.fixed);
        serialize(out, nodeProperties.textProperties);
    }

    private NodeImpl.NodePropertiesImpl deserializeNodeProperties(final DataInput is) throws IOException, ClassNotFoundException {
        float x = (Float) deserialize(is);
        float y = (Float) deserialize(is);
        float z = (Float) deserialize(is);
        int rgba = (Integer) deserialize(is);
        float size = (Float) deserialize(is);
        boolean fixed = (Boolean) deserialize(is);
        TextPropertiesImpl textProperties = (TextPropertiesImpl) deserialize(is);

        NodeImpl.NodePropertiesImpl props = new NodeImpl.NodePropertiesImpl();
        props.x = x;
        props.y = y;
        props.z = z;
        props.rgba = rgba;
        props.size = size;
        props.fixed = fixed;
        props.setTextProperties(textProperties);

        return props;
    }

    private void serializeEdgeProperties(final DataOutput out, final EdgeImpl.EdgePropertiesImpl edgeProperties) throws IOException {
        serialize(out, edgeProperties.rgba);
        serialize(out, edgeProperties.textProperties);
    }

    private EdgeImpl.EdgePropertiesImpl deserializeEdgeProperties(final DataInput is) throws IOException, ClassNotFoundException {
        int rgba = (Integer) deserialize(is);
        TextPropertiesImpl textProperties = (TextPropertiesImpl) deserialize(is);

        EdgeImpl.EdgePropertiesImpl props = new EdgeImpl.EdgePropertiesImpl();
        props.rgba = rgba;
        props.setTextProperties(textProperties);

        return props;
    }

    private void serializeTextProperties(final DataOutput out, final TextPropertiesImpl textProperties) throws IOException {
        serialize(out, textProperties.size);
        serialize(out, textProperties.rgba);
        serialize(out, textProperties.visible);
        serialize(out, textProperties.text);
        serialize(out, textProperties.width);
        serialize(out, textProperties.height);
    }

    private TextPropertiesImpl deserializeTextProperties(final DataInput is) throws IOException, ClassNotFoundException {
        float size = (Float) deserialize(is);
        int rgba = (Integer) deserialize(is);
        boolean visible = (Boolean) deserialize(is);
        String text = (String) deserialize(is);
        float width = (Float) deserialize(is);
        float height = (Float) deserialize(is);

        TextPropertiesImpl props = new TextPropertiesImpl();
        props.size = size;
        props.rgba = rgba;
        props.visible = visible;
        props.text = text;
        props.width = width;
        props.height = height;

        return props;
    }

    private void serializeTimestampSet(final DataOutput out, final TimestampSet timestampSet) throws IOException {
        serialize(out, timestampSet.toPrimitiveArray());
    }

    private TimestampSet deserializeTimestampSet(DataInput is) throws IOException, ClassNotFoundException {
        double[] r = (double[]) deserialize(is);

        return new TimestampSet(r);
    }

    private void serializeIntervalSet(final DataOutput out, final IntervalSet intervalSet) throws IOException {
        serialize(out, intervalSet.getIntervals());
    }

    private IntervalSet deserializeIntervalSet(DataInput is) throws IOException, ClassNotFoundException {
        double[] r = (double[]) deserialize(is);

        return new IntervalSet(r);
    }

    private void serializeTimestampMap(final DataOutput out, final AbstractTimestampMap timestampMap) throws IOException {
        serialize(out, timestampMap.getTimestamps());
        Class mapClass = timestampMap.getClass();
        if (mapClass.equals(TimestampBooleanMap.class)) {
            serialize(out, ((TimestampBooleanMap) timestampMap).toBooleanArray());
        } else if (mapClass.equals(TimestampByteMap.class)) {
            serialize(out, ((TimestampByteMap) timestampMap).toByteArray());
        } else if (mapClass.equals(TimestampCharMap.class)) {
            serialize(out, ((TimestampCharMap) timestampMap).toCharacterArray());
        } else if (mapClass.equals(TimestampDoubleMap.class)) {
            serialize(out, ((TimestampDoubleMap) timestampMap).toDoubleArray());
        } else if (mapClass.equals(TimestampFloatMap.class)) {
            serialize(out, ((TimestampFloatMap) timestampMap).toFloatArray());
        } else if (mapClass.equals(TimestampIntegerMap.class)) {
            serialize(out, ((TimestampIntegerMap) timestampMap).toIntegerArray());
        } else if (mapClass.equals(TimestampLongMap.class)) {
            serialize(out, ((TimestampLongMap) timestampMap).toLongArray());
        } else if (mapClass.equals(TimestampShortMap.class)) {
            serialize(out, ((TimestampShortMap) timestampMap).toShortArray());
        } else if (mapClass.equals(TimestampStringMap.class)) {
            serialize(out, timestampMap.toValuesArray());
        } else {
            throw new RuntimeException("Unrecognized timestamp map class");
        }
    }

    private AbstractTimestampMap deserializeTimestampMap(final DataInput is) throws IOException, ClassNotFoundException {
        double[] timeStamps = (double[]) deserialize(is);
        Object values = deserialize(is);

        Class mapClass = values.getClass();
        AbstractTimestampMap valueSet;
        if (mapClass.equals(boolean[].class)) {
            valueSet = new TimestampBooleanMap(timeStamps, (boolean[]) values);
        } else if (mapClass.equals(byte[].class)) {
            valueSet = new TimestampByteMap(timeStamps, (byte[]) values);
        } else if (mapClass.equals(char[].class)) {
            valueSet = new TimestampCharMap(timeStamps, (char[]) values);
        } else if (mapClass.equals(double[].class)) {
            valueSet = new TimestampDoubleMap(timeStamps, (double[]) values);
        } else if (mapClass.equals(float[].class)) {
            valueSet = new TimestampFloatMap(timeStamps, (float[]) values);
        } else if (mapClass.equals(int[].class)) {
            valueSet = new TimestampIntegerMap(timeStamps, (int[]) values);
        } else if (mapClass.equals(long[].class)) {
            valueSet = new TimestampLongMap(timeStamps, (long[]) values);
        } else if (mapClass.equals(short[].class)) {
            valueSet = new TimestampShortMap(timeStamps, (short[]) values);
        } else if (mapClass.equals(String[].class)) {
            valueSet = new TimestampStringMap(timeStamps, (String[]) values);
        } else {
            throw new RuntimeException("Unrecognized timestamp map class");
        }
        return valueSet;
    }

    private void serializeIntervalMap(final DataOutput out, final AbstractIntervalMap intervalMap) throws IOException {
        serialize(out, intervalMap.getIntervals());
        Class mapClass = intervalMap.getClass();
        if (mapClass.equals(IntervalBooleanMap.class)) {
            serialize(out, ((IntervalBooleanMap) intervalMap).toBooleanArray());
        } else if (mapClass.equals(IntervalByteMap.class)) {
            serialize(out, ((IntervalByteMap) intervalMap).toByteArray());
        } else if (mapClass.equals(IntervalCharMap.class)) {
            serialize(out, ((IntervalCharMap) intervalMap).toCharacterArray());
        } else if (mapClass.equals(IntervalDoubleMap.class)) {
            serialize(out, ((IntervalDoubleMap) intervalMap).toDoubleArray());
        } else if (mapClass.equals(IntervalFloatMap.class)) {
            serialize(out, ((IntervalFloatMap) intervalMap).toFloatArray());
        } else if (mapClass.equals(IntervalIntegerMap.class)) {
            serialize(out, ((IntervalIntegerMap) intervalMap).toIntegerArray());
        } else if (mapClass.equals(IntervalLongMap.class)) {
            serialize(out, ((IntervalLongMap) intervalMap).toLongArray());
        } else if (mapClass.equals(IntervalShortMap.class)) {
            serialize(out, ((IntervalShortMap) intervalMap).toShortArray());
        } else if (mapClass.equals(IntervalStringMap.class)) {
            serialize(out, intervalMap.toValuesArray());
        } else {
            throw new RuntimeException("Unrecognized interval map class");
        }
    }

    private AbstractIntervalMap deserializeIntervalMap(final DataInput is) throws IOException, ClassNotFoundException {
        double[] intervals = (double[]) deserialize(is);
        Object values = deserialize(is);

        Class mapClass = values.getClass();
        AbstractIntervalMap valueSet;
        if (mapClass.equals(boolean[].class)) {
            valueSet = new IntervalBooleanMap(intervals, (boolean[]) values);
        } else if (mapClass.equals(byte[].class)) {
            valueSet = new IntervalByteMap(intervals, (byte[]) values);
        } else if (mapClass.equals(char[].class)) {
            valueSet = new IntervalCharMap(intervals, (char[]) values);
        } else if (mapClass.equals(double[].class)) {
            valueSet = new IntervalDoubleMap(intervals, (double[]) values);
        } else if (mapClass.equals(float[].class)) {
            valueSet = new IntervalFloatMap(intervals, (float[]) values);
        } else if (mapClass.equals(int[].class)) {
            valueSet = new IntervalIntegerMap(intervals, (int[]) values);
        } else if (mapClass.equals(long[].class)) {
            valueSet = new IntervalLongMap(intervals, (long[]) values);
        } else if (mapClass.equals(short[].class)) {
            valueSet = new IntervalShortMap(intervals, (short[]) values);
        } else if (mapClass.equals(String[].class)) {
            valueSet = new IntervalStringMap(intervals, (String[]) values);
        } else {
            throw new RuntimeException("Unrecognized timestamp map class");
        }
        return valueSet;
    }

    private void serializeTimestampIndexStore(final DataOutput out, final TimestampIndexStore timestampIndexStore) throws IOException {
        serialize(out, timestampIndexStore.elementType);

        serialize(out, timestampIndexStore.length);
        serialize(out, timestampIndexStore.getMap().keySet().toDoubleArray());
        serialize(out, timestampIndexStore.getMap().values().toIntArray());
        serialize(out, timestampIndexStore.garbageQueue.toIntArray());
        serialize(out, timestampIndexStore.countMap);
    }

    private TimestampIndexStore deserializeTimestampIndexStore(final DataInput is) throws IOException, ClassNotFoundException {
        TimestampIndexStore timestampIndexStore;

        Class cls = (Class) deserialize(is);
        if (cls.equals(Node.class)) {
            timestampIndexStore = (TimestampIndexStore) model.store.timeStore.nodeIndexStore;
        } else {
            timestampIndexStore = (TimestampIndexStore) model.store.timeStore.edgeIndexStore;
        }

        int length = (Integer) deserialize(is);
        double[] doubles = (double[]) deserialize(is);
        int[] ints = (int[]) deserialize(is);
        int[] garbage = (int[]) deserialize(is);
        int[] counts = (int[]) deserialize(is);

        timestampIndexStore.length = length;
        for (int i : garbage) {
            timestampIndexStore.garbageQueue.add(i);
        }
        Double2IntMap m = timestampIndexStore.getMap();
        for (int i = 0; i < ints.length; i++) {
            m.put(doubles[i], ints[i]);
        }
        timestampIndexStore.countMap = counts;
        return timestampIndexStore;
    }

    private void serializeIntervalIndexStore(final DataOutput out, final IntervalIndexStore intervalIndexStore) throws IOException {
        serialize(out, intervalIndexStore.elementType);

        serialize(out, intervalIndexStore.length);
        serialize(out, intervalIndexStore.getMap().size());
        for (Map.Entry<Interval, Integer> entry : intervalIndexStore.getMap().entrySet()) {
            serialize(out, entry.getKey());
            serialize(out, entry.getValue());
        }
        serialize(out, intervalIndexStore.garbageQueue.toIntArray());
        serialize(out, intervalIndexStore.countMap);
    }

    private IntervalIndexStore deserializeIntervalIndexStore(final DataInput is) throws IOException, ClassNotFoundException {
        IntervalIndexStore intervalIndexStore;

        Class cls = (Class) deserialize(is);
        if (cls.equals(Node.class)) {
            intervalIndexStore = (IntervalIndexStore) model.store.timeStore.nodeIndexStore;
        } else {
            intervalIndexStore = (IntervalIndexStore) model.store.timeStore.edgeIndexStore;
        }

        int length = (Integer) deserialize(is);
        int mapSize = (Integer) deserialize(is);

        Interval2IntTreeMap map = intervalIndexStore.getMap();
        for (int i = 0; i < mapSize; i++) {
            Interval key = (Interval) deserialize(is);
            Integer value = (Integer) deserialize(is);
            map.put(key, value);
        }
        int[] garbage = (int[]) deserialize(is);
        int[] counts = (int[]) deserialize(is);

        intervalIndexStore.length = length;
        for (int i : garbage) {
            intervalIndexStore.garbageQueue.add(i);
        }
        intervalIndexStore.countMap = counts;
        return intervalIndexStore;
    }

    private void serializeGraphAttributes(final DataOutput out, final GraphAttributesImpl graphAttributes) throws IOException {
        serialize(out, graphAttributes.attributes.size());
        for (Map.Entry<String, Object> entry : graphAttributes.attributes.entrySet()) {
            serialize(out, entry.getKey());
            serialize(out, entry.getValue());
        }
    }

    private GraphAttributesImpl deserializeGraphAttributes(final DataInput is) throws IOException, ClassNotFoundException {
        GraphAttributesImpl attributes = new GraphAttributesImpl();
        int size = (Integer) deserialize(is);
        for (int i = 0; i < size; i++) {
            String key = (String) deserialize(is);
            Object value = deserialize(is);
            attributes.attributes.put(key, value);
        }
        return attributes;
    }

    private void serializeTimeFormat(final DataOutput out, final TimeFormat timeFormat) throws IOException {
        serialize(out, timeFormat.name());
    }

    private TimeFormat deserializeTimeFormat(final DataInput is) throws IOException, ClassNotFoundException {
        String name = (String) deserialize(is);

        TimeFormat tf = TimeFormat.valueOf(name);
        model.store.timeFormat = tf;

        return tf;
    }

    private void serializeTimeZone(final DataOutput out, final DateTimeZone timeZone) throws IOException {
        serialize(out, timeZone.getID());
    }

    private DateTimeZone deserializeTimeZone(final DataInput is) throws IOException, ClassNotFoundException {
        String id = (String) deserialize(is);

        DateTimeZone tz = DateTimeZone.forID(id);
        model.store.timeZone = tz;

        return tz;
    }

    private void serializeTimeStore(final DataOutput out) throws IOException {
        TimeStore timeStore = model.store.timeStore;

        serialize(out, timeStore.nodeIndexStore);
        serialize(out, timeStore.edgeIndexStore);
    }

    private void serializeInterval(final DataOutput out, final Interval interval) throws IOException {
        serialize(out, interval.getLow());
        serialize(out, interval.getHigh());
    }

    private Interval deserializeInterval(final DataInput is) throws IOException, ClassNotFoundException {
        double start = (Double) deserialize(is);
        double end = (Double) deserialize(is);
        return new Interval(start, end);
    }

    private TimeStore deserializeTimeStore(final DataInput is) throws IOException, ClassNotFoundException {
        TimeStore timeStore = model.store.timeStore;

        deserialize(is);
        deserialize(is);

        return timeStore;
    }

    private void serializeConfiguration(final DataOutput out) throws IOException {
        Configuration config = model.store.configuration;

        serialize(out, config.getNodeIdType());
        serialize(out, config.getEdgeIdType());
        serialize(out, config.getEdgeLabelType());
        serialize(out, config.getEdgeWeightType());
        serialize(out, config.getTimeRepresentation());
        serialize(out, config.getEdgeWeightColumn());
    }

    private Configuration deserializeConfiguration(final DataInput is) throws IOException, ClassNotFoundException {
        Configuration config = new Configuration();

        Class nodeIdType = (Class) deserialize(is);
        Class edgeIdType = (Class) deserialize(is);
        Class edgeLabelType = (Class) deserialize(is);
        Class edgeWeightType = (Class) deserialize(is);
        TimeRepresentation timeRepresentation = (TimeRepresentation) deserialize(is);

        config.setNodeIdType(nodeIdType);
        config.setEdgeIdType(edgeIdType);
        config.setEdgeLabelType(edgeLabelType);
        config.setEdgeWeightType(edgeWeightType);
        config.setTimeRepresentation(timeRepresentation);
        if (readVersion >= 0.5) {
            Boolean edgeColumn = (Boolean) deserialize(is);
            config.setEdgeWeightColumn(edgeColumn);
        }

        return config;
    }

    private void serializeList(final DataOutput out, final List list) throws IOException {
        Class oCls = list.getClass();
        if (oCls.equals(IntArrayList.class)) {
            serialize(out, ((IntArrayList) list).toIntArray());
        } else if (oCls.equals(FloatArrayList.class)) {
            serialize(out, ((FloatArrayList) list).toFloatArray());
        } else if (oCls.equals(DoubleArrayList.class)) {
            serialize(out, ((DoubleArrayList) list).toDoubleArray());
        } else if (oCls.equals(ShortArrayList.class)) {
            serialize(out, ((ShortArrayList) list).toShortArray());
        } else if (oCls.equals(ByteArrayList.class)) {
            serialize(out, ((ByteArrayList) list).toByteArray());
        } else if (oCls.equals(LongArrayList.class)) {
            serialize(out, ((LongArrayList) list).toLongArray());
        } else if (oCls.equals(BooleanArrayList.class)) {
            serialize(out, ((BooleanArrayList) list).toBooleanArray());
        } else if (oCls.equals(CharArrayList.class)) {
            serialize(out, ((CharArrayList) list).toCharArray());
        } else {
            serialize(out, list.size());
            for (Object obj : list) {
                serialize(out, obj);
            }
        }
    }

    private List deserializeList(final DataInput is) throws IOException, ClassNotFoundException {
        Object h = deserialize(is);
        Class oCls = h.getClass();
        if (oCls.equals(Integer.class)) {
            int size = (Integer) h;
            ObjectArrayList list = new ObjectArrayList(size);
            for (int i = 0; i < size; i++) {
                list.add(deserialize(is));
            }
            return list;
        } else if (oCls.equals(int[].class)) {
            return new IntArrayList((int[]) h);
        } else if (oCls.equals(float[].class)) {
            return new FloatArrayList((float[]) h);
        } else if (oCls.equals(double[].class)) {
            return new DoubleArrayList((double[]) h);
        } else if (oCls.equals(short[].class)) {
            return new ShortArrayList((short[]) h);
        } else if (oCls.equals(byte[].class)) {
            return new ByteArrayList((byte[]) h);
        } else if (oCls.equals(long[].class)) {
            return new LongArrayList((long[]) h);
        } else if (oCls.equals(boolean[].class)) {
            return new BooleanArrayList((boolean[]) h);
        } else if (oCls.equals(char[].class)) {
            return new CharArrayList((char[]) h);
        }
        throw new EOFException();
    }

    private void serializeSet(final DataOutput out, final Set set) throws IOException {
        Class oCls = set.getClass();
        if (oCls.equals(IntOpenHashSet.class)) {
            serialize(out, ((IntOpenHashSet) set).toIntArray());
        } else if (oCls.equals(FloatOpenHashSet.class)) {
            serialize(out, ((FloatOpenHashSet) set).toFloatArray());
        } else if (oCls.equals(DoubleOpenHashSet.class)) {
            serialize(out, ((DoubleOpenHashSet) set).toDoubleArray());
        } else if (oCls.equals(ShortOpenHashSet.class)) {
            serialize(out, ((ShortOpenHashSet) set).toShortArray());
        } else if (oCls.equals(ByteOpenHashSet.class)) {
            serialize(out, ((ByteOpenHashSet) set).toByteArray());
        } else if (oCls.equals(LongOpenHashSet.class)) {
            serialize(out, ((LongOpenHashSet) set).toLongArray());
        } else if (oCls.equals(BooleanOpenHashSet.class)) {
            serialize(out, ((BooleanOpenHashSet) set).toBooleanArray());
        } else if (oCls.equals(CharOpenHashSet.class)) {
            serialize(out, ((CharOpenHashSet) set).toCharArray());
        } else {
            serialize(out, set.size());
            for (Object obj : set) {
                serialize(out, obj);
            }
        }
    }

    private Set deserializeSet(final DataInput is) throws IOException, ClassNotFoundException {
        Object h = deserialize(is);
        Class oCls = h.getClass();
        if (oCls.equals(Integer.class)) {
            int size = (Integer) h;
            ObjectOpenHashSet set = new ObjectOpenHashSet(size);
            for (int i = 0; i < size; i++) {
                set.add(deserialize(is));
            }
            return set;
        } else if (oCls.equals(int[].class)) {
            return new IntOpenHashSet((int[]) h);
        } else if (oCls.equals(float[].class)) {
            return new FloatOpenHashSet((float[]) h);
        } else if (oCls.equals(double[].class)) {
            return new DoubleOpenHashSet((double[]) h);
        } else if (oCls.equals(short[].class)) {
            return new ShortOpenHashSet((short[]) h);
        } else if (oCls.equals(byte[].class)) {
            return new ByteOpenHashSet((byte[]) h);
        } else if (oCls.equals(long[].class)) {
            return new LongOpenHashSet((long[]) h);
        } else if (oCls.equals(boolean[].class)) {
            return new BooleanOpenHashSet((boolean[]) h);
        } else if (oCls.equals(char[].class)) {
            return new CharOpenHashSet((char[]) h);
        }
        throw new EOFException();
    }

    private void serializeMap(final DataOutput out, final Map map) throws IOException {
        Class oCls = map.getClass();
        if (oCls.equals(Int2ObjectOpenHashMap.class)) {
            serialize(out, ((Int2ObjectOpenHashMap) map).keySet().toIntArray());
            serialize(out, map.values().toArray());
        } else if (oCls.equals(Float2ObjectOpenHashMap.class)) {
            serialize(out, ((Float2ObjectOpenHashMap) map).keySet().toFloatArray());
            serialize(out, map.values().toArray());
        } else if (oCls.equals(Double2ObjectOpenHashMap.class)) {
            serialize(out, ((Double2ObjectOpenHashMap) map).keySet().toDoubleArray());
            serialize(out, map.values().toArray());
        } else if (oCls.equals(Short2ObjectOpenHashMap.class)) {
            serialize(out, ((Short2ObjectOpenHashMap) map).keySet().toShortArray());
            serialize(out, map.values().toArray());
        } else if (oCls.equals(Long2ObjectOpenHashMap.class)) {
            serialize(out, ((Long2ObjectOpenHashMap) map).keySet().toLongArray());
            serialize(out, map.values().toArray());
        } else if (oCls.equals(Byte2ObjectOpenHashMap.class)) {
            serialize(out, ((Byte2ObjectOpenHashMap) map).keySet().toByteArray());
            serialize(out, map.values().toArray());
        } else if (oCls.equals(Char2ObjectOpenHashMap.class)) {
            serialize(out, ((Char2ObjectOpenHashMap) map).keySet().toCharArray());
            serialize(out, map.values().toArray());
        } else {
            serialize(out, map.size());

            Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
            for (Map.Entry entry : entrySet) {
                serialize(out, entry.getKey());
                serialize(out, entry.getValue());
            }
        }
    }

    private Map deserializeMap(final DataInput is) throws IOException, ClassNotFoundException {
        Object h = deserialize(is);
        Class oCls = h.getClass();
        if (oCls.equals(Integer.class)) {
            int size = (Integer) h;
            Object2ObjectOpenHashMap set = new Object2ObjectOpenHashMap(size);
            for (int i = 0; i < size; i++) {
                set.put(deserialize(is), deserialize(is));
            }
            return set;
        } else if (oCls.equals(int[].class)) {
            return new Int2ObjectOpenHashMap((int[]) h, (Object[]) deserialize(is));
        } else if (oCls.equals(float[].class)) {
            return new Float2ObjectOpenHashMap((float[]) h, (Object[]) deserialize(is));
        } else if (oCls.equals(double[].class)) {
            return new Double2ObjectOpenHashMap((double[]) h, (Object[]) deserialize(is));
        } else if (oCls.equals(short[].class)) {
            return new Short2ObjectOpenHashMap((short[]) h, (Object[]) deserialize(is));
        } else if (oCls.equals(byte[].class)) {
            return new Byte2ObjectOpenHashMap((byte[]) h, (Object[]) deserialize(is));
        } else if (oCls.equals(long[].class)) {
            return new Long2ObjectOpenHashMap((long[]) h, (Object[]) deserialize(is));
        } else if (oCls.equals(char[].class)) {
            return new Char2ObjectOpenHashMap((char[]) h, (Object[]) deserialize(is));
        }
        throw new EOFException();
    }

    // SERIALIZE PRIMITIVES
    protected byte[] serialize(Object obj) throws IOException {
        DataInputOutput ba = new DataInputOutput();

        serialize(ba, obj);

        return ba.toByteArray();
    }

    protected void serialize(final DataOutput out, final Object obj) throws IOException {
        final Class clazz = obj != null ? obj.getClass() : null;

        if (obj == null) {
            out.write(NULL);

        } else if (clazz == Boolean.class) {
            if ((Boolean) obj) {
                out.write(BOOLEAN_TRUE);
            } else {
                out.write(BOOLEAN_FALSE);

            }
        } else if (clazz == Integer.class) {
            final int val = (Integer) obj;
            writeInteger(out, val);

        } else if (clazz == Double.class) {
            double v = (Double) obj;
            if (v == -1d) {
                out.write(DOUBLE_MINUS_1);
            } else if (v == 0d) {
                out.write(DOUBLE_0);
            } else if (v == 1d) {
                out.write(DOUBLE_1);
            } else if (v >= 0 && v <= 255 && (int) v == v) {
                out.write(DOUBLE_255);
                out.write((int) v);
            } else if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE && (short) v == v) {
                out.write(DOUBLE_SHORT);
                out.writeShort((int) v);
            } else {
                out.write(DOUBLE_FULL);
                out.writeDouble(v);

            }
        } else if (clazz == Float.class) {
            float v = (Float) obj;
            if (v == -1f) {
                out.write(FLOAT_MINUS_1);
            } else if (v == 0f) {
                out.write(FLOAT_0);
            } else if (v == 1f) {
                out.write(FLOAT_1);
            } else if (v >= 0 && v <= 255 && (int) v == v) {
                out.write(FLOAT_255);
                out.write((int) v);
            } else if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE && (short) v == v) {
                out.write(FLOAT_SHORT);
                out.writeShort((int) v);

            } else {
                out.write(FLOAT_FULL);
                out.writeFloat(v);

            }
        } else if (clazz == Long.class) {
            final long val = (Long) obj;
            writeLong(out, val);

        } else if (clazz == BigInteger.class) {
            out.write(BIGINTEGER);
            byte[] buf = ((BigInteger) obj).toByteArray();
            serializeByteArrayInt(out, buf);

        } else if (clazz == BigDecimal.class) {
            out.write(BIGDECIMAL);
            BigDecimal d = (BigDecimal) obj;
            serializeByteArrayInt(out, d.unscaledValue().toByteArray());
            LongPacker.packInt(out, d.scale());

        } else if (clazz == Short.class) {
            short val = (Short) obj;
            if (val == -1) {
                out.write(SHORT_MINUS_1);
            } else if (val == 0) {
                out.write(SHORT_0);
            } else if (val == 1) {
                out.write(SHORT_1);
            } else if (val > 0 && val < 255) {
                out.write(SHORT_255);
                out.write(val);
            } else {
                out.write(SHORT_FULL);
                out.writeShort(val);

            }
        } else if (clazz == Byte.class) {
            byte val = (Byte) obj;
            if (val == -1) {
                out.write(BYTE_MINUS_1);
            } else if (val == 0) {
                out.write(BYTE_0);
            } else if (val == 1) {
                out.write(BYTE_1);
            } else {
                out.write(BYTE_FULL);
                out.writeByte(val);

            }
        } else if (clazz == Character.class) {
            out.write(CHAR);
            out.writeChar((Character) obj);

        } else if (clazz == String.class) {
            String s = (String) obj;
            if (s.length() == 0) {
                out.write(STRING_EMPTY);
            } else {
                out.write(STRING);
                serializeString(out, s);
            }
        } else if (obj instanceof Class) {
            out.write(CLASS);
            serialize(out, ((Class) obj).getName());
        } else if (obj instanceof int[]) {
            writeIntArray(out, (int[]) obj);
        } else if (obj instanceof long[]) {
            writeLongArray(out, (long[]) obj);
        } else if (obj instanceof short[]) {
            out.write(SHORT_ARRAY);
            short[] a = (short[]) obj;
            LongPacker.packInt(out, a.length);
            for (short s : a) {
                out.writeShort(s);
            }
        } else if (obj instanceof boolean[]) {
            out.write(BOOLEAN_ARRAY);
            boolean[] a = (boolean[]) obj;
            LongPacker.packInt(out, a.length);
            for (boolean s : a) {
                out.writeBoolean(s); // TODO pack 8 booleans to single byte
            }
        } else if (obj instanceof double[]) {
            out.write(DOUBLE_ARRAY);
            double[] a = (double[]) obj;
            LongPacker.packInt(out, a.length);
            for (double s : a) {
                out.writeDouble(s);
            }
        } else if (obj instanceof float[]) {
            out.write(FLOAT_ARRAY);
            float[] a = (float[]) obj;
            LongPacker.packInt(out, a.length);
            for (float s : a) {
                out.writeFloat(s);
            }
        } else if (obj instanceof char[]) {
            out.write(CHAR_ARRAY);
            char[] a = (char[]) obj;
            LongPacker.packInt(out, a.length);
            for (char s : a) {
                out.writeChar(s);
            }
        } else if (obj instanceof byte[]) {
            byte[] b = (byte[]) obj;
            out.write(ARRAY_BYTE_INT);
            serializeByteArrayInt(out, b);

        } else if (clazz == Date.class) {
            out.write(DATE);
            out.writeLong(((Date) obj).getTime());

        } else if (clazz == Locale.class) {
            out.write(LOCALE);
            Locale l = (Locale) obj;
            out.writeUTF(l.getLanguage());
            out.writeUTF(l.getCountry());
            out.writeUTF(l.getVariant());
        } else if (obj instanceof String[]) {
            String[] b = (String[]) obj;
            out.write(STRING_ARRAY);
            LongPacker.packInt(out, b.length);
            for (String s : b) {
                serializeString(out, s);
            }
        } else if (obj instanceof Object[]) {
            Object[] b = (Object[]) obj;
            out.write(ARRAY_OBJECT);
            LongPacker.packInt(out, b.length);
            for (Object o : b) {
                serialize(out, o);
            }
        } else if (obj instanceof TimestampSet) {
            TimestampSet b = (TimestampSet) obj;
            out.write(TIMESTAMP_SET);
            serializeTimestampSet(out, b);
        } else if (obj instanceof IntervalSet) {
            IntervalSet b = (IntervalSet) obj;
            out.write(INTERVAL_SET);
            serializeIntervalSet(out, b);
        } else if (obj instanceof NodeImpl) {
            NodeImpl b = (NodeImpl) obj;
            out.write(NODE);
            serializeNode(out, b);
        } else if (obj instanceof EdgeImpl) {
            EdgeImpl b = (EdgeImpl) obj;
            out.write(EDGE);
            serializeEdge(out, b);
        } else if (obj instanceof EdgeTypeStore) {
            EdgeTypeStore b = (EdgeTypeStore) obj;
            out.write(EDGETYPE_STORE);
            serializeEdgeTypeStore(out);
        } else if (obj instanceof Origin) {
            Origin b = (Origin) obj;
            out.write(COLUMN_ORIGIN);
            serialize(out, b.name());
        } else if (obj instanceof TableImpl) {
            TableImpl b = (TableImpl) obj;
            out.write(TABLE);
            serializeTable(out, b);
        } else if (obj instanceof GraphStore) {
            GraphStore b = (GraphStore) obj;
            out.write(GRAPH_STORE);
            serializeGraphStore(out, b);
        } else if (obj instanceof GraphFactoryImpl) {
            GraphFactoryImpl b = (GraphFactoryImpl) obj;
            out.write(GRAPH_FACTORY);
            serializeGraphFactory(out);
        } else if (obj instanceof GraphViewStore) {
            GraphViewStore b = (GraphViewStore) obj;
            out.write(GRAPH_VIEW_STORE);
            serializeViewStore(out);
        } else if (obj instanceof GraphViewImpl) {
            GraphViewImpl b = (GraphViewImpl) obj;
            out.write(GRAPH_VIEW);
            serializeGraphView(out, b);
        } else if (obj instanceof BitVector) {
            BitVector bv = (BitVector) obj;
            out.write(BIT_VECTOR);
            serializeBitVector(out, bv);
        } else if (obj instanceof GraphVersion) {
            GraphVersion b = (GraphVersion) obj;
            out.write(GRAPH_VERSION);
            serializeGraphVersion(out, b);
        } else if (obj instanceof NodeImpl.NodePropertiesImpl) {
            NodeImpl.NodePropertiesImpl b = (NodeImpl.NodePropertiesImpl) obj;
            out.write(NODE_PROPERTIES);
            serializeNodeProperties(out, b);
        } else if (obj instanceof EdgeImpl.EdgePropertiesImpl) {
            EdgeImpl.EdgePropertiesImpl b = (EdgeImpl.EdgePropertiesImpl) obj;
            out.write(EDGE_PROPERTIES);
            serializeEdgeProperties(out, b);
        } else if (obj instanceof TextPropertiesImpl) {
            TextPropertiesImpl b = (TextPropertiesImpl) obj;
            out.write(TEXT_PROPERTIES);
            serializeTextProperties(out, b);
        } else if (obj instanceof Estimator) {
            Estimator b = (Estimator) obj;
            out.write(ESTIMATOR);
            serializeString(out, b.name());
        } else if (obj instanceof TimeRepresentation) {
            TimeRepresentation b = (TimeRepresentation) obj;
            out.write(TIME_REPRESENTATION);
            serializeString(out, b.name());
        } else if (obj instanceof AbstractTimestampMap) {
            AbstractTimestampMap b = (AbstractTimestampMap) obj;
            out.write(TIMESTAMP_MAP);
            serializeTimestampMap(out, b);
        } else if (obj instanceof AbstractIntervalMap) {
            AbstractIntervalMap b = (AbstractIntervalMap) obj;
            out.write(INTERVAL_MAP);
            serializeIntervalMap(out, b);
        } else if (obj instanceof TimestampIndexStore) {
            TimestampIndexStore b = (TimestampIndexStore) obj;
            out.write(TIMESTAMP_INDEX_STORE);
            serializeTimestampIndexStore(out, b);
        } else if (obj instanceof IntervalIndexStore) {
            IntervalIndexStore b = (IntervalIndexStore) obj;
            out.write(INTERVAL_INDEX_STORE);
            serializeIntervalIndexStore(out, b);
        } else if (obj instanceof GraphAttributesImpl) {
            GraphAttributesImpl b = (GraphAttributesImpl) obj;
            out.write(GRAPH_ATTRIBUTES);
            serializeGraphAttributes(out, b);
        } else if (obj instanceof TimeFormat) {
            TimeFormat b = (TimeFormat) obj;
            out.write(TIME_FORMAT);
            serializeTimeFormat(out, b);
        } else if (obj instanceof DateTimeZone) {
            DateTimeZone b = (DateTimeZone) obj;
            out.write(TIME_ZONE);
            serializeTimeZone(out, b);
        } else if (obj instanceof TimeStore) {
            TimeStore b = (TimeStore) obj;
            out.write(TIME_STORE);
            serializeTimeStore(out);
        } else if (obj instanceof Configuration) {
            Configuration b = (Configuration) obj;
            out.write(CONFIGURATION);
            serializeConfiguration(out);
        } else if (obj instanceof Interval) {
            Interval b = (Interval) obj;
            out.write(INTERVAL);
            serializeInterval(out, b);
        } else if (obj instanceof List) {
            List b = (List) obj;
            out.write(LIST);
            serializeList(out, b);
        } else if (obj instanceof Set) {
            Set b = (Set) obj;
            out.write(SET);
            serializeSet(out, b);
        } else if (obj instanceof Map) {
            Map b = (Map) obj;
            out.write(MAP);
            serializeMap(out, b);
        } else {
            throw new IOException("No serialization handler for this class: " + clazz.getName());
        }
    }

    public static void serializeString(DataOutput out, String obj) throws IOException {
        final int len = obj.length();
        LongPacker.packInt(out, len);
        for (int i = 0; i < len; i++) {
            int c = (int) obj.charAt(i); // TODO investigate if c could be
            // negative here
            LongPacker.packInt(out, c);
        }
    }

    private void serializeByteArrayInt(DataOutput out, byte[] b) throws IOException {
        LongPacker.packInt(out, b.length);
        out.write(b);
    }

    private void writeLongArray(DataOutput da, long[] obj) throws IOException {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        for (long i : obj) {
            max = Math.max(max, i);
            min = Math.min(min, i);
        }

        if (0 <= min && max <= 255) {
            da.write(ARRAY_LONG_B);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                da.write((int) l);
            }
        } else if (0 <= min && max <= Long.MAX_VALUE) {
            da.write(ARRAY_LONG_PACKED);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                LongPacker.packLong(da, l);
            }
        } else if (Short.MIN_VALUE <= min && max <= Short.MAX_VALUE) {
            da.write(ARRAY_LONG_S);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                da.writeShort((short) l);
            }
        } else if (Integer.MIN_VALUE <= min && max <= Integer.MAX_VALUE) {
            da.write(ARRAY_LONG_I);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                da.writeInt((int) l);
            }
        } else {
            da.write(ARRAY_LONG_L);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                da.writeLong(l);
            }
        }
    }

    private void writeIntArray(DataOutput da, int[] obj) throws IOException {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (int i : obj) {
            max = Math.max(max, i);
            min = Math.min(min, i);
        }

        boolean fitsInByte = 0 <= min && max <= 255;
        boolean fitsInShort = min >= Short.MIN_VALUE && max <= Short.MAX_VALUE;

        if (obj.length <= 255 && fitsInByte) {
            da.write(ARRAY_INT_B_255);
            da.write(obj.length);
            for (int i : obj) {
                da.write(i);
            }
        } else if (fitsInByte) {
            da.write(ARRAY_INT_B_INT);
            LongPacker.packInt(da, obj.length);
            for (int i : obj) {
                da.write(i);
            }
        } else if (0 <= min && max <= Integer.MAX_VALUE) {
            da.write(ARRAY_INT_PACKED);
            LongPacker.packInt(da, obj.length);
            for (int l : obj) {
                LongPacker.packInt(da, l);
            }
        } else if (fitsInShort) {
            da.write(ARRAY_INT_S);
            LongPacker.packInt(da, obj.length);
            for (int i : obj) {
                da.writeShort(i);
            }
        } else {
            da.write(ARRAY_INT_I);
            LongPacker.packInt(da, obj.length);
            for (int i : obj) {
                da.writeInt(i);
            }
        }

    }

    private void writeInteger(DataOutput da, final int val) throws IOException {
        if (val == -1) {
            da.write(INTEGER_MINUS_1);
        } else if (val == 0) {
            da.write(INTEGER_0);
        } else if (val == 1) {
            da.write(INTEGER_1);
        } else if (val == 2) {
            da.write(INTEGER_2);
        } else if (val == 3) {
            da.write(INTEGER_3);
        } else if (val == 4) {
            da.write(INTEGER_4);
        } else if (val == 5) {
            da.write(INTEGER_5);
        } else if (val == 6) {
            da.write(INTEGER_6);
        } else if (val == 7) {
            da.write(INTEGER_7);
        } else if (val == 8) {
            da.write(INTEGER_8);
        } else if (val == Integer.MIN_VALUE) {
            da.write(INTEGER_MINUS_MAX);
        } else if (val > 0 && val < 255) {
            da.write(INTEGER_255);
            da.write(val);
        } else if (val < 0) {
            da.write(INTEGER_PACK_NEG);
            LongPacker.packInt(da, -val);
        } else {
            da.write(INTEGER_PACK);
            LongPacker.packInt(da, val);
        }
    }

    private void writeLong(DataOutput da, final long val) throws IOException {
        if (val == -1) {
            da.write(LONG_MINUS_1);
        } else if (val == 0) {
            da.write(LONG_0);
        } else if (val == 1) {
            da.write(LONG_1);
        } else if (val == 2) {
            da.write(LONG_2);
        } else if (val == 3) {
            da.write(LONG_3);
        } else if (val == 4) {
            da.write(LONG_4);
        } else if (val == 5) {
            da.write(LONG_5);
        } else if (val == 6) {
            da.write(LONG_6);
        } else if (val == 7) {
            da.write(LONG_7);
        } else if (val == 8) {
            da.write(LONG_8);
        } else if (val == Long.MIN_VALUE) {
            da.write(LONG_MINUS_MAX);
        } else if (val > 0 && val < 255) {
            da.write(LONG_255);
            da.write((int) val);
        } else if (val < 0) {
            da.write(LONG_PACK_NEG);
            LongPacker.packLong(da, -val);
        } else {
            da.write(LONG_PACK);
            LongPacker.packLong(da, val);
        }
    }

    // DESERIALIZE PRIMITIVES
    protected Object deserialize(byte[] buf) throws ClassNotFoundException, IOException {
        DataInputOutput bs = new DataInputOutput(buf);
        Object ret = deserialize(bs);
        if (bs.available() != 0) {
            throw new RuntimeException("bytes left: " + bs.available());
        }

        return ret;
    }

    protected Object deserialize(DataInput is) throws IOException, ClassNotFoundException {
        Object ret = null;

        final int head = is.readUnsignedByte();

        switch (head) {
            case NULL:
                break;
            case BOOLEAN_TRUE:
                ret = Boolean.TRUE;
                break;
            case BOOLEAN_FALSE:
                ret = Boolean.FALSE;
                break;
            case INTEGER_MINUS_1:
                ret = -1;
                break;
            case INTEGER_0:
                ret = 0;
                break;
            case INTEGER_1:
                ret = 1;
                break;
            case INTEGER_2:
                ret = 2;
                break;
            case INTEGER_3:
                ret = 3;
                break;
            case INTEGER_4:
                ret = 4;
                break;
            case INTEGER_5:
                ret = 5;
                break;
            case INTEGER_6:
                ret = 6;
                break;
            case INTEGER_7:
                ret = 7;
                break;
            case INTEGER_8:
                ret = 8;
                break;
            case INTEGER_MINUS_MAX:
                ret = Integer.MIN_VALUE;
                break;
            case INTEGER_255:
                ret = is.readUnsignedByte();
                break;
            case INTEGER_PACK_NEG:
                ret = -LongPacker.unpackInt(is);
                break;
            case INTEGER_PACK:
                ret = LongPacker.unpackInt(is);
                break;
            case LONG_MINUS_1:
                ret = Long.valueOf(-1);
                break;
            case LONG_0:
                ret = Long.valueOf(0);
                break;
            case LONG_1:
                ret = Long.valueOf(1);
                break;
            case LONG_2:
                ret = Long.valueOf(2);
                break;
            case LONG_3:
                ret = Long.valueOf(3);
                break;
            case LONG_4:
                ret = Long.valueOf(4);
                break;
            case LONG_5:
                ret = Long.valueOf(5);
                break;
            case LONG_6:
                ret = Long.valueOf(6);
                break;
            case LONG_7:
                ret = Long.valueOf(7);
                break;
            case LONG_8:
                ret = Long.valueOf(8);
                break;
            case LONG_255:
                ret = Long.valueOf(is.readUnsignedByte());
                break;
            case LONG_PACK_NEG:
                ret = -LongPacker.unpackLong(is);
                break;
            case LONG_PACK:
                ret = LongPacker.unpackLong(is);
                break;
            case LONG_MINUS_MAX:
                ret = Long.MIN_VALUE;
                break;
            case SHORT_MINUS_1:
                ret = (short) -1;
                break;
            case SHORT_0:
                ret = (short) 0;
                break;
            case SHORT_1:
                ret = (short) 1;
                break;
            case SHORT_255:
                ret = (short) is.readUnsignedByte();
                break;
            case SHORT_FULL:
                ret = is.readShort();
                break;
            case BYTE_MINUS_1:
                ret = (byte) -1;
                break;
            case BYTE_0:
                ret = (byte) 0;
                break;
            case BYTE_1:
                ret = (byte) 1;
                break;
            case BYTE_FULL:
                ret = is.readByte();
                break;
            case SHORT_ARRAY:
                int size = LongPacker.unpackInt(is);
                ret = new short[size];
                for (int i = 0; i < size; i++) {
                    ((short[]) ret)[i] = is.readShort();
                }
                break;
            case BOOLEAN_ARRAY:
                size = LongPacker.unpackInt(is);
                ret = new boolean[size];
                for (int i = 0; i < size; i++) {
                    ((boolean[]) ret)[i] = is.readBoolean();
                }
                break;
            case DOUBLE_ARRAY:
                size = LongPacker.unpackInt(is);
                ret = new double[size];
                for (int i = 0; i < size; i++) {
                    ((double[]) ret)[i] = is.readDouble();
                }
                break;
            case FLOAT_ARRAY:
                size = LongPacker.unpackInt(is);
                ret = new float[size];
                for (int i = 0; i < size; i++) {
                    ((float[]) ret)[i] = is.readFloat();
                }
                break;
            case CHAR_ARRAY:
                size = LongPacker.unpackInt(is);
                ret = new char[size];
                for (int i = 0; i < size; i++) {
                    ((char[]) ret)[i] = is.readChar();
                }
                break;
            case CHAR:
                ret = is.readChar();
                break;
            case FLOAT_MINUS_1:
                ret = Float.valueOf(-1);
                break;
            case FLOAT_0:
                ret = Float.valueOf(0);
                break;
            case FLOAT_1:
                ret = Float.valueOf(1);
                break;
            case FLOAT_255:
                ret = Float.valueOf(is.readUnsignedByte());
                break;
            case FLOAT_SHORT:
                ret = Float.valueOf(is.readShort());
                break;
            case FLOAT_FULL:
                ret = is.readFloat();
                break;
            case DOUBLE_MINUS_1:
                ret = Double.valueOf(-1);
                break;
            case DOUBLE_0:
                ret = Double.valueOf(0);
                break;
            case DOUBLE_1:
                ret = Double.valueOf(1);
                break;
            case DOUBLE_255:
                ret = Double.valueOf(is.readUnsignedByte());
                break;
            case DOUBLE_SHORT:
                ret = Double.valueOf(is.readShort());
                break;
            case DOUBLE_FULL:
                ret = is.readDouble();
                break;
            case BIGINTEGER:
                ret = new BigInteger(deserializeArrayByteInt(is));
                break;
            case BIGDECIMAL:
                ret = new BigDecimal(new BigInteger(deserializeArrayByteInt(is)), LongPacker.unpackInt(is));
                break;
            case STRING:
                ret = deserializeString(is);
                break;
            case STRING_EMPTY:
                ret = EMPTY_STRING;
                break;
            case CLASS:
                ret = deserializeClass(is);
                break;
            case DATE:
                ret = new Date(is.readLong());
                break;
            case ARRAY_INT_B_255:
                ret = deserializeArrayIntB255(is);
                break;
            case ARRAY_INT_B_INT:
                ret = deserializeArrayIntBInt(is);
                break;
            case ARRAY_INT_S:
                ret = deserializeArrayIntSInt(is);
                break;
            case ARRAY_INT_I:
                ret = deserializeArrayIntIInt(is);
                break;
            case ARRAY_INT_PACKED:
                ret = deserializeArrayIntPack(is);
                break;
            case ARRAY_LONG_B:
                ret = deserializeArrayLongB(is);
                break;
            case ARRAY_LONG_S:
                ret = deserializeArrayLongS(is);
                break;
            case ARRAY_LONG_I:
                ret = deserializeArrayLongI(is);
                break;
            case ARRAY_LONG_L:
                ret = deserializeArrayLongL(is);
                break;
            case ARRAY_LONG_PACKED:
                ret = deserializeArrayLongPack(is);
                break;
            case ARRAY_BYTE_INT:
                ret = deserializeArrayByteInt(is);
                break;
            case LOCALE:
                ret = new Locale(is.readUTF(), is.readUTF(), is.readUTF());
                break;
            case STRING_ARRAY:
                ret = deserializeStringArray(is);
                break;
            case ARRAY_OBJECT:
                ret = deserializeArrayObject(is);
                break;
            case TIMESTAMP_SET:
                ret = deserializeTimestampSet(is);
                break;
            case INTERVAL_SET:
                ret = deserializeIntervalSet(is);
                break;
            case NODE:
                ret = deserializeNode(is);
                break;
            case EDGE:
                ret = deserializeEdge(is);
                break;
            case EDGETYPE_STORE:
                ret = deserializeEdgeTypeStore(is);
                break;
            case COLUMN_ORIGIN:
                ret = Origin.valueOf((String) deserialize(is));
                break;
            case TABLE:
                ret = deserializeTable(is);
                break;
            case GRAPH_STORE:
                ret = deserializeGraphStore(is);
                break;
            case GRAPH_FACTORY:
                ret = deserializeGraphFactory(is);
                break;
            case GRAPH_VIEW_STORE:
                ret = deserializeViewStore(is);
                break;
            case GRAPH_VIEW:
                ret = deserializeGraphView(is);
                break;
            case BIT_VECTOR:
                ret = deserializeBitVector(is);
                break;
            case GRAPH_STORE_CONFIGURATION:
                ret = deserializeGraphStoreConfiguration(is);
                break;
            case GRAPH_VERSION:
                ret = deserializeGraphVersion(is);
                break;
            case NODE_PROPERTIES:
                ret = deserializeNodeProperties(is);
                break;
            case EDGE_PROPERTIES:
                ret = deserializeEdgeProperties(is);
                break;
            case TEXT_PROPERTIES:
                ret = deserializeTextProperties(is);
                break;
            case ESTIMATOR:
                ret = Estimator.valueOf(deserializeString(is));
                break;
            case TIME_REPRESENTATION:
                ret = TimeRepresentation.valueOf(deserializeString(is));
                break;
            case TIMESTAMP_MAP:
                ret = deserializeTimestampMap(is);
                break;
            case INTERVAL_MAP:
                ret = deserializeIntervalMap(is);
                break;
            case TIMESTAMP_INDEX_STORE:
                ret = deserializeTimestampIndexStore(is);
                break;
            case INTERVAL_INDEX_STORE:
                ret = deserializeIntervalIndexStore(is);
                break;
            case GRAPH_ATTRIBUTES:
                ret = deserializeGraphAttributes(is);
                break;
            case TIME_FORMAT:
                ret = deserializeTimeFormat(is);
                break;
            case TIME_ZONE:
                ret = deserializeTimeZone(is);
                break;
            case TIME_STORE:
                ret = deserializeTimeStore(is);
                break;
            case CONFIGURATION:
                ret = deserializeConfiguration(is);
                break;
            case INTERVAL:
                ret = deserializeInterval(is);
                break;
            case LIST:
                ret = deserializeList(is);
                break;
            case SET:
                ret = deserializeSet(is);
                break;
            case MAP:
                ret = deserializeMap(is);
                break;
            case -1:
                throw new EOFException();

        }
        return ret;
    }

    public static String deserializeString(DataInput buf) throws IOException {
        int len = LongPacker.unpackInt(buf);
        char[] b = new char[len];
        for (int i = 0; i < len; i++) {
            b[i] = (char) LongPacker.unpackInt(buf);
        }

        return new String(b);
    }

    private Class deserializeClass(DataInput is) throws IOException, ClassNotFoundException {
        String className = (String) deserialize(is);
        Class cls = Class.forName(className);
        return cls;
    }

    private byte[] deserializeArrayByteInt(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        byte[] b = new byte[size];
        is.readFully(b);
        return b;
    }

    private long[] deserializeArrayLongL(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readLong();
        }
        return ret;
    }

    private long[] deserializeArrayLongI(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readInt();
        }
        return ret;
    }

    private long[] deserializeArrayLongS(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readShort();
        }
        return ret;
    }

    private long[] deserializeArrayLongB(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readUnsignedByte();
            if (ret[i] < 0) {
                throw new EOFException();
            }
        }
        return ret;
    }

    private int[] deserializeArrayIntIInt(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readInt();
        }
        return ret;
    }

    private int[] deserializeArrayIntSInt(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readShort();
        }
        return ret;
    }

    private int[] deserializeArrayIntBInt(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readUnsignedByte();
            if (ret[i] < 0) {
                throw new EOFException();
            }
        }
        return ret;
    }

    private int[] deserializeArrayIntPack(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        if (size < 0) {
            throw new EOFException();
        }

        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = LongPacker.unpackInt(is);
        }
        return ret;
    }

    private long[] deserializeArrayLongPack(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        if (size < 0) {
            throw new EOFException();
        }

        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = LongPacker.unpackLong(is);
        }
        return ret;
    }

    private int[] deserializeArrayIntB255(DataInput is) throws IOException {
        int size = is.readUnsignedByte();
        if (size < 0) {
            throw new EOFException();
        }

        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readUnsignedByte();
            if (ret[i] < 0) {
                throw new EOFException();
            }
        }
        return ret;
    }

    private String[] deserializeStringArray(DataInput is) throws IOException, ClassNotFoundException {
        int size = LongPacker.unpackInt(is);

        String[] s = (String[]) Array.newInstance(String.class, size);
        for (int i = 0; i < size; i++) {
            s[i] = deserializeString(is);
        }
        return s;

    }

    private Object[] deserializeArrayObject(DataInput is) throws IOException, ClassNotFoundException {
        int size = LongPacker.unpackInt(is);

        Object[] s = (Object[]) Array.newInstance(Object.class, size);
        for (int i = 0; i < size; i++) {
            s[i] = deserialize(is);
        }
        return s;

    }

    protected static class GraphStoreConfigurationVersion {

        protected final boolean enableElementLabel;
        protected final boolean enableElementTimestamp;
        protected final boolean enableNodeProperties;
        protected final boolean enableEdgeProperties;

        public GraphStoreConfigurationVersion(boolean enableElementLabel, boolean enableElementTimestamp, boolean enableNodeProperties, boolean enableEdgeProperties) {
            this.enableElementLabel = enableElementLabel;
            this.enableElementTimestamp = enableElementTimestamp;
            this.enableNodeProperties = enableNodeProperties;
            this.enableEdgeProperties = enableEdgeProperties;
        }
    }
}
