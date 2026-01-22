package no.nav.sosialhjelp.modia.soknad.dokumentasjonkrav

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class DokumentasjonkravResponse(
    val referanse: String,
    val sakstittel: String?,
    val status: String,
    @param:JsonFormat(pattern = "yyyy-MM-dd")
    val innsendelsesfrist: LocalDate?,
    @param:JsonFormat(pattern = "yyyy-MM-dd")
    val datoLagtTil: LocalDate?,
)
