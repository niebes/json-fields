package net.niebes.jsonfields.jackson

import tools.jackson.core.Version
import tools.jackson.databind.AnnotationIntrospector
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated

internal class JsonFieldsAnnotationIntrospector : AnnotationIntrospector() {
    override fun version(): Version = Version.unknownVersion()

    override fun findFilterId(config: MapperConfig<*>, ann: Annotated): Any = JsonFieldsFilterProvider.FILTER_ID
}
