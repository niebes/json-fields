package org.zalando.guild.api.json.fields.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonassert.JsonAssert;
import com.jayway.jsonassert.JsonAsserter;
import org.junit.Before;
import org.junit.Test;
import org.zalando.guild.api.json.fields.java.model.FieldPredicate;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.*;

/**
 * @author Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since 24.09.2015
 */
public class JsonFieldsModuleTest {

    private static final ThreadLocal<FieldPredicate> PREDICATE = new InheritableThreadLocal<>() {
        @Override
        protected FieldPredicate initialValue() {
            return alwaysTrue();
        }
    };

    private ObjectMapper objectMapper;
    private final Outer outer = new Outer();
    private final AtomicInteger runs = new AtomicInteger();

    @Before
    public void setUp() throws Exception {

        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        final Supplier<FieldPredicate> predicateSupplier = new Supplier<>() {
            @Nonnull
            @Override
            public FieldPredicate get() {
                return PREDICATE.get();
            }
        };

        final ContextProvider contextProvider = ThreadLocalContextProvider.getInstance();
        objectMapper.registerModule(JsonFieldsModule.createJsonFieldsModule(predicateSupplier, contextProvider));
    }

    static class Outer {
        private final Middle foo = new Middle();
        private final String foo2 = "FOO2";
    }

    static class Middle {
        private final Inner bar = new Inner();
        private final int bar2 = 123;

    }

    static class Inner {
        private final String baz = "BAZ";
        private final boolean phleem = true;
    }

    private JsonAsserter asserterFor(final Object obj) {
        try {
            final String json = objectMapper.writeValueAsString(obj);
            System.out.println(json);
            return getJsonAsserter(json);
        } catch (JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Create a Proxy around JsonAsserter that rethrows AssertionError but filters out everything else. This is
     * necessary because otherwise JsonAsserter chokes on paths that go deeper than it can follow.
     */
    private JsonAsserter getJsonAsserter(final String json) {
        final JsonAsserter delegate = JsonAssert.with(json);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final Class<?>[] interfaces = {JsonAsserter.class};
        final AtomicReference<JsonAsserter> asserterHolder = new AtomicReference<>();
        final InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                try {
                    method.invoke(delegate, args);
                } catch (InvocationTargetException e) {
                    final Throwable cause = e.getCause();
                    if (cause instanceof AssertionError) {
                        throw cause;
                    }
                }

                // all methods are fluid, so always return the proxy instance.
                return asserterHolder.get();
            }
        };

        final JsonAsserter jsonAsserter = (JsonAsserter) newProxyInstance(classLoader, interfaces, invocationHandler);
        asserterHolder.set(jsonAsserter);
        return jsonAsserter;
    }

    @Test
    public void singleThreadEnvironment() {
        task(1000).run();
    }

    @Test
    public void multiThreadedEnvironment() throws InterruptedException {
        final int threads = Runtime.getRuntime().availableProcessors() * 2;
        final int runsPerThread = 100;
        final int totalRuns = threads * runsPerThread;

        runs.set(0);

        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        final Runnable task = task(runsPerThread);
        for (int i = 0; i < threads; i++) {
            executorService.submit(task);
        }

        executorService.shutdown();
        assertThat(executorService.awaitTermination(Long.MAX_VALUE, NANOSECONDS), is(true));
        assertThat(runs.get(), is(totalRuns));

    }

    private CountingRunner task(final int runsPerThread) {
        return new CountingRunner(runsPerThread);
    }

    private class CountingRunner implements Runnable {

        private final int runsPerThread;

        public CountingRunner(final int runsPerThread) {
            this.runsPerThread = runsPerThread;
        }

        @Override
        public void run() {
            for (int i = 0; i < runsPerThread; i++) {
                doRun();
            }
        }

        private void doRun() {

            allFieldsEnabled();
            noFieldsEnabled();
            simpleMatch();
            complicatedMatch();

            runs.incrementAndGet();
        }

        private void complicatedMatch() {
            PREDICATE.set(and(not(matchIndex(1, "bar")),
                    not(matchIndex(0, "foo2"))));
            asserterFor(outer).assertThat("$.foo2", is(nullValue()))
                    .assertThat("$.foo.bar", is(nullValue()))
                    .assertThat("$.foo.bar2", is(123));
        }

        private void simpleMatch() {
            PREDICATE.set(matchIndex(1, "bar"));
            asserterFor(outer).assertThat("$.foo2", is("FOO2"))
                    .assertThat("$.foo.bar.baz", is("BAZ"))
                    .assertThat("$.foo.bar2", is(nullValue()))
                    .assertThat("$.foo.bar.phleem", is(true));
        }

        private void noFieldsEnabled() {
            PREDICATE.set(alwaysFalse());
            asserterFor(outer).assertThat("$.foo", is(nullValue()))
                    .assertThat("$.foo2", is(nullValue()));
        }

        private void allFieldsEnabled() {
            PREDICATE.set(alwaysTrue());
            asserterFor(outer).assertThat("$.foo2", is("FOO2"))
                    .assertThat("$.foo.bar.baz", is("BAZ"))
                    .assertThat("$.foo.bar2", is(123))
                    .assertThat("$.foo.bar.phleem", is(true));
        }

    }
}
