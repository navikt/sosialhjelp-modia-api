package no.nav.sosialhjelp.modia.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


data class PersoninfoResponse(
        val sammensattNavn: String?,
        val alder: Int?,
        val kjoenn: String?,
        val tlfnr: String?
)

data class Ident(
        val fnr: String
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
