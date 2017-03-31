package com.ua.combiner;

import com.ua.combiner.infrastructure.Product;
import org.junit.Test;

import java.util.concurrent.SynchronousQueue;

public class CombinerImplIntegrationTest {

    private CombinerImplIntegrationTestHelper combinerImplIntegrationTestHelper = new CombinerImplIntegrationTestHelper();

    @Test
    public void shouldCombineWithDefinedQueuesPriorities() throws Combiner.CombinerException, InterruptedException {
        SynchronousQueue<Product> outputQueue = new SynchronousQueue<>();
        combinerImplIntegrationTestHelper.shouldCombineWithDefinedQueuesPriorities(new SynchronizedCombiner<>(outputQueue), outputQueue);
    }
}
