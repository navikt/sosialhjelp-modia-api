package no.nav.sosialhjelp.modia.soknadoversikt

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.util.Date

data class SoknadResponse(
    val fiksDigisosId: String,
    val soknadTittel: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val sistOppdatert: Date,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val sendt: Date?,
    val kilde: String,
    val papirSoknad: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val papirSoknadRegistrerteDato: LocalDate?
)
