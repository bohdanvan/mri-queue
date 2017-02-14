package com.bvan.mriqueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * @author bvanchuhov
 */
public class MostRecentlyInsertedQueueIterationTest {

    @Test(expected = ConcurrentModificationException.class)
    public void offeringDuringIterationForbidsNextIterations() {
        Queue<Integer> queue = new MostRecentlyInsertedQueue<>(3);
        offerAll(queue, asList(10, 20));

        Iterator<Integer> iterator = queue.iterator();

        queue.offer(30);

        Integer elem = iterator.next();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void pollingDuringIterationForbidsNextIterations() {
        Queue<Integer> queue = new MostRecentlyInsertedQueue<>(3);
        offerAll(queue, asList(10, 20));

        Iterator<Integer> iterator = queue.iterator();

        queue.poll();

        Integer elem = iterator.next();
    }

    private static <E> void offerAll(Queue<E> queue, Iterable<E> elems) {
        for (E elem : elems) {
            queue.offer(elem);
        }
    }
}
