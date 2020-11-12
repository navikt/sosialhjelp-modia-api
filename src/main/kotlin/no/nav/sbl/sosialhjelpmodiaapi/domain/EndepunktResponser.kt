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
        val status: SaksStatus?,
        val vedtak: List<VedtakResponse>?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val datoOpprettet: LocalDate,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val datoAvsluttet: LocalDate?
)

data class VedtakResponse(
        @JsonFormat(pattern = "yyyy-MM-dd")
        val vedtakDato: LocalDate,
        val utfall: UtfallVedtak?
)

data class HendelseResponse(
        val tittel: String,
        val tidspunkt: String,
        val beskrivelse: String?,
        val filbeskrivelse: String?
)

data class OppgaveResponse(
        val dokumenttype: String,
        val tilleggsinformasjon: String?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val innsendelsesfrist: LocalDate?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val vedleggDatoLagtTil: LocalDate?,
        val antallVedlegg: Int,
        val erFraInnsyn: Boolean
)

data class UtbetalingerResponse(
        val tittel: String?,
        val belop: Double,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val utbetalingEllerForfallDigisosSoker: LocalDate?,
        val status: UtbetalingsStatus,
        val fiksDigisosId: String,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val fom: LocalDate?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val tom: LocalDate?,
        val mottaker: String?,
        val annenMottaker: Boolean,
        val kontonummer: String?,
        val utbetalingsmetode: String?,
        val harVilkar: Boolean,
        val navKontor: NavKontor?
)

data class VedleggResponse(
        val type: String,
        val tilleggsinfo: String?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val innsendelsesfrist: LocalDateTime?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val datoLagtTil: LocalDateTime?,
        val antallVedlegg: Int
)

data class SoknadNoekkelinfoResponse(
        val tittel: String,
        val status: SoknadsStatus,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val sistOppdatert: LocalDate,
        val saksId: String?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val sendtEllerMottattTidspunkt: LocalDate,
        val navKontor: NavKontor?,
        val kommunenavn: String,
        val videresendtHistorikk: List<VideresendtInfo>?,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val tidspunktForelopigSvar: LocalDateTime?
)

data class VideresendtInfo(
        val type: SendingType,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val tidspunkt: LocalDate,
        val navKontor: NavKontor
)

data class NavKontor(
        val enhetsNavn: String,
        val enhetsNr: String
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
        val status: SoknadsStatus,
        val harNyeOppgaver: Boolean,
        val harVilkar: Boolean
)

data class PersoninfoResponse(
        val sammensattNavn: String?,
        val alder: Int?,
        val kjoenn: String?,
        val tlfnr: String?
)

data class Ident(
        val fnr: String
)

data class KommuneResponse(
        val erInnsynDeaktivert: Boolean,
        val erInnsynMidlertidigDeaktivert: Boolean,
        val erInnsendingEttersendelseDeaktivert: Boolean,
        val erInnsendingEttersendelseMidlertidigDeaktivert: Boolean,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val tidspunkt: Date,
        val harNksTilgang: Boolean,
        val behandlingsansvarlig: String?
)

data class KontorinfoResponse(
        val navn: String,
        val kontorinfo: String,
        val norgUrl: String,
)

data class NavKontorResponse(
        val enhetsnr: String,
        val navn: String,
        val sosialeTjenester: String
)
