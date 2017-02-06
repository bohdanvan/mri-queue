package com.bvan.mriqueue;

import net.jcip.annotations.NotThreadSafe;

import java.util.*;

/**
 * @author bvanchuhov
 */
@NotThreadSafe
public class MostRecentlyInsertedQueue<E> extends AbstractQueue<E> implements Queue<E> {

    private final int capacity;

    private Node<E> head;
    private Node<E> tail;
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
    }

    public int size() {
        return size;
    }

    public boolean offer(E e) {
        if (isEmpty()) {
            addFirstElem(e);
        } else {
            if (size == capacity) {
                addToTail(e);
                removeFromHead();
            } else {
                addToTail(e);
            }
        }
        return true;
    }

    public E poll() {
        return (!isEmpty()) ? removeFromHead() : null;
    }

    public E peek() {
        return (!isEmpty()) ? head.item : null;
    }

    public Iterator<E> iterator() {
        return new QueueIterator();
    }

    private E removeFromHead() {
        E oldValue = head.item;

        Node<E> newHead = head.next;

        head.item = null; // help GC
        head.next = null;

        head = newHead;
        if (newHead == null) {
            tail = null;
        }

        size--;
        mod++;

        return oldValue;
    }

    private void addToTail(E e) {
        Node<E> node = new Node<>(e);
        tail.next = node;
        tail = node;

        size++;
        mod++;
    }

    private void addFirstElem(E e) {
        Node<E> firstNode = new Node<>(e);
        head = firstNode;
        tail = firstNode;

        size++;
        mod++;
    }

    private static class Node<E> {
        E item;
        Node<E> next;

        Node(E item) {
            this.item = item;
        }
    }

    private class QueueIterator implements Iterator<E> {
        private Node<E> node = head;
        private final int expectedMod = mod;

        @Override
        public boolean hasNext() {
            return node != null;
        }

        /**
         * @throws ConcurrentModificationException if the queue has been changed during iteration.
         */
        @Override
        public E next() {
            if (mod != expectedMod) {
                throw new ConcurrentModificationException();
            }
            if (node == null) {
                throw new NoSuchElementException();
            }
            E res = node.item;
            node = node.next;
            return res;
        }
    }
}
