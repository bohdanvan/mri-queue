package com.bvan.mriqueue;

import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author bvanchuhov
 */
public class MostRecentlyInsertedBlockingQueueTest {

    @Test
    public void putAndTakeAtMultithreading() throws InterruptedException {
        final BlockingQueue<Integer> queue = new MostRecentlyInsertedBlockingQueue<>(100);

        int tasksCountForPut = 60;
        int tasksCountForTake = 50;

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        for (int i = 0; i < tasksCountForPut; i++) {
            executorService.submit(() -> putTask(queue));
        }
        for (int i = 0; i < tasksCountForTake; i++) {
            executorService.submit(() -> takeTask(queue));
        }
        executorService.shutdown();

        executorService.awaitTermination(2, TimeUnit.SECONDS);

        assertThat(queue, hasSize(tasksCountForPut - tasksCountForTake));
    }

    @Test
    public void takingFromEmptyQueueShouldBlockThread() throws InterruptedException {
        BlockingQueue<Integer> queue = new MostRecentlyInsertedBlockingQueue<>(3);
        Thread takingThread = new Thread(() -> takeTask(queue));
        takingThread.start();

        TimeUnit.MILLISECONDS.sleep(100);

        assertThat(takingThread.getState(), is(Thread.State.WAITING));

        takingThread.interrupt();
    }

    @Test
    public void puttingThreadAwakesBlockingThread() throws InterruptedException {
        BlockingQueue<Integer> queue = new MostRecentlyInsertedBlockingQueue<>(3);

        Thread takingThread = new Thread(() -> takeTask(queue));
        takingThread.start();
        TimeUnit.MICROSECONDS.sleep(100);

        new Thread(() -> putTask(queue)).start();
        TimeUnit.MICROSECONDS.sleep(100);

        assertThat(takingThread.getState(), is(Thread.State.TERMINATED));
    }

    @Test
    public void fillAndEmptyQueue_takingFromEmptyQueueShouldBlockThread() throws InterruptedException {
        BlockingQueue<Integer> queue = new MostRecentlyInsertedBlockingQueue<>(3);

        new Thread(() -> takeTask(queue)).start();
        TimeUnit.MICROSECONDS.sleep(100);
        new Thread(() -> putTask(queue)).start();
    }

    private void putTask(BlockingQueue<Integer> queue) {
        try {
            queue.put(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void takeTask(BlockingQueue<Integer> queue) {
        try {
            queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}