package com.bvan.mriqueue;

import java.util.Queue;

/**
 * @author bvanchuhov
 */
@FunctionalInterface
public interface MRIQueueFactory<T> {

    Queue<T> create(int capacity);
}
