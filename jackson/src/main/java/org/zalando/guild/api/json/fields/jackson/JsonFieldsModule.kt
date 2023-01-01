package org.zalando.guild.api.json.fields.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import org.zalando.guild.api.json.fields.jackson.generated.PackageVersion;
import org.zalando.guild.api.json.fields.java.model.FieldPredicate;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Module that will enable field-based filtering.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
public class JsonFieldsModule extends SimpleModule {

    private static final long serialVersionUID = 7598419837008787123L;
    private final JsonFieldsAnnotationIntrospector jsonFieldsAnnotationIntrospector;

    /**
     * Instantiate the module with a custom subclass of JsonFieldsAnnotationIntrospector.
     */
    public static JsonFieldsModule createJsonFieldsModuleWithCustomIntrospector(
            @Nonnull final Supplier<FieldPredicate> predicateSupplier, @Nonnull final ContextProvider contextProvider,
            @Nonnull final JsonFieldsAnnotationIntrospector introspector) {
        checkNotNull(predicateSupplier, "PredicateSupplier required");
        checkNotNull(contextProvider, "ContextProvider required");
        checkNotNull(introspector, "Introspector required");
        return new JsonFieldsModule(predicateSupplier, contextProvider, introspector);
    }

    /**
     * Instantiate the module with a default JsonFieldsAnnotationIntrospector.
     */
    public static JsonFieldsModule createJsonFieldsModule(@Nonnull final Supplier<FieldPredicate> predicateSupplier,
            @Nonnull final ContextProvider contextProvider) {
        checkNotNull(predicateSupplier, "PredicateSupplier required");
        checkNotNull(contextProvider, "ContextProvider required");
        return new JsonFieldsModule(predicateSupplier, contextProvider);
    }

    @Override
    public void setupModule(final SetupContext context) {
        context.insertAnnotationIntrospector(jsonFieldsAnnotationIntrospector);

        final ObjectMapper objectMapper = context.getOwner();

        final FilterProvider filterProvider = new JsonFieldsFilterProvider(predicateSupplier, contextProvider);
        objectMapper.setFilterProvider(filterProvider);
    }

    private final Supplier<FieldPredicate> predicateSupplier;
    private final ContextProvider contextProvider;

    private JsonFieldsModule(@Nonnull final Supplier<FieldPredicate> predicateSupplier,
            @Nonnull final ContextProvider contextProvider,
            @Nonnull final JsonFieldsAnnotationIntrospector introspector) {
        super(PackageVersion.VERSION);
        this.predicateSupplier = predicateSupplier;
        this.contextProvider = contextProvider;
        this.jsonFieldsAnnotationIntrospector = introspector;
    }

    private JsonFieldsModule(@Nonnull final Supplier<FieldPredicate> predicateSupplier,
            @Nonnull final ContextProvider contextProvider) {
        this(predicateSupplier, contextProvider, new JsonFieldsAnnotationIntrospector());
    }

}
