package net.niebes.jsonfields.webflux

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = ["spring.json-fields.enabled=false"])
class JsonFieldsDisabledTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var context: ApplicationContext

    @Test
    fun autoConfigurationDisabled_noFilterBean() {
        assertTrue(context.getBeansOfType(JsonFieldsWebFilter::class.java).isEmpty())
    }

    @Test
    fun autoConfigurationDisabled_fieldsParamIgnored() {
        val body = webTestClient.get().uri("/api/sample?fields=(id)")
            .exchange()
            .expectStatus().isOk
            .expectBody<String>()
            .returnResult()
            .responseBody!!
        assertTrue(body.contains("\"name\""))
        assertTrue(body.contains("\"email\""))
        assertTrue(body.contains("\"profile\""))
    }
}
