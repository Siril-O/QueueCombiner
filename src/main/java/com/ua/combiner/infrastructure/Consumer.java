package com.ua.combiner.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Consumer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private static final int DELAY_TIME = 25;
    private final List<Product> consumedItems = new ArrayList<>();
    private final BlockingQueue<Product> outputQueue;
    private int queuePollTimeout = 1;

    public List<Product> getConsumedItems() {
        return consumedItems;
    }

    public Consumer(BlockingQueue<Product> outputQueue) {
        this.outputQueue = outputQueue;
    }

    public Consumer(BlockingQueue<Product> outputQueue, int queuePollTimeout) {
        this.outputQueue = outputQueue;
        this.queuePollTimeout = queuePollTimeout;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Product consumed = outputQueue.poll(queuePollTimeout, TimeUnit.SECONDS);
                if (consumed == null) {
                    LOGGER.debug("Consumer thread successfully finished");
                    return;
                }
                consumedItems.add(consumed);
                LOGGER.debug("Consumed:{}", consumed);
                Thread.sleep(DELAY_TIME);
            } catch (InterruptedException exc) {
                LOGGER.debug("Consumer thread is interrupted", exc);
                return;
            }
        }
    }
}
