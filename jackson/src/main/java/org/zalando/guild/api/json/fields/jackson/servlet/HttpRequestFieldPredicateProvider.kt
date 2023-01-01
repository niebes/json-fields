package org.zalando.guild.api.json.fields.jackson.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import javax.servlet.http.HttpServletRequest;

import org.zalando.guild.api.json.fields.java.model.FieldPredicate;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

/**
 * FieldPredicate supplier that gets the predicate from a HttpServletRequest supplier and a Transformer function.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
public class HttpRequestFieldPredicateProvider implements Supplier<FieldPredicate> {
    private final Supplier<HttpServletRequest> requestSupplier;
    private final Function<HttpServletRequest, FieldPredicate> predicateFunction;

    private HttpRequestFieldPredicateProvider(final Supplier<HttpServletRequest> requestSupplier,
            final Function<HttpServletRequest, FieldPredicate> predicateFunction) {
        this.requestSupplier = requestSupplier;
        this.predicateFunction = predicateFunction;
    }

    @Nonnull
    public static HttpRequestFieldPredicateProvider httpRequestFieldPredicateProvider(
            @Nonnull final Supplier<HttpServletRequest> requestSupplier,
            @Nonnull final Function<HttpServletRequest, FieldPredicate> predicateFunction) {
        checkNotNull(requestSupplier, "RequestSupplier required");
        checkNotNull(predicateFunction, "PredicateFunction required");
        return new HttpRequestFieldPredicateProvider(requestSupplier, predicateFunction);
    }

    @Override
    @Nonnull
    public FieldPredicate get() {
        return predicateFunction.apply(requestSupplier.get());
    }
}
