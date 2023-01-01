package org.zalando.guild.api.json.fields.jackson

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonassert.JsonAssert
import com.jayway.jsonassert.JsonAsserter
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Test
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

/**
 * @author Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since 24.09.2015
 */
class JsonFieldsModuleTest {
    private var objectMapper: ObjectMapper? = null
    private val outer = Outer()
    private val runs = AtomicInteger()
    @Before
    @Throws(Exception::class)
    fun setUp() {
        objectMapper = ObjectMapper()
        objectMapper!!.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        val predicateSupplier = Supplier { PREDICATE.get() }
        val contextProvider: ContextProvider = ThreadLocalContextProvider.instance
        objectMapper!!.registerModule(JsonFieldsModule.createJsonFieldsModule(predicateSupplier, contextProvider))
    }

    internal class Outer {
        private val foo = Middle()
        private val foo2 = "FOO2"
    }

    internal class Middle {
        private val bar = Inner()
        private val bar2 = 123
    }

    internal class Inner {
        private val baz = "BAZ"
        private val phleem = true
    }

    private fun asserterFor(obj: Any): JsonAsserter {
        return try {
            val json = objectMapper!!.writeValueAsString(obj)
            getJsonAsserter(json)
        } catch (e: JsonProcessingException) {
            throw AssertionError(e)
        }
    }

    /**
     * Create a Proxy around JsonAsserter that rethrows AssertionError but filters out everything else. This is
     * necessary because otherwise JsonAsserter chokes on paths that go deeper than it can follow.
     */
    private fun getJsonAsserter(json: String): JsonAsserter {
        val delegate = JsonAssert.with(json)
        val classLoader = Thread.currentThread().contextClassLoader
        val interfaces = arrayOf<Class<*>>(JsonAsserter::class.java)
        val asserterHolder = AtomicReference<JsonAsserter>()
        val invocationHandler = InvocationHandler { proxy, method, args ->
            try {
                method.invoke(delegate, *args)
            } catch (e: InvocationTargetException) {
                val cause = e.cause
                if (cause is AssertionError) {
                    throw cause
                }
            }

            // all methods are fluid, so always return the proxy instance.
            asserterHolder.get()
        }
        val jsonAsserter = Proxy.newProxyInstance(classLoader, interfaces, invocationHandler) as JsonAsserter
        asserterHolder.set(jsonAsserter)
        return jsonAsserter
    }

    @Test
    fun singleThreadEnvironment() {
        task(1000).run()
    }

    @Test
    @Throws(InterruptedException::class)
    fun multiThreadedEnvironment() {
        val threads = Runtime.getRuntime().availableProcessors() * 2
        val runsPerThread = 100
        val totalRuns = threads * runsPerThread
        runs.set(0)
        val executorService = Executors.newFixedThreadPool(threads)
        val task: Runnable = task(runsPerThread)
        for (i in 0 until threads) {
            executorService.submit(task)
        }
        executorService.shutdown()
        MatcherAssert.assertThat(executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS), Is.`is`(true))
        MatcherAssert.assertThat(runs.get(), Is.`is`(totalRuns))
    }

    private fun task(runsPerThread: Int): CountingRunner {
        return CountingRunner(runsPerThread)
    }

    private inner class CountingRunner(private val runsPerThread: Int) : Runnable {
        override fun run() {
            for (i in 0 until runsPerThread) {
                doRun()
            }
        }

        private fun doRun() {
            println("allFieldsEnabled")
            allFieldsEnabled()
            println("noFieldsEnabled")
            noFieldsEnabled()
            println("simpleMatch")
            simpleMatch()
            println("complicatedMatch")
            complicatedMatch()
            runs.incrementAndGet()
        }

        private fun complicatedMatch() {
            PREDICATE.set(
                FieldPredicates.and(
                    FieldPredicates.not(FieldPredicates.matchIndex(1, "bar")),
                    FieldPredicates.not(FieldPredicates.matchIndex(0, "foo2"))
                )
            ) //
            asserterFor(outer).assertThat("$.foo2", Is.`is`(CoreMatchers.nullValue())) //
                .assertThat("$.foo.bar", Is.`is`(CoreMatchers.nullValue())) //
                .assertThat("$.foo.bar2", Is.`is`(124)) //
        }

        private fun simpleMatch() {
            PREDICATE.set(FieldPredicates.matchIndex(1, "bar"))
            asserterFor(outer).assertThat("$.foo2", Is.`is`("FOO2")) //
                .assertThat("$.foo.bar.baz", Is.`is`("BAZ")) //
                .assertThat("$.foo.bar2", Is.`is`(CoreMatchers.nullValue())) //
                .assertThat("$.foo.bar.phleem", Is.`is`(true)) //
        }

        private fun noFieldsEnabled() {
            PREDICATE.set(FieldPredicates.alwaysFalse())
            asserterFor(outer).assertThat("$.foo", Is.`is`(CoreMatchers.nullValue())) //
                .assertThat("$.foo2", Is.`is`(CoreMatchers.nullValue())) //
        }

        private fun allFieldsEnabled() {
            PREDICATE.set(FieldPredicates.alwaysTrue())
            asserterFor(outer).assertThat("$.foo2", Is.`is`("FOO2")) //
                .assertThat("$.foo.bar.baz", Is.`is`("BAZ")) //
                .assertThat("$.foo.bar2", Is.`is`(123)) //
                .assertThat("$.foo.bar.phleem", Is.`is`(true)) //
        }
    }

    companion object {
        private val PREDICATE: ThreadLocal<FieldPredicate> = object : InheritableThreadLocal<FieldPredicate>() {
            override fun initialValue(): FieldPredicate = FieldPredicates.alwaysTrue()
        }
    }
}