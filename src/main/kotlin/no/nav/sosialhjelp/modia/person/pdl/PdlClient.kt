package no.nav.sosialhjelp.modia.person.pdl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.client.unproxiedHttpClient
import no.nav.sosialhjelp.modia.app.exceptions.PdlException
import no.nav.sosialhjelp.modia.app.mdc.MDCUtils.getCallId
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.tilgang.azure.AzuredingsService
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_TEMA
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.TEMA_KOM
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono

interface PdlClient {
    fun hentPerson(ident: String, veilederToken: String): PdlHentPerson?
    fun ping()
}

@Profile("!local")
@Component
class PdlClientImpl(
    webClientBuilder: WebClient.Builder,
    private val azuredingsService: AzuredingsService,
    private val clientProperties: ClientProperties,
) : PdlClient {

    private val pdlWebClient = webClientBuilder
        .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    override fun hentPerson(ident: String, veilederToken: String): PdlHentPerson? {
        val query = getResourceAsString("/pdl/hentPerson.graphql")

        val pdlPersonResponse = try {
            runBlocking(Dispatchers.IO) {
                val azureAdToken = azuredingsService.exchangeToken(veilederToken, clientProperties.pdlScope)

                pdlWebClient.post()
                    .uri(clientProperties.pdlEndpointUrl)
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + azureAdToken)
                    .header(HEADER_CALL_ID, getCallId())
                    .header(HEADER_TEMA, TEMA_KOM)
                    .bodyValue(PdlRequest(query, Variables(ident)))
                    .retrieve()
                    .awaitBody<PdlPersonResponse>()
            }
        } catch (e: WebClientResponseException) {
            log.error("PDL - ${e.statusCode} ${e.statusText} feil ved henting av navn fra PDL", e)
            throw PdlException(e.message)
        }

        checkForPdlApiErrors(pdlPersonResponse)

        return pdlPersonResponse.data
    }

    override fun ping() {
        pdlWebClient.options()
            .uri(clientProperties.pdlEndpointUrl)
            .retrieve()
            .bodyToMono<String>()
            .doOnError { e ->
                log.error("PDL - ping feilet", e)
            }
            .block()
    }

    @Suppress("SameParameterValue")
    private fun getResourceAsString(path: String) = this.javaClass.getResource(path)?.readText()?.replace("[\n\r]", "")
        ?: throw RuntimeException("Feil ved lesing av graphql-spørring fra fil")

    private fun checkForPdlApiErrors(response: PdlPersonResponse?) {
        response?.errors?.let { handleErrors(it) }
    }

    private fun handleErrors(errors: List<PdlError>) {
        val errorString: String = errors
            .map { it.message + "(feilkode: " + it.extensions.code + ")" }
            .joinToString(prefix = "Error i respons fra pdl-api: ") { it }
        throw PdlException(errorString)
    }

    companion object {
        private val log by logger()
    }
}

@Component
@Profile("local")
class PdlClientMock : PdlClient {

    private val pdlHentPersonMap = mutableMapOf<String, PdlHentPerson>()

    override fun hentPerson(ident: String, veilederToken: String): PdlHentPerson? {
        return pdlHentPersonMap.getOrElse(
            ident
        ) {
            val default = defaultPdlHentPerson()
            pdlHentPersonMap[ident] = default
            default
        }
    }

    private fun defaultPdlHentPerson(): PdlHentPerson {
        @Suppress("SameParameterValue")
        return PdlHentPerson(
            PdlPerson(
                listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                listOf(PdlPersonNavn("Bruce", "mock", "Banner")),
                listOf(PdlKjoenn(Kjoenn.KVINNE)),
                listOf(PdlFoedselsdato("2000-01-01")),
                listOf(PdlTelefonnummer("+47", "12345678", 1))
            )
        )
    }

    override fun ping() {
    }
}
