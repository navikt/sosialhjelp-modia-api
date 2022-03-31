package no.nav.sosialhjelp.modia.logging.cef

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
    // suid - sourceUserId - Identifies the source user by ID. This is the user associated with the source of the event.
    val navIdent: String,
    // duid - destinationUserId - Identifies the destination user by ID.
    val brukerFnr: String,
    // sproc - sourceProcessName - The name of the event’s source process.
    val callId: String,
    // dproc - destinationProcessName - The name of the event’s destination process.
    val consumerId: String,
    // request - requestUrl - In the case of an HTTP request, this field contains the URL accessed. The URL should contain the protocol as well.
    val url: String,
    // requestMethod - requestMethod - The method used to access a URL.
    val httpMethod: HttpMethod,
    val fiks: Fiks?
) {
    override fun toString(): String {
        return "end=${System.currentTimeMillis()} suid=$navIdent duid=$brukerFnr sproc=$callId dproc=$consumerId request=$url requestMethod=${httpMethod.name}"
            .plus(fiks?.toString() ?: "")
    }
}

data class Fiks(
    val fiksRequestId: String
) {
    override fun toString(): String {
        return " cs5=$fiksRequestId cs5Label=fiksRequestId"
    }
}
