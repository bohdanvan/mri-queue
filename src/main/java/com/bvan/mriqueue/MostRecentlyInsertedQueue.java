package com.bvan.mriqueue;

import net.jcip.annotations.NotThreadSafe;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Queue;

/**
 * @author bvanchuhov
 */
@NotThreadSafe
public class MostRecentlyInsertedQueue<E> extends AbstractQueue<E> implements Queue<E> {

    public int size() {
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
