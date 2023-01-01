package org.zalando.guild.api.json.fields.jackson.servlet;

import static org.zalando.guild.api.json.fields.java.expression.ParserFramework.parseFieldsExpressionOrFail;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import javax.servlet.http.HttpServletRequest;

import org.zalando.guild.api.json.fields.java.model.FieldPredicate;
import org.zalando.guild.api.json.fields.java.model.FieldPredicates;

import com.google.common.base.Function;

/**
 * A Predicate Function that parses a FieldPredicate from a request parameter.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
public class ParamBasedPredicateFunction implements Function<HttpServletRequest, FieldPredicate> {

    private static final String DEFAULT_PARAM_NAME = "fields";
    private final String paramName;

    /**
     * Protected to allow subclassing.
     */
    protected ParamBasedPredicateFunction(final String paramName) {
        this.paramName = paramName;
    }

    /**
     * Protected to allow subclassing.
     */
    protected ParamBasedPredicateFunction() {
        this(DEFAULT_PARAM_NAME);
    }

    /**
     * Return a Predicate Function that maps the query parameter "fields" to a FieldPredicate.
     */
    @Nonnull
    public static ParamBasedPredicateFunction paramBasedPredicateFunctionWithDefaultName() {
        return new ParamBasedPredicateFunction();
    }

    /**
     * Return a Predicate Function that maps the query parameter "fields" to a FieldPredicate.
     */
    @Nonnull
    public static ParamBasedPredicateFunction paramBasedPredicateFunctionWithCustomName(
            @Nonnull final String paramName) {
        checkNotNull(paramName, "ParamName required");
        return new ParamBasedPredicateFunction(paramName);
    }

    /**
     * Open-closed principle.
     */
    @Nonnull
    @Override
    public final FieldPredicate apply(@Nonnull final HttpServletRequest request) {
        checkNotNull(request, "Request required");

        final String parameterValue = request.getParameter(paramName);

        if (parameterValue != null) {
            try {
                return parseFieldsExpressionOrFail(parameterValue);
            } catch (IllegalArgumentException e) {
                return handleExpressionFailure(parameterValue, e);
            }
        } else {

            return FieldPredicates.alwaysTrue();
        }
    }

    /**
     * If you want custom error handling for illegal expressions, override this method. The default behavior is to
     * return a FieldPredicate that never matches.
     */
    @Nonnull
    protected FieldPredicate handleExpressionFailure(@Nonnull final String expression,
            @Nonnull final IllegalArgumentException e) {
        return FieldPredicates.alwaysFalse();
    }
}
