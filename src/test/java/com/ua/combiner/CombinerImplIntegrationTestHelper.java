package com.ua.combiner;

import com.google.common.collect.Lists;
import com.ua.combiner.infrastructure.Consumer;
import com.ua.combiner.infrastructure.Producer;
import com.ua.combiner.infrastructure.Product;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

/**
 * Created by k.kovalchuk on 31.03.2017.
 */
public class CombinerImplIntegrationTestHelper {

    private static final int QUEUE_CAPACITY = 100;
    private static final double PRIORITY_1 = 0.7;
    private static final double PRIORITY_2 = 0.2;
    private static final double PRIORITY_3 = 0.1;

    private static final String A_QUEUE = "A";
    private static final String B_QUEUE = "B";
    private static final String C_QUEUE = "C";

    private static final double DELTA = 0.05;
    private static final int NUMBER_OF_THREADS = 5;

    protected void shouldCombineWithDefinedQueuesPriorities(Combiner<Product> combiner, SynchronousQueue<Product> outputQueue) throws Combiner.CombinerException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        Consumer consumer = new Consumer(outputQueue);
        List<Producer> producers = givenProducers(combiner);

        List<Future> producerFutures = producers.stream().map(executorService::submit).collect(Collectors.toList());

        executorService.submit(consumer);
        executorService.submit(() -> {
            try {
                combiner.combine();
            } catch (Combiner.CombinerException e) {
                System.out.println("Error during combining queues");
            }
        });

        performDelay(Duration.ofSeconds(10).toMillis());
        producerFutures.forEach(p -> p.cancel(true));
        combiner.stop();

        assertResultsFrequencies(consumer);
        shutdownExecutor(executorService);
    }

    private List<Producer> givenProducers(Combiner<Product> combiner) throws Combiner.CombinerException {
        BlockingQueue<Product> inputQueue1 = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        BlockingQueue<Product> inputQueue2 = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        BlockingQueue<Product> inputQueue3 = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        combiner.addInputQueue(inputQueue1, PRIORITY_1, 1L, TimeUnit.SECONDS);
        combiner.addInputQueue(inputQueue2, PRIORITY_2, 1L, TimeUnit.SECONDS);
        combiner.addInputQueue(inputQueue3, PRIORITY_3, 1L, TimeUnit.SECONDS);

        return Lists.newArrayList(new Producer(inputQueue1, A_QUEUE),
                new Producer(inputQueue2, B_QUEUE), new Producer(inputQueue3, C_QUEUE));
    }

    private static void assertResultsFrequencies(Consumer consumerRunnable) {
        Map<String, Long> hitsByQueues = consumerRunnable.getConsumedItems().stream().collect(Collectors.groupingBy(Product::getQueueName, Collectors.counting()));
        System.out.println(hitsByQueues);
        long sum = hitsByQueues.values().stream().mapToLong(e -> e).sum();
        Map<String, Double> frequencies = hitsByQueues.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> (double) e.getValue() / sum));
        System.out.println(frequencies);
        assertThat(frequencies.get(A_QUEUE), closeTo(PRIORITY_1, DELTA));
        assertThat(frequencies.get(B_QUEUE), closeTo(PRIORITY_2, DELTA));
        assertThat(frequencies.get(C_QUEUE), closeTo(PRIORITY_3, DELTA));
    }

    private static void shutdownExecutor(ExecutorService executorService) throws InterruptedException {
        try {
            executorService.shutdown();
            executorService.awaitTermination(3, TimeUnit.SECONDS);
        } finally {
            executorService.shutdownNow();
        }
    }

    private static void performDelay(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }


}
