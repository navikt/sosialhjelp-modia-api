package no.nav.sbl.sosialhjelpmodiaapi

import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.service.saksstatus.DEFAULT_TITTEL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.full.companionObject

private const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"
private const val NAIS_NAMESPACE = "NAIS_NAMESPACE"
const val SOKNAD_DEFAULT_TITTEL = "Søknad om økonomisk sosialhjelp"

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

fun String.toLocalDateTime(): LocalDateTime {
    return ZonedDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()
}

fun String.toLocalDate(): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)

fun unixToLocalDateTime(tidspunkt: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(tidspunkt), ZoneId.of("Europe/Oslo"))
}

fun unixTimestampToDate(tidspunkt: Long): Date {
    return Timestamp.valueOf(unixToLocalDateTime(tidspunkt))
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
    return System.getenv(NAIS_CLUSTER_NAME) == "prod-fss" && System.getenv(NAIS_NAMESPACE) == "default"
}

fun hentSoknadTittel(digisosSak: DigisosSak, model: InternalDigisosSoker): String {
    return when (digisosSak.digisosSoker) {
        null -> SOKNAD_DEFAULT_TITTEL
        else -> model.saker.joinToString { it.tittel ?: DEFAULT_TITTEL }
    }
}
