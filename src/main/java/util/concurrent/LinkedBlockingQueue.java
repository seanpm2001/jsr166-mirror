/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain. Use, modify, and
 * redistribute this code in any way without acknowledgement.
 */

package java.util.concurrent;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.*;

/**
 * An optionally-bounded blocking queue based on linked nodes.  Linked
 * queues typically have higher throughput than array-based queues but
 * less predicatble performance in most concurrent applications.
 * 
 * <p> The optional capacity bound constructor argument serves as a
 * way to prevent unlmited queue expansion.  Linked nodes are
 * dynamically created upon each insertion unless this would bring the
 * queue above capacity.
 * @since 1.5
 * @author Doug Lea
 * 
 **/
public class LinkedBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {

    /*
     * A variant of the "two lock queue" algorithm.  The putLock gates
     * entry to put (and offer), and has an associated condition for
     * waiting puts.  Similarly for the takeLock.  The "count" field
     * that they both rely on is maintained as an atomic to avoid
     * needing to get both locks in most cases. Also, to minimize need
     * for puts to get takeLock and vice-versa, cascading notifies are
     * used. When a put notices that it has enabled at least one take,
     * it signals taker. That taker in turn signals others if more
     * items have been entered since the signal. And symmetrically for
     * takes signalling puts. Operations such as remove(Object) and 
     * iterators acquire both locks.
    */

    /**
     * Linked list node class
     */
    static class Node<E> {
        /** The item, volatile to ensure barrier separating write and read */
        volatile E item;
        Node<E> next;
        Node(E x) { item = x; }
    }

    /** The capacity bound, or Integer.MAX_VALUE if none */
    private final int capacity;

    /** Current number of elements */
    private transient final AtomicInteger count = new AtomicInteger(0);

    /** Head of linked list */
    private transient Node<E> head;

    /** Tail of lined list */
    private transient Node<E> last;

    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /** Lock held by put, offer, etc */
    private final ReentrantLock putLock = new ReentrantLock();

    /** Wait queue for waiting puts */
    private final Condition notFull = putLock.newCondition();

    /**
     * Signal a waiting take. Called only from put/offer (which do not
     * otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signal();
        }
        finally {
            takeLock.unlock();
        }
    }

    /**
     * Signal a waiting put. Called only from take/poll.
     */
    private void signalNotFull() {
        putLock.lock();
        try {
            notFull.signal();
        }
        finally {
            putLock.unlock();
        }
    }

    /**
     * Create a node and link it and end of queue
     * @param x the item
     */
    private void insert(E x) {
        last = last.next = new Node<E>(x);
    }

    /**
     * Remove a node from head of queue,
     * @return the node
     */
    private E extract() {
        Node<E> first = head.next;
        head = first;
        E x = (E)first.item;
        first.item = null;
        return x;
    }

