package org.zalando.guild.api.json.fields.webmvc

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.zalando.guild.api.json.fields.java.expression.ParserFramework
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import java.util.function.Supplier

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
            } catch (e: IllegalArgumentException) {
                log.warn("Invalid fields expression '{}': {}", fieldsParam, e.message)
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
        private val log = LoggerFactory.getLogger(JsonFieldsFilter::class.java)
    }
}
