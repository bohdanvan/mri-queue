package com.bvan.mriqueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author bvanchuhov
 */
@RunWith(value = Parameterized.class)
public class MostRecentlyInsertedQueueTest {

    private Class<? extends Queue> queueClass;

    @Parameters(name = "{index} : {0}")
    public static Collection<Class<? extends Queue>> data() {
        return Arrays.asList(
                MostRecentlyInsertedQueue.class,
                ConcurrentMostRecentlyInsertedQueue.class
        );
    }

    public MostRecentlyInsertedQueueTest(Class<? extends Queue> queueClass) {
        this.queueClass = queueClass;
    }

    private Queue<Integer> createQueue(int capacity) {
        try {
            return queueClass.getConstructor(int.class).newInstance(capacity);
        } catch (Exception e1) {
            throw new RuntimeException("can't create queue with class " + queueClass.getName());
        }
    }

    @Test
    public void offerOneElem() {
        Queue<Integer> queue = createQueue(1);
        queue.offer(10);

        assertThat(queue, contains(10));
        assertThat(queue, hasSize(1));
    }

    @Test
    public void offerElemsNotGreaterThanCapacity() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20));

        assertThat(queue, contains(10, 20));
        assertThat(queue, hasSize(2));
    }

    @Test
    public void offerElemsGreaterThanCapacity() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30, 40));

        assertThat(queue, contains(20, 30, 40));
        assertThat(queue, hasSize(3));
    }

    @Test
    public void offerAndPoll() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30));
        int polled = queue.poll();

        assertThat(polled, is(10));
        assertThat(queue, contains(20, 30));
        assertThat(queue, hasSize(2));
    }

    @Test
    public void pollFromEmptyQueueShouldReturnNull() {
        Queue<Integer> queue = createQueue(3);
        Integer polled = queue.poll();

        assertThat(polled, is(nullValue()));
        assertThat(queue, hasSize(0));
    }

    @Test
    public void fillAndEmptyQueue_pollFromEmptyQueueShouldReturnNull() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10));

        queue.poll();
        Integer polled = queue.poll();

        assertThat(polled, is(nullValue()));
        assertThat(queue, hasSize(0));
    }

    @Test
    public void offerAndPeek() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30));
        int peeked = queue.peek();

        assertThat(peeked, is(10));
        assertThat(queue, contains(10, 20, 30));
        assertThat(queue, hasSize(3));
    }

    @Test
    public void peekFromEmptyQueueShouldReturnNull() {
        Queue<Integer> queue = createQueue(3);
        Integer peeked = queue.peek();

        assertThat(peeked, is(nullValue()));
        assertThat(queue, hasSize(0));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void offerDuringIterationViolatesNextIterations() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20));

        Iterator<Integer> iterator = queue.iterator();

        queue.offer(30);

        Integer elem = iterator.next();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void pollDuringIterationForbidsNextIterations() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20));

        Iterator<Integer> iterator = queue.iterator();

        queue.poll();

        Integer elem = iterator.next();
    }

    @Test(expected = IllegalArgumentException.class)
    public void queueWithNonPositiveCapacityShouldBeForbidden() {
        new MostRecentlyInsertedQueue<>(-1);
    }

    private static <E> void offerAll(Queue<E> queue, Iterable<E> elems) {
        for (E elem : elems) {
            queue.offer(elem);
        }
    }
}
