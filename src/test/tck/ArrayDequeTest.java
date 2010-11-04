/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

import junit.framework.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class ArrayDequeTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(ArrayDequeTest.class);
    }

    /**
     * Create a deque of given size containing consecutive
     * Integers 0 ... n.
     */
    private ArrayDeque populatedDeque(int n) {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.isEmpty());
        for (int i = 0; i < n; ++i)
            assertTrue(q.offerLast(new Integer(i)));
        assertFalse(q.isEmpty());
        assertEquals(n, q.size());
        return q;
    }

    /**
     * new deque is empty
     */
    public void testConstructor1() {
        assertEquals(0, new ArrayDeque().size());
    }

    /**
     * Initializing from null Collection throws NPE
     */
    public void testConstructor3() {
        try {
            ArrayDeque q = new ArrayDeque((Collection)null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Initializing from Collection of null elements throws NPE
     */
    public void testConstructor4() {
        try {
            Integer[] ints = new Integer[SIZE];
            ArrayDeque q = new ArrayDeque(Arrays.asList(ints));
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Initializing from Collection with some null elements throws NPE
     */
    public void testConstructor5() {
        try {
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new Integer(i);
            ArrayDeque q = new ArrayDeque(Arrays.asList(ints));
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Deque contains all elements of collection used to initialize
     */
    public void testConstructor6() {
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE; ++i)
            ints[i] = new Integer(i);
        ArrayDeque q = new ArrayDeque(Arrays.asList(ints));
        for (int i = 0; i < SIZE; ++i)
            assertEquals(ints[i], q.pollFirst());
    }

    /**
     * isEmpty is true before add, false after
     */
    public void testEmpty() {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.isEmpty());
        q.add(new Integer(1));
        assertFalse(q.isEmpty());
        q.add(new Integer(2));
        q.removeFirst();
        q.removeFirst();
        assertTrue(q.isEmpty());
    }

    /**
     * size changes when elements added and removed
     */
    public void testSize() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(SIZE-i, q.size());
            q.removeFirst();
        }
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.size());
            q.add(new Integer(i));
        }
    }

    /**
     * push(null) throws NPE
     */
    public void testPushNull() {
        try {
            ArrayDeque q = new ArrayDeque(1);
            q.push(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * peekFirst() returns element inserted with push
     */
    public void testPush() {
        ArrayDeque q = populatedDeque(3);
        q.pollLast();
        q.push(four);
        assertSame(four, q.peekFirst());
    }

    /**
     * pop() removes next element, or throws NSEE if empty
     */
    public void testPop() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.pop());
        }
        try {
            q.pop();
            shouldThrow();
        } catch (NoSuchElementException success) {}
    }

    /**
     * offer(null) throws NPE
     */
    public void testOfferNull() {
        try {
            ArrayDeque q = new ArrayDeque();
            q.offer(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * offerFirst(null) throws NPE
     */
    public void testOfferFirstNull() {
        try {
            ArrayDeque q = new ArrayDeque();
            q.offerFirst(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * offerLast(null) throws NPE
     */
    public void testOfferLastNull() {
        try {
            ArrayDeque q = new ArrayDeque();
            q.offerLast(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * offer(x) succeeds
     */
    public void testOffer() {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.offer(zero));
        assertTrue(q.offer(one));
        assertSame(zero, q.peekFirst());
        assertSame(one, q.peekLast());
    }

    /**
     * offerFirst(x) succeeds
     */
    public void testOfferFirst() {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.offerFirst(zero));
        assertTrue(q.offerFirst(one));
        assertSame(one, q.peekFirst());
        assertSame(zero, q.peekLast());
    }

    /**
     * offerLast(x) succeeds
     */
    public void testOfferLast() {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.offerLast(zero));
        assertTrue(q.offerLast(one));
        assertSame(zero, q.peekFirst());
        assertSame(one, q.peekLast());
    }

    /**
     * add(null) throws NPE
     */
    public void testAddNull() {
        try {
            ArrayDeque q = new ArrayDeque();
            q.add(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * addFirst(null) throws NPE
     */
    public void testAddFirstNull() {
        try {
            ArrayDeque q = new ArrayDeque();
            q.addFirst(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * addLast(null) throws NPE
     */
    public void testAddLastNull() {
        try {
            ArrayDeque q = new ArrayDeque();
            q.addLast(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * add(x) succeeds
     */
    public void testAdd() {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.add(zero));
        assertTrue(q.add(one));
        assertSame(zero, q.peekFirst());
        assertSame(one, q.peekLast());
    }

    /**
     * addFirst(x) succeeds
     */
    public void testAddFirst() {
        ArrayDeque q = new ArrayDeque();
        q.addFirst(zero);
        q.addFirst(one);
        assertSame(one, q.peekFirst());
        assertSame(zero, q.peekLast());
    }

    /**
     * addLast(x) succeeds
     */
    public void testAddLast() {
        ArrayDeque q = new ArrayDeque();
        q.addLast(zero);
        q.addLast(one);
        assertSame(zero, q.peekFirst());
        assertSame(one, q.peekLast());
    }

    /**
     * addAll(null) throws NPE
     */
    public void testAddAll1() {
        try {
            ArrayDeque q = new ArrayDeque();
            q.addAll(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * addAll of a collection with null elements throws NPE
     */
    public void testAddAll2() {
        try {
            ArrayDeque q = new ArrayDeque();
            Integer[] ints = new Integer[SIZE];
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * addAll of a collection with any null elements throws NPE after
     * possibly adding some elements
     */
    public void testAddAll3() {
        try {
            ArrayDeque q = new ArrayDeque();
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new Integer(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Deque contains all elements, in traversal order, of successful addAll
     */
    public void testAddAll5() {
        Integer[] empty = new Integer[0];
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE; ++i)
            ints[i] = new Integer(i);
        ArrayDeque q = new ArrayDeque();
        assertFalse(q.addAll(Arrays.asList(empty)));
        assertTrue(q.addAll(Arrays.asList(ints)));
        for (int i = 0; i < SIZE; ++i)
            assertEquals(ints[i], q.pollFirst());
    }

    /**
     * pollFirst() succeeds unless empty
     */
    public void testPollFirst() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.pollFirst());
        }
        assertNull(q.pollFirst());
    }

    /**
     * pollLast() succeeds unless empty
     */
    public void testPollLast() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = SIZE-1; i >= 0; --i) {
            assertEquals(i, q.pollLast());
        }
        assertNull(q.pollLast());
    }

    /**
     * poll() succeeds unless empty
     */
    public void testPoll() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.poll());
        }
        assertNull(q.poll());
    }

    /**
     * remove() removes next element, or throws NSEE if empty
     */
    public void testRemove() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.remove());
        }
        try {
            q.remove();
            shouldThrow();
        } catch (NoSuchElementException success) {}
    }

    /**
     * remove(x) removes x and returns true if present
     */
    public void testRemoveElement() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 1; i < SIZE; i+=2) {
            assertTrue(q.remove(new Integer(i)));
        }
        for (int i = 0; i < SIZE; i+=2) {
            assertTrue(q.remove(new Integer(i)));
            assertFalse(q.remove(new Integer(i+1)));
        }
        assertTrue(q.isEmpty());
    }

    /**
     * peekFirst() returns next element, or null if empty
     */
    public void testPeekFirst() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.peekFirst());
            assertEquals(i, q.pollFirst());
            assertTrue(q.peekFirst() == null ||
                       !q.peekFirst().equals(i));
        }
        assertNull(q.peekFirst());
    }

    /**
     * peek() returns next element, or null if empty
     */
    public void testPeek() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.peek());
            assertEquals(i, q.poll());
            assertTrue(q.peek() == null ||
                       !q.peek().equals(i));
        }
        assertNull(q.peek());
    }

    /**
     * peekLast() returns next element, or null if empty
     */
    public void testPeekLast() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = SIZE-1; i >= 0; --i) {
            assertEquals(i, q.peekLast());
            assertEquals(i, q.pollLast());
            assertTrue(q.peekLast() == null ||
                       !q.peekLast().equals(i));
        }
        assertNull(q.peekLast());
    }

    /**
     * element() returns first element, or throws NSEE if empty
     */
    public void testElement() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.element());
            assertEquals(i, q.poll());
        }
        try {
            q.element();
            shouldThrow();
        } catch (NoSuchElementException success) {}
    }

    /**
     * getFirst() returns first element, or throws NSEE if empty
     */
    public void testFirstElement() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.getFirst());
            assertEquals(i, q.pollFirst());
        }
        try {
            q.getFirst();
            shouldThrow();
        } catch (NoSuchElementException success) {}
    }

    /**
     * getLast() returns last element, or throws NSEE if empty
     */
    public void testLastElement() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = SIZE-1; i >= 0; --i) {
            assertEquals(i, q.getLast());
            assertEquals(i, q.pollLast());
        }
        try {
            q.getLast();
            shouldThrow();
        } catch (NoSuchElementException success) {}
        assertNull(q.peekLast());
    }


    /**
     * removeFirst() removes first element, or throws NSEE if empty
     */
    public void testRemoveFirst() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.removeFirst());
        }
        try {
            q.removeFirst();
            shouldThrow();
        } catch (NoSuchElementException success) {}
        assertNull(q.peekFirst());
    }

    /**
     * removeLast() removes last element, or throws NSEE if empty
     */
    public void testRemoveLast() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = SIZE - 1; i >= 0; --i) {
            assertEquals(i, q.removeLast());
        }
        try {
            q.removeLast();
            shouldThrow();
        } catch (NoSuchElementException success) {}
        assertNull(q.peekLast());
    }

    /**
     * removeFirstOccurrence(x) removes x and returns true if present
     */
    public void testRemoveFirstOccurrence() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 1; i < SIZE; i+=2) {
            assertTrue(q.removeFirstOccurrence(new Integer(i)));
        }
        for (int i = 0; i < SIZE; i+=2) {
            assertTrue(q.removeFirstOccurrence(new Integer(i)));
            assertFalse(q.removeFirstOccurrence(new Integer(i+1)));
        }
        assertTrue(q.isEmpty());
    }

    /**
     * removeLastOccurrence(x) removes x and returns true if present
     */
    public void testRemoveLastOccurrence() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 1; i < SIZE; i+=2) {
            assertTrue(q.removeLastOccurrence(new Integer(i)));
        }
        for (int i = 0; i < SIZE; i+=2) {
            assertTrue(q.removeLastOccurrence(new Integer(i)));
            assertFalse(q.removeLastOccurrence(new Integer(i+1)));
        }
        assertTrue(q.isEmpty());
    }

    /**
     * contains(x) reports true when elements added but not yet removed
     */
    public void testContains() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.contains(new Integer(i)));
            assertEquals(i, q.pollFirst());
            assertFalse(q.contains(new Integer(i)));
        }
    }

    /**
     * clear removes all elements
     */
    public void testClear() {
        ArrayDeque q = populatedDeque(SIZE);
        q.clear();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        assertTrue(q.add(new Integer(1)));
        assertFalse(q.isEmpty());
        q.clear();
        assertTrue(q.isEmpty());
    }

    /**
     * containsAll(c) is true when c contains a subset of elements
     */
    public void testContainsAll() {
        ArrayDeque q = populatedDeque(SIZE);
        ArrayDeque p = new ArrayDeque();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.containsAll(p));
            assertFalse(p.containsAll(q));
            assertTrue(p.add(new Integer(i)));
        }
        assertTrue(p.containsAll(q));
    }

    /**
     * retainAll(c) retains only those elements of c and reports true if changed
     */
    public void testRetainAll() {
        ArrayDeque q = populatedDeque(SIZE);
        ArrayDeque p = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            boolean changed = q.retainAll(p);
            assertEquals(changed, (i > 0));
            assertTrue(q.containsAll(p));
            assertEquals(SIZE-i, q.size());
            p.removeFirst();
        }
    }

    /**
     * removeAll(c) removes only those elements of c and reports true if changed
     */
    public void testRemoveAll() {
        for (int i = 1; i < SIZE; ++i) {
            ArrayDeque q = populatedDeque(SIZE);
            ArrayDeque p = populatedDeque(i);
            assertTrue(q.removeAll(p));
            assertEquals(SIZE-i, q.size());
            for (int j = 0; j < i; ++j) {
                assertFalse(q.contains(p.removeFirst()));
            }
        }
    }

    /**
     * toArray() contains all elements in FIFO order
     */
    public void testToArray() {
        ArrayDeque q = populatedDeque(SIZE);
        Object[] o = q.toArray();
        for (int i = 0; i < o.length; i++)
            assertSame(o[i], q.pollFirst());
    }

    /**
     * toArray(a) contains all elements in FIFO order
     */
    public void testToArray2() {
        ArrayDeque q = populatedDeque(SIZE);
        Integer[] ints = new Integer[SIZE];
        assertSame(ints, q.toArray(ints));
        for (int i = 0; i < ints.length; i++)
            assertSame(ints[i], q.remove());
    }

    /**
     * toArray(null) throws NullPointerException
     */
    public void testToArray_NullArg() {
        ArrayDeque l = new ArrayDeque();
        l.add(new Object());
        try {
            l.toArray(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * toArray(incompatible array type) throws ArrayStoreException
     */
    public void testToArray1_BadArg() {
        ArrayDeque l = new ArrayDeque();
        l.add(new Integer(5));
        try {
            l.toArray(new String[10]);
            shouldThrow();
        } catch (ArrayStoreException success) {}
    }

    /**
     * Iterator iterates through all elements
     */
    public void testIterator() {
        ArrayDeque q = populatedDeque(SIZE);
        int i = 0;
        Iterator it = q.iterator();
        while (it.hasNext()) {
            assertTrue(q.contains(it.next()));
            ++i;
        }
        assertEquals(i, SIZE);
    }

    /**
     * Iterator ordering is FIFO
     */
    public void testIteratorOrdering() {
        final ArrayDeque q = new ArrayDeque();
        q.add(one);
        q.add(two);
        q.add(three);
        int k = 0;
        for (Iterator it = q.iterator(); it.hasNext();) {
            assertEquals(++k, it.next());
        }

        assertEquals(3, k);
    }

    /**
     * iterator.remove() removes current element
     */
    public void testIteratorRemove() {
        final ArrayDeque q = new ArrayDeque();
        final Random rng = new Random();
        for (int iters = 0; iters < 100; ++iters) {
            int max = rng.nextInt(5) + 2;
            int split = rng.nextInt(max-1) + 1;
            for (int j = 1; j <= max; ++j)
                q.add(new Integer(j));
            Iterator it = q.iterator();
            for (int j = 1; j <= split; ++j)
                assertEquals(it.next(), new Integer(j));
            it.remove();
            assertEquals(it.next(), new Integer(split+1));
            for (int j = 1; j <= split; ++j)
                q.remove(new Integer(j));
            it = q.iterator();
            for (int j = split+1; j <= max; ++j) {
                assertEquals(it.next(), new Integer(j));
                it.remove();
            }
            assertFalse(it.hasNext());
            assertTrue(q.isEmpty());
        }
    }

    /**
     * Descending iterator iterates through all elements
     */
    public void testDescendingIterator() {
        ArrayDeque q = populatedDeque(SIZE);
        int i = 0;
        Iterator it = q.descendingIterator();
        while (it.hasNext()) {
            assertTrue(q.contains(it.next()));
            ++i;
        }
        assertEquals(i, SIZE);
        assertFalse(it.hasNext());
        try {
            it.next();
            shouldThrow();
        } catch (NoSuchElementException success) {}
    }

    /**
     * Descending iterator ordering is reverse FIFO
     */
    public void testDescendingIteratorOrdering() {
        final ArrayDeque q = new ArrayDeque();
        for (int iters = 0; iters < 100; ++iters) {
            q.add(new Integer(3));
            q.add(new Integer(2));
            q.add(new Integer(1));
            int k = 0;
            for (Iterator it = q.descendingIterator(); it.hasNext();) {
                assertEquals(++k, it.next());
            }

            assertEquals(3, k);
            q.remove();
            q.remove();
            q.remove();
        }
    }

    /**
     * descendingIterator.remove() removes current element
     */
    public void testDescendingIteratorRemove() {
        final ArrayDeque q = new ArrayDeque();
        final Random rng = new Random();
        for (int iters = 0; iters < 100; ++iters) {
            int max = rng.nextInt(5) + 2;
            int split = rng.nextInt(max-1) + 1;
            for (int j = max; j >= 1; --j)
                q.add(new Integer(j));
            Iterator it = q.descendingIterator();
            for (int j = 1; j <= split; ++j)
                assertEquals(it.next(), new Integer(j));
            it.remove();
            assertEquals(it.next(), new Integer(split+1));
            for (int j = 1; j <= split; ++j)
                q.remove(new Integer(j));
            it = q.descendingIterator();
            for (int j = split+1; j <= max; ++j) {
                assertEquals(it.next(), new Integer(j));
                it.remove();
            }
            assertFalse(it.hasNext());
            assertTrue(q.isEmpty());
        }
    }


    /**
     * toString() contains toStrings of elements
     */
    public void testToString() {
        ArrayDeque q = populatedDeque(SIZE);
        String s = q.toString();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(s.indexOf(String.valueOf(i)) >= 0);
        }
    }

    /**
     * A deserialized serialized deque has same elements in same order
     */
    public void testSerialization() throws Exception {
        ArrayDeque q = populatedDeque(SIZE);
        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
        out.writeObject(q);
        out.close();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
        ArrayDeque r = (ArrayDeque)in.readObject();
        assertEquals(q.size(), r.size());
        while (!q.isEmpty())
            assertEquals(q.remove(), r.remove());
    }

}
