package org.zalando.guild.api.json.fields.webflux

import org.slf4j.LoggerFactory
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.zalando.guild.api.json.fields.java.expression.ParserFramework
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import reactor.core.publisher.Mono
import java.util.function.Supplier

class JsonFieldsWebFilter(
    private val paramName: String = DEFAULT_PARAM_NAME
) : WebFilter, Supplier<FieldPredicate> {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val fieldsParam = exchange.request.queryParams.getFirst(paramName)
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
        private val log = LoggerFactory.getLogger(JsonFieldsWebFilter::class.java)
        internal const val CONTEXT_KEY = "json-fields-predicate"
        internal val CURRENT_PREDICATE = ThreadLocal<FieldPredicate>()
    }
}
