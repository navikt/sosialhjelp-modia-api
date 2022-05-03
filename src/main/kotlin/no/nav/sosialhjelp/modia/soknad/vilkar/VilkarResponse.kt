package no.nav.sosialhjelp.modia.soknad.vilkar

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class VilkarResponse(
    val referanse: String,
    val sakstittel: String?,
    val utbetalingsreferanse: List<String>,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val datoLagtTil: LocalDate?,
)
