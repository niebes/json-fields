package org.zalando.guild.api.json.fields.jackson

import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.FilterProvider
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import java.util.function.Supplier

class JsonFieldsModule private constructor(
    private val predicateSupplier: Supplier<FieldPredicate>
) : SimpleModule(
    VersionUtil.packageVersionFor(JsonFieldsModule::class.java)
) {
    override fun setupModule(context: SetupContext) {
        context.insertAnnotationIntrospector(JsonFieldsAnnotationIntrospector())
        val objectMapper = context.getOwner<ObjectMapper>()
        val filterProvider: FilterProvider = JsonFieldsFilterProvider(predicateSupplier)
        objectMapper.setFilterProvider(filterProvider)
    }

    companion object {
        fun createJsonFieldsModule(
            predicateSupplier: Supplier<FieldPredicate>
        ): JsonFieldsModule = JsonFieldsModule(predicateSupplier)
    }
}
