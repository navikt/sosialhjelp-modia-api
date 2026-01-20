package no.nav.sosialhjelp.modia.login

import jakarta.servlet.http.HttpServletResponse
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.modia.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Unprotected
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"])
class LoginController(
    @param:Value($$"${NAIS_TOKEN_INTROSPECTION_ENDPOINT}")
    val introEndpoint: String,
    val webClient: WebClient,
) {
    val log by logger()

    @GetMapping("/login")
    fun login(
        @RequestHeader("Authorization", required = false)
        authHeader: String?,
        response: HttpServletResponse,
    ): ResponseEntity<LoginResponse> {
        val responseEntity =
            webClient
                .post()
                .uri(introEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    BodyInserters.fromFormData(
                        MultiValueMap.fromMultiValue(
                            mapOf(
                                "token" to
                                    listOf(
                                        authHeader?.removePrefix("Bearer ")
                                            ?: throw IllegalArgumentException("Manglende Authorization header"),
                                    ),
                                "identity_provider" to listOf("entra_id"),
                            ),
                        ),
                    ),
                ).retrieve()
                .bodyToMono<String>()
                .block()

        log.info(responseEntity.toString())
        return ResponseEntity.ok(LoginResponse("ok"))
    }

    data class LoginResponse(
        val melding: String,
    )
}
