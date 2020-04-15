package no.nav.sbl.sosialhjelpmodiaapi.logging

import no.nav.abac.xacml.NavAttributter
import no.nav.sbl.sosialhjelpmodiaapi.abac.Decision
import no.nav.sbl.sosialhjelpmodiaapi.abac.Request
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!(mock | local)")
@Component
class AuditLogger {

    private val log = LoggerFactory.getLogger("auditLogger")

    fun log(navIdent: String, fiksRequestId: String, tjenesteFunctionName: String) {
        log.info("BrukerId=\"${getUserIdFromToken()}\" - metode=$tjenesteFunctionName")
    }

    fun logAbac(request: Request, decision: Decision) {
        log.info("BrukerId=\"${getUserIdFromToken()}\" ber om tilgang til fnr=\"${request.fnr}\" i domene=\"sosialhjelp\". Tilgang gis med decision=\"$decision\"")
    }

    private val Request.fnr: String
        get() {
            return resource?.attributes?.first { it.attributeId == NavAttributter.RESOURCE_FELLES_PERSON_FNR }?.value!!
        }
}