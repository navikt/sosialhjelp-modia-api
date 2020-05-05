package no.nav.sbl.sosialhjelpmodiaapi.mock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.sosialhjelpmodiaapi.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.DigisosApiWrapper
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Profile("mock")
@Unprotected
@RestController
@RequestMapping("/api/v1/mock/innsyn")
class MockController(
        private val fiksClientMock: FiksClientMock,
        private val innsynService: InnsynService
) {

    private val mapper = jacksonObjectMapper()
    private val sosialhjelpMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    @PostMapping("/{soknadId}", consumes = ["application/json;charset=UTF-8"], produces = ["application/json;charset=UTF-8"])
    fun postJsonDigisosSoker(@PathVariable soknadId: String, @RequestBody digisosApiWrapper: DigisosApiWrapper) {
        log.info("soknadId: $soknadId, jsonDigisosSoker: $digisosApiWrapper")
        val digisosSak = fiksClientMock.hentDigisosSak(soknadId, "Token")

        val jsonNode = mapper.convertValue(digisosApiWrapper.sak.soker, JsonNode::class.java)
        val jsonDigisosSoker = sosialhjelpMapper.convertValue(jsonNode, JsonDigisosSoker::class.java)
        digisosSak.digisosSoker?.metadata?.let { fiksClientMock.postDokument(it, jsonDigisosSoker) }
    }

    @GetMapping("/{soknadId}", produces = ["application/json;charset=UTF-8"])
    fun getInnsynForSoknad(@PathVariable soknadId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<JsonDigisosSoker> {
        val digisosSak = fiksClientMock.hentDigisosSak(soknadId, token)
        val jsonDigisosSoker = innsynService.hentJsonDigisosSoker(soknadId, digisosSak.digisosSoker?.metadata, token)
        return ResponseEntity.ok(jsonDigisosSoker!!)
    }

    companion object {
        private val log by logger()
    }
}
