package net.niebes.jsonfields.jackson

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.AnnotationIntrospector
import com.fasterxml.jackson.databind.introspect.Annotated

internal class JsonFieldsAnnotationIntrospector : AnnotationIntrospector() {
    override fun version(): Version = VersionUtil.packageVersionFor(JsonFieldsAnnotationIntrospector::class.java)

    override fun findFilterId(ann: Annotated): Any = JsonFieldsFilterProvider.FILTER_ID
}