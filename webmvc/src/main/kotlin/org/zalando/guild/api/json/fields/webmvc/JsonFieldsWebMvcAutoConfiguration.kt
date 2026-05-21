package org.zalando.guild.api.json.fields.webmvc

import jakarta.servlet.Filter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.context.annotation.Bean
import org.zalando.guild.api.json.fields.jackson.JsonFieldsModule

@AutoConfiguration(before = [JacksonAutoConfiguration::class])
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(JsonFieldsFilter::class)
class JsonFieldsWebMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun jsonFieldsFilter(): JsonFieldsFilter = JsonFieldsFilter()

    @Bean
    @ConditionalOnMissingBean
    fun jsonFieldsModule(jsonFieldsFilter: JsonFieldsFilter): JsonFieldsModule =
        JsonFieldsModule.createJsonFieldsModule(jsonFieldsFilter)

    @Bean
    fun jsonFieldsFilterRegistration(jsonFieldsFilter: JsonFieldsFilter): org.springframework.boot.web.servlet.FilterRegistrationBean<JsonFieldsFilter> {
        val registration = org.springframework.boot.web.servlet.FilterRegistrationBean(jsonFieldsFilter)
        registration.order = Int.MIN_VALUE
        return registration
    }
}
