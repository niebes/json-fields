package net.niebes.jsonfields.webmvc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.getForEntity
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(properties = ["spring.json-fields.enabled=false"])
class JsonFieldsDisabledTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var context: ApplicationContext

    @Test
    fun autoConfigurationDisabled_noFilterBean() {
        assertTrue(context.getBeansOfType(JsonFieldsFilter::class.java).isEmpty())
    }

    @Test
    fun autoConfigurationDisabled_fieldsParamIgnored() {
        val response = restTemplate.getForEntity<String>("/api/sample?fields=(id)")
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertTrue(body.contains("\"name\""))
        assertTrue(body.contains("\"email\""))
        assertTrue(body.contains("\"profile\""))
    }
}
