package com.ua.combiner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by k.kovalchuk on 31.03.2017.
 */
class PriorityCombinableQueue<T> extends CombinableQueue<T>{
    private double relativePriority;

    PriorityCombinableQueue(BlockingQueue<T> queue, double priority, long isEmptyTimeout, TimeUnit timeUnit) {
        super(queue, priority, isEmptyTimeout, timeUnit);
    }

    double getRelativePriority() {
        return relativePriority;
    }

    void setRelativePriority(double relativePriority) {
        this.relativePriority = relativePriority;
    }

    @Override
    public String toString() {
        return "PriorityCombinableQueue{" +
                "relativePriority=" + relativePriority +
                '}';
    }


}
