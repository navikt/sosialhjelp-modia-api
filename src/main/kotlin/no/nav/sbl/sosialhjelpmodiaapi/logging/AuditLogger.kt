package no.nav.sbl.sosialhjelpmodiaapi.logging

import no.nav.abac.xacml.NavAttributter.ADVICEOROBLIGATION_CAUSE
import no.nav.abac.xacml.NavAttributter.ADVICEOROBLIGATION_DENY_POLICY
import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacResponse
import no.nav.sbl.sosialhjelpmodiaapi.abac.Decision
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.Abac
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.CommonEventFormat
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.Extension
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.Fiks
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.Headers
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.Log
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.Severity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Profile("!(mock | local)")
@Component
class AuditLogger {

    companion object {
        // TODO: burde være private, men er public for å få testet klassen (https://github.com/mockk/mockk/issues/147)
        val auditlog: Logger = LoggerFactory.getLogger("auditLogger")
    }

    fun report(values: Map<String, Any>) {
        val cef = createCef(values)
        if (cef.headers.severity == Severity.WARN) {
            auditlog.warn(cef.toString())
        } else {
            auditlog.info(cef.toString())
        }
    }

    private fun createCef(values: Map<String, Any>): CommonEventFormat {
        val extension = Extension(
                navIdent = values[NAVIDENT] as String,
                brukerFnr = values[BRUKER_FNR] as String,
                callId = values[CALL_ID] as String,
                consumerId = values[CONSUMER_ID] as String,
                url = values[URL] as String,
                httpMethod = values[HTTP_METHOD] as HttpMethod,
                abac = populateAbacIfPresent(values),
                fiks = populateFiksIfPresent(values)
        )

        return CommonEventFormat(
                headers = Headers(
                        log = getLog(values, extension),
                        resource = values.getOrDefault(RESOURCE, "") as String,
                        title = values.getOrDefault(TITLE, "") as String,
                        severity = getSeverity(values, extension)
                ),
                extension = extension
        )
    }

    private fun populateAbacIfPresent(values: Map<String, Any>): Abac? {
        return if (values.containsKey(ABAC_RESPONSE)) {
            val abacResponse = values[ABAC_RESPONSE] as AbacResponse
            Abac(
                    decision = abacResponse.decision,
                    denyPolicy = getAdvice(abacResponse, ADVICEOROBLIGATION_DENY_POLICY),
                    denyCause = getAdvice(abacResponse, ADVICEOROBLIGATION_CAUSE)
            )
        } else {
            null
        }
    }

    private fun getAdvice(abacResponse: AbacResponse, abacAttributeConstant: String): String {
        return abacResponse.associatedAdvice
                ?.flatMap { advice ->
                    advice.attributeAssignment
                            ?.filter { it.attributeId.equals(abacAttributeConstant, true) }
                            .orEmpty()
                }
                ?.joinToString(separator = ",") { it.value } // potensielt flere denypolicies eller denycauses?
                .orEmpty()
    }

    private fun populateFiksIfPresent(values: Map<String, Any>): Fiks? {
        return if (values.containsKey(FIKS_REQUEST_ID)) {
            Fiks(values[FIKS_REQUEST_ID] as String)
        } else {
            null
        }
    }

    private fun getLog(values: Map<String, Any>, extension: Extension): Log {
        return if (extension.abac != null && extension.abac.decision === Decision.Deny) {
            Log.ABAC
        } else {
            (values[LOG] ?: Log.AUDIT) as Log
        }
    }

    private fun getSeverity(values: Map<String, Any>, extension: Extension): Severity {
        return if (extension.abac != null && extension.abac.decision == Decision.Deny) {
            Severity.WARN
        } else {
            (values[SEVERITY] ?: Severity.INFO) as Severity
        }
    }
}