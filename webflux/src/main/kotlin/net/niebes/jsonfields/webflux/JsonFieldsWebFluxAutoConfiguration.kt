package net.niebes.jsonfields.webflux

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import net.niebes.jsonfields.jackson.JsonFieldsModule
import net.niebes.jsonfields.jackson.JsonFieldsProperties

@AutoConfiguration(before = [JacksonAutoConfiguration::class])
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(JsonFieldsWebFilter::class)
@ConditionalOnProperty(name = ["spring.json-fields.enabled"], matchIfMissing = true)
class JsonFieldsWebFluxAutoConfiguration {

    @Bean
    @ConfigurationProperties("spring.json-fields")
    fun jsonFieldsProperties(): JsonFieldsProperties = JsonFieldsProperties()

    @Bean
    @ConditionalOnMissingBean
    fun jsonFieldsWebFilter(properties: JsonFieldsProperties): JsonFieldsWebFilter =
        JsonFieldsWebFilter(properties.parameterName)

    @Bean
    @ConditionalOnMissingBean
    fun jsonFieldsModule(jsonFieldsWebFilter: JsonFieldsWebFilter): JsonFieldsModule =
        JsonFieldsModule.createJsonFieldsModule(jsonFieldsWebFilter)
}
