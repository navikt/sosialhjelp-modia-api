package no.nav.sosialhjelp.modia

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.SaksStatus
import no.nav.sosialhjelp.modia.digisossak.event.SAK_DEFAULT_TITTEL
import no.nav.sosialhjelp.modia.digisossak.event.SOKNAD_DEFAULT_TITTEL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.reflect.full.companionObject

fun String.toLocalDateTime(): LocalDateTime =
    ZonedDateTime
        .parse(this, DateTimeFormatter.ISO_DATE_TIME)
        .withZoneSameInstant(ZoneId.of("Europe/Oslo"))
        .toLocalDateTime()

fun String.toLocalDate(): LocalDate = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)

fun unixToLocalDateTime(tidspunkt: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(tidspunkt), ZoneId.of("Europe/Oslo"))

fun unixTimestampToDate(tidspunkt: Long): Date = Timestamp.valueOf(unixToLocalDateTime(tidspunkt))

fun <R : Any> R.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }

// unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> =
    ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject
            ?.java == ofClass
    } ?: ofClass

fun hentSoknadTittel(
    digisosSak: DigisosSak,
    model: InternalDigisosSoker,
): String =
    when (digisosSak.digisosSoker) {
        null -> {
            SOKNAD_DEFAULT_TITTEL
        }

        else -> {
            model.saker
                .filter { SaksStatus.FEILREGISTRERT != it.saksStatus }
                .joinToString { it.tittel ?: SAK_DEFAULT_TITTEL }
        }
    }

val String.maskerFnr: String
    get() {
        return this.replace(Regex("""\b[0-9]{11}\b"""), "[FNR]")
    }

/**
 * Executes the given function in parallel for each element in the iterable,
 * using coroutines with MDC context propagation.
 *
 * This is a synchronous function that blocks until all parallel operations complete.
 */
fun <A, B> Iterable<A>.flatMapParallel(f: (A) -> List<B>): List<B> =
    runBlocking(Dispatchers.IO + MDCContext()) {
        map { element ->
            async {
                f(element)
            }
        }.awaitAll().flatten()
    }
