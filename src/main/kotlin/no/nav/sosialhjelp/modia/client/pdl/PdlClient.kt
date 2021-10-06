package no.nav.sosialhjelp.modia.client.pdl

import no.nav.sosialhjelp.modia.client.sts.STSClient
import no.nav.sosialhjelp.modia.common.PdlException
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CONSUMER_TOKEN
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_TEMA
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.TEMA_KOM
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.getCallId
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

interface PdlClient {
    fun hentPerson(ident: String): PdlHentPerson?
    fun ping()
}

@Profile("!local")
@Component
class PdlClientImpl(
    private val pdlWebClient: WebClient,
    private val stsClient: STSClient
) : PdlClient {

    override fun hentPerson(ident: String): PdlHentPerson? {
        val query = getResourceAsString("/pdl/hentPerson.graphql")

        val pdlPersonResponse = pdlWebClient.post()
            .headers { it.addAll(headers()) }
            .bodyValue(PdlRequest(query, Variables(ident)))
            .retrieve()
            .bodyToMono<PdlPersonResponse>()
            .onErrorMap(WebClientResponseException::class.java) {
                log.error("PDL - ${it.rawStatusCode} ${it.statusText} feil ved henting av navn fra PDL", it)
                PdlException(it.message)
            }
            .block()

        checkForPdlApiErrors(pdlPersonResponse)

        return pdlPersonResponse?.data
    }

    override fun ping() {
        pdlWebClient.options()
            .retrieve()
            .bodyToMono<String>()
            .doOnError { e ->
                log.error("PDL - ping feilet", e)
            }
            .block()
    }

    private fun getResourceAsString(path: String) = this.javaClass.getResource(path)?.readText()?.replace("[\n\r]", "")
        ?: throw RuntimeException("Feil ved lesing av graphql-spørring fra fil")

    private fun headers(): HttpHeaders {
        val stsToken: String = stsClient.token()

        val headers = HttpHeaders()
        headers.contentType = APPLICATION_JSON
        headers.set(HEADER_CALL_ID, getCallId())
        headers.set(HEADER_CONSUMER_TOKEN, BEARER + stsToken)
        headers.set(AUTHORIZATION, BEARER + stsToken)
        headers.set(HEADER_TEMA, TEMA_KOM)
        return headers
    }

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

    override fun hentPerson(ident: String): PdlHentPerson? {
        return pdlHentPersonMap.getOrElse(
            ident
        ) {
            val default = defaultPdlHentPerson()
            pdlHentPersonMap[ident] = default
            default
        }
    }

    private fun defaultPdlHentPerson(): PdlHentPerson {
        return PdlHentPerson(
            PdlPerson(
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
