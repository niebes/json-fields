package net.niebes.jsonfields.webmvc

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import net.niebes.jsonfields.jackson.JsonFieldsModule
import net.niebes.jsonfields.jackson.JsonFieldsProperties
import tools.jackson.databind.json.JsonMapper

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
    fun jsonFieldsModule(): JsonFieldsModule =
        JsonFieldsModule.createJsonFieldsModule()

    @Bean
    fun jsonFieldsMapperBuilderCustomizer(jsonFieldsFilter: JsonFieldsFilter): JsonMapperBuilderCustomizer =
        JsonMapperBuilderCustomizer { builder ->
            builder.filterProvider(JsonFieldsModule.createFilterProvider(jsonFieldsFilter))
        }

    @Bean
    fun jsonFieldsFilterRegistration(jsonFieldsFilter: JsonFieldsFilter): FilterRegistrationBean<JsonFieldsFilter> {
        val registration = FilterRegistrationBean(jsonFieldsFilter)
        registration.order = Int.MIN_VALUE
        return registration
    }
}
