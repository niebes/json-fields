package org.zalando.guild.api.json.fields.jackson

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.AnnotationIntrospector
import com.fasterxml.jackson.databind.introspect.Annotated
import org.zalando.guild.api.json.fields.jackson.generated.PackageVersion

/**
 * An "annotation introspector" that doesn't actually introspect any annotations, but rather returns a constant. This is
 * necessary to trigger filtering in Jackson. Otherwise we'd have to annotate every single bean class.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   24.09.2015
 */
internal class JsonFieldsAnnotationIntrospector : AnnotationIntrospector() {
    override fun version(): Version = PackageVersion.VERSION

    override fun findFilterId(ann: Annotated): Any = JsonFieldsFilterProvider.FILTER_ID
}