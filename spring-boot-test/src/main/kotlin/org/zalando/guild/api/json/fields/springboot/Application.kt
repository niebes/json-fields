package org.zalando.guild.api.json.fields.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.zalando.guild.api.json.fields.jackson.JsonFieldsModule
import org.zalando.guild.api.json.fields.jackson.ThreadLocalContextProvider
import org.zalando.guild.api.json.fields.jackson.servlet.HttpRequestFieldPredicateProvider.Companion.httpRequestFieldPredicateProvider
import org.zalando.guild.api.json.fields.jackson.servlet.ParamBasedPredicateFunction.Companion.paramBasedPredicateFunctionWithDefaultName
import org.zalando.guild.api.json.fields.jackson.servlet.ThreadLocalRequestProvider
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import java.util.function.Supplier

@SpringBootApplication
class Application {

    companion object {
        private val currentRequest = ThreadLocal<HttpServletRequest>()
    }

    @Bean
    fun jsonFieldsFilter(): Filter {
        return Filter { request: ServletRequest, response: ServletResponse, chain: FilterChain ->
            val httpRequest = request as HttpServletRequest
            currentRequest.set(httpRequest)
            ThreadLocalRequestProvider.assignRequest(httpRequest)
            try {
                chain.doFilter(request, response)
            } finally {
                ThreadLocalRequestProvider.removeRequest()
                ThreadLocalContextProvider.instance.clear()
                currentRequest.remove()
            }
        }
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        val contextProvider = ThreadLocalContextProvider.instance
        val predicateFunction = paramBasedPredicateFunctionWithDefaultName()
        val requestSupplier = Supplier<HttpServletRequest> { currentRequest.get() }
        val predicateSupplier = httpRequestFieldPredicateProvider(
            requestSupplier,
            predicateFunction
        )
        val safeSupplier = Supplier { predicateSupplier.get() ?: FieldPredicates.alwaysTrue() }

        return ObjectMapper()
            .registerKotlinModule()
            .apply {
                registerModule(JsonFieldsModule.createJsonFieldsModule(safeSupplier, contextProvider))
            }
    }
}
