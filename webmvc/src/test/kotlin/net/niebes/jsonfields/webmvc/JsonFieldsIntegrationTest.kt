package net.niebes.jsonfields.webmvc

import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.getForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class JsonFieldsIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val mapper = ObjectMapper()

    private fun get(fields: String? = null): JsonNode {
        val url = if (fields != null) "/api/sample?fields=$fields" else "/api/sample"
        val response = restTemplate.getForEntity<String>(url)
        assertEquals(HttpStatus.OK, response.statusCode)
        return mapper.readTree(response.body)
    }

    @Test
    fun noFieldsParam_returnsFullResponse() {
        val json = get()
        assertEquals(1, json["id"].intValue())
        assertEquals("test", json["name"].textValue())
        assertEquals("test@example.com", json["email"].textValue())
        assertEquals("hello", json["profile"]["bio"].textValue())
        assertEquals(30, json["profile"]["age"].intValue())
    }

    @Test
    fun whitelistTopLevel_returnsOnlySelectedFields() {
        val json = get("(id,name)")
        assertEquals(1, json["id"].intValue())
        assertEquals("test", json["name"].textValue())
        assertFalse(json.has("email"))
        assertFalse(json.has("profile"))
    }

    @Test
    fun nestedWhitelist_returnsNestedSelection() {
        val json = get("(id,profile(bio))")
        assertEquals(1, json["id"].intValue())
        assertFalse(json.has("name"))
        assertFalse(json.has("email"))
        assertNotNull(json["profile"])
        assertEquals("hello", json["profile"]["bio"].textValue())
        assertFalse(json["profile"].has("age"))
    }

    @Test
    fun blacklist_excludesSelectedFields() {
        val json = get("!(email)")
        assertEquals(1, json["id"].intValue())
        assertEquals("test", json["name"].textValue())
        assertNotNull(json["profile"])
        assertFalse(json.has("email"))
    }

    @Test
    fun nestedBlacklist_excludesSpecificNestedField() {
        val json = get("(id,profile!(age))")
        assertEquals(1, json["id"].intValue())
        assertFalse(json.has("name"))
        assertFalse(json.has("email"))
        assertNotNull(json["profile"])
        assertEquals("hello", json["profile"]["bio"].textValue())
        assertFalse(json["profile"].has("age"))
    }

    @Test
    fun nestedBlacklist_parentFieldIsPreserved() {
        val json = get("(profile!(age))")
        assertNotNull(json["profile"])
        assertEquals("hello", json["profile"]["bio"].textValue())
        assertFalse(json["profile"].has("age"))
    }

    @Test
    fun blacklistPreservesNestedFields() {
        val json = get("!(name)")
        assertEquals(1, json["id"].intValue())
        assertFalse(json.has("name"))
        assertEquals("test@example.com", json["email"].textValue())
        assertNotNull(json["profile"])
        assertEquals("hello", json["profile"]["bio"].textValue())
        assertEquals(30, json["profile"]["age"].intValue())
    }
}
