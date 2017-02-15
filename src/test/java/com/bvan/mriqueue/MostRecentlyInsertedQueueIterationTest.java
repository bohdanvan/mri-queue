package com.bvan.mriqueue;

import org.junit.Test;

import java.util.*;

import static com.bvan.mriqueue.QueueTestUtils.offerAll;
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
}
