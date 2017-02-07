package com.bvan.mriqueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * @author bvanchuhov
 */
@RunWith(Parameterized.class)
public class MostRecentlyInsertedQueueIterationTest {

    private Class<? extends Queue> queueClass;

    @Parameterized.Parameters(name = "{index} : {0}")
    public static Collection<Class<? extends Queue>> data() {
        return Arrays.asList(
                MostRecentlyInsertedQueue.class,
                ConcurrentMostRecentlyInsertedQueue.class
        );
    }

    public MostRecentlyInsertedQueueIterationTest(Class<? extends Queue> queueClass) {
        this.queueClass = queueClass;
    }

    private Queue<Integer> createQueue(int capacity) {
        try {
            return queueClass.getConstructor(int.class).newInstance(capacity);
        } catch (Exception e1) {
            throw new RuntimeException("can't create queue with class " + queueClass.getName());
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void offeringDuringIterationForbidsNextIterations() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20));

        Iterator<Integer> iterator = queue.iterator();

        queue.offer(30);

        Integer elem = iterator.next();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void pollingDuringIterationForbidsNextIterations() {
        Queue<Integer> queue = createQueue(3);
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
