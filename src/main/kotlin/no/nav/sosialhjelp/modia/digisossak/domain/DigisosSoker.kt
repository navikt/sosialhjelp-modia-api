package no.nav.sosialhjelp.modia.digisossak.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class InternalDigisosSoker(
    var referanse: String?,
    var status: SoknadsStatus,
    var saker: MutableList<Sak>,
    var utbetalinger: MutableList<Utbetaling>,
    var forvaltningsbrev: MutableList<Forvaltningsbrev>,
    var soknadsmottaker: Soknadsmottaker?,
    var navKontorHistorikk: MutableList<NavKontorInformasjon>,
    var oppgaver: MutableList<Oppgave>,
    var dokumentasjonkrav: MutableList<Dokumentasjonkrav>,
    var vilkar: MutableList<Vilkar>,
    var historikk: MutableList<Hendelse>,
    var forelopigSvar: ForelopigSvar?
) {
    constructor() : this(null, SoknadsStatus.SENDT, mutableListOf(), mutableListOf(), mutableListOf(), null, mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), null)
}

data class Forvaltningsbrev(
    var referanse: String,
    var tittel: String
)

data class Soknadsmottaker(
    val navEnhetsnummer: String,
    val navEnhetsnavn: String
)

data class Oppgave(
    var tittel: String,
    var tilleggsinfo: String?,
    var innsendelsesfrist: LocalDateTime?,
    var tidspunktForKrav: LocalDateTime,
    var erFraInnsyn: Boolean
)

data class Sak(
    var referanse: String,
    var saksStatus: SaksStatus?,
    var tittel: String?,
    var vedtak: MutableList<Vedtak>,
    var utbetalinger: MutableList<Utbetaling>,
    var datoOpprettet: LocalDate
)

data class Vedtak(
    var utfall: UtfallVedtak?,
    var datoFattet: LocalDate
)

data class Utbetaling(
    var referanse: String,
    var status: UtbetalingsStatus,
    var belop: BigDecimal,
    var beskrivelse: String?,
    var forfallsDato: LocalDate?,
    var utbetalingsDato: LocalDate?,
    var fom: LocalDate?,
    var tom: LocalDate?,
    var mottaker: String?,
    var annenMottaker: Boolean,
    var kontonummer: String?,
    var utbetalingsmetode: String?,
    var vilkar: MutableList<Vilkar>,
    var dokumentasjonkrav: MutableList<Dokumentasjonkrav>,
    var datoHendelse: LocalDateTime
)

data class Vilkar(
    var referanse: String,
    var beskrivelse: String?,
    var status: OppgaveStatus,
    var datoLagtTil: LocalDateTime,
    var datoSistEndret: LocalDateTime,
    var utbetalingsReferanse: List<String>,
    var saksreferanse: String?
)

data class Dokumentasjonkrav(
    var dokumentasjonkravId: String,
    var tittel: String?,
    var beskrivelse: String?,
    var status: OppgaveStatus?,
    var utbetalingsReferanse: List<String>?,
    var datoLagtTil: LocalDateTime?,
    var frist: LocalDateTime?,
    val saksreferanse: String?
) {
    fun isEmpty(): Boolean = tittel.isNullOrBlank() && beskrivelse.isNullOrBlank()
}

data class Hendelse(
    val tittel: String,
    val beskrivelse: String?,
    val tidspunkt: LocalDateTime,
    val filbeskrivelse: String? = null
)

data class ForelopigSvar(
    val hendelseTidspunkt: LocalDateTime
)

data class NavKontorInformasjon(
    val type: SendingType,
    val tidspunkt: LocalDateTime,
    val navEnhetsnummer: String,
    val navEnhetsnavn: String
)

enum class SendingType {
    SENDT, VIDERESENDT
}

enum class SoknadsStatus {
    SENDT, MOTTATT, UNDER_BEHANDLING, FERDIGBEHANDLET, BEHANDLES_IKKE
}

enum class SaksStatus {
    UNDER_BEHANDLING, IKKE_INNSYN, FERDIGBEHANDLET, BEHANDLES_IKKE, FEILREGISTRERT
}

enum class UtbetalingsStatus {
    PLANLAGT_UTBETALING, UTBETALT, STOPPET, ANNULLERT
}

enum class UtfallVedtak {
    INNVILGET, DELVIS_INNVILGET, AVSLATT, AVVIST
}

enum class OppgaveStatus {
    RELEVANT, ANNULLERT, OPPFYLT, IKKE_OPPFYLT, LEVERT_TIDLIGERE
}
