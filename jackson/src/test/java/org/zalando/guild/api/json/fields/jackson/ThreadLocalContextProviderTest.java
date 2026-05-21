package org.zalando.guild.api.json.fields.jackson;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.hamcrest.core.Is.is;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Test to see if ThreadLocalContextProvider is actually thread safe.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   24.09.2015
 */
public class ThreadLocalContextProviderTest {

    @Test
    public void lotsOfThreads() throws InterruptedException {
        final int threads = Runtime.getRuntime().availableProcessors() * 10;
        final int runsPerThread = 100;
        final int submits = threads * 2;
        final int totalruns = runsPerThread * submits;

        final AtomicInteger counter = new AtomicInteger();

        final ExecutorService executorService = Executors.newFixedThreadPool(threads);

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < runsPerThread; i++) {
                    doRun();
                }

            }

            private void doRun() {
                final ContextProvider contextProvider = ThreadLocalContextProvider.Companion.getInstance();
                contextProvider.pushContext("foo");
                contextProvider.pushContext("notfoo");
                contextProvider.popContext();
                contextProvider.pushContext("bar");
                assertThat(contextProvider.getContext(), is(asList("foo", "bar")));

                contextProvider.pushContext("baz");
                contextProvider.pushContext("phleem");
                contextProvider.popContext();
                assertThat(contextProvider.getContext(), is(asList("foo", "bar", "baz")));

                // clean up
                contextProvider.clear();

                counter.incrementAndGet();
            }
        };
        for (int i = 0; i < submits; i++) {
            executorService.submit(task);
        }

        executorService.shutdown();
        executorService.awaitTermination(5, SECONDS);

        assertThat(counter.get(), is(totalruns));

    }

}
