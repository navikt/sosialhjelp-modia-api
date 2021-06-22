package no.nav.sosialhjelp.modia.client.abac

import no.nav.abac.xacml.NavAttributter
import no.nav.sosialhjelp.modia.common.AbacException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.AuditService
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

interface AbacClient {

    fun sjekkTilgang(request: Request): AbacResponse
}

@Profile("!(mock | local)")
@Component
class AbacClientImpl(
    private val clientProperties: ClientProperties,
    private val abacWebClient: WebClient,
    private val auditService: AuditService
) : AbacClient {

    override fun sjekkTilgang(request: Request): AbacResponse {
        val postingString = XacmlMapper.mapRequestToEntity(XacmlRequest(request))

        val response: String = abacWebClient.post()
            .bodyValue(postingString)
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap { e ->
                when (e) {
                    is WebClientResponseException -> log.error("Abac - noe feilet. Status: ${e.statusCode}, message: ${e.message}.", e)
                    else -> log.error("Abac - noe feilet.", e)
                }
                AbacException("Noe feilet ved kall til Abac.", e)
            }
            .block()!!

        val xacmlResponse = XacmlMapper.mapRawResponse(response)
        val abacResponse: AbacResponse = xacmlResponse.response[0]

        auditService.reportAbac(request.fnr, clientProperties.abacPdpEndpointUrl, HttpMethod.POST, abacResponse)

        return abacResponse
    }

    companion object {
        private val log by logger()

        private val Request.fnr: String
            get() = resource?.attributes?.first { it.attributeId == NavAttributter.RESOURCE_FELLES_PERSON_FNR }?.value!!
    }
}
