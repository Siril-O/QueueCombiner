package com.ua.combiner.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    private static final int CONSUMPTION_DELAY = 50;

    private final BlockingQueue<Product> inputQueue;
    private final String queueName;

    public Producer(BlockingQueue<Product> inputQueue, String queueName) {
        this.inputQueue = inputQueue;
        this.queueName = queueName;
    }

    @Override
    public void run() {
        int counter = 0;
        while (true) {
            Product product = new Product(String.valueOf(counter++), queueName);
            try {
                inputQueue.put(product);
                Thread.sleep(CONSUMPTION_DELAY);
                LOGGER.debug("Produced:{}", product);
            } catch (InterruptedException exc) {
                LOGGER.debug("Producer queue:{}, is interrupted", queueName);
                return;
            }
        }
    }
}
