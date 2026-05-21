package net.niebes.jsonfields.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.PropertyFilter
import com.fasterxml.jackson.databind.ser.PropertyWriter
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
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

    override fun findPropertyFilter(filterId: Any, valueToFilter: Any): PropertyFilter {
        val propertyFilter = super.findPropertyFilter(filterId, valueToFilter)
        return FieldPredicatePropertyFilter(propertyFilter ?: INCLUDE_ALL)
    }

    private inner class FieldPredicatePropertyFilter(private val delegate: PropertyFilter) : PropertyFilter {
        @Throws(Exception::class)
        override fun serializeAsField(
            pojo: Any, jgen: JsonGenerator,
            prov: SerializerProvider,
            writer: PropertyWriter
        ) {
            val name = writer.name
            val fieldPredicate = predicateSupplier.get()
            if (fieldPredicate.test(qualifiedPath(name))) {
                contextStore.get().addLast(name)
                try {
                    delegate.serializeAsField(pojo, jgen, prov, writer)
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
            prov: SerializerProvider, writer: PropertyWriter
        ) {
            delegate.serializeAsElement(elementValue, jgen, prov, writer)
        }

        @Throws(JsonMappingException::class)
        override fun depositSchemaProperty(
            writer: PropertyWriter, propertiesNode: ObjectNode,
            provider: SerializerProvider
        ) {
            delegate.depositSchemaProperty(writer, propertiesNode, provider)
        }

        @Throws(JsonMappingException::class)
        override fun depositSchemaProperty(
            writer: PropertyWriter, objectVisitor: JsonObjectFormatVisitor,
            provider: SerializerProvider
        ) {
            delegate.depositSchemaProperty(writer, objectVisitor, provider)
        }
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
