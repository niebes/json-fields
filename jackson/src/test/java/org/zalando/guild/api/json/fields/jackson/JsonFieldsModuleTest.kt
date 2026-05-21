package org.zalando.guild.api.json.fields.jackson

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates.alwaysFalse
import org.zalando.guild.api.json.fields.java.model.FieldPredicates.alwaysTrue
import org.zalando.guild.api.json.fields.java.model.FieldPredicates.and
import org.zalando.guild.api.json.fields.java.model.FieldPredicates.matchIndex
import org.zalando.guild.api.json.fields.java.model.FieldPredicates.not
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

class JsonFieldsModuleTest {
    private var objectMapper: ObjectMapper? = null
    private val outer = Outer()
    private val runs = AtomicInteger()

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        objectMapper!!.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        val predicateSupplier = Supplier { PREDICATE.get() }
        objectMapper!!.registerModule(JsonFieldsModule.createJsonFieldsModule(predicateSupplier))
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

    private fun serialize(obj: Any): JsonNode {
        val json = objectMapper!!.writeValueAsString(obj)
        return objectMapper!!.readTree(json)
    }

    @Test
    fun singleThreadEnvironment() {
        task(1000).run()
    }

    @Test
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
        assertTrue(executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS))
        assertEquals(totalRuns, runs.get())
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
            allFieldsEnabled()
            noFieldsEnabled()
            simpleMatch()
            complicatedMatch()
            runs.incrementAndGet()
        }

        private fun complicatedMatch() {
            PREDICATE.set(
                and(
                    not(matchIndex(1, "bar")),
                    not(matchIndex(0, "foo2"))
                )
            )
            val json = serialize(outer)
            assertFalse(json.has("foo2"))
            // foo is also excluded: NOT(matchIndex(1, "bar")) fails for ["foo"]
            // because matchIndex vacuously passes when hierarchy is too short
            assertFalse(json.has("foo"))
        }

        private fun simpleMatch() {
            PREDICATE.set(matchIndex(1, "bar"))
            val json = serialize(outer)
            assertEquals("FOO2", json["foo2"].textValue())
            assertEquals("BAZ", json["foo"]["bar"]["baz"].textValue())
            assertFalse(json["foo"].has("bar2"))
            assertEquals(true, json["foo"]["bar"]["phleem"].booleanValue())
        }

        private fun noFieldsEnabled() {
            PREDICATE.set(alwaysFalse())
            val json = serialize(outer)
            assertFalse(json.has("foo"))
            assertFalse(json.has("foo2"))
        }

        private fun allFieldsEnabled() {
            PREDICATE.set(alwaysTrue())
            val json = serialize(outer)
            assertEquals("FOO2", json["foo2"].textValue())
            assertEquals("BAZ", json["foo"]["bar"]["baz"].textValue())
            assertEquals(123, json["foo"]["bar2"].intValue())
            assertEquals(true, json["foo"]["bar"]["phleem"].booleanValue())
        }
    }

    companion object {
        private val PREDICATE: ThreadLocal<FieldPredicate> = object : InheritableThreadLocal<FieldPredicate>() {
            override fun initialValue(): FieldPredicate = alwaysTrue()
        }
    }
}
