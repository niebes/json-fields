package org.zalando.guild.api.json.fields.jackson;

import org.zalando.guild.api.json.fields.jackson.generated.PackageVersion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.Annotated;

/**
 * An "annotation introspector" that doesn't actually introspect any annotations, but rather returns a constant. This is
 * necessary to trigger filtering in Jackson. Otherwise we'd have to annotate every single bean class.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   24.09.2015
 */
public class JsonFieldsAnnotationIntrospector extends AnnotationIntrospector {
    private static final long serialVersionUID = -8876631291707972177L;

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public Object findFilterId(final Annotated ann) {
        return JsonFieldsFilterProvider.FILTER_ID;
    }

}
