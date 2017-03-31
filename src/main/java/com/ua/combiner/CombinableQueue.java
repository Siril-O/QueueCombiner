package com.ua.combiner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by k.kovalchuk on 31.03.2017.
 */
class CombinableQueue<T> {
    private final BlockingQueue<T> queue;
    private final double priority;
    private final long isEmptyTimeout;
    private final TimeUnit timeUnit;
    private long hits;

    CombinableQueue(BlockingQueue<T> queue, double priority, long isEmptyTimeout, TimeUnit timeUnit) {
        this.queue = queue;
        this.priority = priority;
        this.isEmptyTimeout = isEmptyTimeout;
        this.timeUnit = timeUnit;
    }

    void incrementHits() {
        hits++;
    }

    BlockingQueue<T> getQueue() {
        return queue;
    }

    double getPriority() {
        return priority;
    }

    long getHits() {
        return hits;
    }

    long getIsEmptyTimeout() {
        return isEmptyTimeout;
    }

    TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public String toString() {
        return "PriorityCombinableQueue{ priority=" + priority +
                ", hits=" + hits +
                '}';
    }
}
