package no.nav.sbl.sosialhjelpmodiaapi.logging.cef

import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Decision
import org.springframework.http.HttpMethod

data class CommonEventFormat(
    val headers: Headers,
    val extension: Extension
) {
    override fun toString(): String {
        // CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
        return "$headers$extension"
    }
}

data class Headers(
    val log: String,
    val resource: String,
    val title: String,
    val severity: Severity
) {
    override fun toString(): String {
        return "CEF:0|sosialhjelp-modia-api|$log|1.0|$resource|$title|$severity|"
    }
}

enum class Severity {
    INFO, WARN
}

data class Extension(
    val navIdent: String, // suid - sourceUserId - Identifies the source user by ID. This is the user associated with the source of the event.
    val brukerFnr: String, // duid - destinationUserId - Identifies the destination user by ID.
    val callId: String, // sproc - sourceProcessName - The name of the event’s source process.
    val consumerId: String, // dproc - destinationProcessName - The name of the event’s destination process.
    val url: String, // request - requestUrl - In the case of an HTTP request, this field contains the URL accessed. The URL should contain the protocol as well.
    val httpMethod: HttpMethod, // requestMethod - requestMethod - The method used to access a URL.
    val abac: Abac?,
    val fiks: Fiks?
) {
    override fun toString(): String {
        return "end=${System.currentTimeMillis()} suid=$navIdent duid=$brukerFnr sproc=$callId dproc=$consumerId request=$url requestMethod=${httpMethod.name}"
            .plus(abac?.toString() ?: "")
            .plus(fiks?.toString() ?: "")
    }
}

data class Abac(
    val decision: Decision,
    val denyPolicy: String?,
    val denyCause: String?,
    val denyRule: String?
) {
    override fun toString(): String {
        return " flexString1=$decision flexString1Label=Decision ${denyPart()}"
    }

    private fun denyPart(): String? {
        return if (denyCause != null || denyPolicy != null || denyRule != null) {
            " flexString2=[cause=$denyCause,policy=$denyPolicy,rule=$denyRule] flexString2Label=abac_deny_response"
        } else {
            null
        }
    }
}

data class Fiks(
    val fiksRequestId: String
) {
    override fun toString(): String {
        return " cs5=$fiksRequestId cs5Label=fiksRequestId"
    }
}
