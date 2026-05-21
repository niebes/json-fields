package org.zalando.guild.api.json.fields.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.FilterProvider
import org.zalando.guild.api.json.fields.jackson.generated.PackageVersion
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import java.util.function.Supplier

class JsonFieldsModule private constructor(
    private val predicateSupplier: Supplier<FieldPredicate>,
    private val jsonFieldsAnnotationIntrospector: JsonFieldsAnnotationIntrospector = JsonFieldsAnnotationIntrospector()
) : SimpleModule(
    PackageVersion.VERSION
) {
    override fun setupModule(context: SetupContext) {
        context.insertAnnotationIntrospector(jsonFieldsAnnotationIntrospector)
        val objectMapper = context.getOwner<ObjectMapper>()
        val filterProvider: FilterProvider = JsonFieldsFilterProvider(predicateSupplier)
        objectMapper.setFilterProvider(filterProvider)
    }

    companion object {
        private const val serialVersionUID = 7598419837008787123L

        fun createJsonFieldsModuleWithCustomIntrospector(
            predicateSupplier: Supplier<FieldPredicate>,
            introspector: JsonFieldsAnnotationIntrospector
        ): JsonFieldsModule = JsonFieldsModule(predicateSupplier, introspector)

        fun createJsonFieldsModule(
            predicateSupplier: Supplier<FieldPredicate>
        ): JsonFieldsModule = JsonFieldsModule(predicateSupplier)
    }
}
