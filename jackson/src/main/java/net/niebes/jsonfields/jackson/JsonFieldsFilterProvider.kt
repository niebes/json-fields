package net.niebes.jsonfields.jackson

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor
import tools.jackson.databind.ser.BeanPropertyWriter
import tools.jackson.databind.ser.PropertyFilter
import tools.jackson.databind.ser.PropertyWriter
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter
import tools.jackson.databind.ser.std.SimpleFilterProvider
import net.niebes.jsonfields.core.model.FieldPredicate
import java.util.*
import java.util.function.Supplier

class JsonFieldsFilterProvider(
    private val predicateSupplier: Supplier<FieldPredicate>
) : SimpleFilterProvider() {
    private val contextStore: ThreadLocal<LinkedList<String>> = ThreadLocal.withInitial { LinkedList() }

    init {
        super.setFailOnUnknownId(false)
    }

    override fun setFailOnUnknownId(state: Boolean): SimpleFilterProvider {
        throw UnsupportedOperationException()
    }

    override fun willFailOnUnknownId(): Boolean {
        return false
    }

    override fun snapshot(): JsonFieldsFilterProvider {
        return JsonFieldsFilterProvider(predicateSupplier)
    }

    override fun findPropertyFilter(ctxt: SerializationContext, filterId: Any, valueToFilter: Any): PropertyFilter {
        val propertyFilter = super.findPropertyFilter(ctxt, filterId, valueToFilter)
        return FieldPredicatePropertyFilter(propertyFilter ?: INCLUDE_ALL)
    }

    private inner class FieldPredicatePropertyFilter(private val delegate: PropertyFilter) : PropertyFilter {
        @Throws(Exception::class)
        override fun serializeAsProperty(
            pojo: Any, jgen: JsonGenerator,
            ctxt: SerializationContext,
            writer: PropertyWriter
        ) {
            val name = writer.name
            val fieldPredicate = predicateSupplier.get()
            if (fieldPredicate.test(qualifiedPath(name))) {
                contextStore.get().addLast(name)
                try {
                    delegate.serializeAsProperty(pojo, jgen, ctxt, writer)
                } finally {
                    contextStore.get().removeLast()
                }
            }
        }

        private fun qualifiedPath(path: String): List<String> {
            val context = contextStore.get()
            val paths: MutableList<String> = ArrayList(context.size + 1)
            paths.addAll(context)
            paths.add(path)
            return paths
        }

        @Throws(Exception::class)
        override fun serializeAsElement(
            elementValue: Any, jgen: JsonGenerator,
            ctxt: SerializationContext, writer: PropertyWriter
        ) {
            delegate.serializeAsElement(elementValue, jgen, ctxt, writer)
        }

        override fun depositSchemaProperty(
            writer: PropertyWriter, objectVisitor: JsonObjectFormatVisitor,
            ctxt: SerializationContext
        ) {
            delegate.depositSchemaProperty(writer, objectVisitor, ctxt)
        }

        override fun snapshot(): PropertyFilter = FieldPredicatePropertyFilter(delegate.snapshot())
    }

    companion object {
        @JvmField
        val FILTER_ID: String = FieldPredicatePropertyFilter::class.java.name
        private val INCLUDE_ALL: PropertyFilter = object : SimpleBeanPropertyFilter() {
            override fun include(writer: BeanPropertyWriter): Boolean {
                return true
            }

            override fun include(writer: PropertyWriter): Boolean {
                return true
            }

            override fun toString(): String {
                return "JsonFieldsFilterProvider.INCLUDE_ALL"
            }
        }
    }
}
