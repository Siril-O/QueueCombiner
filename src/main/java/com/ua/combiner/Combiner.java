package com.ua.combiner; /**
 * Please implement this interface.
 */

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * A com.tech.task.com.tech.task.Combiner takes items from multiple input queues and feeds them into a
 * single output queue. Each input queue has a priority, which determines the
 * approximate frequency at which its items are added to the output queue. E.g.
 * if queue A has priority 9.5, and queue B has priority 0.5, then on average,
 * for every 100 items added to the output queue, 95 should come from queue A,
 * and 5 should come from queue B.
 * <p>
 * Input queues can be dynamically added and removed.
 * </p>
 */
public abstract class Combiner<T> {

    protected final SynchronousQueue<T> outputQueue;
    protected volatile boolean isStopped;

    protected Combiner(SynchronousQueue<T> outputQueue) {
        this.outputQueue = outputQueue;
    }

    /**
     * Adds the given queue to this com.tech.task.com.tech.task.Combiner.
     *
     * @param queue          the input queue to add to this com.tech.task.com.tech.task.Combiner
     * @param priority       the priority to assign to the input queue
     * @param isEmptyTimeout if the input queue is seen as empty for a duration
     *                       longer than isEmptyTimeout, then it is removed from the com.tech.task.com.tech.task.Combiner
     * @param timeUnit       the time unit of the isEmptyTimeout argument
     */
    public abstract void addInputQueue(BlockingQueue<T> queue, double priority,
                                       long isEmptyTimeout, TimeUnit timeUnit) throws CombinerException;

    /**
     * Removes the given queue from this com.tech.task.com.tech.task.Combiner.
     *
     * @param queue the input queue to remove from this com.tech.task.com.tech.task.Combiner
     */
    public abstract boolean removeInputQueue(BlockingQueue<T> queue) throws CombinerException;

    /**
     * Returns true if the given queue is currently an input queue to this com.tech.task.com.tech.task.Combiner.
     */
    public abstract boolean hasInputQueue(BlockingQueue<T> queue);

    /**
     * This method was added to Combiner to perform combine operations. There are few other options:
     * 1)can be removed by making Combiner implement Runnable and place this logic to {@link Runnable#run()}  label} method
     * 2)can be removed and this logic will be moved to addInputQueue method, and will create and run separate thread
     * in addInputQueue method which not complies with Single Responsibility principle
     * 3) can be removed and this logic will be moved to {@link package.class#addInputQueue label} method
     * and thread which calls {@link package.class#addInputQueue label}
     * will perform combine operations
     */
    public abstract void combine() throws CombinerException;

    public void stop(){
        isStopped = true;
    }

    /**
     * Thrown to indicate a com.tech.task.com.tech.task.Combiner specific exception.
     */
    public static class CombinerException extends Exception {
        public CombinerException() {
            super();
        }

        public CombinerException(String message) {
            super(message);
        }
    }
}


