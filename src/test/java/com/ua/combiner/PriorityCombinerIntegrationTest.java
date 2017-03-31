package com.ua.combiner;

import com.ua.combiner.infrastructure.Product;
import org.junit.Test;

import java.util.concurrent.SynchronousQueue;

/**
 * Created by k.kovalchuk on 31.03.2017.
 */
public class PriorityCombinerIntegrationTest {

    private CombinerImplIntegrationTestHelper combinerImplIntegrationTestHelper = new CombinerImplIntegrationTestHelper();

    @Test
    public void shouldCombineWithDefinedQueuesPriorities() throws Combiner.CombinerException, InterruptedException {
        SynchronousQueue<Product> outputQueue = new SynchronousQueue<>();
        combinerImplIntegrationTestHelper.shouldCombineWithDefinedQueuesPriorities(new PriorityCombiner<>(outputQueue), outputQueue);
    }
}