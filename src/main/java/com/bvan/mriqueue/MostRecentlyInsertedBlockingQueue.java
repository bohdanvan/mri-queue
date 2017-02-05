package com.bvan.mriqueue;

import net.jcip.annotations.ThreadSafe;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author bvanchuhov
 */
@ThreadSafe
public class MostRecentlyInsertedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

    public int size() {
        return 0;
    }

    public void put(E e) throws InterruptedException {

    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public E take() throws InterruptedException {
        return null;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    public int remainingCapacity() {
        return 0;
    }

    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }

    public boolean offer(E e) {
        return false;
    }

    public E poll() {
        return null;
    }

    public E peek() {
        return null;
    }

    public Iterator<E> iterator() {
        return null;
    }
}
