package com.ua.combiner;

import com.ua.combiner.infrastructure.Product;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;

public class CombinerImplTest {

    private final SynchronousQueue<Product> outputQueue = new SynchronousQueue<>();
    private final SynchronizedCombiner<Product> testingInstance = new SynchronizedCombiner<>(outputQueue);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldTrowCombinerExceptionWhenOutputQueueTimeoutOccurred() throws Combiner.CombinerException, InterruptedException {
        BlockingQueue<Product> inputQueue = new ArrayBlockingQueue<>(100);
        inputQueue.put(new Product("1", "A"));
        expectedEx.expect(Combiner.CombinerException.class);
        expectedEx.expectMessage("Output queue offer timeout occurred");
        testingInstance.addInputQueue(inputQueue, 1, 1, TimeUnit.SECONDS);

        testingInstance.combine();
    }

    @Test
    public void shouldRemoveQueueWhenPollTimeoutOccurred() throws Combiner.CombinerException {
        BlockingQueue<Product> inputQueue = new ArrayBlockingQueue<>(100);
        testingInstance.addInputQueue(inputQueue, 1, 1, TimeUnit.SECONDS);

        testingInstance.combine();
        assertFalse(testingInstance.hasInputQueue(inputQueue));
    }

}