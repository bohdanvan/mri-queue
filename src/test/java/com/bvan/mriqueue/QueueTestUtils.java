package com.bvan.mriqueue;

import java.util.Queue;

/**
 * @author bvanchuhov
 */
public class QueueTestUtils {

    private QueueTestUtils() {}

    public static <E> void offerAll(Queue<E> queue, Iterable<E> elems) {
        for (E elem : elems) {
            queue.offer(elem);
        }
    }
}
