package org.zalando.guild.api.json.fields.jackson.servlet

import jakarta.servlet.http.HttpServletRequest
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import java.util.function.Function
import java.util.function.Supplier

/**
 * FieldPredicate supplier that gets the predicate from a HttpServletRequest supplier and a Transformer function.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
class HttpRequestFieldPredicateProvider private constructor(
    private val requestSupplier: Supplier<HttpServletRequest>,
    private val predicateFunction: Function<HttpServletRequest, FieldPredicate>
) : Supplier<FieldPredicate?> {

    override fun get(): FieldPredicate? {
        return predicateFunction.apply(requestSupplier.get())
    }

    companion object {

        fun httpRequestFieldPredicateProvider(
            requestSupplier: Supplier<HttpServletRequest>,
            predicateFunction: Function<HttpServletRequest, FieldPredicate>
        ): HttpRequestFieldPredicateProvider = HttpRequestFieldPredicateProvider(requestSupplier, predicateFunction)
    }
}