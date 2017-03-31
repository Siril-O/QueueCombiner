package com.ua.combiner;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class is not fully synchronized implementation of Combiner, it ensures approximate distribution of polling from queues
 * possible situations of reading from removed queue, because PriorityBlockingQueue returns weakly consistent spliterator
 * @param <T>
 */
public class PriorityCombiner<T> extends Combiner<T> {

    private static final int OUTPUT_QUEUE_OFFER_TIMEOUT = 2;
    private static final int QUEUES_INITIAL_QUANTITY = 10;

    private final AtomicInteger totalHits = new AtomicInteger(0);

    private final PriorityBlockingQueue<PriorityCombinableQueue<T>> queues = new PriorityBlockingQueue<>(QUEUES_INITIAL_QUANTITY, new PriorityCombinerComparator());


    public PriorityCombiner(SynchronousQueue<T> outputQueue) {
        super(outputQueue);
    }

    @Override
    public void addInputQueue(BlockingQueue<T> queue, double priority, long isEmptyTimeout, TimeUnit timeUnit) {
        if (queue == null || priority <= 0 || isEmptyTimeout <= 0 || timeUnit == null) {
            throw new IllegalArgumentException();
        }
        queues.add(new PriorityCombinableQueue<>(queue, priority, isEmptyTimeout, timeUnit));
        recalculateQueues();
    }

    @Override
    public boolean removeInputQueue(BlockingQueue<T> queue) {
        Optional<PriorityCombinableQueue<T>> queueToRemove = queues.stream().filter(q -> q.getQueue().equals(queue)).findAny();
        boolean isRemoved = queues.remove(queueToRemove.orElse(null));
        if (isRemoved) {
            recalculateQueues();
        }
        return isRemoved;
    }

    @Override
    public boolean hasInputQueue(BlockingQueue<T> queue) {
        return queues.stream().anyMatch(q -> q.getQueue().equals(queue));
    }

    /**
     *  designed to be executed by single thread
     * @throws Combiner.CombinerException
     */
    @Override
    public void combine() throws Combiner.CombinerException {
        synchronized (this) {
            while (!queues.isEmpty() && !isStopped) {
                PriorityCombinableQueue<T> queue = queues.poll();
                if (queue != null) {
                    combineInternal(queue);
                    queues.add(queue);
                }
            }
        }
    }

    private void combineInternal(PriorityCombinableQueue<T> queue) throws Combiner.CombinerException {
        try {
            T product = queue.getQueue().poll(queue.getIsEmptyTimeout(), queue.getTimeUnit());
            if (product != null) {
                if (outputQueue.offer(product, OUTPUT_QUEUE_OFFER_TIMEOUT, TimeUnit.SECONDS)) {
                    queue.incrementHits();
                    totalHits.incrementAndGet();
                } else {
                    throw new Combiner.CombinerException("Output queue offer timeout occurred");
                }
            } else {
                removeInputQueue(queue.getQueue());
            }
        } catch (InterruptedException e) {
            throw new Combiner.CombinerException("Combiner was interrupted while polling from queue");
        }
    }

    private void recalculateQueues() {
        Double totalPriority = queues.stream().mapToDouble(PriorityCombinableQueue::getPriority).sum();
        queues.forEach(q -> q.setRelativePriority(q.getPriority() / totalPriority));
    }

    private class PriorityCombinerComparator implements Comparator<PriorityCombinableQueue<T>> {

        private double calcFrequency(PriorityCombinableQueue queue) {
            return (double) queue.getHits() / totalHits.get() - queue.getRelativePriority();
        }

        @Override
        public int compare(PriorityCombinableQueue<T> queue1, PriorityCombinableQueue<T> queue2) {
            return Double.compare(calcFrequency(queue1), calcFrequency(queue2));
        }
    }
}

