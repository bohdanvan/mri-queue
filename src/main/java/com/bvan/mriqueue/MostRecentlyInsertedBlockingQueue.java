package com.bvan.mriqueue;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe blocking implementation of {@code MostRecentlyInsertedQueue}.
 *
 * @author bvanchuhov
 */
public class MostRecentlyInsertedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

    private final int capacity;

    private Node<E> beforeFirst;
    private Node<E> last;

    private final AtomicInteger count = new AtomicInteger(0);

    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmptyCondition = takeLock.newCondition();

    private final ReentrantLock putLock = new ReentrantLock();

    /**
     * @throws IllegalArgumentException if {@code capacity} is not positive
     */
    public MostRecentlyInsertedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity should be greater than 0: " + capacity);
        }
        this.capacity = capacity;
        beforeFirst = last = Node.emptyNode();
    }

    @Override
    public int size() {
        return count.get();
    }

    @Override
    public void put(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }

        putLock.lockInterruptibly();
        try {
            if (size() == capacity) {
                enqueue(e);
                interruptiblyLockedDequeue();
            } else {
                enqueue(e);
                notEmptySignal();
            }
        } finally {
            putLock.unlock();
        }
    }

    private void interruptiblyLockedDequeue() throws InterruptedException {
        takeLock.lockInterruptibly();
        try {
            dequeue();
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        put(e);
        return true;
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }

        putLock.lock();
        try {
            if (size() == capacity) {
                enqueue(e);
                lockedDeque();
            } else {
                enqueue(e);
                notEmptySignal();
            }
        } finally {
            putLock.unlock();
        }

        return true;
    }

    private void lockedDeque() {
        takeLock.lock();
        try {
            dequeue();
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        E res;

        takeLock.lockInterruptibly();
        try {
            while (isEmpty()) {
                notEmptyCondition.await();
            }
            res = dequeue();
        } finally {
            takeLock.unlock();
        }

        return res;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E res;

        long nanos = unit.toNanos(timeout);
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmptyCondition.awaitNanos(nanos);
            }
            res = dequeue();
        } finally {
            takeLock.unlock();
        }

        return res;
    }

    @Override
    public E poll() {
        if (isEmpty()) {
            return null;
        }

        takeLock.lock();
        try {
            return (!isEmpty()) ? dequeue() : null;
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public E peek() {
        if (isEmpty()) {
            return null;
        }

        takeLock.lock();
        try {
            return (!isEmpty()) ? firstNode().item : null;
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (obj == null) {
            return false;
        }

        fullyLock();
        try {
            for (Node<E> before = beforeFirstNode(), node = firstNode();
                 node != null;
                 before = node, node = node.next) {

                if (Objects.equals(obj, node.item)) {
                    unlink(node, before);
                    return true;
                }
            }
            return false;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public boolean contains(Object obj) {
        if (isEmpty()) {
            return false;
        }

        fullyLock();
        try {
            if (isEmpty()) {
                return false;
            }

            for (Node<E> node = firstNode(); node != null; node = node.next) {
                if (Objects.equals(obj, node.item)) {
                    return true;
                }
            }
            return false;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public Object[] toArray() {
        fullyLock();
        try {
            Object[] res = new Object[size()];
            int i = 0;
            for (Node<E> node = beforeFirst; node != null; node = node.next) {
                res[i++] = node.item;
            }
            return res;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = size();
            if (a.length < size) {
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            int i = 0;
            for (Node<E> node = beforeFirst; node != null; node = node.next) {
                a[i++] = (T) node.item;
            }
            if (a.length > i) {
                a[i] = null;
            }
            return a;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public void clear() {
        fullyLock();
        try {
            super.clear();
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public String toString() {
        fullyLock();
        try {
            Node<E> node = firstNode();
            if (node == null) {
                return "[]";
            }

            StringJoiner joiner = new StringJoiner(", ", "[", "]");
            for (; node != null; node = node.next) {
                E item = node.item;
                String s = (item == this) ? "(this)" : item.toString();
                joiner.add(s);
            }
            return joiner.toString();
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        if (maxElements <= 0) {
            return 0;
        }

        takeLock.lock();
        try {
            int n = Math.min(maxElements, size());

            Node<E> prevNode = beforeFirstNode();
            int i = 0;
            try {
                while (i < n) {
                    Node<E> node = prevNode.next;
                    c.add(node.item);
                    node.item = null;
                    prevNode.next = prevNode;
                    prevNode = node;
                    i++;
                }
                return n;
            } finally {
                if (i > 0) {
                    beforeFirst = prevNode;
                }
            }
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new QueueIterator();
    }


    /**
     * Guarded by {@code putLock}.
     */
    private void enqueue(E e) {
        Node<E> node = new Node<>(e);
        last.next = node;
        last = node;

        count.incrementAndGet();
    }

    /**
     * Guarded by {@code takeLock}.
     */
    private E dequeue() {
        return unlink(firstNode(), beforeFirstNode());
    }

    /**
     * Guarded by {@code takeLock}.
     * @param node {@code not null}.
     * @param node {@code not null}.
     */
    private E unlink(Node<E> node, Node<E> prev) {
        prev.next = node.next;

        E res = node.item;
        node.item = null;
        node.next = null;

        if (last == node) {
            last = prev;
        }

        count.decrementAndGet();

        return res;
    }

    private Node<E> firstNode() {
        return beforeFirst.next;
    }

    private Node<E> beforeFirstNode() {
        return beforeFirst;
    }

    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

    private void notEmptySignal() {
        takeLock.lock();
        try {
            notEmptyCondition.signal();
        } finally {
            takeLock.unlock();
        }
    }


    private static class Node<E> {
        E item;
        Node<E> next;

        public static <E> Node<E> emptyNode() {
            return new Node<>(null);
        }

        Node(E item) {
            this.item = item;
        }
    }

    /**
     * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html#Weakly">
     *     <i>Weakly consistent</i>
     * </a> iterator.
     */
    private class QueueIterator implements Iterator<E> {

        private Node<E> lastRet;
        private Node<E> current;
        private E currentElem;

        public QueueIterator() {
            fullyLock();
            try {
                current = firstNode();
                if (current != null) {
                    currentElem = current.item;
                }
            } finally {
                fullyUnlock();
            }
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            fullyLock();
            try {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                E res = currentElem;
                lastRet = current;
                current = nextNode(current);
                currentElem = (current != null) ? current.item : null;
                return res;
            } finally {
                fullyUnlock();
            }
        }

        private Node<E> nextNode(Node<E> node) {
            while (true) {
                Node<E> next = node.next;
                if (next == node) {
                    return firstNode();
                }
                if (next == null || next.item != null) {
                    return next;
                }
                node = next;
            }
        }

        @Override
        public void remove() {
            if (lastRet == null) {
                throw new IllegalStateException();
            }
            fullyLock();
            try {
                Node<E> node = lastRet;
                lastRet = null;
                for (Node<E> prev = beforeFirstNode(), n = firstNode();
                     n != null;
                     prev = n, n = n.next) {
                    if (n == node) {
                        unlink(n, prev);
                        break;
                    }
                }
            } finally {
                fullyUnlock();
            }
        }
    }
}
