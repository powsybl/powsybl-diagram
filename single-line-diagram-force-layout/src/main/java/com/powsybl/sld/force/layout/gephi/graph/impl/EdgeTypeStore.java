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

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;
import it.unimi.dsi.fastutil.shorts.ShortSortedSet;
import com.powsybl.sld.force.layout.gephi.graph.api.Configuration;
import com.powsybl.sld.force.layout.gephi.graph.impl.utils.MapDeepEquals;

import java.util.Arrays;

public class EdgeTypeStore {

    // Const
    protected static final int NULL_TYPE = -1;
    protected static final int NULL_LABEL = 0;
    private static final short NULL_SHORT = Short.MIN_VALUE;
    // Config
    public static final int MAX_SIZE = 65534;
    // Data
    protected final Configuration configuration;
    protected final Object2ShortMap labelMap;
    protected final Short2ObjectMap idMap;
    protected final ShortSortedSet garbageQueue;
    protected int length;

    public EdgeTypeStore() {
        this(new Configuration());
    }

    public EdgeTypeStore(Configuration config) {
        if (MAX_SIZE >= Short.MAX_VALUE - Short.MIN_VALUE + 1) {
            throw new RuntimeException("Edge Type Store size can't exceed 65534");
        }
        this.configuration = config;
        this.garbageQueue = new ShortRBTreeSet();
        this.labelMap = new Object2ShortOpenHashMap(MAX_SIZE);
        this.idMap = new Short2ObjectOpenHashMap(MAX_SIZE);
        labelMap.defaultReturnValue(NULL_SHORT);

        // Add null type
        short id = intToShort(NULL_LABEL);
        length++;
        labelMap.put(null, id);
        idMap.put(id, null);
    }

    public int getId(final Object label) {
        short id = labelMap.getShort(label);
        if (id == NULL_SHORT) {
            return NULL_TYPE;
        }
        return shortToInt(id);
    }

    public Object getLabel(final int id) {
        checkValidId(id);
        checkIdExists(id);

        return idMap.get(intToShort(id));
    }

    public int addType(final Object label) {
        checkType(label);

        short id = labelMap.getShort(label);
        if (id == NULL_SHORT) {
            if (!garbageQueue.isEmpty()) {
                id = garbageQueue.firstShort();
                garbageQueue.remove(id);
            } else {
                id = intToShort(length);
                if (length >= MAX_SIZE) {
                    throw new RuntimeException("Maximum number of edge types reached at " + MAX_SIZE);
                }
                length++;
            }
            labelMap.put(label, id);
            idMap.put(id, label);
        }
        return shortToInt(id);
    }

    public boolean addType(final Object label, final int id) {
        short givenId = intToShort(id);
        short foundId = labelMap.getShort(label);
        if (foundId != NULL_SHORT && foundId != givenId) {
            throw new RuntimeException("This label '" + label + "' is already assigned to a different id");
        } else if (idMap.containsKey(givenId)) {
            if ((label == null && idMap.get(givenId) == null) || idMap.get(givenId).equals(label)) {
                return false;
            } else {
                throw new RuntimeException("This id '" + id + "' is already assigned to a different label");
            }
        }
        if (garbageQueue.contains(givenId)) {
            garbageQueue.remove(givenId);
            labelMap.put(label, givenId);
            idMap.put(givenId, label);
        } else {
            short insertId = intToShort(length++);
            while (insertId < givenId) {
                garbageQueue.add(insertId);
                insertId = intToShort(length++);
                if (length >= MAX_SIZE) {
                    throw new RuntimeException("Maximum number of edge types reached at " + MAX_SIZE);
                }
            }
            labelMap.put(label, givenId);
            idMap.put(givenId, label);
        }
        return true;
    }

    public int removeType(final Object label) {
        short id = labelMap.removeShort(label);
        if (id == NULL_SHORT) {
            return NULL_TYPE;
        }
        idMap.remove(id);
        garbageQueue.add(id);

        int intId = shortToInt(id);
        return intId;
    }

    public Object removeType(final int type) {
        checkValidId(type);

        short id = intToShort(type);
        Object label = idMap.remove(id);
        if (label != null) {
            labelMap.remove(label);
            garbageQueue.add(id);

        }
        return label;
    }

    public Object[] getLabels() {
        return labelMap.keySet().toArray();
    }

    protected short[] getIds() {
        return labelMap.values().toShortArray();
    }

    public int[] getIdsAsInts() {
        short[] s = getIds();
        int[] res = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            res[i] = shortToInt(s[i]);
        }
        return res;
    }

    protected short[] getGarbage() {
        return garbageQueue.toShortArray();
    }

    public boolean contains(final Object label) {
        return labelMap.containsKey(label);
    }

    public boolean contains(final int id) {
        checkValidId(id);

        return idMap.containsKey(intToShort(id));
    }

    public void clear() {
        labelMap.clear();
        idMap.clear();
        garbageQueue.clear();
        length = 0;

        // Add null type
        short id = intToShort(NULL_LABEL);
        length++;
        labelMap.put(null, id);
        idMap.put(id, null);
    }

    public int size() {
        return length - garbageQueue.size();
    }

    private short intToShort(final int id) {
        return (short) (id + Short.MIN_VALUE + 1);
    }

    private int shortToInt(final short id) {
        return id - Short.MIN_VALUE - 1;
    }

    private void checkValidId(final int id) {
        if (id < 0 || id >= MAX_SIZE) {
            throw new IllegalArgumentException("The type must be included between 0 and 65535");
        }
    }

    private void checkType(final Object o) {
        if (o != null) {
            Class cl = o.getClass();
            if (!(cl.equals(Integer.class) || cl.equals(String.class) || cl.equals(Float.class) || cl
                    .equals(Double.class) || cl.equals(Short.class) || cl.equals(Byte.class) || cl.equals(Long.class) || cl
                    .equals(Character.class) || cl.equals(Boolean.class))) {
                throw new IllegalArgumentException(
                        "The type id is " + cl.getCanonicalName() + " but must be a primitive type (int, string, long...)");
            }
            if (!configuration.getEdgeLabelType().equals(o.getClass())) {
                throw new IllegalArgumentException("The expected type was " + configuration.getEdgeLabelType()
                        .getCanonicalName() + " and " + o.getClass().getCanonicalName() + " was found");
            }
        }
    }

    private void checkIdExists(final int id) {
        if (!idMap.containsKey(intToShort(id))) {
            throw new IllegalArgumentException("The id " + id + " doesn' exist");
        }
    }

    public int deepHashCode() {
        int hash = 3;
        short[] keys = idMap.keySet().toShortArray();
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++) {
            Short s = keys[i];
            Object o = idMap.get(s);
            hash = 67 * hash + (o != null ? o.hashCode() : 0);
            hash = 67 * hash + s.hashCode();
        }
        return hash;
    }

    public boolean deepEquals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EdgeTypeStore other = (EdgeTypeStore) obj;
        if (!MapDeepEquals.mapDeepEquals(labelMap, other.labelMap)) {
            return false;
        }
        if (!MapDeepEquals.mapDeepEquals(idMap, other.idMap)) {
            return false;
        }
        return true;
    }
}
