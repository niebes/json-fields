package org.zalando.guild.api.json.fields.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.zalando.guild.api.json.fields.java.model.FieldPredicate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A FilterProvider that always returns a filter, backed by a supplier of FieldPredicate.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
public final class JsonFieldsFilterProvider extends SimpleFilterProvider {

    public static final String FILTER_ID = FieldPredicatePropertyFilter.class.getName();

    private static final long serialVersionUID = -1263420090088201679L;

    private final Supplier<FieldPredicate> predicateSupplier;
    private final ContextProvider contextProvider;

    public JsonFieldsFilterProvider(@Nonnull final Supplier<FieldPredicate> predicateSupplier,
            @Nonnull final ContextProvider contextProvider) {
        this.predicateSupplier = checkNotNull(predicateSupplier, "PredicateProvider required");
        this.contextProvider = checkNotNull(contextProvider, "ContextProvider required");
        super.setFailOnUnknownId(false);
    }

    private static final PropertyFilter INCLUDE_ALL = new SimpleBeanPropertyFilter() {
        @Override
        protected boolean include(final BeanPropertyWriter writer) {
            return true;
        }

        @Override
        protected boolean include(final PropertyWriter writer) {
            return true;
        }

        @Override
        public String toString() {
            return "JsonFieldsFilterProvider.INCLUDE_ALL";
        }
    };

    @Override
    public SimpleFilterProvider setFailOnUnknownId(final boolean state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean willFailOnUnknownId() {
        return false;
    }

    @Override
    public PropertyFilter findPropertyFilter(final Object filterId, final Object valueToFilter) {

        final PropertyFilter propertyFilter = super.findPropertyFilter(filterId, valueToFilter);

        return new FieldPredicatePropertyFilter(propertyFilter == null ? INCLUDE_ALL : propertyFilter);
    }

    private class FieldPredicatePropertyFilter implements PropertyFilter {

        private final PropertyFilter delegate;

        public FieldPredicatePropertyFilter(final PropertyFilter delegate) {

            this.delegate = delegate;
        }

        @Override
        public void serializeAsField(final Object pojo, final JsonGenerator jgen, final SerializerProvider prov,
                final PropertyWriter writer) throws Exception {

            final String name = writer.getName();
            final FieldPredicate fieldPredicate = predicateSupplier.get();
            if (fieldPredicate.apply(qualifiedPath(name))) {
                contextProvider.pushContext(name);
                try {
                    delegate.serializeAsField(pojo, jgen, prov, writer);
                } finally {
                    contextProvider.popContext();
                }
            }
        }

        private List<String> qualifiedPath(final String path) {
            final List<String> context = contextProvider.getContext();
            final List<String> paths = new ArrayList<>(context.size() + 1);
            paths.addAll(context);
            paths.add(path);
            return paths;
        }

        @Override
        public void serializeAsElement(final Object elementValue, final JsonGenerator jgen,
                final SerializerProvider prov, final PropertyWriter writer) throws Exception {
            delegate.serializeAsElement(elementValue, jgen, prov, writer);
        }

        @Override
        public void depositSchemaProperty(final PropertyWriter writer, final ObjectNode propertiesNode,
                final SerializerProvider provider) throws JsonMappingException {
            delegate.depositSchemaProperty(writer, propertiesNode, provider);
        }

        @Override
        public void depositSchemaProperty(final PropertyWriter writer, final JsonObjectFormatVisitor objectVisitor,
                final SerializerProvider provider) throws JsonMappingException {
            delegate.depositSchemaProperty(writer, objectVisitor, provider);
        }
    }
}
