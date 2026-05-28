package net.niebes.jsonfields.jackson

import tools.jackson.core.Version
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.FilterProvider
import net.niebes.jsonfields.core.model.FieldPredicate
import java.util.function.Supplier

class JsonFieldsModule private constructor() : SimpleModule(Version.unknownVersion()) {
    override fun setupModule(context: SetupContext) {
        context.insertAnnotationIntrospector(JsonFieldsAnnotationIntrospector())
    }

    companion object {
        fun createJsonFieldsModule(): JsonFieldsModule = JsonFieldsModule()

        fun createFilterProvider(predicateSupplier: Supplier<FieldPredicate>): FilterProvider =
            JsonFieldsFilterProvider(predicateSupplier)

    }
}
