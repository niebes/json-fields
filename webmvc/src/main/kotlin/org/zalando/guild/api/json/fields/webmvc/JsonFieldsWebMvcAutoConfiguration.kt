package org.zalando.guild.api.json.fields.webmvc

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.zalando.guild.api.json.fields.jackson.JsonFieldsModule
import org.zalando.guild.api.json.fields.jackson.JsonFieldsProperties

@AutoConfiguration(before = [JacksonAutoConfiguration::class])
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(JsonFieldsFilter::class)
@ConditionalOnProperty(name = ["spring.json-fields.enabled"], matchIfMissing = true)
class JsonFieldsWebMvcAutoConfiguration {

    @Bean
    @ConfigurationProperties("spring.json-fields")
    fun jsonFieldsProperties(): JsonFieldsProperties = JsonFieldsProperties()

    @Bean
    @ConditionalOnMissingBean
    fun jsonFieldsFilter(properties: JsonFieldsProperties): JsonFieldsFilter =
        JsonFieldsFilter(properties.parameterName)

    @Bean
    @ConditionalOnMissingBean
    fun jsonFieldsModule(jsonFieldsFilter: JsonFieldsFilter): JsonFieldsModule =
        JsonFieldsModule.createJsonFieldsModule(jsonFieldsFilter)

    @Bean
    fun jsonFieldsFilterRegistration(jsonFieldsFilter: JsonFieldsFilter): FilterRegistrationBean<JsonFieldsFilter> {
        val registration = FilterRegistrationBean(jsonFieldsFilter)
        registration.order = Int.MIN_VALUE
        return registration
    }
}
