package org.zalando.guild.api.json.fields.jackson

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
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import java.util.function.Supplier

/**
 * A FilterProvider that always returns a filter, backed by a supplier of FieldPredicate.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
class JsonFieldsFilterProvider(
    private val predicateSupplier: Supplier<FieldPredicate>,
    private val contextProvider: ContextProvider
) : SimpleFilterProvider() {

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

    private inner class FieldPredicatePropertyFilter(
        private val delegate: PropertyFilter
    ) : PropertyFilter {

        override fun serializeAsField(
            pojo: Any,
            jgen: JsonGenerator,
            prov: SerializerProvider,
            writer: PropertyWriter
        ) {
            val name = writer.name
            val fieldPredicate = predicateSupplier.get()
            if (fieldPredicate.test(qualifiedPath(name))) {
                contextProvider.pushContext(name)
                try {
                    delegate.serializeAsField(pojo, jgen, prov, writer)
                } finally {
                    contextProvider.popContext()
                }
            }
        }

        private fun qualifiedPath(
            path: String
        ): List<String> = buildList {
            addAll(contextProvider.context)
            add(path)
        }


        override fun serializeAsElement(
            elementValue: Any, jgen: JsonGenerator,
            prov: SerializerProvider, writer: PropertyWriter
        ) {
            delegate.serializeAsElement(elementValue, jgen, prov, writer)
        }

        @Throws(JsonMappingException::class)
        override fun depositSchemaProperty(
            writer: PropertyWriter,
            propertiesNode: ObjectNode,
            provider: SerializerProvider
        ) {
            @Suppress("DEPRECATION") // delegate
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
        private const val serialVersionUID = -1263420090088201679L
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