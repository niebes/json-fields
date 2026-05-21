package org.zalando.guild.api.json.fields.webmvc

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class Profile(val bio: String, val age: Int)
data class Sample(val id: Int, val name: String, val profile: Profile, val email: String)

@RestController
class SampleController {

    @GetMapping("/api/sample")
    fun sample(): Sample = Sample(
        id = 1,
        name = "test",
        profile = Profile(bio = "hello", age = 30),
        email = "test@example.com"
    )
}
