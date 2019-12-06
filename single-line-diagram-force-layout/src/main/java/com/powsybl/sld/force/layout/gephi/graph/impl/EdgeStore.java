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

import com.powsybl.sld.force.layout.gephi.graph.api.EdgeIterable;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.longs.LongHash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import com.powsybl.sld.force.layout.gephi.graph.api.Edge;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EdgeStore implements Collection<Edge>, EdgeIterable {

    // Const
    protected static final int NULL_ID = -1;
    protected static final int NODE_BITS = 31;
    protected static final Iterator<Edge> EMPTY_EDGE_ITERATOR = Collections.<Edge> emptyList().iterator();
    // Data
    protected int size;
    protected int garbageSize;
    protected int blocksCount;
    protected int currentBlockIndex;
    protected EdgeBlock[] blocks;
    protected EdgeBlock currentBlock;
    protected Object2IntOpenHashMap dictionary;
    protected Long2ObjectOpenCustomHashMap<int[]>[] longDictionary;
    // Stats
    protected int undirectedSize;
    protected int mutualEdgesSize;
    protected int[] mutualEdgesTypeSize;
    // Locking (optional)
    protected final GraphLock lock;
    // Version
    protected final GraphVersion version;
    // Types counting (optional)
    protected final EdgeTypeStore edgeTypeStore;
    // View store
    protected final GraphViewStore viewStore;

    public EdgeStore() {
        initStore();
        this.lock = null;
        this.edgeTypeStore = null;
        this.viewStore = null;
        this.version = null;
    }

    public EdgeStore(final EdgeTypeStore edgeTypeStore, final GraphLock lock, final GraphViewStore viewStore, final GraphVersion graphVersion) {
        initStore();
        this.lock = lock;
        this.edgeTypeStore = edgeTypeStore;
        this.viewStore = viewStore;
        this.version = graphVersion;
    }

    private void initStore() {
        this.size = 0;
        this.garbageSize = 0;
        this.blocksCount = 1;
        this.currentBlockIndex = 0;
        this.blocks = new EdgeBlock[GraphStoreConfiguration.EDGESTORE_DEFAULT_BLOCKS];
        this.blocks[0] = new EdgeBlock(0);
        this.currentBlock = blocks[currentBlockIndex];
        this.dictionary = new Object2IntOpenHashMap(GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE);
        this.dictionary.defaultReturnValue(NULL_ID);
        this.longDictionary = new Long2ObjectOpenCustomHashMap[GraphStoreConfiguration.EDGESTORE_DEFAULT_TYPE_COUNT];
        this.longDictionary[0] = new Long2ObjectOpenCustomHashMap(
                GraphStoreConfiguration.EDGESTORE_DEFAULT_DICTIONARY_SIZE,
                GraphStoreConfiguration.EDGESTORE_DICTIONARY_LOAD_FACTOR, new DictionaryHashStrategy());
        this.mutualEdgesTypeSize = new int[GraphStoreConfiguration.EDGESTORE_DEFAULT_TYPE_COUNT];
    }

    private void ensureCapacity(final int capacity) {
        assert capacity > 0;

        int blockCapacity = currentBlock.getCapacity();
        while (capacity > blockCapacity) {
            if (currentBlockIndex == blocksCount - 1) {
                int blocksNeeded = (int) Math
                        .ceil((capacity - blockCapacity) / (double) GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE);
                for (int i = 0; i < blocksNeeded; i++) {
                    if (blocksCount == blocks.length) {
                        EdgeBlock[] newBlocks = new EdgeBlock[blocksCount + 1];
                        System.arraycopy(blocks, 0, newBlocks, 0, blocks.length);
                        blocks = newBlocks;
                    }
                    EdgeBlock block = blocks[blocksCount];
                    if (block == null) {
                        block = new EdgeBlock(blocksCount);
                        blocks[blocksCount] = block;
                    }
                    if (blockCapacity == 0 && i == 0) {
                        currentBlockIndex = blocksCount;
                        currentBlock = block;
                    }
                    blocksCount++;
                }
                break;
            } else {
                currentBlockIndex++;
                currentBlock = blocks[currentBlockIndex];
                blockCapacity = currentBlock.getCapacity();
            }
        }
    }

    private void trimDictionary() {
        dictionary.trim(Math.max(GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE, size * 2));
    }

    private void ensureHeadOutCapacity(final NodeImpl node, final int type) {
        EdgeImpl[] out = node.headOut;
        int outLength = out.length;
        if (type >= outLength) {
            EdgeImpl[] newArray = new EdgeImpl[type + 1];
            System.arraycopy(out, 0, newArray, 0, outLength);
            node.headOut = newArray;
        }
    }

    private void ensureHeadInCapacity(NodeImpl node, final int type) {
        EdgeImpl[] in = node.headIn;
        int inLength = in.length;
        if (type >= inLength) {
            EdgeImpl[] newArray = new EdgeImpl[type + 1];
            System.arraycopy(in, 0, newArray, 0, inLength);
            node.headIn = newArray;
        }
    }

    private void trimHeadOutCapacity(NodeImpl node, int length) {
        EdgeImpl[] out = node.headOut;
        int outLength = out.length;
        if (length < outLength) {
            EdgeImpl[] newArray = new EdgeImpl[length];
            System.arraycopy(out, 0, newArray, 0, length);
            node.headOut = newArray;
        }
    }

    private void trimHeadInCapacity(NodeImpl node, int length) {
        EdgeImpl[] in = node.headIn;
        int inLength = in.length;
        if (length < inLength) {
            EdgeImpl[] newArray = new EdgeImpl[length];
            System.arraycopy(in, 0, newArray, 0, length);
            node.headIn = newArray;
        }
    }

    private void ensureLongDictionaryCapacity(int type) {
        int length = longDictionary.length;
        if (type >= length) {
            Long2ObjectOpenCustomHashMap[] newArray = new Long2ObjectOpenCustomHashMap[type + 1];
            System.arraycopy(longDictionary, 0, newArray, 0, length);
            longDictionary = newArray;
            for (int i = length; i <= type; i++) {
                Long2ObjectOpenCustomHashMap newMap = new Long2ObjectOpenCustomHashMap(
                        GraphStoreConfiguration.EDGESTORE_DEFAULT_DICTIONARY_SIZE,
                        GraphStoreConfiguration.EDGESTORE_DICTIONARY_LOAD_FACTOR, new DictionaryHashStrategy());
                longDictionary[i] = newMap;
            }
            int[] newSizeArray = new int[type + 1];
            System.arraycopy(mutualEdgesTypeSize, 0, newSizeArray, 0, length);
            mutualEdgesTypeSize = newSizeArray;
        }
    }

    private void insertOutEdge(EdgeImpl edge) {
        NodeImpl source = edge.source;
        int type = edge.type;

        ensureHeadOutCapacity(source, type);

        int edgeId = edge.getStoreId();
        EdgeImpl[] headOutArray = source.headOut;
        EdgeImpl headOutEdge = headOutArray[type];
        if (headOutEdge != null) {
            headOutEdge.previousOutEdge = edgeId;
            edge.nextOutEdge = headOutEdge.storeId;
        }
        headOutArray[type] = edge;
    }

    private void insertInEdge(EdgeImpl edge) {
        NodeImpl target = edge.target;
        int type = edge.type;

        ensureHeadInCapacity(target, type);

        int edgeId = edge.getStoreId();
        EdgeImpl[] headInArray = target.headIn;
        EdgeImpl headInEdge = headInArray[type];
        if (headInEdge != null) {
            headInEdge.previousInEdge = edgeId;
            edge.nextInEdge = headInEdge.storeId;
        }
        headInArray[type] = edge;
    }

    private void removeOutEdge(EdgeImpl edge) {
        int previousOutEdgeId = edge.previousOutEdge;
        int nextOutEdgeId = edge.nextOutEdge;
        int type = edge.type;

        EdgeImpl nextOutEdge = null;
        if (nextOutEdgeId != EdgeStore.NULL_ID) {
            nextOutEdge = get(nextOutEdgeId);
            nextOutEdge.previousOutEdge = previousOutEdgeId;
        }

        if (previousOutEdgeId == EdgeStore.NULL_ID) {
            NodeImpl source = edge.source;
            EdgeImpl[] headOutArray = source.headOut;
            headOutArray[type] = nextOutEdge;
            if (nextOutEdge == null && type > GraphStoreConfiguration.EDGESTORE_DEFAULT_TYPE_COUNT - 1 && type == headOutArray.length - 1) {
                trimHeadOutCapacity(source, type - 1);
            }
        } else {
            EdgeImpl previousOutEdge = get(previousOutEdgeId);
            previousOutEdge.nextOutEdge = nextOutEdgeId;
        }

        edge.nextOutEdge = EdgeStore.NULL_ID;
        edge.previousOutEdge = EdgeStore.NULL_ID;
    }

    private void removeInEdge(EdgeImpl edge) {
        int previousInEdgeId = edge.previousInEdge;
        int nextInEdgeId = edge.nextInEdge;
        int type = edge.type;

        EdgeImpl nextInEdge = null;
        if (nextInEdgeId != EdgeStore.NULL_ID) {
            nextInEdge = get(nextInEdgeId);
            nextInEdge.previousInEdge = previousInEdgeId;
        }

        if (previousInEdgeId == EdgeStore.NULL_ID) {
            NodeImpl target = edge.target;
            EdgeImpl[] headInArray = target.headIn;
            headInArray[type] = nextInEdge;
            if (nextInEdge == null && type > GraphStoreConfiguration.EDGESTORE_DEFAULT_TYPE_COUNT - 1 && type == headInArray.length - 1) {
                trimHeadInCapacity(target, type - 1);
            }
        } else {
            EdgeImpl previousInEdge = get(previousInEdgeId);
            previousInEdge.nextInEdge = nextInEdgeId;
        }

        edge.nextInEdge = EdgeStore.NULL_ID;
        edge.previousInEdge = EdgeStore.NULL_ID;
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            incrementVersion();
        }

        for (EdgeStoreIterator itr = new EdgeStoreIterator(); itr.hasNext();) {
            EdgeImpl edge = itr.next();
            edge.setStoreId(EdgeStore.NULL_ID);
        }
        initStore();
    }

    @Override
    public int size() {
        return size;
    }

    public int undirectedSize() {
        return size - mutualEdgesSize;
    }

    public int size(int type) {
        if (type < longDictionary.length) {
            return longDictionary[type].size();
        }
        return 0;
    }

    public int undirectedSize(int type) {
        if (type < longDictionary.length) {
            return longDictionary[type].size() - mutualEdgesTypeSize[type];
        }
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public EdgeStoreIterator iterator() {
        return new EdgeStoreIterator();
    }

    public EdgeStoreIterator iteratorUndirected() {
        return new UndirectedEdgeStoreIterator();
    }

    public SelfLoopIterator iteratorSelfLoop() {
        return new SelfLoopIterator();
    }

    public EdgeOutIterator edgeOutIterator(final Node node) {
        checkValidNodeObject(node);
        return new EdgeOutIterator((NodeImpl) node);
    }

    public EdgeInIterator edgeInIterator(final Node node) {
        checkValidNodeObject(node);
        return new EdgeInIterator((NodeImpl) node);
    }

    public EdgeInOutIterator edgeIterator(final Node node) {
        checkValidNodeObject(node);
        return new EdgeInOutIterator((NodeImpl) node);
    }

    public Iterator<Edge> edgeUndirectedIterator(final Node node) {
        checkValidNodeObject(node);
        return undirectedIterator(new EdgeInOutIterator((NodeImpl) node));
    }

    public EdgeTypeOutIterator edgeOutIterator(final Node node, int type) {
        checkValidNodeObject(node);
        return new EdgeTypeOutIterator((NodeImpl) node, type);
    }

    public Iterator<Edge> edgeUndirectedIterator(final Node node, int type) {
        checkValidNodeObject(node);
        return undirectedIterator(new EdgeTypeInOutIterator((NodeImpl) node, type));
    }

    public EdgeTypeInIterator edgeInIterator(final Node node, int type) {
        checkValidNodeObject(node);
        return new EdgeTypeInIterator((NodeImpl) node, type);
    }

    public EdgeTypeInOutIterator edgeIterator(final Node node, int type) {
        checkValidNodeObject(node);
        return new EdgeTypeInOutIterator((NodeImpl) node, type);
    }

    public NeighborsIterator neighborOutIterator(final Node node) {
        checkValidNodeObject(node);
        return new NeighborsIterator((NodeImpl) node, new EdgeOutIterator((NodeImpl) node));
    }

    public NeighborsIterator neighborOutIterator(final Node node, int type) {
        checkValidNodeObject(node);
        return new NeighborsIterator((NodeImpl) node, new EdgeTypeOutIterator((NodeImpl) node, type));
    }

    public NeighborsIterator neighborInIterator(final Node node) {
        checkValidNodeObject(node);
        return new NeighborsIterator((NodeImpl) node, new EdgeInIterator((NodeImpl) node));
    }

    public NeighborsIterator neighborInIterator(final Node node, int type) {
        checkValidNodeObject(node);
        return new NeighborsIterator((NodeImpl) node, new EdgeTypeInIterator((NodeImpl) node, type));
    }

    public NeighborsIterator neighborIterator(Node node) {
        checkValidNodeObject(node);
        return new NeighborsUndirectedIterator((NodeImpl) node, new EdgeInOutIterator((NodeImpl) node));
    }

    public NeighborsIterator neighborIterator(final Node node, int type) {
        checkValidNodeObject(node);
        return new NeighborsUndirectedIterator((NodeImpl) node, new EdgeTypeInOutIterator((NodeImpl) node, type));
    }

    public Iterator<Edge> edgesUndirectedIterator(final Node node1, final Node node2) {
        checkValidNodeObject(node1);
        checkValidNodeObject(node2);
        return undirectedIterator(getAll(node1, node2, true));
    }

    public Iterator<Edge> edgesUndirectedIterator(final Node node1, final Node node2, int type) {
        checkValidNodeObject(node1);
        checkValidNodeObject(node2);
        return undirectedIterator(getAll(node1, node2, type, true));
    }

    public EdgeImpl get(int id) {
        checkValidId(id);

        return blocks[id / GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE].get(id);
    }

    public EdgeImpl get(final Object id) {
        checkNonNullObject(id);

        int index = dictionary.getInt(id);
        if (index != EdgeStore.NULL_ID) {
            return get(index);
        }
        return null;
    }

    public EdgeImpl get(final Node source, final Node target, boolean undirectedDecorator) {
        return get(source, target, EdgeTypeStore.NULL_LABEL, undirectedDecorator);
    }

    public Iterator<Edge> getAll(final Node source, final Node target, boolean undirectedDecorator) {
        return getAll(source, target, EdgeTypeStore.NULL_LABEL, undirectedDecorator);
    }

    public EdgeImpl get(final Node source, final Node target, final int type, boolean undirectedDecorator) {
        checkNonNullObject(source);
        checkNonNullObject(target);
        NodeImpl sourceImpl = (NodeImpl) source;
        NodeImpl targetImpl = (NodeImpl) target;

        if (type < longDictionary.length) {
            if (isUndirectedGraph()) {
                int[] index = longDictionary[type].get(getLongId(sourceImpl, targetImpl, false));
                if (index != null) {
                    return get(index[0]);
                }
            } else if (isMixedGraph()) {
                int[] index = longDictionary[type].get(getLongId(sourceImpl, targetImpl, true));
                if (index != null) {
                    return get(index[0]);
                } else if (targetImpl.storeId > sourceImpl.storeId) {
                    index = longDictionary[type].get(getLongId(sourceImpl, targetImpl, false));
                    if (index != null) {
                        EdgeImpl e = get(index[0]);
                        if (!e.isDirected() || undirectedDecorator) {
                            return e;
                        }
                    }
                } else if (undirectedDecorator) {
                    index = longDictionary[type].get(getLongId(targetImpl, sourceImpl, true));
                    if (index != null) {
                        return get(index[0]);
                    }
                }
            } else {
                int[] index = longDictionary[type].get(getLongId(sourceImpl, targetImpl, true));
                if (index != null) {
                    return get(index[0]);
                } else if (undirectedDecorator) {
                    index = longDictionary[type].get(getLongId(targetImpl, sourceImpl, true));
                    if (index != null) {
                        return get(index[0]);
                    }
                }
            }
        }
        return null;
    }

    public Iterator<Edge> getAll(final Node source, final Node target, final int type, boolean undirectedDecorator) {
        checkNonNullObject(source);
        checkNonNullObject(target);
        NodeImpl sourceImpl = (NodeImpl) source;
        NodeImpl targetImpl = (NodeImpl) target;

        if (type < longDictionary.length) {
            if (isUndirectedGraph()) {
                int[] index = longDictionary[type].get(getLongId(sourceImpl, targetImpl, false));
                if (index != null) {
                    return new EdgesIterator(index);
                }
            } else if (isMixedGraph() && !undirectedDecorator) {
                int[] index = longDictionary[type].get(getLongId(sourceImpl, targetImpl, true));
                if (index != null) {
                    return new EdgesIterator(index);
                } else if (targetImpl.storeId > sourceImpl.storeId) {
                    index = longDictionary[type].get(getLongId(sourceImpl, targetImpl, false));
                    if (index != null) {
                        return new EdgesIteratorOnlyUndirected(index);
                    }
                }
            } else {
                int[] index = longDictionary[type].get(getLongId(sourceImpl, targetImpl, true));
                if (undirectedDecorator) {
                    int[] reverseIndex = longDictionary[type].get(getLongId(targetImpl, sourceImpl, true));
                    if (reverseIndex != null) {
                        if (index != null) {
                            index = Arrays.copyOf(index, index.length + reverseIndex.length);
                            System.arraycopy(reverseIndex, 0, index, index.length - reverseIndex.length, reverseIndex.length);
                        } else {
                            index = reverseIndex;
                        }
                    }
                }
                if (index != null) {
                    return new EdgesIterator(index);
                }
            }
        }

        return EMPTY_EDGE_ITERATOR;
    }

    public EdgeImpl getMutualEdge(final Edge e) {
        checkNonNullEdgeObject(e);

        EdgeImpl edge = (EdgeImpl) e;
        checkEdgeExists(edge);

        return getMutual(edge);
    }

    private EdgeImpl getMutual(final EdgeImpl edge) {
        return get(edge.target, edge.source, edge.type, false);
    }

    @Override
    public boolean add(final Edge e) {
        checkNonNullEdgeObject(e);

        EdgeImpl edge = (EdgeImpl) e;
        if (edge.storeId == EdgeStore.NULL_ID) {
            checkIdDoesntExist(e.getId());
            checkSourceTargets(edge);
            checkUndirectedNotExist(edge);

            int type = edge.type;
            boolean directed = edge.isDirected();
            NodeImpl source = edge.source;
            NodeImpl target = edge.target;

            ensureLongDictionaryCapacity(type);
            Long2ObjectOpenCustomHashMap<int[]> dico = longDictionary[type];
            long longId = getLongId(source, target, directed);
            int[] dicoValue = dico.get(longId);
            if (dicoValue != null && !GraphStoreConfiguration.ENABLE_PARALLEL_EDGES) {
                return false;
            }

            incrementVersion();

            if (garbageSize > 0) {
                for (int i = 0; i < blocksCount; i++) {
                    EdgeBlock edgeBlock = blocks[i];
                    if (edgeBlock.hasGarbage()) {
                        edgeBlock.set(edge);
                        garbageSize--;
                        dictionary.put(edge.getId(), edge.storeId);
                        break;
                    }
                }
            } else {
                ensureCapacity(1);
                currentBlock.add(edge);
                dictionary.put(edge.getId(), edge.storeId);
            }

            insertOutEdge(edge);
            insertInEdge(edge);

            source.outDegree++;
            target.inDegree++;

            if (dicoValue == null) {
                dicoValue = new int[] {edge.storeId};
            } else {
                dicoValue = Arrays.copyOf(dicoValue, dicoValue.length + 1);
                dicoValue[dicoValue.length - 1] = edge.storeId;
            }
            dico.put(longId, dicoValue);

            if (viewStore != null) {
                viewStore.addEdge(edge);
            }
            edge.indexAttributes();

            if (directed && !edge.isSelfLoop()) {
                int[] index = longDictionary[type].get(getLongId(edge.target, edge.source, true));
                if (index != null) {
                    for (int i = 0; i < index.length; i++) {
                        EdgeImpl mutual = get(index[i]);
                        if (!mutual.isMutual()) {
                            mutual.setMutual(true);
                            edge.setMutual(true);
                            source.mutualDegree++;
                            target.mutualDegree++;
                            mutualEdgesSize++;
                            mutualEdgesTypeSize[type]++;
                            break;
                        }
                    }
                }
            }

            if (!directed) {
                undirectedSize++;
            }

            size++;
            return true;
        } else if (isValidIndex(edge.storeId) && get(edge.storeId) == edge) {
            return false;
        } else {
            throw new IllegalArgumentException("The edge already belongs to another store");
        }
    }

    @Override
    public boolean remove(final Object o) {
        checkNonNullEdgeObject(o);

        EdgeImpl edge = (EdgeImpl) o;
        int id = edge.storeId;
        if (id != EdgeStore.NULL_ID) {
            checkEdgeExists(edge);

            incrementVersion();

            if (viewStore != null) {
                viewStore.removeEdge(edge);
            }

            edge.clearAttributes();

            int storeIndex = id / GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE;
            EdgeBlock block = blocks[storeIndex];
            block.remove(edge);

            removeOutEdge(edge);
            removeInEdge(edge);

            boolean directed = edge.isDirected();
            NodeImpl source = edge.source;
            NodeImpl target = edge.target;

            source.outDegree--;
            target.inDegree--;

            size--;
            garbageSize++;
            dictionary.remove(edge.getId());
            trimDictionary();

            for (int i = storeIndex; i == (blocksCount - 1) && block.garbageLength == block.nodeLength && i >= 0;) {
                if (i != 0) {
                    blocks[i] = null;
                    blocksCount--;
                    garbageSize -= block.nodeLength;
                    block = blocks[--i];
                    currentBlock = block;
                    currentBlockIndex--;
                } else {
                    currentBlock.clear();
                    garbageSize = 0;
                    break;
                }
            }

            int type = edge.type;

            Long2ObjectOpenCustomHashMap<int[]> dico = longDictionary[type];
            long longId = getLongId(source, target, directed);
            int[] dicoValue = dico.get(longId);
            if (dicoValue.length == 1) {
                dico.remove(longId);
            } else {
                int[] newDicoValue = new int[dicoValue.length - 1];
                int j = 0;
                for (int i = 0; i < dicoValue.length; i++) {
                    int v = dicoValue[i];
                    if (v != id) {
                        newDicoValue[j++] = v;
                    }
                }
                dico.put(longId, newDicoValue);
            }

            if (directed && !edge.isSelfLoop()) {
                int[] index = longDictionary[type].get(getLongId(edge.target, edge.source, true));
                if (index != null) {
                    for (int i = 0; i < index.length; i++) {
                        EdgeImpl mutual = get(index[i]);
                        if (mutual.isMutual()) {
                            edge.setMutual(true);

                            mutual.setMutual(false);
                            source.mutualDegree--;
                            target.mutualDegree--;
                            mutualEdgesSize--;
                            mutualEdgesTypeSize[type]--;
                            break;
                        }
                    }
                }
            }

            if (!directed) {
                undirectedSize--;
            }

            if (edgeTypeStore != null) {
                // TODO - if type count is zero, do smthing
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean contains(Object o) {
        checkNonNullEdgeObject(o);

        EdgeImpl edge = (EdgeImpl) o;
        int id = edge.getStoreId();
        if (id != EdgeStore.NULL_ID) {
            if (get(id) == edge) {
                return true;
            }
        }
        return false;
    }

    public boolean containsId(final Object id) {
        return dictionary.containsKey(id);
    }

    public boolean contains(NodeImpl source, NodeImpl target, int type) {
        checkNonNullObject(source);
        checkNonNullObject(target);

        if (type < longDictionary.length) {
            if (isUndirectedGraph()) {
                return longDictionary[type].containsKey(getLongId(source, target, false));
            } else if (isMixedGraph()) {
                if (longDictionary[type].containsKey(getLongId(source, target, true))) {
                    return true;
                } else if (target.storeId > source.storeId) {
                    int[] index = longDictionary[type].get(getLongId(source, target, false));
                    if (index != null) {
                        for (int i = 0; i < index.length; i++) {
                            EdgeImpl mutual = get(index[i]);
                            if (!mutual.isDirected()) {
                                return true;
                            }
                        }
                    }
                }
            } else {
                return longDictionary[type].containsKey(getLongId(source, target, true));
            }
        }
        return false;
    }

    @Override
    public EdgeImpl[] toArray() {
        readLock();

        EdgeImpl[] array = new EdgeImpl[size];
        if (garbageSize == 0) {
            for (int i = 0; i < blocksCount; i++) {
                EdgeBlock block = blocks[i];
                System.arraycopy(block.backingArray, 0, array, block.offset, block.nodeLength);
            }
        } else {
            EdgeStoreIterator itr = iterator();
            int offset = 0;
            while (itr.hasNext()) {
                EdgeImpl n = itr.next();
                array[offset++] = n;
            }
        }

        readUnlock();
        return array;
    }

    @Override
    public <T> T[] toArray(T[] arrayTmp) {
        T[] array = arrayTmp;
        checkNonNullObject(array);

        readLock();

        if (array.length < size()) {
            array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size());
        }
        if (garbageSize == 0) {
            for (int i = 0; i < blocksCount; i++) {
                EdgeBlock block = blocks[i];
                System.arraycopy(block.backingArray, 0, array, block.offset, block.nodeLength);
            }
        } else {
            EdgeStoreIterator itr = iterator();
            int offset = 0;
            while (itr.hasNext()) {
                EdgeImpl n = itr.next();
                array[offset++] = (T) n;
            }
        }
        readUnlock();

        return array;
    }

    @Override
    public Collection<Edge> toCollection() {
        readLock();

        List<Edge> list = new ArrayList<Edge>(size);
        EdgeStoreIterator itr = iterator();
        while (itr.hasNext()) {
            EdgeImpl n = itr.next();
            list.add(n);
        }

        readUnlock();
        return list;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            int found = 0;
            for (Object o : c) {
                if (contains((EdgeImpl) o)) {
                    found++;
                }
            }
            return found == c.size();
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            int capacityNeeded = c.size() - garbageSize;
            if (capacityNeeded > 0) {
                ensureCapacity(capacityNeeded);
            }
            boolean changed = false;
            Iterator<? extends Edge> itr = c.iterator();
            while (itr.hasNext()) {
                Edge e = itr.next();
                if (add(e)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            boolean changed = false;
            Iterator itr = c.iterator();
            while (itr.hasNext()) {
                Object o = itr.next();
                if (remove(o)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            ObjectSet<EdgeImpl> set = new ObjectOpenHashSet(c.size());
            for (Object o : c) {
                checkNonNullObject(o);
                checkEdgeExists((EdgeImpl) o);
                set.add((EdgeImpl) o);
            }

            boolean changed = false;
            EdgeStoreIterator itr = iterator();
            while (itr.hasNext()) {
                EdgeImpl e = itr.next();
                if (!set.contains(e)) {
                    itr.remove();
                    changed = true;
                }
            }
            return changed;
        } else {
            clear();
        }
        return false;
    }

    public int deepHashCode() {
        int hash = 7;
        hash = 67 * hash + this.size;
        EdgeStoreIterator itr = this.iterator();
        while (itr.hasNext()) {
            hash = 67 * hash + itr.next().hashCode();
        }
        return hash;
    }

    public boolean deepEquals(EdgeStore obj) {
        if (obj == null || this.size != obj.size) {
            return false;
        }
        EdgeStoreIterator itr1 = this.iterator();
        EdgeStoreIterator itr2 = obj.iterator();
        while (itr1.hasNext()) {
            if (!itr2.hasNext()) {
                return false;
            }
            if (!itr1.next().equals(itr2.next())) {
                return false;
            }
        }
        return true;
    }

    public boolean isAdjacent(Node node1, Node node2, int type) {
        checkValidNodeObject(node1);
        checkValidNodeObject(node2);

        return contains((NodeImpl) node1, (NodeImpl) node2, type);
    }

    public boolean isAdjacent(Node node1, Node node2) {
        checkValidNodeObject(node1);
        checkValidNodeObject(node2);

        int typeLength = longDictionary.length;
        for (int i = 0; i < typeLength; i++) {
            if (contains((NodeImpl) node1, (NodeImpl) node2, i)) {
                return true;
            }
        }

        return false;
    }

    public boolean isIncident(EdgeImpl edge1, EdgeImpl edge2) {
        return edge1.source == edge2.source || edge1.target == edge2.target || edge1.source == edge2.target || edge1.target == edge2.source;
    }

    public boolean isIncident(NodeImpl node, EdgeImpl edge) {
        return edge.source == node || edge.target == node;
    }

    protected Iterator<Edge> undirectedIterator(Iterator<Edge> edgeIterator) {
        return new UndirectedIterator(edgeIterator);
    }

    @Override
    public void doBreak() {
        readUnlock();
    }

    void checkUndirectedNotExist(EdgeImpl edge) {
        int type = edge.type;
        if (type < longDictionary.length) {
            if (edge.isDirected() && !isDirectedGraph()) {
                int[] index = longDictionary[type].get(getLongId(edge.source, edge.target, false));
                if (index != null && !get(index[0]).isDirected()) {
                    throw new IllegalArgumentException("An undirected edge already exists");
                }
            } else if (!edge.isDirected() && !isUndirectedGraph()) {
                int[] index = longDictionary[type].get(getLongId(edge.source, edge.target, true));
                if (index != null && get(index[0]).isDirected()) {
                    throw new IllegalArgumentException("An directed edge already exists");
                }
                index = longDictionary[type].get(getLongId(edge.target, edge.source, true));
                if (index != null && get(index[0]).isDirected()) {
                    throw new IllegalArgumentException("An directed edge already exists");
                }
            }
        }
    }

    void checkIdDoesntExist(Object id) {
        if (dictionary.containsKey(id)) {
            throw new IllegalArgumentException("The edge id already exist");
        }
    }

    boolean isMixedGraph() {
        return undirectedSize > 0 && undirectedSize != size;
    }

    boolean isUndirectedGraph() {
        return size > 0 && undirectedSize == size;
    }

    boolean isDirectedGraph() {
        return undirectedSize == 0;
    }

    protected boolean isValidIndex(int id) {
        return id >= 0 && id < currentBlock.offset + currentBlock.nodeLength;
    }

    void checkCollection(final Collection<?> collection) {
        if (collection == this) {
            throw new IllegalArgumentException("Can't pass itself");
        }
    }

    void checkNonNullObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    void checkValidNodeObject(final Node n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof NodeImpl)) {
            throw new ClassCastException("Object must be a NodeImpl object");
        }
        if (((NodeImpl) n).storeId == NodeStore.NULL_ID) {
            throw new IllegalArgumentException("Node should belong to a store");
        }
    }

    void checkNonNullEdgeObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof EdgeImpl)) {
            throw new ClassCastException("Object must be a EdgeImpl object");
        }
    }

    void checkSourceTargets(final EdgeImpl e) {
        if (e.source == null || e.target == null) {
            throw new NullPointerException();
        }
        if (e.source.storeId == NodeStore.NULL_ID || e.target.storeId == NodeStore.NULL_ID) {
            throw new RuntimeException("Source and target nodes should be valid and belong to a store");
        }
    }

    void checkEdgeExists(final EdgeImpl edge) {
        if (get(edge.storeId) != edge) {
            throw new IllegalArgumentException("The edge is invalid");
        }
    }

    void checkValidId(final int id) {
        if (id < 0 || !isValidIndex(id)) {
            throw new IllegalArgumentException("Edge id=" + id + " is invalid");
        }
    }

    void readLock() {
        if (lock != null) {
            lock.readLock();
        }
    }

    void readUnlock() {
        if (lock != null) {
            lock.readUnlock();
        }
    }

    void checkWriteLock() {
        if (lock != null) {
            lock.checkHoldWriteLock();
        }
    }

    private void incrementVersion() {
        if (version != null) {
            version.incrementAndGetEdgeVersion();
        }
    }

    boolean isUndirectedToIgnore(EdgeImpl edge) {
        return edge.isMutual() && edge.source.storeId < edge.target.storeId;
    }

    int maxStoreId() {
        return currentBlock.offset + currentBlock.nodeLength;
    }

    protected static long getLongId(NodeImpl source, NodeImpl target, boolean directed) {
        if (directed) {
            long edgeId = ((long) source.storeId) << NODE_BITS;
            edgeId = edgeId | (long) (target.storeId);
            return edgeId;
        } else {
            long edgeId = ((long) (source.storeId > target.storeId ? source.storeId : target.storeId)) << NODE_BITS;
            edgeId = edgeId | (long) (source.storeId > target.storeId ? target.storeId : source.storeId);
            return edgeId;
        }
    }

    protected static class EdgeBlock {

        protected final int offset;
        protected final short[] garbageArray;
        protected final EdgeImpl[] backingArray;
        protected int nodeLength;
        protected int garbageLength;

        public EdgeBlock(int index) {
            this.offset = index * GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE;
            if (GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE >= Short.MAX_VALUE - Short.MIN_VALUE) {
                throw new RuntimeException("BLOCK SIZE can't exceed 65535");
            }
            this.garbageArray = new short[GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE];
            this.backingArray = new EdgeImpl[GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE];
        }

        public boolean hasGarbage() {
            return garbageLength > 0;
        }

        public int getCapacity() {
            return GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE - nodeLength - garbageLength;
        }

        public void add(EdgeImpl k) {
            int i = nodeLength++;
            backingArray[i] = k;
            k.setStoreId(i + offset);
        }

        public void set(EdgeImpl k) {
            int i = garbageArray[--garbageLength] - Short.MIN_VALUE;
            backingArray[i] = k;
            k.setStoreId(i + offset);
        }

        public EdgeImpl get(int id) {
            return backingArray[id - offset];
        }

        public void remove(EdgeImpl k) {
            int i = k.getStoreId() - offset;
            backingArray[i] = null;
            garbageArray[garbageLength++] = (short) (i + Short.MIN_VALUE);
            k.setStoreId(NULL_ID);
        }

        public void clear() {
            nodeLength = 0;
            garbageLength = 0;
        }
    }

    protected class EdgeStoreIterator implements Iterator<Edge> {

        protected int blockIndex;
        protected EdgeImpl[] backingArray;
        protected int blockLength;
        protected int cursor;
        protected EdgeImpl pointer;

        public EdgeStoreIterator() {
            readLock();
            this.backingArray = blocks[blockIndex].backingArray;
            this.blockLength = blocks[blockIndex].nodeLength;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (cursor == blockLength || ((pointer = backingArray[cursor++]) == null)) {
                if (cursor == blockLength) {
                    if (++blockIndex < blocksCount) {
                        backingArray = blocks[blockIndex].backingArray;
                        blockLength = blocks[blockIndex].nodeLength;
                        cursor = 0;
                    } else {
                        break;
                    }
                }
            }
            if (pointer == null) {
                readUnlock();
                return false;
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            return pointer;
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(pointer);
        }
    }

    protected final class UndirectedEdgeStoreIterator extends EdgeStoreIterator {

        public UndirectedEdgeStoreIterator() {
            super();
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                if (!super.hasNext()) {
                    return false;
                }
                if (isUndirectedToIgnore(pointer)) {
                    pointer = null;
                }
            }
            return true;
        }

        @Override
        public void remove() {
            if (pointer.isMutual()) {
                throw new UnsupportedOperationException(
                        "Removing directed edges from undirected iterator is not supported");
            }
            EdgeStore.this.remove(pointer);
        }
    }

    protected final class SelfLoopIterator extends EdgeStoreIterator {

        public SelfLoopIterator() {
            super();
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                if (!super.hasNext()) {
                    return false;
                }
                if (!pointer.isSelfLoop()) {
                    pointer = null;
                }
            }
            return true;
        }
    }

    protected final class EdgeInOutIterator implements Iterator<Edge> {

        protected final int outTypeLength;
        protected final int inTypeLength;
        protected EdgeImpl[] outArray;
        protected EdgeImpl[] inArray;
        protected int typeIndex = 0;
        protected EdgeImpl pointer;
        protected EdgeImpl lastEdge;
        protected boolean out = true;

        public EdgeInOutIterator(NodeImpl node) {
            readLock();
            outArray = node.headOut;
            outTypeLength = outArray.length;
            inArray = node.headIn;
            inTypeLength = inArray.length;
        }

        @Override
        public boolean hasNext() {
            if (pointer == null) {
                if (out) {
                    while (pointer == null && typeIndex < outTypeLength) {
                        pointer = outArray[typeIndex++];
                    }
                    if (pointer == null) {
                        out = false;
                        typeIndex = 0;
                    }
                }
                if (!out) {
                    while (pointer == null && typeIndex < inTypeLength) {
                        pointer = inArray[typeIndex++];
                        while (pointer != null && pointer.isSelfLoop()) {
                            int id = pointer.nextInEdge;
                            if (id != EdgeStore.NULL_ID) {
                                pointer = get(id);
                            } else {
                                pointer = null;
                            }
                        }
                    }
                }

                if (pointer == null) {
                    readUnlock();
                    return false;
                }
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            lastEdge = pointer;
            if (out) {
                int id = lastEdge.nextOutEdge;
                if (id != EdgeStore.NULL_ID) {
                    pointer = get(id);
                } else {
                    pointer = null;
                }
            } else {
                int id = EdgeStore.NULL_ID;
                while (id == EdgeStore.NULL_ID) {
                    id = pointer.nextInEdge;
                    if (id != EdgeStore.NULL_ID) {
                        pointer = get(id);
                        if (pointer.isSelfLoop()) {
                            id = EdgeStore.NULL_ID;
                        }
                    } else {
                        pointer = null;
                        break;
                    }
                }
            }
            return lastEdge;
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(lastEdge);
        }
    }

    protected final class EdgeOutIterator implements Iterator<Edge> {

        protected final int typeLength;
        protected EdgeImpl[] outArray;
        protected int typeIndex = 0;
        protected EdgeImpl pointer;
        protected EdgeImpl lastEdge;

        public EdgeOutIterator(NodeImpl node) {
            readLock();
            outArray = node.headOut;
            typeLength = outArray.length;
        }

        @Override
        public boolean hasNext() {
            if (pointer == null) {
                while (pointer == null && typeIndex < typeLength) {
                    pointer = outArray[typeIndex++];
                }
                if (pointer == null) {
                    readUnlock();
                    return false;
                }
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            lastEdge = pointer;
            int id = lastEdge.nextOutEdge;
            if (id != EdgeStore.NULL_ID) {
                pointer = get(id);
            } else {
                pointer = null;
            }
            return lastEdge;
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(lastEdge);
        }
    }

    protected final class EdgeInIterator implements Iterator<Edge> {

        protected final int typeLength;
        protected EdgeImpl[] inArray;
        protected int typeIndex = 0;
        protected EdgeImpl pointer;
        protected EdgeImpl lastEdge;

        public EdgeInIterator(NodeImpl node) {
            readLock();
            inArray = node.headIn;
            typeLength = inArray.length;
        }

        @Override
        public boolean hasNext() {
            if (pointer == null) {
                while (pointer == null && typeIndex < typeLength) {
                    pointer = inArray[typeIndex++];
                }
                if (pointer == null) {
                    readUnlock();
                    return false;
                }
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            lastEdge = pointer;
            int id = lastEdge.nextInEdge;
            if (id != EdgeStore.NULL_ID) {
                pointer = get(id);
            } else {
                pointer = null;
            }
            return lastEdge;
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(lastEdge);
        }
    }

    protected final class EdgeTypeInOutIterator implements Iterator<Edge> {

        protected final int type;
        protected EdgeImpl lastEdge;
        protected EdgeImpl outPointer;
        protected EdgeImpl inPointer;
        protected boolean out = true;

        public EdgeTypeInOutIterator(NodeImpl node, int type) {
            this.type = type;
            readLock();
            EdgeImpl[] outArray = node.headOut;
            EdgeImpl[] inArray = node.headIn;
            outPointer = type < outArray.length ? outArray[type] : null;
            inPointer = type < inArray.length ? inArray[type] : null;
        }

        @Override
        public boolean hasNext() {
            if (outPointer == null) {
                if (out) {
                    out = false;
                    while (inPointer != null && inPointer.isSelfLoop()) {
                        int id = inPointer.nextInEdge;
                        if (id != EdgeStore.NULL_ID) {
                            inPointer = get(id);
                        } else {
                            inPointer = null;
                        }
                    }
                }
                if (inPointer == null) {
                    readUnlock();
                    return false;
                }
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            if (out) {
                lastEdge = outPointer;
                int id = lastEdge.nextOutEdge;
                if (id != EdgeStore.NULL_ID) {
                    outPointer = get(id);
                } else {
                    outPointer = null;
                }
            } else {
                lastEdge = inPointer;
                int id = EdgeStore.NULL_ID;
                while (id == EdgeStore.NULL_ID) {
                    id = inPointer.nextInEdge;
                    if (id != EdgeStore.NULL_ID) {
                        inPointer = get(id);
                        if (inPointer.isSelfLoop()) {
                            id = EdgeStore.NULL_ID;
                        }
                    } else {
                        inPointer = null;
                        break;
                    }
                }
            }

            return lastEdge;
        }

        public void reset(NodeImpl node) {
            EdgeImpl[] outArray = node.headOut;
            EdgeImpl[] inArray = node.headIn;
            outPointer = type < outArray.length ? outArray[type] : null;
            inPointer = type < inArray.length ? inArray[type] : null;
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(lastEdge);
        }
    }

    protected final class EdgeTypeOutIterator implements Iterator<Edge> {

        protected final int type;
        protected EdgeImpl lastEdge;
        protected EdgeImpl pointer;

        public EdgeTypeOutIterator(NodeImpl node, int type) {
            this.type = type;
            readLock();
            EdgeImpl[] outArray = node.headOut;
            pointer = type < outArray.length ? outArray[type] : null;
        }

        @Override
        public boolean hasNext() {
            if (pointer == null) {
                readUnlock();
                return false;
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            lastEdge = pointer;
            int id = lastEdge.nextOutEdge;
            if (id != EdgeStore.NULL_ID) {
                pointer = get(id);
            } else {
                pointer = null;
            }
            return lastEdge;
        }

        public void reset(NodeImpl node) {
            EdgeImpl[] outArray = node.headOut;
            pointer = type < outArray.length ? outArray[type] : null;
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(lastEdge);
        }
    }

    protected class EdgesIterator implements Iterator<Edge> {

        protected final int[] indices;
        protected int index;

        public EdgesIterator(int[] indices) {
            this.indices = indices;
            readLock();
        }

        @Override
        public boolean hasNext() {
            boolean res = index < indices.length;
            if (!res) {
                readUnlock();
            }
            return res;
        }

        @Override
        public Edge next() {
            return get(indices[index++]);
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(get(indices[index - 1]));
        }
    }

    protected final class EdgesIteratorOnlyUndirected extends EdgesIterator {

        protected EdgeImpl pointer;

        public EdgesIteratorOnlyUndirected(int[] indices) {
            super(indices);
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (index < indices.length && pointer == null) {
                pointer = EdgeStore.this.get(indices[index++]);
                if (pointer.isDirected()) {
                    pointer = null;
                }
            }
            if (pointer == null) {
                readUnlock();
                return false;
            }
            return true;
        }

        @Override
        public Edge next() {
            return pointer;
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(pointer);
        }
    }

    protected final class EdgeTypeInIterator implements Iterator<Edge> {

        protected final int type;
        protected EdgeImpl lastEdge;
        protected EdgeImpl pointer;

        public EdgeTypeInIterator(NodeImpl node, int type) {
            this.type = type;
            readLock();
            EdgeImpl[] inArray = node.headIn;
            pointer = type < inArray.length ? inArray[type] : null;
        }

        @Override
        public boolean hasNext() {
            if (pointer == null) {
                readUnlock();
                return false;
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            lastEdge = pointer;
            int id = lastEdge.nextInEdge;
            if (id != EdgeStore.NULL_ID) {
                pointer = get(id);
            } else {
                pointer = null;
            }
            return lastEdge;
        }

        public void reset(NodeImpl node) {
            EdgeImpl[] inArray = node.headIn;
            pointer = type < inArray.length ? inArray[type] : null;
        }

        @Override
        public void remove() {
            checkWriteLock();
            EdgeStore.this.remove(lastEdge);
        }
    }

    protected class NeighborsIterator implements Iterator<Node> {

        protected final NodeImpl node;
        protected final Iterator<Edge> itr;

        public NeighborsIterator(NodeImpl node, Iterator<Edge> itr) {
            this.node = node;
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Node next() {
            Edge e = itr.next();
            return e.getSource() == node ? e.getTarget() : e.getSource();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for this iterator");
        }
    }

    protected final class NeighborsUndirectedIterator extends NeighborsIterator {

        protected EdgeImpl pointer;

        public NeighborsUndirectedIterator(NodeImpl node, Iterator<Edge> itr) {
            super(node, itr);
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null || isUndirectedToIgnore(pointer)) {
                if (!itr.hasNext()) {
                    return false;
                }
                pointer = (EdgeImpl) itr.next();
            }
            return true;
        }

        @Override
        public Node next() {
            return pointer.getSource() == node ? pointer.getTarget() : pointer.getSource();
        }
    }

    protected final class UndirectedIterator implements Iterator<Edge> {

        protected final Iterator<Edge> itr;
        protected EdgeImpl pointer;

        public UndirectedIterator(Iterator<Edge> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null || isUndirectedToIgnore(pointer)) {
                if (!itr.hasNext()) {
                    return false;
                }
                pointer = (EdgeImpl) itr.next();
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            return pointer;
        }

        @Override
        public void remove() {
            if (pointer.isMutual()) {
                throw new UnsupportedOperationException(
                        "Removing directed edges from undirected iterator is not supported");
            }
            EdgeStore.this.remove(pointer);
        }
    }

    private static class DictionaryHashStrategy implements LongHash.Strategy {

        @Override
        public int hashCode(long l) {
            return (int) (l ^ (l >>> 32));
        }

        @Override
        public boolean equals(long l1, long l2) {
            return l1 == l2;
        }
    }
}
