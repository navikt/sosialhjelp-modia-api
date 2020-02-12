package no.nav.sbl.sosialhjelpmodiaapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

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
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
        val innsendelsesfrist: LocalDateTime?,
        val dokumenttype: String,
        val tilleggsinformasjon: String?,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
        val datoLagtTil: LocalDateTime,
        val antallVedlegg: Int,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
        val vedleggDatoLagtTil: LocalDateTime?,
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
        val fiksDigisosId: String,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val fom: LocalDate?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val tom: LocalDate?,
        val mottaker: String?,
        val harVilkar: Boolean
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

data class SoknadsInfoResponse(
        val status: SoknadsStatus,
        val tittel: String,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val sistOppdatert: LocalDateTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val soknadSendtMottattTidspunkt: LocalDateTime,
        val navKontorSoknad: String?,
        val navKontorTildelt: String?,
        val tidspunktForelopigSvar: LocalDateTime?,
        val navKontorSaksbehandlingstid: String?
)

data class SaksListeResponse(
        val fiksDigisosId: String,
        val soknadTittel: String,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val sistOppdatert: Date,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val sendt: Date?,
        val kilde: String
)

data class SaksDetaljerResponse(
        val fiksDigisosId: String,
        val soknadTittel: String,
        val status: String,
        val antallNyeOppgaver: Int?
)