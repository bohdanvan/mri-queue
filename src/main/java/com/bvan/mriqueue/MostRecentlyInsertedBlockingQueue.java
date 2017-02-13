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
    private Node<E> beforeLast;

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
        beforeFirst = beforeLast = Node.emptyNode();
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

        fullyLockInterruptibly();
        try {
            offerInternally(e);
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Guarded by {@code putLock} and {@code takeLock}.
     */
    private void offerInternally(E e) {
        if (size() == capacity) {
            enqueue(e);
            dequeue();
        } else {
            enqueue(e);
            notEmptySignal();
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

        fullyLock();
        try {
            offerInternally(e);
        } finally {
            fullyUnlock();
        }

        return true;
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

        E res = null;
        takeLock.lock();
        try {
            if (!isEmpty()) {
                res = dequeue();
            }
        } finally {
            takeLock.unlock();
        }

        return res;
    }

    @Override
    public E peek() {
        if (isEmpty()) {
            return null;
        }

        takeLock.lock();
        try {
            Node<E> firstNode = firstNode();
            return (beforeFirst != null) ? firstNode.item : null;
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Guarded by {@code putLock}.
     */
    private void enqueue(E e) {
        Node<E> node = new Node<>(e);
        beforeLast.next = node;
        beforeLast = node;

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

        count.decrementAndGet();

        return res;
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
            return toStringInternally();
        } finally {
            fullyUnlock();
        }
    }

    private String toStringInternally() {
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
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new QueueIterator();
    }

    private void fullyLockInterruptibly() throws InterruptedException {
        putLock.lockInterruptibly();
        takeLock.lockInterruptibly();
    }

    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

    private Node<E> firstNode() {
        return beforeFirst.next;
    }

    private Node<E> beforeFirstNode() {
        return beforeFirst;
    }

    /**
     * Guarded by takeLock.
     */
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

        public boolean isEmpty() {
            return item == null;
        }
    }

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
