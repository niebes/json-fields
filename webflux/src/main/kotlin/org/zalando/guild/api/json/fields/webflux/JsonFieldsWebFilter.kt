package org.zalando.guild.api.json.fields.webflux

import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.zalando.guild.api.json.fields.java.expression.ParserFramework
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import reactor.core.publisher.Mono
import java.util.function.Supplier

/**
 * A WebFilter that extracts a fields expression from the request query parameter
 * and makes the resulting FieldPredicate available for Jackson serialization.
 *
 * Stores the predicate in both Reactor Context (for propagation through the
 * reactive chain) and a ThreadLocal (for Jackson serialization which runs
 * synchronously). Uses a ThreadLocalAccessor to bridge Reactor Context to
 * ThreadLocal when context-propagation is enabled.
 *
 * Register as a WebFilter bean and pass as the predicateSupplier to
 * [org.zalando.guild.api.json.fields.jackson.JsonFieldsModule].
 */
class JsonFieldsWebFilter(
    private val paramName: String = DEFAULT_PARAM_NAME
) : WebFilter, Supplier<FieldPredicate> {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val fieldsParam = exchange.request.queryParams.getFirst(paramName)
        val predicate = if (fieldsParam != null) {
            try {
                ParserFramework.parseFieldsExpressionOrFail(fieldsParam)
            } catch (_: IllegalArgumentException) {
                FieldPredicates.alwaysFalse()
            }
        } else {
            FieldPredicates.alwaysTrue()
        }
        CURRENT_PREDICATE.set(predicate)
        return chain.filter(exchange)
            .contextWrite { ctx -> ctx.put(CONTEXT_KEY, predicate) }
            .doFinally { CURRENT_PREDICATE.remove() }
    }

    override fun get(): FieldPredicate {
        return CURRENT_PREDICATE.get() ?: FieldPredicates.alwaysTrue()
    }

    companion object {
        private const val DEFAULT_PARAM_NAME = "fields"
        internal const val CONTEXT_KEY = "json-fields-predicate"
        internal val CURRENT_PREDICATE = ThreadLocal<FieldPredicate>()
    }
}
