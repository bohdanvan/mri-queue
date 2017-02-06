package com.bvan.mriqueue;

import net.jcip.annotations.ThreadSafe;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author bvanchuhov
 */
@ThreadSafe
public class ConcurrentMostRecentlyInsertedQueue<E> implements Queue<E> {

    private final Queue<E> queue;

    public ConcurrentMostRecentlyInsertedQueue(int capacity) {
        queue = new MostRecentlyInsertedQueue<>(capacity);
    }

    @Override
    public synchronized boolean add(E e) {
        return queue.add(e);
    }

    @Override
    public synchronized boolean offer(E e) {
        return queue.offer(e);
    }

    @Override
    public synchronized E remove() {
        return queue.remove();
    }

    @Override
    public synchronized E poll() {
        return queue.poll();
    }

    @Override
    public synchronized E element() {
        return queue.element();
    }

    @Override
    public synchronized E peek() {
        return queue.peek();
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return queue.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator(); // Must be manually synched by user
    }

    @Override
    public synchronized Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return queue.toArray(a);
    }

    @Override
    public synchronized boolean remove(Object o) {
        return queue.remove(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        return queue.addAll(c);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public synchronized boolean removeIf(Predicate<? super E> filter) {
        return queue.removeIf(filter);
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        return queue.retainAll(c);
    }

    @Override
    public synchronized void clear() {
        queue.clear();
    }

    @Override
    public synchronized boolean equals(Object o) {
        return queue.equals(o);
    }

    @Override
    public synchronized int hashCode() {
        return queue.hashCode();
    }

    @Override
    public Spliterator<E> spliterator() {
        return queue.spliterator(); // Must be manually synched by user!
    }

    @Override
    public Stream<E> stream() {
        return queue.stream(); // Must be manually synched by user!
    }

    @Override
    public Stream<E> parallelStream() {
        return queue.parallelStream(); // Must be manually synched by user!
    }

    @Override
    public synchronized void forEach(Consumer<? super E> action) {
        queue.forEach(action);
    }
}
