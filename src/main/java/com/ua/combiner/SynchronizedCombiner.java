package com.ua.combiner;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class is synchronized, so situations of reading from removed queue can not happen
 * @param <T>
 */
public class SynchronizedCombiner<T> extends Combiner<T> {

    private static final int OUTPUT_QUEUE_OFFER_TIMEOUT = 2;
    private final List<CombinableQueue<T>> queues = new ArrayList<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public SynchronizedCombiner(SynchronousQueue<T> outputQueue) {
        super(outputQueue);
    }

    @Override
    public void addInputQueue(BlockingQueue<T> queue, double priority, long isEmptyTimeout, TimeUnit timeUnit) {
        if (queue == null || priority <= 0 || isEmptyTimeout <= 0 || timeUnit == null) {
            throw new IllegalArgumentException();
        }
        readWriteLock.writeLock().lock();
        try {
            queues.add(new CombinableQueue<>(queue, priority, isEmptyTimeout, timeUnit));
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeInputQueue(BlockingQueue<T> queue) {
        readWriteLock.writeLock().lock();
        try {
            Optional<CombinableQueue<T>> queueToRemove = queues.stream().filter(q -> q.getQueue().equals(queue)).findAny();
            return queues.remove(queueToRemove.orElse(null));
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean hasInputQueue(BlockingQueue<T> queue) {
        readWriteLock.readLock().lock();
        try {
            return queues.stream().anyMatch(q -> q.getQueue().equals(queue));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * method is designed to be executed by single thread
     *
     * @throws CombinerException
     */
    @Override
    public void combine() throws CombinerException {
        synchronized (this) {
            while (!queues.isEmpty() && !isStopped) {
                combineInternal();
            }
        }
    }

    private void combineInternal() throws CombinerException {
        readWriteLock.readLock().lock();
        T product = null;
        if (queues.isEmpty()) {
            return;
        }
        CombinableQueue<T> queue = getNextQueueToPoll();
        try {
            product = queue.getQueue().poll(queue.getIsEmptyTimeout(), queue.getTimeUnit());
            if (product != null) {
                if (outputQueue.offer(product, OUTPUT_QUEUE_OFFER_TIMEOUT, TimeUnit.SECONDS)) {
                    queue.incrementHits();
                } else {
                    throw new CombinerException("Output queue offer timeout occurred");
                }
            }
        } catch (InterruptedException e) {
            throw new CombinerException("Combiner was interrupted while polling from queue");
        } finally {
            readWriteLock.readLock().unlock();
            if (product == null) {
                removeInputQueue(queue.getQueue());
            }
        }
    }

    private CombinableQueue<T> getNextQueueToPoll() {
        double totalHits = this.queues.stream().mapToDouble(CombinableQueue::getHits).sum();
        double totalPriority = queues.stream().mapToDouble(CombinableQueue::getPriority).sum();
        return Collections.max(queues, Comparator.comparingDouble(queue -> queue.getPriority() / totalPriority - queue.getHits() / totalHits));
    }

}
