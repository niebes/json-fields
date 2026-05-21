package org.zalando.guild.api.json.fields.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.server.WebFilter
import org.zalando.guild.api.json.fields.jackson.JsonFieldsModule

@SpringBootApplication
class Application {

    private val jsonFieldsWebFilter = JsonFieldsWebFilter()

    @Bean
    fun jsonFieldsWebFilter(): WebFilter = jsonFieldsWebFilter

    @Bean
    fun jsonFieldsModule(): JsonFieldsModule =
        JsonFieldsModule.createJsonFieldsModule(jsonFieldsWebFilter)
}
