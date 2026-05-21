package org.zalando.guild.api.json.fields.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.servlet.Filter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.zalando.guild.api.json.fields.jackson.JsonFieldsModule
import org.zalando.guild.api.json.fields.jackson.servlet.JsonFieldsFilter

@SpringBootApplication
class Application {

    private val jsonFieldsFilter = JsonFieldsFilter()

    @Bean
    fun jsonFieldsFilter(): Filter = jsonFieldsFilter

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .apply {
            registerModule(JsonFieldsModule.createJsonFieldsModule(jsonFieldsFilter))
        }
}
