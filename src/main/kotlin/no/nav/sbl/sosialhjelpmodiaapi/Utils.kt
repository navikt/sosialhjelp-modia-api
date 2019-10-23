package no.nav.sbl.sosialhjelpmodiaapi

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonDokumentlagerFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonSvarUtFilreferanse
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.full.companionObject

const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"
const val NAIS_NAMESPACE = "NAIS_NAMESPACE"

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

fun hentUrlFraFilreferanse(clientProperties: ClientProperties, filreferanse: JsonFilreferanse): String {
    return when (filreferanse) {
        is JsonDokumentlagerFilreferanse -> clientProperties.fiksDokumentlagerEndpointUrl + "/dokumentlager/nedlasting/${filreferanse.id}"
        is JsonSvarUtFilreferanse -> clientProperties.fiksSvarUtEndpointUrl + "/forsendelse/${filreferanse.id}/${filreferanse.nr}"
        else -> throw RuntimeException("Noe uventet feilet. JsonFilreferanse på annet format enn JsonDokumentlagerFilreferanse og JsonSvarUtFilreferanse")
    }
}

fun hentDokumentlagerUrl(clientProperties: ClientProperties, dokumentlagerId: String): String {
    return clientProperties.fiksDokumentlagerEndpointUrl + "/dokumentlager/nedlasting/${dokumentlagerId}"
}

fun toLocalDateTime(hendelsetidspunkt: String): LocalDateTime {
    return ZonedDateTime.parse(hendelsetidspunkt, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()
}

fun unixToLocalDateTime(tidspunkt: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(tidspunkt), ZoneId.of("Europe/Oslo"))
}

fun enumNameToLowercase(string: String): String {
    return string.toLowerCase().replace('_', ' ')
}

fun lagNavEksternRefId(digisosSak: DigisosSak): String {
    val previousId: Long = digisosSak.ettersendtInfoNAV?.ettersendelser?.map { it.navEksternRefId.toLowerCase().toLong(36) }?.max()
            ?: digisosSak.originalSoknadNAV?.navEksternRefId?.toLowerCase()?.plus("000")?.toLong(36)
            ?: return UUID.randomUUID().toString()

    return (previousId + 1L).toString(36).toUpperCase().replace("O", "o").replace("I", "i")
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }
}

// unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}

fun isRunningInProd(): Boolean {
    return System.getenv(NAIS_CLUSTER_NAME) == "prod-sbs" && System.getenv(NAIS_NAMESPACE) == "default"
}