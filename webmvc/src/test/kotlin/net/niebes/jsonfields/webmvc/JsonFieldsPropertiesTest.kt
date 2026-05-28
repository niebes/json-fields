package net.niebes.jsonfields.webmvc

import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.getForEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(properties = ["spring.json-fields.parameter-name=select"])
class JsonFieldsPropertiesTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val mapper = ObjectMapper()

    private fun get(url: String): JsonNode {
        val response = restTemplate.getForEntity<String>(url)
        assertEquals(HttpStatus.OK, response.statusCode)
        return mapper.readTree(response.body)
    }

    @Test
    fun customParameterName_filtersWithSelectParam() {
        val json = get("/api/sample?select=(id,name)")
        assertEquals(1, json["id"].intValue())
        assertEquals("test", json["name"].textValue())
        assertFalse(json.has("email"))
        assertFalse(json.has("profile"))
    }

    @Test
    fun defaultFieldsParam_ignoredWithCustomName() {
        val json = get("/api/sample?fields=(id)")
        assertEquals(1, json["id"].intValue())
        assertEquals("test", json["name"].textValue())
        assertEquals("test@example.com", json["email"].textValue())
    }
}
