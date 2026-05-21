package org.zalando.guild.api.json.fields.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.FilterProvider
import org.zalando.guild.api.json.fields.jackson.generated.PackageVersion
import org.zalando.guild.api.json.fields.jackson.servlet.JsonFieldsFilter
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import java.util.function.Supplier

/**
 * Module that will enable field-based filtering.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
class JsonFieldsModule private constructor(
    private val predicateSupplier: Supplier<FieldPredicate>,
    private val contextProvider: ContextProvider,
    private val jsonFieldsAnnotationIntrospector: JsonFieldsAnnotationIntrospector = JsonFieldsAnnotationIntrospector()
) : SimpleModule(
    PackageVersion.VERSION
) {
    override fun setupModule(context: SetupContext) {
        context.insertAnnotationIntrospector(jsonFieldsAnnotationIntrospector)
        val objectMapper = context.getOwner<ObjectMapper>()
        val filterProvider: FilterProvider = JsonFieldsFilterProvider(predicateSupplier, contextProvider)
        objectMapper.setFilterProvider(filterProvider)
    }

    companion object {
        private const val serialVersionUID = 7598419837008787123L

        /**
         * Instantiate the module with a custom subclass of JsonFieldsAnnotationIntrospector.
         */
        fun createJsonFieldsModuleWithCustomIntrospector(
            predicateSupplier: Supplier<FieldPredicate>,
            contextProvider: ContextProvider,
            introspector: JsonFieldsAnnotationIntrospector
        ): JsonFieldsModule = JsonFieldsModule(predicateSupplier, contextProvider, introspector)

        /**
         * Instantiate the module with a default JsonFieldsAnnotationIntrospector.
         */
        fun createJsonFieldsModule(
            predicateSupplier: Supplier<FieldPredicate>,
            contextProvider: ContextProvider
        ): JsonFieldsModule = JsonFieldsModule(predicateSupplier, contextProvider)

        /**
         * Instantiate the module from a [JsonFieldsFilter], which serves as both
         * the predicate supplier and the source of the context provider.
         */
        fun createJsonFieldsModule(
            filter: JsonFieldsFilter
        ): JsonFieldsModule = JsonFieldsModule(filter, filter.contextProvider())
    }
}