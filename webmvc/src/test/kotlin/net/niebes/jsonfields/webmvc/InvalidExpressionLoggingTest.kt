package net.niebes.jsonfields.webmvc

import tools.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.getForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ExtendWith(OutputCaptureExtension::class)
class InvalidExpressionLoggingTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val mapper = ObjectMapper()

    @Test
    fun invalidExpression_logsWarningAndReturnsEmptyJson(output: CapturedOutput) {
        val response = restTemplate.getForEntity<String>("/api/sample?fields=invalid")
        assertEquals(HttpStatus.OK, response.statusCode)
        val json = mapper.readTree(response.body)
        assertEquals(0, json.size())
        assertTrue(output.out.contains("Invalid fields expression") || output.all.contains("Invalid fields expression"))
    }
}
