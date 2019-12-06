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

import com.powsybl.sld.force.layout.gephi.graph.api.Interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Interval2IntTreeMap implements Map<Interval, Integer> {

    // Constant so the min/max returns the lowest/highest non-infinite value
    private static final boolean EXCLUDE_INFINITE = true;
    //
    private Node nil; // the sentinel node
    private Node root; // the root of this interval tree
    private int size = 0;

    /**
     * Constructs an empty map.
     */
    public Interval2IntTreeMap() {
        nil = new Node();
        nil.left = nil.right = nil.p = nil;
        root = nil;
    }

    private boolean compareLow(Interval a, Interval b) {
        if (a.getLow() == b.getLow()) {
            if (a.getHigh() <= b.getHigh()) {
                return true;
            }
        } else if (a.getLow() < b.getLow()) {
            return true;
        }
        return false;
    }

    @Override
    public Integer put(Interval interval, Integer value) {
        if (interval == null) {
            throw new NullPointerException("Interval cannot be null.");
        }
        if (value == null) {
            throw new NullPointerException("Value cannot be null.");
        }

        Node n = findNode(root.left, interval);
        if (n != null) {
            remove(interval);
            insert(new Node(interval, value));
            return n.v;
        }

        insert(new Node(interval, value));

        return null;
    }

    private void insert(Node z) {
        z.left = z.right = nil;

        Node y = root;
        Node x = root.left;
        while (x != nil) {
            y = x;
            if (compareLow(z.i, y.i)) {
                x = x.left;
            } else {
                x = x.right;
            }

            y.max = Math.max(z.max, y.max);
            if (y.p == root) {
                root.max = y.max;
            }
        }
        z.p = y;
        if (y == root) {
            root.max = z.max;
        }
        if (y == root || compareLow(z.i, y.i)) {
            y.left = z;
        } else {
            y.right = z;
        }
        insertFixup(z);
        size++;
    }

    private void insertFixup(Node zParam) {
        Node z = zParam;
        Node y;

        z.color = RED;
        while (z.p.color == RED) {
            if (z.p == z.p.p.left) {
                y = z.p.p.right;
                if (y.color == RED) {
                    z.p.color = BLACK;
                    y.color = BLACK;
                    z.p.p.color = RED;
                    z = z.p.p;
                } else {
                    if (z == z.p.right) {
                        z = z.p;
                        leftRotate(z);
                    }
                    z.p.color = BLACK;
                    z.p.p.color = RED;
                    rightRotate(z.p.p);
                }
            } else {
                y = z.p.p.left;
                if (y.color == RED) {
                    z.p.color = BLACK;
                    y.color = BLACK;
                    z.p.p.color = RED;
                    z = z.p.p;
                } else {
                    if (z == z.p.left) {
                        z = z.p;
                        rightRotate(z);
                    }
                    z.p.color = BLACK;
                    z.p.p.color = RED;
                    leftRotate(z.p.p);
                }
            }
        }
        root.left.color = BLACK;
    }

    @Override
    public Integer remove(Object interval) {
        Node n = findNode(root.left, (Interval) interval);
        if (n != null) {
            delete(n);
            return n.v;
        }
        return null;
    }

    @Override
    public Integer get(Object interval) {
        Node n = findNode(root.left, (Interval) interval);
        return n != null ? n.v : null;
    }

    @Override
    public boolean containsKey(Object interval) {
        return findNode(root.left, (Interval) interval) != null;
    }

    private Node findNode(Node z, Interval i) {
        if (z != null) {
            if (z.i != null && z.i.equals(i)) {
                return z;
            } else if (z.i == null) {
                return null;
            }
            boolean cmp = compareLow(i, z.i);
            if (z.left != null && cmp && i.getHigh() <= z.left.max) {
                Node r = findNode(z.left, i);
                if (r != null) {
                    return r;
                }
            }
            if (z.right != null && !cmp && i.getHigh() <= z.right.max) {
                Node r = findNode(z.right, i);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private void delete(Node z) {
        z.max = Double.NEGATIVE_INFINITY;

        Node y;
        Node x;

        if (z.left == nil || z.right == nil) {
            y = z;
        } else {
            y = succesor(z);
        }
        if (y.left == nil) {
            x = y.right;
        } else {
            x = y.left;
        }
        x.p = y.p;
        if (root == x.p) {
            root.left = x;
        } else if (y == y.p.left) {
            y.p.left = x;
        } else {
            y.p.right = x;
        }
        if (y != z) {
            if (y.color == BLACK) {
                deleteFixup(x);
            }

            y.left = z.left;
            y.right = z.right;
            y.p = z.p;
            y.color = z.color;
            z.left.p = z.right.p = y;
            if (z == z.p.left) {
                z.p.left = y;
            } else {
                z.p.right = y;
            }
        } else if (y.color == BLACK) {
            deleteFixup(x);
        }

        computeMax(y);
        for (Node i = y.p; i != root; i = i.p) {
            computeMax(i);
            if (i.p == root) {
                root.max = i.max;
            }
        }
        size--;
    }

    private void deleteFixup(Node xParam) {
        Node x = xParam;
        while (x != root.left && x.color == BLACK) {
            if (x == x.p.left) {
                Node w = x.p.right;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.p.color = RED;
                    leftRotate(x.p);
                    w = x.p.right;
                }
                if (w.left.color == BLACK && w.right.color == BLACK) {
                    w.color = RED;
                    x = x.p;
                } else {
                    if (w.right.color == BLACK) {
                        w.left.color = BLACK;
                        w.color = RED;
                        rightRotate(w);
                        w = x.p.right;
                    }
                    w.color = x.p.color;
                    x.p.color = BLACK;
                    w.right.color = BLACK;
                    leftRotate(x.p);
                    x = root.left;
                }
            } else {
                Node w = x.p.left;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.p.color = RED;
                    rightRotate(x.p);
                    w = x.p.left;
                }
                if (w.right.color == BLACK && w.left.color == BLACK) {
                    w.color = RED;
                    x = x.p;
                } else {
                    if (w.left.color == BLACK) {
                        w.right.color = BLACK;
                        w.color = RED;
                        leftRotate(w);
                        w = x.p.left;
                    }
                    w.color = x.p.color;
                    x.p.color = BLACK;
                    w.left.color = BLACK;
                    rightRotate(x.p);
                    x = root.left;
                }
            }
        }
        x.color = BLACK;
    }

    private void leftRotate(Node x) {
        Node y = x.right;

        x.right = y.left;
        if (y.left != nil) {
            y.left.p = x;
        }
        y.p = x.p;
        if (x == x.p.left) {
            x.p.left = y;
        } else {
            x.p.right = y;
        }
        y.left = x;
        x.p = y;

        if (y.p == root) {
            root.max = x.max;
        }
        y.max = x.max;
        computeMax(x);
    }

    private void rightRotate(Node x) {
        Node y = x.left;

        x.left = y.right;
        if (y.right != nil) {
            y.right.p = x;
        }
        y.p = x.p;
        if (x == x.p.left) {
            x.p.left = y;
        } else {
            x.p.right = y;
        }
        y.right = x;
        x.p = y;

        if (y.p == root) {
            root.max = x.max;
        }
        y.max = x.max;
        computeMax(x);
    }

    private void computeMax(Node x) {
        if (x.left == nil && x.right == nil) {
            x.max = x.i.getHigh();
        } else if (x.left == nil) {
            x.max = Math.max(x.i.getHigh(), x.right.max);
        } else if (x.right == nil) {
            x.max = Math.max(x.i.getHigh(), x.left.max);
        } else {
            x.max = Math.max(x.i.getHigh(), Math.max(x.left.max, x.right.max));
        }
    }

    private Node succesor(Node xParam) {
        Node x = xParam;
        Node y = x.right;
        if (y != nil) {
            while (y.left != nil) {
                y = y.left;
            }
            return y;
        }
        y = x.p;
        while (x == y.right) {
            x = y;
            y = y.p;
        }
        if (y == root) {
            return nil;
        }
        return y;
    }

    /**
     * Returns the interval with the lowest left endpoint.
     *
     * @return the interval with the lowest left endpoint or null if the tree is
     *         empty.
     */
    public Interval minimum() {
        if (root.left == nil) {
            return null;
        }

        return treeMinimum(root.left);
    }

    private Interval treeMinimum(Node xParam) {
        Node x = xParam;
        // TODO: Better algorithm
        while (x.left != nil) {
            x = x.left;
        }
        if (EXCLUDE_INFINITE && Double.isInfinite(x.i.getLow())) {
            List<Interval> ints = getIntervals();
            for (Interval i : ints) {
                if (!Double.isInfinite(i.getLow())) {
                    return i;
                }
            }
            return ints.get(0);
        }
        return x.i;
    }

    /**
     * Returns the interval with the highest right endpoint.
     *
     * @return the interval with the highest right endpoint or null if the tree
     *         is empty.
     */
    public Interval maximum() {
        if (root.left == nil) {
            return null;
        }
        return treeMaximum(root.left).i;
    }

    private Node treeMaximum(Node x) {
        if (x.right != nil && x.right.max == x.max) {
            return treeMaximum(x.right);
        } else if (x.left != nil && x.left.max == x.max) {
            return treeMaximum(x.left);
        }
        return x;
    }

    /**
     * Returns the leftmost point or {@code Double.NEGATIVE_INFINITY} in case of
     * no intervals.
     *
     * @return the leftmost point
     */
    public double getLow() {
        if (isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }
        Interval min = minimum();
        if (Double.isInfinite(min.getLow()) && !Double.isInfinite(min.getHigh())) {
            return min.getHigh();
        }
        return min.getLow();
    }

    /**
     * Returns the rightmost point or {@code Double.POSITIVE_INFINITY} in case
     * of no intervals.
     *
     * @return the rightmost point
     */
    public double getHigh() {
        if (isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        double max = root.left.max;
        if (Double.isInfinite(max)) {
            // TODO: Better alg
            max = Double.NEGATIVE_INFINITY;
            for (Interval i : getIntervals()) {
                if (!Double.isInfinite(i.getLow())) {
                    max = Math.max(max, i.getLow());
                }
                if (!Double.isInfinite(i.getHigh())) {
                    max = Math.max(max, i.getHigh());
                }
            }
            if (max == Double.NEGATIVE_INFINITY) {
                return Double.POSITIVE_INFINITY;
            }
        }
        return max;
    }

    @Override
    public boolean isEmpty() {
        return root.left == nil;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        size = 0;
        root = nil;
        nil.left = nil.right = nil.p = nil;
    }

    /**
     * Returns all intervals ordered first by low and then by high bounds.
     *
     * @return all intervals
     */
    public List<Interval> getIntervals() {
        List<Interval> list = new ArrayList<Interval>();
        inorderTreeWalk(root.left, list);
        return list;
    }

    @Override
    public Set<Map.Entry<Interval, Integer>> entrySet() {
        return entrySet(Interval.INFINITY_INTERVAL);
    }

    /**
     * Returns an entry set of all entries, which interval keys overlap with
     * <code>point</code>.
     *
     * @param point point
     * @return entry set
     */
    public Set<Map.Entry<Interval, Integer>> entrySet(double point) {
        return new EntryIterable(searchNodes(point));
    }

    /**
     * Returns an entry set of all entries, which interval keys overlap with
     * <code>interval</code>.
     *
     * @param interval interval
     * @return entry set
     */
    public Set<Map.Entry<Interval, Integer>> entrySet(Interval interval) {
        if (interval == null) {
            throw new NullPointerException("Interval cannot be null.");
        }

        return new EntryIterable(searchNodes(interval));
    }

    @Override
    public Collection<Integer> values() {
        return Interval2IntTreeMap.this.values(Interval.INFINITY_INTERVAL);
    }

    /**
     * Returns values which interval keys overlap with <code>interval</code>.
     *
     * @param interval interval
     * @return values
     */
    public Collection<Integer> values(Interval interval) {
        if (interval == null) {
            throw new NullPointerException("Interval cannot be null.");
        }

        return new ValueIterable(searchNodes(interval));
    }

    /**
     * Returns values which interval keys overlap with <code>point</code>.
     *
     * @param point point
     * @return values
     */
    public Iterable<Integer> values(double point) {
        return new ValueIterable(searchNodes(point));
    }

    private List<Node> searchNodes(Interval interval) {
        List<Node> result = new ArrayList<Node>();
        searchNodes(root.left, interval, result);
        return result;
    }

    private List<Node> searchNodes(double point) {
        List<Node> result = new ArrayList<Node>();
        searchNodes(root.left, point, result);
        return result;
    }

    private void searchNodes(Node n, Interval interval, List<Node> result) {
        // Don't search nodes that don't exist.
        if (n == nil) {
            return;
        }

        // Skip all nodes that have got their max value below the start of
        // the given interval.
        if (interval.getLow() > n.max) {
            return;
        }

        // Search left children.
        if (n.left != nil) {
            searchNodes(n.left, interval, result);
        }

        // Check this node.
        if (n.i.compareTo(interval) == 0) {
            result.add(n);
        }

        // Skip all nodes to the right of nodes whose low value is past the end
        // of the given interval.
        if (interval.compareTo(n.i) < 0) {
            return;
        }

        // Otherwise, search right children.
        if (n.right != nil) {
            searchNodes(n.right, interval, result);
        }
    }

    private void searchNodes(Node n, double point, List<Node> result) {
        // Don't search nodes that don't exist.
        if (n == nil) {
            return;
        }

        // Skip all nodes that have got their max value below the start of
        // the given interval.
        if (point > n.max) {
            return;
        }

        // Search left children.
        if (n.left != nil) {
            searchNodes(n.left, point, result);
        }

        // Check this node.
        if (n.i.compareTo(point) == 0) {
            result.add(n);
        }

        // Skip all nodes to the right of nodes whose low value is past the end
        // of the given interval.
        if (point < n.i.getLow()) {
            return;
        }

        // Otherwise, search right children.
        if (n.right != nil) {
            searchNodes(n.right, point, result);
        }
    }

    private void inorderTreeWalk(Node x, List<Interval> list) {
        if (x != nil) {
            inorderTreeWalk(x.left, list);
            list.add(x.i);
            inorderTreeWalk(x.right, list);
        }
    }

    /**
     * Compares this interval tree with the specified object for equality.
     *
     * <p>
     * Note that two interval trees are equal if they contain the same
     * intervals.
     *
     * @param obj object to which this interval tree is to be compared
     *
     * @return {@code true} if and only if the specified {@code Object} is a
     *         {@code IntervalTree} which contain the same intervals as this
     *         {@code IntervalTree's}.
     *
     * @see #hashCode
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())) {
            Interval2IntTreeMap other = (Interval2IntTreeMap) obj;
            if (other.size != size) {
                return false;
            }

            Iterator<Map.Entry<Interval, Integer>> thisIntervals = entrySet(Interval.INFINITY_INTERVAL).iterator();
            Iterator<Map.Entry<Interval, Integer>> otherIntervals = other.entrySet(Interval.INFINITY_INTERVAL)
                    .iterator();

            while (thisIntervals.hasNext()) {
                Map.Entry<Interval, Integer> thisEntry = thisIntervals.next();
                Map.Entry<Interval, Integer> otherEntry = otherIntervals.next();
                if (!thisEntry.equals(otherEntry)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns a hashcode of this interval tree.
     *
     * @return a hashcode of this interval tree.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        Iterator<Map.Entry<Interval, Integer>> thisIntervals = entrySet(Interval.INFINITY_INTERVAL).iterator();
        while (thisIntervals.hasNext()) {
            hash = 97 * hash + thisIntervals.next().hashCode();
        }
        return hash;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void putAll(Map<? extends Interval, ? extends Integer> m) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<Interval> keySet() {
        throw new UnsupportedOperationException("Not supported.");
    }

    private static class Node {

        private final Interval i; // i.low is the key of this node
        private final int v; // value
        private double max; // the maximum value of any interval endpoint stored
        // in the subtree rooted at this node

        private Color color; // the color of this node
        private Node left; // the left subtree of this node
        private Node right; // the right subtree of this node
        private Node p; // the parent node

        public Node() {
            color = BLACK;
            i = null;
            v = 0;
        }

        public Node(Interval key, int value) {
            color = BLACK;
            i = key;
            v = value;
            max = i.getHigh();
        }
    }

    private enum Color {
        RED, BLACK
    }

    private static final Color RED = Color.RED;
    private static final Color BLACK = Color.BLACK;

    private static class EntryIterable implements Set<Map.Entry<Interval, Integer>> {

        public final List<Node> nodes;

        public EntryIterable(List<Node> nodes) {
            this.nodes = nodes;
        }

        @Override
        public Iterator<Map.Entry<Interval, Integer>> iterator() {
            return new EntryIterator(nodes.iterator());
        }

        @Override
        public int size() {
            return nodes.size();
        }

        @Override
        public boolean isEmpty() {
            return nodes.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean add(Map.Entry<Interval, Integer> e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean addAll(Collection<? extends Map.Entry<Interval, Integer>> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    private static class EntryIterator implements Iterator<Map.Entry<Interval, Integer>> {

        private final Iterator<Node> nodes;
        private final Entry entry = new Entry();

        public EntryIterator(Iterator<Node> nodes) {
            this.nodes = nodes;
        }

        @Override
        public boolean hasNext() {
            return nodes.hasNext();
        }

        @Override
        public Map.Entry<Interval, Integer> next() {
            Node n = nodes.next();
            entry.set(n.i, n.v);
            return entry;
        }

    }

    private static class Entry implements Map.Entry<Interval, Integer> {

        private Interval key;
        private int value;

        public int getIntValue() {
            return value;
        }

        @Override
        public Interval getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(Integer value) {
            throw new UnsupportedOperationException("Not supported.");
        }

        private void set(Interval key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.key != null ? this.key.hashCode() : 0);
            hash = 89 * hash + this.value;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if (this.value != other.value) {
                return false;
            }
            if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
                return false;
            }
            return true;
        }
    }

    private static class ValueIterable implements Collection<Integer> {

        public final List<Node> nodes;

        public ValueIterable(List<Node> nodes) {
            this.nodes = nodes;
        }

        @Override
        public Iterator<Integer> iterator() {
            return new ValueIterator(nodes.iterator());
        }

        @Override
        public int size() {
            return nodes.size();
        }

        @Override
        public boolean isEmpty() {
            return nodes.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean add(Integer e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean addAll(Collection<? extends Integer> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    private static class ValueIterator implements Iterator<Integer> {

        private final Iterator<Node> nodes;

        public ValueIterator(Iterator<Node> nodes) {
            this.nodes = nodes;
        }

        @Override
        public boolean hasNext() {
            return nodes.hasNext();
        }

        @Override
        public Integer next() {
            Node n = nodes.next();
            return n.v;
        }

    }
}
