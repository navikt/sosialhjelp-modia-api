package no.nav.sosialhjelp.modia.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*




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
        val enhetsnr: String,
        val navn: String,
        val kontorinfo: String,
        val norgUrl: String,
)

data class LoginResponse(
        val melding: String,
)
