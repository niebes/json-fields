package org.zalando.guild.api.json.fields.springboot

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
    fun jsonFieldsModule(): JsonFieldsModule =
        JsonFieldsModule.createJsonFieldsModule(jsonFieldsFilter)
}
