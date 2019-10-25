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
        val fiksDigisosId: String,
        val utbetalinger: MutableList<UtbetalingerManedResponse>
)

data class UtbetalingerManedResponse(
        val tittel: String,
        val utbetalinger: MutableList<UtbetalingResponse>,
        val belop: Double
)

data class UtbetalingResponse(
        val tittel: String?,
        val belop: Double,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val utbetalingsdato: LocalDate?,
        val vilkar: MutableList<VilkarResponse>
)

data class VilkarResponse(
        val beskrivelse: String?,
        val oppfylt: Boolean
)

data class VedleggResponse(
        val filnavn: String,
        val storrelse: Long,
        val url: String,
        val type: String,
        val tilleggsinfo: String?,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
        val datoLagtTil: LocalDateTime
)

data class VedleggOpplastingResponse(
        val filnavn: String?,
        val status: String
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