package no.nav.sosialhjelp.modia.logging

const val TITLE = "title"
const val RESOURCE = "resource"

const val CALL_ID = "callId"
const val CONSUMER_ID = "consumerId"
const val NAVIDENT = "subject"
const val BRUKER_FNR = "brukerFnr"
const val URL = "url"
const val HTTP_METHOD = "httpMethod"

const val ABAC_RESPONSE = "abacResponse"
const val FIKS_REQUEST_ID = "fiksRequestId"

const val SEVERITY = "severity"
const val LOG = "log"

const val SPORINGSLOGG = "sporingslogg"

const val TITLE_ABAC = "Abac"
const val TITLE_FIKS = "Fiks"

const val RESOURCE_AUDIT_ACCESS = "audit:access"

enum class Access() {
    PERMIT,
    DENY,
}
