package com.bvan.mriqueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

import static com.bvan.mriqueue.QueueTestUtils.offerAll;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author bvanchuhov
 */
@RunWith(value = Parameterized.class)
public class MostRecentlyInsertedQueueTest {

    private final MRIQueueFactory<Integer> queueFactory;

    @Parameters(name = "{index} : {0}")
    public static Collection data() {
        return Arrays.asList(new Object[][]{
                {
                        "MostRecentlyInsertedQueue",
                        (MRIQueueFactory) (capacity) -> new MostRecentlyInsertedQueue(capacity)
                },
                {
                        "ConcurrentMostRecentlyInsertedQueue",
                        (MRIQueueFactory) (capacity) -> new ConcurrentMostRecentlyInsertedQueue(capacity)
                },
                {
                        "MostRecentlyInsertedBlockingQueue",
                        (MRIQueueFactory) (capacity) -> new MostRecentlyInsertedBlockingQueue(capacity)
                }
        });
    }

    public MostRecentlyInsertedQueueTest(String name, MRIQueueFactory<Integer> queueFactory) {
        this.queueFactory = queueFactory;
    }

    private Queue<Integer> createQueue(int capacity) {
        return queueFactory.create(capacity);
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

        assertThat(queue.poll(), is(10));
        assertThat(queue.poll(), is(20));
        assertThat(queue.poll(), is(30));
        assertThat(queue.poll(), is(nullValue()));
        assertThat(queue.poll(), is(nullValue()));
    }

    @Test
    public void offerPollOfferPoll() {
        Queue<Integer> queue = createQueue(3);

        queue.offer(10);
        queue.poll();
        queue.offer(20);

        assertThat(queue.poll(), is(20));
        assertThat(queue.poll(), is(nullValue()));
        assertThat(queue.poll(), is(nullValue()));
    }

    @Test
    public void pollFromEmptyQueueShouldReturnNull() {
        Queue<Integer> queue = createQueue(3);

        assertThat(queue.poll(), is(nullValue()));
    }

    @Test
    public void offerAndPeek() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30));
        int peeked = queue.peek();

        assertThat(peeked, is(10));
        assertThat(queue, contains(10, 20, 30));
    }

    @Test
    public void peekFromEmptyQueueShouldReturnNull() {
        Queue<Integer> queue = createQueue(3);
        Integer peeked = queue.peek();

        assertThat(peeked, is(nullValue()));
    }

    @Test
    public void removeFromHead() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30));

        boolean removeRes = queue.remove(10);

        assertThat(removeRes, is(true));
        assertThat(queue, contains(20, 30));
    }

    @Test
    public void removeFromTail() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30));

        boolean removeRes = queue.remove(30);

        assertThat(removeRes, is(true));
        assertThat(queue, contains(10, 20));
    }

    @Test
    public void removeFromMiddle() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30));

        assertThat(queue.remove(20), is(true));
        assertThat(queue, contains(10, 30));
    }

    @Test
    public void removeSingleElem() {
        Queue<Integer> queue = createQueue(1);
        queue.offer(10);

        assertThat(queue.remove(10), is(true));
        assertThat(queue, is(empty()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void queueWithNonPositiveCapacityShouldBeForbidden() {
        new MostRecentlyInsertedQueue<>(-1);
    }

    @Test
    public void offerAndToArray() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30));

        assertThat(queue.toArray(), arrayContaining(10, 20, 30));
    }

    @Test
    public void offerAndClear() {
        Queue<Integer> queue = createQueue(3);
        offerAll(queue, asList(10, 20, 30));

        queue.clear();
        assertThat(queue, is(empty()));
    }
}
