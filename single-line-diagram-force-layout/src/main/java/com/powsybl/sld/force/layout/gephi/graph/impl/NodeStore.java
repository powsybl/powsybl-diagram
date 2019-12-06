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

import com.powsybl.sld.force.layout.gephi.graph.api.NodeIterable;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import com.powsybl.sld.force.layout.gephi.graph.api.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class NodeStore implements Collection<Node>, NodeIterable {

    // Const
    protected static final int NULL_ID = -1;
    // Store
    protected final EdgeStore edgeStore;
    // Locking (optional)
    protected final GraphLock lock;
    // Version
    protected final GraphVersion version;
    // Data
    protected int size;
    protected int garbageSize;
    protected int blocksCount;
    protected int currentBlockIndex;
    protected NodeBlock[] blocks;
    protected NodeBlock currentBlock;
    protected Object2IntOpenHashMap dictionary;
    // View store
    protected final GraphViewStore viewStore;

    public NodeStore() {
        initStore();
        this.lock = null;
        this.edgeStore = null;
        this.viewStore = null;
        this.version = null;
    }

    public NodeStore(final EdgeStore edgeStore, final GraphLock lock, final GraphViewStore viewStore, final GraphVersion graphVersion) {
        initStore();
        this.lock = lock;
        this.edgeStore = edgeStore;
        this.viewStore = viewStore;
        this.version = graphVersion;
    }

    private void initStore() {
        this.size = 0;
        this.garbageSize = 0;
        this.blocksCount = 1;
        this.currentBlockIndex = 0;
        this.blocks = new NodeBlock[GraphStoreConfiguration.NODESTORE_DEFAULT_BLOCKS];
        this.blocks[0] = new NodeBlock(0);
        this.currentBlock = blocks[currentBlockIndex];
        this.dictionary = new Object2IntOpenHashMap(GraphStoreConfiguration.NODESTORE_DEFAULT_DICTIONARY_SIZE,
                GraphStoreConfiguration.NODESTORE_DICTIONARY_LOAD_FACTOR);
        this.dictionary.defaultReturnValue(NULL_ID);
    }

    private void ensureCapacity(final int capacity) {
        assert capacity > 0;

        int blockCapacity = currentBlock.getCapacity();
        while (capacity > blockCapacity) {
            if (currentBlockIndex == blocksCount - 1) {
                int blocksNeeded = (int) Math
                        .ceil((capacity - blockCapacity) / (double) GraphStoreConfiguration.NODESTORE_BLOCK_SIZE);
                for (int i = 0; i < blocksNeeded; i++) {
                    if (blocksCount == blocks.length) {
                        NodeBlock[] newBlocks = new NodeBlock[blocksCount + 1];
                        System.arraycopy(blocks, 0, newBlocks, 0, blocks.length);
                        blocks = newBlocks;
                    }
                    NodeBlock block = blocks[blocksCount];
                    if (block == null) {
                        block = new NodeBlock(blocksCount);
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
        dictionary.trim(Math.max(GraphStoreConfiguration.NODESTORE_BLOCK_SIZE, size * 2));
    }

    public NodeImpl get(final int id) {
        checkValidId(id);

        return blocks[id / GraphStoreConfiguration.NODESTORE_BLOCK_SIZE].get(id);
    }

    public NodeImpl get(final Object id) {
        int index = dictionary.getInt(id);
        if (index != NodeStore.NULL_ID) {
            return get(index);
        }
        return null;
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            incrementVersion();
        }

        for (NodeStoreIterator itr = new NodeStoreIterator(); itr.hasNext();) {
            NodeImpl node = itr.next();
            node.setStoreId(NodeStore.NULL_ID);
        }
        initStore();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public NodeStoreIterator iterator() {
        return new NodeStoreIterator();
    }

    @Override
    public NodeImpl[] toArray() {
        readLock();

        NodeImpl[] array = new NodeImpl[size];
        if (garbageSize == 0) {
            for (int i = 0; i < blocksCount; i++) {
                NodeBlock block = blocks[i];
                System.arraycopy(block.backingArray, 0, array, block.offset, block.nodeLength);
            }
        } else {
            NodeStoreIterator itr = iterator();
            int offset = 0;
            while (itr.hasNext()) {
                NodeImpl n = itr.next();
                array[offset++] = n;
            }
        }

        readUnlock();

        return array;
    }

    @Override
    public <T> T[] toArray(T[] array) {
        checkNonNullObject(array);

        readLock();

        T[] arrayTmp = array;
        if (array.length < size()) {
            arrayTmp = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size());
        }
        if (garbageSize == 0) {
            for (int i = 0; i < blocksCount; i++) {
                NodeBlock block = blocks[i];
                System.arraycopy(block.backingArray, 0, arrayTmp, block.offset, block.nodeLength);
            }
        } else {
            NodeStoreIterator itr = iterator();
            int offset = 0;
            while (itr.hasNext()) {
                NodeImpl n = itr.next();
                arrayTmp[offset++] = (T) n;
            }
        }

        readUnlock();
        return arrayTmp;
    }

    @Override
    public Collection<Node> toCollection() {
        readLock();

        List<Node> list = new ArrayList<Node>(size);

        NodeStoreIterator itr = iterator();
        while (itr.hasNext()) {
            NodeImpl n = itr.next();
            list.add(n);
        }

        readUnlock();

        return list;
    }

    @Override
    public boolean add(final Node n) {
        checkNonNullNodeObject(n);

        NodeImpl node = (NodeImpl) n;
        if (node.storeId == NodeStore.NULL_ID) {
            checkIdDoesntExist(n.getId());

            incrementVersion();

            if (garbageSize > 0) {
                for (int i = 0; i < blocksCount; i++) {
                    NodeBlock nodeBlock = blocks[i];
                    if (nodeBlock.hasGarbage()) {
                        nodeBlock.set(node);
                        garbageSize--;
                        dictionary.put(node.getId(), node.storeId);
                        break;
                    }
                }
            } else {
                ensureCapacity(1);
                currentBlock.add(node);
                dictionary.put(node.getId(), node.storeId);
            }
            if (viewStore != null) {
                viewStore.addNode(node);
            }
            node.indexAttributes();

            size++;

            return true;
        } else if (isValidIndex(node.storeId) && get(node.storeId) == node) {
            return false;
        } else {
            throw new IllegalArgumentException("The node already belongs to another store");
        }
    }

    @Override
    public boolean remove(final Object o) {
        checkNonNullNodeObject(o);

        NodeImpl node = (NodeImpl) o;
        int id = node.storeId;
        if (id != NodeStore.NULL_ID) {
            checkNodeExists(node);

            if (viewStore != null) {
                viewStore.removeNode(node);
            }

            node.clearAttributes();

            incrementVersion();

            int storeIndex = id / GraphStoreConfiguration.NODESTORE_BLOCK_SIZE;
            NodeBlock block = blocks[storeIndex];
            block.remove(node);
            size--;
            garbageSize++;
            dictionary.remove(node.getId());
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
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(final Object o) {
        checkNonNullNodeObject(o);

        NodeImpl node = (NodeImpl) o;
        int id = node.getStoreId();
        if (id != NodeStore.NULL_ID) {
            if (get(id) == node) {
                return true;
            }
        }

        return false;
    }

    public boolean containsId(final Object id) {
        checkNonNullObject(id);

        return dictionary.containsKey(id);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            int found = 0;
            for (Object o : c) {
                if (contains((NodeImpl) o)) {
                    found++;
                }
            }
            return found == c.size();
        }
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends Node> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            int capacityNeeded = c.size() - garbageSize;
            if (capacityNeeded > 0) {
                ensureCapacity(capacityNeeded);
            }
            boolean changed = false;
            Iterator<? extends Node> itr = c.iterator();
            while (itr.hasNext()) {
                Node e = itr.next();
                if (add(e)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
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
    public boolean retainAll(final Collection<?> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            ObjectSet<NodeImpl> set = new ObjectOpenHashSet(c.size());
            for (Object o : c) {
                checkNonNullObject(o);
                checkNodeExists((NodeImpl) o);
                set.add((NodeImpl) o);
            }

            boolean changed = false;
            NodeStoreIterator itr = iterator();
            while (itr.hasNext()) {
                NodeImpl e = itr.next();
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
        NodeStoreIterator itr = this.iterator();
        while (itr.hasNext()) {
            hash = 67 * hash + itr.next().hashCode();
        }
        return hash;
    }

    public boolean deepEquals(NodeStore obj) {
        if (obj == null) {
            return false;
        }
        if (this.size != obj.size) {
            return false;
        }
        NodeStoreIterator itr1 = this.iterator();
        NodeStoreIterator itr2 = obj.iterator();
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

    @Override
    public void doBreak() {
        readUnlock();
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

    private void checkIdDoesntExist(Object id) {
        if (dictionary.containsKey(id)) {
            throw new IllegalArgumentException("The node id already exist");
        }
    }

    private int incrementVersion() {
        if (version != null) {
            return version.incrementAndGetNodeVersion();
        }
        return 0;
    }

    protected boolean isValidIndex(int id) {
        if (id < 0 || id >= currentBlock.offset + currentBlock.nodeLength) {
            return false;
        }
        return true;
    }

    private void checkNonNullObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    void checkNonNullNodeObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof NodeImpl)) {
            throw new ClassCastException("Object must be a NodeImpl object");
        }
    }

    void checkNodeExists(final NodeImpl node) {
        if (get(node.storeId) != node) {
            throw new IllegalArgumentException("The node is invalid");
        }
    }

    private void checkValidId(final int id) {
        if (id < 0 || !isValidIndex(id)) {
            throw new IllegalArgumentException("Node id=" + id + " is invalid");
        }
    }

    private void checkCollection(final Collection<?> collection) {
        if (collection == this) {
            throw new IllegalArgumentException("Can't pass itself");
        }
    }

    int maxStoreId() {
        return currentBlock.offset + currentBlock.nodeLength;
    }

    protected static class NodeBlock {

        protected final int offset;
        protected final short[] garbageArray;
        protected final NodeImpl[] backingArray;
        protected int nodeLength;
        protected int garbageLength;

        public NodeBlock(int index) {
            this.offset = index * GraphStoreConfiguration.NODESTORE_BLOCK_SIZE;
            if (GraphStoreConfiguration.NODESTORE_BLOCK_SIZE >= Short.MAX_VALUE - Short.MIN_VALUE) {
                throw new RuntimeException("BLOCK SIZE can't exceed 65535");
            }
            this.garbageArray = new short[GraphStoreConfiguration.NODESTORE_BLOCK_SIZE];
            this.backingArray = new NodeImpl[GraphStoreConfiguration.NODESTORE_BLOCK_SIZE];
        }

        public boolean hasGarbage() {
            return garbageLength > 0;
        }

        public int getCapacity() {
            return GraphStoreConfiguration.NODESTORE_BLOCK_SIZE - nodeLength - garbageLength;
        }

        public void add(NodeImpl k) {
            int i = nodeLength++;
            backingArray[i] = k;
            k.setStoreId(i + offset);
        }

        public void set(NodeImpl k) {
            int i = garbageArray[--garbageLength] - Short.MIN_VALUE;
            backingArray[i] = k;
            k.setStoreId(i + offset);
        }

        public NodeImpl get(int id) {
            return backingArray[id - offset];
        }

        public void remove(NodeImpl k) {
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

    protected final class NodeStoreIterator implements Iterator<Node> {

        protected int blockIndex;
        protected NodeImpl[] backingArray;
        protected int blockLength;
        protected int cursor;
        protected NodeImpl pointer;

        public NodeStoreIterator() {
            this.backingArray = blocks[blockIndex].backingArray;
            this.blockLength = blocks[blockIndex].nodeLength;
            readLock();
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
        public NodeImpl next() {
            return pointer;
        }

        @Override
        public void remove() {
            checkWriteLock();
            if (edgeStore != null) {
                for (EdgeStore.EdgeInOutIterator edgeIterator = edgeStore.edgeIterator(pointer); edgeIterator.hasNext();) {
                    edgeIterator.next();
                    edgeIterator.remove();
                }
            }
            NodeStore.this.remove(pointer);
        }
    }
}