    /**
     * Lock to prevent both puts and takes. 
     */
    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * Unlock to allow both puts and takes. 
     */
    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }


    /**
     * Creates a LinkedBlockingQueue with no intrinsic capacity constraint.
     */
    public LinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a LinkedBlockingQueue with the given capacity constraint.
     * @param capacity the maminum number of elements to hold without blocking.
     */
    public LinkedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new NullPointerException();
        this.capacity = capacity;
        last = head = new Node<E>(null);
    }

    /**
     * Creates a LinkedBlockingQueue without an intrinsic capacity
     * constraint, initially holding the given elements, added in
     * traveral order of the collection's iterator.
     * @param initialElements the elements to initially contain
     */
    public LinkedBlockingQueue(Collection<E> initialElements) {
        this(Integer.MAX_VALUE);
        for (Iterator<E> it = initialElements.iterator(); it.hasNext();) 
            add(it.next());
    }

    public int size() {
        return count.get();
    }

    public int remainingCapacity() {
        return capacity - count.get();
    }

    public void put(E x) throws InterruptedException {
        if (x == null) throw new NullPointerException();
        // Note: convention in all put/take/etc is to preset
        // local var holding count  negative to indicate failure unless set.
        int c = -1; 
        putLock.lockInterruptibly();
        try {
            /*
             * Note that count is used in wait guard even though it is
             * not protected by lock. This works because count can
             * only decrease at this point (all other puts are shut
             * out by lock), and we (or some other waiting put) are
             * signalled if it ever changes from
             * capacity. Similarly for all other uses of count in
             * other wait guards.
             */
            try {
                while (count.get() == capacity) 
                    notFull.await();
            }
            catch (InterruptedException ie) {
                notFull.signal(); // propagate to a non-interrupted thread
                throw ie;
            }
            insert(x);
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();
        }
        finally {
            putLock.unlock();
        }
        if (c == 0) 
            signalNotEmpty();
    }

    public boolean offer(E x, long timeout, TimeUnit unit) throws InterruptedException {
        if (x == null) throw new NullPointerException();
        putLock.lockInterruptibly();
        long nanos = unit.toNanos(timeout);
        int c = -1;
        try {
            for (;;) {
                if (count.get() < capacity) {
                    insert(x);
                    c = count.getAndIncrement();
                    if (c + 1 < capacity)
                        notFull.signal();
                    break;
                }
                if (nanos <= 0)
                    return false;
                try {
                    nanos = notFull.awaitNanos(nanos);
                }
                catch (InterruptedException ie) {
                    notFull.signal(); // propagate to a non-interrupted thread
                    throw ie;
                }
            }
        }
        finally {
            putLock.unlock();
        }
        if (c == 0) 
            signalNotEmpty();
        return true;
    }

    public boolean offer(E x) {
        if (x == null) throw new NullPointerException();
        if (count.get() == capacity)
            return false;
        putLock.tryLock();
        int c = -1; 
        try {
            if (count.get() < capacity) {
                insert(x);
                c = count.getAndIncrement();
                if (c + 1 < capacity)
                    notFull.signal();
            }
        }
        finally {
            putLock.unlock();
        }
        if (c == 0) 
            signalNotEmpty();
        return c >= 0;
    }


    public E take() throws InterruptedException {
        E x;
        int c = -1;
        takeLock.lockInterruptibly();
        try {
            try {
                while (count.get() == 0) 
                    notEmpty.await();
            }
            catch (InterruptedException ie) {
                notEmpty.signal(); // propagate to a non-interrupted thread
                throw ie;
            }

            x = extract();
            c = count.getAndDecrement();
            if (c > 1)
                notEmpty.signal();
        }
        finally {
            takeLock.unlock();
        }
        if (c == capacity) 
            signalNotFull();
        return x;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        takeLock.lockInterruptibly();
        long nanos = unit.toNanos(timeout);
        try {
            for (;;) {
                if (count.get() > 0) {
                    x = extract();
                    c = count.getAndDecrement();
                    if (c > 1)
                        notEmpty.signal();
                    break;
                }
                if (nanos <= 0)
                    return null;
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                }
                catch (InterruptedException ie) {
                    notEmpty.signal(); // propagate to a non-interrupted thread
                    throw ie;
                }
            }
        }
        finally {
            takeLock.unlock();
        }
        if (c == capacity) 
            signalNotFull();
        return x;
    }

    public E poll() {
        if (count.get() == 0)
            return null;
        E x = null;
        int c = -1; 
        takeLock.tryLock();
        try {
            if (count.get() > 0) {
                x = extract();
                c = count.getAndDecrement();
                if (c > 1)
                    notEmpty.signal();
            }
        }
        finally {
            takeLock.unlock();
        }
        if (c == capacity) 
            signalNotFull();
        return x;
    }


    public E peek() {
        if (count.get() == 0)
            return null;
        takeLock.tryLock();
        try {
            Node<E> first = head.next;
            if (first == null)
                return null;
            else
                return first.item;
        }
        finally {
            takeLock.unlock();
        }
    }

    public boolean remove(Object x) {
        if (x == null) return false;
        boolean removed = false;
        fullyLock();
        try {
            Node<E> trail = head;
            Node<E> p = head.next;
            while (p != null) {
                if (x.equals(p.item)) {
                    removed = true;
                    break;
                }
                trail = p;
                p = p.next;
            }
            if (removed) {
                p.item = null;
                trail.next = p.next;
                if (count.getAndDecrement() == capacity)
                    notFull.signalAll();
            }
        }
        finally {
            fullyUnlock();
        }
        return removed;
    }

    public Object[] toArray() {
        fullyLock();
        try {
            int size = count.get();
            Object[] a = new Object[size];                
            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next) 
                a[k++] = p.item;
            return a;
        }
        finally {
            fullyUnlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = count.get();
            if (a.length < size)
                a = (T[])java.lang.reflect.Array.newInstance
                    (a.getClass().getComponentType(), size);
            
            int k = 0;
            for (Node p = head.next; p != null; p = p.next) 
                a[k++] = (T)p.item;
            return a;
        }
        finally {
            fullyUnlock();
        }
    }

    public String toString() {
        fullyLock();
        try {
            return super.toString();
        }
        finally {
            fullyUnlock();
        }
    }

    public Iterator<E> iterator() {
      return new Itr();
    }

    private class Itr implements Iterator<E> {
        /* 
         * Basic weak-consistent iterator.  At all times hold the next
         * item to hand out so that if hasNext() reports true, we will
         * still have it to return even if lost race with a take etc.
         */
        Node<E> current;
        Node<E> lastRet;
        E currentElement;
        
        Itr() {
            fullyLock();
            try {
                current = head.next;
                if (current != null)
                    currentElement = current.item;
            }
            finally {
                fullyUnlock();
            }
        }
        
	public boolean hasNext() {
            return current != null;
        }

	public E next() {
            fullyLock();
            try {
                if (current == null)
                    throw new NoSuchElementException();
                E x = currentElement;
                lastRet = current;
                current = current.next;
                if (current != null)
                    currentElement = current.item;
                return x;
            }
            finally {
                fullyUnlock();
            }
            
        }

	public void remove() {
            if (lastRet == null)
		throw new IllegalStateException();
            fullyLock();
            try {
                Node<E> node = lastRet;
                lastRet = null;
                Node<E> trail = head;
                Node<E> p = head.next;
                while (p != null && p != node) {
                    trail = p;
                    p = p.next;
                }
                if (p == node) {
                    p.item = null;
                    trail.next = p.next;
                    int c = count.getAndDecrement();
                    if (c == capacity)
                        notFull.signalAll();
                }
            }
            finally {
                fullyUnlock();
            }
        }
    }

    /**
     * Save the state to a stream (that is, serialize it).
     *
     * @serialData The capacity is emitted (int), followed by all of
     * its elements (each an <tt>Object</tt>) in the proper order,
     * followed by a null
     * @param s the stream
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        fullyLock(); 
        try {
            // Write out any hidden stuff, plus capacity
            s.defaultWriteObject();

            // Write out all elements in the proper order.
            for (Node<E> p = head.next; p != null; p = p.next) 
                s.writeObject(p.item);

            // Use trailing null as sentinel
            s.writeObject(null);
        }
        finally {
            fullyUnlock();
        }
    }

    /**
     * Reconstitute the Queue instance from a stream (that is,
     * deserialize it).
     * @param s the stream
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
	// Read in capacity, and any hidden stuff
	s.defaultReadObject();

        // Read in all elements and place in queue
        for (;;) {
            E item = (E)s.readObject();
            if (item == null)
                break;
            add(item);
        }
    }
}

