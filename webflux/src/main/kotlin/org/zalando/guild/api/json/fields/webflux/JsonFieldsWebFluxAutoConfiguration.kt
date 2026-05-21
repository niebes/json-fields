package org.zalando.guild.api.json.fields.webflux

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.context.annotation.Bean
import org.zalando.guild.api.json.fields.jackson.JsonFieldsModule

@AutoConfiguration(before = [JacksonAutoConfiguration::class])
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(JsonFieldsWebFilter::class)
class JsonFieldsWebFluxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun jsonFieldsWebFilter(): JsonFieldsWebFilter = JsonFieldsWebFilter()

    @Bean
    @ConditionalOnMissingBean
    fun jsonFieldsModule(jsonFieldsWebFilter: JsonFieldsWebFilter): JsonFieldsModule =
        JsonFieldsModule.createJsonFieldsModule(jsonFieldsWebFilter)
}
