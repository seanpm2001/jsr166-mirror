/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import junit.framework.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMap8Test extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return new TestSuite(ConcurrentHashMap8Test.class);
    }

    /**
     * Returns a new map from Integers 1-5 to Strings "A"-"E".
     */
    private static ConcurrentHashMap map5() {
        ConcurrentHashMap map = new ConcurrentHashMap(5);
        assertTrue(map.isEmpty());
        map.put(one, "A");
        map.put(two, "B");
        map.put(three, "C");
        map.put(four, "D");
        map.put(five, "E");
        assertFalse(map.isEmpty());
        assertEquals(5, map.size());
        return map;
    }

    /**
     * getOrDefault returns value if present, else default
     */
    public void testGetOrDefault() {
        ConcurrentHashMap map = map5();
        assertEquals(map.getOrDefault(one, "Z"), "A");
        assertEquals(map.getOrDefault(six, "Z"), "Z");
    }

    /**
     * computeIfAbsent adds when the given key is not present
     */
    public void testComputeIfAbsent() {
        ConcurrentHashMap map = map5();
        map.computeIfAbsent(six, (x) -> "Z");
        assertTrue(map.containsKey(six));
    }

    /**
     * computeIfAbsent does not replace  if the key is already present
     */
    public void testComputeIfAbsent2() {
        ConcurrentHashMap map = map5();
        assertEquals("A", map.computeIfAbsent(one, (x) -> "Z"));
    }

    /**
     * computeIfAbsent does not add if function returns null
     */
    public void testComputeIfAbsent3() {
        ConcurrentHashMap map = map5();
        map.computeIfAbsent(six, (x) -> null);
        assertFalse(map.containsKey(six));
    }

    /**
     * computeIfPresent does not replace  if the key is already present
     */
    public void testComputeIfPresent() {
        ConcurrentHashMap map = map5();
        map.computeIfPresent(six, (x, y) -> "Z");
        assertFalse(map.containsKey(six));
    }

    /**
     * computeIfPresent adds when the given key is not present
     */
    public void testComputeIfPresent2() {
        ConcurrentHashMap map = map5();
        assertEquals("Z", map.computeIfPresent(one, (x, y) -> "Z"));
    }

    /**
     * compute does not replace  if the function returns null
     */
    public void testCompute() {
        ConcurrentHashMap map = map5();
        map.compute(six, (x, y) -> null);
        assertFalse(map.containsKey(six));
    }

    /**
     * compute adds when the given key is not present
     */
    public void testCompute2() {
        ConcurrentHashMap map = map5();
        assertEquals("Z", map.compute(six, (x, y) -> "Z"));
    }

    /**
     * compute replaces when the given key is present
     */
    public void testCompute3() {
        ConcurrentHashMap map = map5();
        assertEquals("Z", map.compute(one, (x, y) -> "Z"));
    }

    /**
     * compute removes when the given key is present and function returns null
     */
    public void testCompute4() {
        ConcurrentHashMap map = map5();
        map.compute(one, (x, y) -> null);
        assertFalse(map.containsKey(one));
    }

    /**
     * merge adds when the given key is not present
     */
    public void testMerge1() {
        ConcurrentHashMap map = map5();
        assertEquals("Y", map.merge(six, "Y", (x, y) -> "Z"));
    }

    /**
     * merge replaces when the given key is present
     */
    public void testMerge2() {
        ConcurrentHashMap map = map5();
        assertEquals("Z", map.merge(one, "Y", (x, y) -> "Z"));
    }

    /**
     * merge removes when the given key is present and function returns null
     */
    public void testMerge3() {
        ConcurrentHashMap map = map5();
        map.merge(one, "Y", (x, y) -> null);
        assertFalse(map.containsKey(one));
    }

    static Set<Integer> populatedSet(int n) {
        Set<Integer> a = ConcurrentHashMap.<Integer>newKeySet();
        assertTrue(a.isEmpty());
        for (int i = 0; i < n; i++)
            a.add(i);
        assertFalse(a.isEmpty());
        assertEquals(n, a.size());
        return a;
    }

    static Set populatedSet(Integer[] elements) {
        Set<Integer> a = ConcurrentHashMap.<Integer>newKeySet();
        assertTrue(a.isEmpty());
        for (int i = 0; i < elements.length; i++)
            a.add(elements[i]);
        assertFalse(a.isEmpty());
        assertEquals(elements.length, a.size());
        return a;
    }

    /**
     * Default-constructed set is empty
     */
    public void testNewKeySet() {
        Set a = ConcurrentHashMap.newKeySet();
        assertTrue(a.isEmpty());
    }

    /**
     * keySet.addAll adds each element from the given collection
     */
    public void testAddAll() {
        Set full = populatedSet(3);
        Vector v = new Vector();
        v.add(three);
        v.add(four);
        v.add(five);
        full.addAll(v);
        assertEquals(6, full.size());
    }

    /**
     * keySet.addAll adds each element from the given collection that did not
     * already exist in the set
     */
    public void testAddAll2() {
        Set full = populatedSet(3);
        Vector v = new Vector();
        v.add(three);
        v.add(four);
        v.add(one); // will not add this element
        full.addAll(v);
        assertEquals(5, full.size());
    }

    /**
     * keySet.add will not add the element if it already exists in the set
     */
    public void testAdd2() {
        Set full = populatedSet(3);
        full.add(one);
        assertEquals(3, full.size());
    }

    /**
     * keySet.add adds the element when it does not exist in the set
     */
    public void testAdd3() {
        Set full = populatedSet(3);
        full.add(three);
        assertTrue(full.contains(three));
    }

    /**
     * keyset.clear removes all elements from the set
     */
    public void testClear() {
        Set full = populatedSet(3);
        full.clear();
        assertEquals(0, full.size());
    }

    /**
     * keyset.contains returns true for added elements
     */
    public void testContains() {
        Set full = populatedSet(3);
        assertTrue(full.contains(one));
        assertFalse(full.contains(five));
    }

    /**
     * KeySets with equal elements are equal
     */
    public void testEquals() {
        Set a = populatedSet(3);
        Set b = populatedSet(3);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
        a.add(m1);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        b.add(m1);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }

    /**
     * KeySet.containsAll returns true for collections with subset of elements
     */
    public void testContainsAll() {
        Set full = populatedSet(3);
        Vector v = new Vector();
        v.add(one);
        v.add(two);
        assertTrue(full.containsAll(v));
        v.add(six);
        assertFalse(full.containsAll(v));
    }

    /**
     * KeySet.isEmpty is true when empty, else false
     */
    public void testIsEmpty() {
        Set empty = ConcurrentHashMap.newKeySet();
        Set full = populatedSet(3);
        assertTrue(empty.isEmpty());
        assertFalse(full.isEmpty());
    }

    /**
     * KeySet.iterator() returns an iterator containing the elements of the
     * set
     */
    public void testIterator() {
        Collection empty = ConcurrentHashMap.newKeySet();
        int size = 20;
        assertFalse(empty.iterator().hasNext());
        try {
            empty.iterator().next();
            shouldThrow();
        } catch (NoSuchElementException success) {}

        Integer[] elements = new Integer[size];
        for (int i = 0; i < size; i++)
            elements[i] = i;
        Collections.shuffle(Arrays.asList(elements));
        Collection<Integer> full = populatedSet(elements);

        Iterator it = full.iterator();
        for (int j = 0; j < size; j++) {
            assertTrue(it.hasNext());
            it.next();
        }
        assertFalse(it.hasNext());
        try {
            it.next();
            shouldThrow();
        } catch (NoSuchElementException success) {}
    }

    /**
     * KeySet.iterator.remove removes current element
     */
    public void testIteratorRemove() {
        Set q = populatedSet(3);
        Iterator it = q.iterator();
        Object removed = it.next();
        it.remove();

        it = q.iterator();
        assertFalse(it.next().equals(removed));
        assertFalse(it.next().equals(removed));
        assertFalse(it.hasNext());
    }

    /**
     * KeySet.toString holds toString of elements
     */
    public void testToString() {
        assertEquals("[]", ConcurrentHashMap.newKeySet().toString());
        Set full = populatedSet(3);
        String s = full.toString();
        for (int i = 0; i < 3; ++i)
            assertTrue(s.contains(String.valueOf(i)));
    }

    /**
     * KeySet.removeAll removes all elements from the given collection
     */
    public void testRemoveAll() {
        Set full = populatedSet(3);
        Vector v = new Vector();
        v.add(one);
        v.add(two);
        full.removeAll(v);
        assertEquals(1, full.size());
    }

    /**
     * KeySet.remove removes an element
     */
    public void testRemove() {
        Set full = populatedSet(3);
        full.remove(one);
        assertFalse(full.contains(one));
        assertEquals(2, full.size());
    }

    /**
     * keySet.size returns the number of elements
     */
    public void testSize() {
        Set empty = ConcurrentHashMap.newKeySet();
        Set full = populatedSet(3);
        assertEquals(3, full.size());
        assertEquals(0, empty.size());
    }

    /**
     * KeySet.toArray() returns an Object array containing all elements from
     * the set
     */
    public void testToArray() {
        Object[] a = ConcurrentHashMap.newKeySet().toArray();
        assertTrue(Arrays.equals(new Object[0], a));
        assertSame(Object[].class, a.getClass());
        int size = 20;
        Integer[] elements = new Integer[size];
        for (int i = 0; i < size; i++)
            elements[i] = i;
        Collections.shuffle(Arrays.asList(elements));
        Collection<Integer> full = populatedSet(elements);

        assertTrue(Arrays.asList(elements).containsAll(Arrays.asList(full.toArray())));
        assertTrue(full.containsAll(Arrays.asList(full.toArray())));
        assertSame(Object[].class, full.toArray().getClass());
    }

    /**
     * toArray(Integer array) returns an Integer array containing all
     * elements from the set
     */
    public void testToArray2() {
        Collection empty = ConcurrentHashMap.newKeySet();
        Integer[] a;
        int size = 20;

        a = new Integer[0];
        assertSame(a, empty.toArray(a));

        a = new Integer[size/2];
        Arrays.fill(a, 42);
        assertSame(a, empty.toArray(a));
        assertNull(a[0]);
        for (int i = 1; i < a.length; i++)
            assertEquals(42, (int) a[i]);

        Integer[] elements = new Integer[size];
        for (int i = 0; i < size; i++)
            elements[i] = i;
        Collections.shuffle(Arrays.asList(elements));
        Collection<Integer> full = populatedSet(elements);

        Arrays.fill(a, 42);
        assertTrue(Arrays.asList(elements).containsAll(Arrays.asList(full.toArray(a))));
        for (int i = 0; i < a.length; i++)
            assertEquals(42, (int) a[i]);
        assertSame(Integer[].class, full.toArray(a).getClass());

        a = new Integer[size];
        Arrays.fill(a, 42);
        assertSame(a, full.toArray(a));
        assertTrue(Arrays.asList(elements).containsAll(Arrays.asList(full.toArray(a))));
    }

    /**
     * A deserialized serialized set is equal
     */
    public void testSerialization() throws Exception {
        int size = 20;
        Set x = populatedSet(size);
        Set y = serialClone(x);

        assertTrue(x != y);
        assertEquals(x.size(), y.size());
        assertEquals(x.toString(), y.toString());
        assertTrue(Arrays.equals(x.toArray(), y.toArray()));
        assertEquals(x, y);
        assertEquals(y, x);
    }


    static final int SIZE = 10000;
    static ConcurrentHashMap<Long, Long> longMap;

    static ConcurrentHashMap<Long, Long> longMap() {
        if (longMap == null) {
            longMap = new ConcurrentHashMap<Long, Long>(SIZE);
            for (int i = 0; i < SIZE; ++i)
                longMap.put(Long.valueOf(i), Long.valueOf(2 *i));
        }
        return longMap;
    }

    // explicit function class to avoid type inference problems
    static class AddKeys implements BiFunction<Map.Entry<Long,Long>, Map.Entry<Long,Long>, Map.Entry<Long,Long>> {
        public Map.Entry<Long,Long> apply(Map.Entry<Long,Long> x, Map.Entry<Long,Long> y) {
            return new AbstractMap.SimpleEntry<Long,Long>
             (Long.valueOf(x.getKey().longValue() + y.getKey().longValue()),
              Long.valueOf(1L));
        }
    }

    /**
     * forEachKeySequentially traverses all keys
     */
    public void testForEachKeySequentially() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachKeySequentially((Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), SIZE * (SIZE - 1) / 2);
    }

    /**
     * forEachValueSequentially traverses all values
     */
    public void testForEachValueSequentially() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachValueSequentially((Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), SIZE * (SIZE - 1));
    }

    /**
     * forEachSequentially traverses all mappings
     */
    public void testForEachSequentially() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachSequentially((Long x, Long y) -> adder.add(x.longValue() + y.longValue()));
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * forEachEntrySequentially traverses all entries
     */
    public void testForEachEntrySequentially() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachEntrySequentially((Map.Entry<Long,Long> e) -> adder.add(e.getKey().longValue() + e.getValue().longValue()));
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * forEachKeyInParallel traverses all keys
     */
    public void testForEachKeyInParallel() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachKeyInParallel((Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), SIZE * (SIZE - 1) / 2);
    }

    /**
     * forEachValueInParallel traverses all values
     */
    public void testForEachValueInParallel() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachValueInParallel((Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), SIZE * (SIZE - 1));
    }

    /**
     * forEachInParallel traverses all mappings
     */
    public void testForEachInParallel() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachInParallel((Long x, Long y) -> adder.add(x.longValue() + y.longValue()));
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * forEachEntryInParallel traverses all entries
     */
    public void testForEachEntryInParallel() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachEntryInParallel((Map.Entry<Long,Long> e) -> adder.add(e.getKey().longValue() + e.getValue().longValue()));
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * Mapped forEachKeySequentially traverses the given
     * transformations of all keys
     */
    public void testMappedForEachKeySequentially() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachKeySequentially((Long x) -> Long.valueOf(4 * x.longValue()),
                                 (Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), 4 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * Mapped forEachValueSequentially traverses the given
     * transformations of all values
     */
    public void testMappedForEachValueSequentially() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachValueSequentially((Long x) -> Long.valueOf(4 * x.longValue()),
                                   (Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), 4 * SIZE * (SIZE - 1));
    }

    /**
     * Mapped forEachSequentially traverses the given
     * transformations of all mappings
     */
    public void testMappedForEachSequentially() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachSequentially((Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()),
                              (Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * Mapped forEachEntrySequentially traverses the given
     * transformations of all entries
     */
    public void testMappedForEachEntrySequentially() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachEntrySequentially((Map.Entry<Long,Long> e) -> Long.valueOf(e.getKey().longValue() + e.getValue().longValue()),
                                   (Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * Mapped forEachKeyInParallel traverses the given
     * transformations of all keys
     */
    public void testMappedForEachKeyInParallel() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachKeyInParallel((Long x) -> Long.valueOf(4 * x.longValue()),
                               (Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), 4 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * Mapped forEachValueInParallel traverses the given
     * transformations of all values
     */
    public void testMappedForEachValueInParallel() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachValueInParallel((Long x) -> Long.valueOf(4 * x.longValue()),
                                 (Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), 4 * SIZE * (SIZE - 1));
    }

    /**
     * Mapped forEachInParallel traverses the given
     * transformations of all mappings
     */
    public void testMappedForEachInParallel() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachInParallel((Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()),
                            (Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
    }

    /**
     * Mapped forEachEntryInParallel traverses the given
     * transformations of all entries
     */
    public void testMappedForEachEntryInParallel() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        m.forEachEntryInParallel((Map.Entry<Long,Long> e) -> Long.valueOf(e.getKey().longValue() + e.getValue().longValue()),
                                 (Long x) -> adder.add(x.longValue()));
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
    }


    /**
     * reduceKeysSequentially accumulates across all keys,
     */
    public void testReduceKeysSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.reduceKeysSequentially((Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)SIZE * (SIZE - 1) / 2);
    }

    /**
     * reduceValuesSequentially accumulates across all values
     */
    public void testReduceValuesSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.reduceKeysSequentially((Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)SIZE * (SIZE - 1) / 2);
    }


    /**
     * reduceEntriesSequentially accumulates across all entries
     */
    public void testReduceEntriesSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Map.Entry<Long,Long> r;
        r = m.reduceEntriesSequentially(new AddKeys());
        assertEquals(r.getKey().longValue(), (long)SIZE * (SIZE - 1) / 2);
    }

    /**
     * reduceKeysInParallel accumulates across all keys
     */
    public void testReduceKeysInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.reduceKeysInParallel((Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)SIZE * (SIZE - 1) / 2);
    }

    /**
     * reduceValuesInParallel accumulates across all values
     */
    public void testReduceValuesInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.reduceValuesInParallel((Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)SIZE * (SIZE - 1));
    }

    /**
     * reduceEntriesInParallel accumulate across all entries
     */
    public void testReduceEntriesInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Map.Entry<Long,Long> r;
        r = m.reduceEntriesInParallel(new AddKeys());
        assertEquals(r.getKey().longValue(), (long)SIZE * (SIZE - 1) / 2);
    }

    /*
     * Mapped reduceKeysSequentially accumulates mapped keys
     */
    public void testMapReduceKeysSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r = m.reduceKeysSequentially((Long x) -> Long.valueOf(4 * x.longValue()),
                                     (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)4 * SIZE * (SIZE - 1) / 2);
    }

    /*
     * Mapped reduceValuesSequentially accumulates mapped values
     */
    public void testMapReduceValuesSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r = m.reduceValuesSequentially((Long x) -> Long.valueOf(4 * x.longValue()),
                                       (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)4 * SIZE * (SIZE - 1));
    }

    /**
     * reduceSequentially accumulates across all transformed mappings
     */
    public void testMappedReduceSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r = m.reduceSequentially((Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()),
                                 (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));

        assertEquals((long)r, (long)3 * SIZE * (SIZE - 1) / 2);
    }

    /*
     * Mapped reduceKeysInParallel, accumulates mapped keys
     */
    public void testMapReduceKeysInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r = m.reduceKeysInParallel((Long x) -> Long.valueOf(4 * x.longValue()),
                                   (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)4 * SIZE * (SIZE - 1) / 2);
    }

    /*
     * Mapped reduceValuesInParallel accumulates mapped values
     */
    public void testMapReduceValuesInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r = m.reduceValuesInParallel((Long x) -> Long.valueOf(4 * x.longValue()),
                                     (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)4 * SIZE * (SIZE - 1));
    }

    /**
     * reduceInParallel accumulate across all transformed mappings
     */
    public void testMappedReduceInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.reduceInParallel((Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()),
                               (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()));
        assertEquals((long)r, (long)3 * SIZE * (SIZE - 1) / 2);
    }


    /*
     * reduceKeysToLongSequentially accumulates mapped keys
     */
    public void testReduceKeysToLongSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        long lr = m.reduceKeysToLongSequentially((Long x) -> x.longValue(), 0L, Long::sum);
        assertEquals(lr, (long)SIZE * (SIZE - 1) / 2);
    }

    /*
     * reduceKeysToIntSequentially accumulates mapped keys
     */
    public void testReduceKeysToIntSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        int ir = m.reduceKeysToIntSequentially((Long x) -> x.intValue(), 0, Integer::sum);
        assertEquals(ir, (int)SIZE * (SIZE - 1) / 2);
    }

    /*
     * reduceKeysToDoubleSequentially accumulates mapped keys
     */
    public void testReduceKeysToDoubleSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        double dr = m.reduceKeysToDoubleSequentially((Long x) -> x.doubleValue(), 0.0, Double::sum);
        assertEquals(dr, (double)SIZE * (SIZE - 1) / 2);
    }

    /*
     * reduceValuesToLongSequentially accumulates mapped values
     */
    public void testReduceValuesToLongSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        long lr = m.reduceValuesToLongSequentially((Long x) -> x.longValue(), 0L, Long::sum);
        assertEquals(lr, (long)SIZE * (SIZE - 1));
    }

    /*
     * reduceValuesToIntSequentially accumulates mapped values
     */
    public void testReduceValuesToIntSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        int ir = m.reduceValuesToIntSequentially((Long x) -> x.intValue(), 0, Integer::sum);
        assertEquals(ir, (int)SIZE * (SIZE - 1));
    }

    /*
     * reduceValuesToDoubleSequentially accumulates mapped values
     */
    public void testReduceValuesToDoubleSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        double dr = m.reduceValuesToDoubleSequentially((Long x) -> x.doubleValue(), 0.0, Double::sum);
        assertEquals(dr, (double)SIZE * (SIZE - 1));
    }

    /*
     * reduceKeysToLongInParallel accumulates mapped keys
     */
    public void testReduceKeysToLongInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        long lr = m.reduceKeysToLongInParallel((Long x) -> x.longValue(), 0L, Long::sum);
        assertEquals(lr, (long)SIZE * (SIZE - 1) / 2);
    }

    /*
     * reduceKeysToIntInParallel accumulates mapped keys
     */
    public void testReduceKeysToIntInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        int ir = m.reduceKeysToIntInParallel((Long x) -> x.intValue(), 0, Integer::sum);
        assertEquals(ir, (int)SIZE * (SIZE - 1) / 2);
    }

    /*
     * reduceKeysToDoubleInParallel accumulates mapped values
     */
    public void testReduceKeysToDoubleInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        double dr = m.reduceKeysToDoubleInParallel((Long x) -> x.doubleValue(), 0.0, Double::sum);
        assertEquals(dr, (double)SIZE * (SIZE - 1) / 2);
    }

    /*
     * reduceValuesToLongInParallel accumulates mapped values
     */
    public void testReduceValuesToLongInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        long lr = m.reduceValuesToLongInParallel((Long x) -> x.longValue(), 0L, Long::sum);
        assertEquals(lr, (long)SIZE * (SIZE - 1));
    }

    /*
     * reduceValuesToIntInParallel accumulates mapped values
     */
    public void testReduceValuesToIntInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        int ir = m.reduceValuesToIntInParallel((Long x) -> x.intValue(), 0, Integer::sum);
        assertEquals(ir, (int)SIZE * (SIZE - 1));
    }

    /*
     * reduceValuesToDoubleInParallel accumulates mapped values
     */
    public void testReduceValuesToDoubleInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        double dr = m.reduceValuesToDoubleInParallel((Long x) -> x.doubleValue(), 0.0, Double::sum);
        assertEquals(dr, (double)SIZE * (SIZE - 1));
    }

    /**
     * searchKeysSequentially returns a non-null result of search
     * function, or null if none
     */
    public void testSearchKeysSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.searchKeysSequentially((Long x) -> x.longValue() == (long)(SIZE/2) ? x : null);
        assertEquals((long)r, (long)(SIZE/2));
        r = m.searchKeysSequentially((Long x) -> x.longValue() < 0L ? x : null);
        assertNull(r);
    }

    /**
     * searchValuesSequentially returns a non-null result of search
     * function, or null if none
     */
    public void testSearchValuesSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.searchValuesSequentially((Long x) -> x.longValue() == (long)(SIZE/2)? x : null);
        assertEquals((long)r, (long)(SIZE/2));
        r = m.searchValuesSequentially((Long x) -> x.longValue() < 0L ? x : null);
        assertNull(r);
    }

    /**
     * searchSequentially returns a non-null result of search
     * function, or null if none
     */
    public void testSearchSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.searchSequentially((Long x, Long y) -> x.longValue() == (long)(SIZE/2) ? x : null);
        assertEquals((long)r, (long)(SIZE/2));
        r = m.searchSequentially((Long x, Long y) -> x.longValue() < 0L ? x : null);
        assertNull(r);
    }

    /**
     * searchEntriesSequentially returns a non-null result of search
     * function, or null if none
     */
    public void testSearchEntriesSequentially() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.searchEntriesSequentially((Map.Entry<Long,Long> e) -> e.getKey().longValue() == (long)(SIZE/2) ? e.getKey() : null);
        assertEquals((long)r, (long)(SIZE/2));
        r = m.searchEntriesSequentially((Map.Entry<Long,Long> e) -> e.getKey().longValue() < 0L ? e.getKey() : null);
        assertNull(r);
    }

    /**
     * searchKeysInParallel returns a non-null result of search
     * function, or null if none
     */
    public void testSearchKeysInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.searchKeysInParallel((Long x) -> x.longValue() == (long)(SIZE/2) ? x : null);
        assertEquals((long)r, (long)(SIZE/2));
        r = m.searchKeysInParallel((Long x) -> x.longValue() < 0L ? x : null);
        assertNull(r);
    }

    /**
     * searchValuesInParallel returns a non-null result of search
     * function, or null if none
     */
    public void testSearchValuesInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.searchValuesInParallel((Long x) -> x.longValue() == (long)(SIZE/2) ? x : null);
        assertEquals((long)r, (long)(SIZE/2));
        r = m.searchValuesInParallel((Long x) -> x.longValue() < 0L ? x : null);
        assertNull(r);
    }

    /**
     * searchInParallel returns a non-null result of search function,
     * or null if none
     */
    public void testSearchInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.searchInParallel((Long x, Long y) -> x.longValue() == (long)(SIZE/2) ? x : null);
        assertEquals((long)r, (long)(SIZE/2));
        r = m.searchInParallel((Long x, Long y) -> x.longValue() < 0L ? x : null);
        assertNull(r);
    }

    /**
     * searchEntriesInParallel returns a non-null result of search
     * function, or null if none
     */
    public void testSearchEntriesInParallel() {
        ConcurrentHashMap<Long, Long> m = longMap();
        Long r;
        r = m.searchEntriesInParallel((Map.Entry<Long,Long> e) -> e.getKey().longValue() == (long)(SIZE/2) ? e.getKey() : null);
        assertEquals((long)r, (long)(SIZE/2));
        r = m.searchEntriesInParallel((Map.Entry<Long,Long> e) -> e.getKey().longValue() < 0L ? e.getKey() : null);
        assertNull(r);
    }

    /**
     * Invoking task versions of bulk methods has same effect as
     * parallel methods
     */
    public void testForkJoinTasks() {
        LongAdder adder = new LongAdder();
        ConcurrentHashMap<Long, Long> m = longMap();
        ConcurrentHashMap.ForkJoinTasks.forEachKey
            (m, (Long x) -> adder.add(x.longValue())).invoke();
        assertEquals(adder.sum(), SIZE * (SIZE - 1) / 2);
        adder.reset();
        ConcurrentHashMap.ForkJoinTasks.forEachValue
            (m, (Long x) -> adder.add(x.longValue())).invoke();
        assertEquals(adder.sum(), SIZE * (SIZE - 1));
        adder.reset();
        ConcurrentHashMap.ForkJoinTasks.forEach
            (m, (Long x, Long y) -> adder.add(x.longValue() + y.longValue())).invoke();
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
        adder.reset();
        ConcurrentHashMap.ForkJoinTasks.forEachEntry
            (m,
             (Map.Entry<Long,Long> e) -> adder.add(e.getKey().longValue() + e.getValue().longValue())).invoke();
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
        adder.reset();
        ConcurrentHashMap.ForkJoinTasks.forEachKey
            (m, (Long x) -> Long.valueOf(4 * x.longValue()),
             (Long x) -> adder.add(x.longValue())).invoke();
        assertEquals(adder.sum(), 4 * SIZE * (SIZE - 1) / 2);
        adder.reset();
        ConcurrentHashMap.ForkJoinTasks.forEachValue
            (m, (Long x) -> Long.valueOf(4 * x.longValue()),
             (Long x) -> adder.add(x.longValue())).invoke();
        assertEquals(adder.sum(), 4 * SIZE * (SIZE - 1));
        adder.reset();
        ConcurrentHashMap.ForkJoinTasks.forEach
            (m, (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()),
             (Long x) -> adder.add(x.longValue())).invoke();
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
        adder.reset();
        ConcurrentHashMap.ForkJoinTasks.forEachEntry
            (m, (Map.Entry<Long,Long> e) -> Long.valueOf(e.getKey().longValue() + e.getValue().longValue()),
             (Long x) -> adder.add(x.longValue())).invoke();
        assertEquals(adder.sum(), 3 * SIZE * (SIZE - 1) / 2);
        adder.reset();

        Long r; long lr; int ir; double dr;
        r = ConcurrentHashMap.ForkJoinTasks.reduceKeys
            (m, (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue())).invoke();
        assertEquals((long)r, (long)SIZE * (SIZE - 1) / 2);
        r = ConcurrentHashMap.ForkJoinTasks.reduceValues
            (m, (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue())).invoke();
        assertEquals((long)r, (long)SIZE * (SIZE - 1));
        r = ConcurrentHashMap.ForkJoinTasks.reduce
            (m, (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue()),
             (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue())).invoke();
        assertEquals((long)r, (long)3 * SIZE * (SIZE - 1) / 2);
        r = ConcurrentHashMap.ForkJoinTasks.reduceEntries
            (m, (Map.Entry<Long,Long> e) -> Long.valueOf(e.getKey().longValue() + e.getValue().longValue()),
             (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue())).invoke();
        assertEquals((long)r, (long)3 * SIZE * (SIZE - 1) / 2);
        r = ConcurrentHashMap.ForkJoinTasks.reduceKeys
            (m, (Long x) -> Long.valueOf(4 * x.longValue()),
             (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue())).invoke();
        assertEquals((long)r, (long)4 * SIZE * (SIZE - 1) / 2);
        lr = ConcurrentHashMap.ForkJoinTasks.reduceKeysToLong
            (m, (Long x) -> x.longValue(), 0L, Long::sum).invoke();
        assertEquals(lr, (long)SIZE * (SIZE - 1) / 2);
        ir = ConcurrentHashMap.ForkJoinTasks.reduceKeysToInt
            (m, (Long x) -> x.intValue(), 0, Integer::sum).invoke();
        assertEquals(ir, (int)SIZE * (SIZE - 1) / 2);
        dr = ConcurrentHashMap.ForkJoinTasks.reduceKeysToDouble
            (m, (Long x) -> x.doubleValue(), 0.0, Double::sum).invoke();
        assertEquals(dr, (double)SIZE * (SIZE - 1) / 2);
        r = ConcurrentHashMap.ForkJoinTasks.reduceValues
            (m, (Long x) -> Long.valueOf(4 * x.longValue()),
             (Long x, Long y) -> Long.valueOf(x.longValue() + y.longValue())).invoke();
        assertEquals((long)r, (long)4 * SIZE * (SIZE - 1));
        lr = ConcurrentHashMap.ForkJoinTasks.reduceValuesToLong
            (m, (Long x) -> x.longValue(), 0L, Long::sum).invoke();
        assertEquals(lr, (long)SIZE * (SIZE - 1));
        ir = ConcurrentHashMap.ForkJoinTasks.reduceValuesToInt
            (m, (Long x) -> x.intValue(), 0, Integer::sum).invoke();
        assertEquals(ir, (int)SIZE * (SIZE - 1));
        dr = ConcurrentHashMap.ForkJoinTasks.reduceValuesToDouble
            (m, (Long x) -> x.doubleValue(), 0.0, Double::sum).invoke();
        assertEquals(dr, (double)SIZE * (SIZE - 1));
        r = ConcurrentHashMap.ForkJoinTasks.searchKeys
            (m, (Long x) -> x.longValue() == (long)(SIZE/2)? x : null).invoke();
        assertEquals((long)r, (long)(SIZE/2));
        r = ConcurrentHashMap.ForkJoinTasks.searchValues
            (m, (Long x) -> x.longValue() == (long)(SIZE/2)? x : null).invoke();
        assertEquals((long)r, (long)(SIZE/2));
        r = ConcurrentHashMap.ForkJoinTasks.search
            (m, (Long x, Long y) -> x.longValue() == (long)(SIZE/2)? x : null).invoke();
        assertEquals((long)r, (long)(SIZE/2));
        r = ConcurrentHashMap.ForkJoinTasks.searchEntries
            (m, (Map.Entry<Long,Long> e) -> e.getKey().longValue() == (long)(SIZE/2)? e.getKey() : null).invoke();
        assertEquals((long)r, (long)(SIZE/2));
    }
}
