package net.niebes.jsonfields.webflux

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

data class Profile(val bio: String, val age: Int)
data class Sample(val id: Int, val name: String, val profile: Profile, val email: String)

@RestController
class SampleController {

    @GetMapping("/api/sample")
    fun sample(): Mono<Sample> = Mono.just(
        Sample(
            id = 1,
            name = "test",
            profile = Profile(bio = "hello", age = 30),
            email = "test@example.com"
        )
    )
}
