package org.zalando.guild.api.json.fields.webmvc

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.zalando.guild.api.json.fields.java.expression.ParserFramework
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import java.util.function.Supplier

/**
 * A servlet Filter that extracts a fields expression from the request and provides
 * the resulting FieldPredicate. Register as a servlet filter and pass as the
 * predicateSupplier to [org.zalando.guild.api.json.fields.jackson.JsonFieldsModule].
 */
class JsonFieldsFilter(
    private val paramName: String = DEFAULT_PARAM_NAME
) : Filter, Supplier<FieldPredicate> {

    private val currentPredicate = ThreadLocal<FieldPredicate>()

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val fieldsParam = httpRequest.getParameter(paramName)
        val predicate = if (fieldsParam != null) {
            try {
                ParserFramework.parseFieldsExpressionOrFail(fieldsParam)
            } catch (_: IllegalArgumentException) {
                FieldPredicates.alwaysFalse()
            }
        } else {
            FieldPredicates.alwaysTrue()
        }
        currentPredicate.set(predicate)
        try {
            chain.doFilter(request, response)
        } finally {
            currentPredicate.remove()
        }
    }

    override fun get(): FieldPredicate {
        return currentPredicate.get() ?: FieldPredicates.alwaysTrue()
    }

    companion object {
        private const val DEFAULT_PARAM_NAME = "fields"
    }
}
