package no.nav.sbl.sosialhjelpmodiaapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class SoknadsStatusResponse(
        val status: SoknadsStatus
)

data class SaksStatusResponse(
        val tittel: String,
        val status: SaksStatus?
)

data class HendelseResponse(
        val tidspunkt: String,
        val beskrivelse: String
)

data class OppgaveResponse(
        val innsendelsesfrist: String?,
        val dokumenttype: String,
        val tilleggsinformasjon: String?,
        val erFraInnsyn: Boolean
)

data class UtbetalingerResponse(
        val ar: Int,
        val maned: String,
        val sum: Double,
        val utbetalinger: List<ManedUtbetaling>
)

data class ManedUtbetaling(
        val tittel: String?,
        val belop: Double,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val utbetalingsdato: LocalDate?,
        val status: String,
        val fiksDigisosId: String
)

data class SakResponse(
        val fiksDigisosId: String,
        val soknadTittel: String,
        val status: String,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
        val sistOppdatert: LocalDateTime,
        val antallNyeOppgaver: Int?,
        val kilde: String
)

data class VedleggResponse(
        val type: String,
        val tilleggsinfo: String?,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val innsendelsesfrist: LocalDateTime?,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val datoLagtTil: LocalDateTime?
)