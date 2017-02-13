package com.bvan.mriqueue;


import java.util.*;

/**
 * Not tread-safe implementation of MostRecentlyInsertedQueue.
 *
 * @author bvanchuhov
 */
public class MostRecentlyInsertedQueue<E> extends AbstractQueue<E> implements Queue<E> {

    private final int capacity;

    private Node<E> beforeFirst;
    private Node<E> beforeLast;
    private int size;

    private int mod;

    /**
     * @throws IllegalArgumentException if {@code capacity} is not positive
     */
    public MostRecentlyInsertedQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity should be greater than 0: " + capacity);
        }
        this.capacity = capacity;
        beforeFirst = beforeLast = Node.emptyNode();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }

        if (size == capacity) {
            enqueue(e);
            dequeue();
        } else {
            enqueue(e);
        }
        return true;
    }

    @Override
    public E poll() {
        return (!isEmpty()) ? dequeue() : null;
    }

    @Override
    public E peek() {
        return (!isEmpty()) ? firstNode().item : null;
    }

    @Override
    public Iterator<E> iterator() {
        return new QueueIterator();
    }

    private Node<E> firstNode() {
        return beforeFirst.next;
    }

    private Node<E> beforeFirstNode() {
        return beforeFirst;
    }

    private E dequeue() {
        return unlink(firstNode(), beforeFirstNode());
    }

    private void enqueue(E e) {
        Node<E> node = new Node<>(e);
        beforeLast.next = node;
        beforeLast = node;

        size++;
        mod++;
    }

    /**
     * @param node {@code not null}.
     * @param node {@code not null}.
     */
    private E unlink(Node<E> node, Node<E> prev) {
        prev.next = node.next;

        E res = node.item;
        node.item = null;
        node.next = null;

        size--;
        mod++;

        return res;
    }

    private static class Node<E> {
        E item;
        Node<E> next;

        static <E> Node<E> emptyNode() {
            return new Node<>(null);
        }

        Node(E item) {
            this.item = item;
        }
    }

    private class QueueIterator implements Iterator<E> {
        private Node<E> lastRet = null;
        private Node<E> current = firstNode();
        private int expectedMod = mod;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        /**
         * @throws ConcurrentModificationException if the queue has been changed during iteration.
         */
        @Override
        public E next() {
            if (mod != expectedMod) {
                throw new ConcurrentModificationException();
            }
            if (current == null) {
                throw new NoSuchElementException();
            }
            E res = current.item;
            lastRet = current;
            current = current.next;
            return res;
        }

        @Override
        public void remove() {
            if (lastRet == null) {
                throw new IllegalStateException();
            }
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
            expectedMod = mod;
        }
    }
}
