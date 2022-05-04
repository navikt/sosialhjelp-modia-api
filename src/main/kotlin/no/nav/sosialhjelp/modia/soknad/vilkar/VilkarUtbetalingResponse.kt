package no.nav.sosialhjelp.modia.soknad.vilkar

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class VilkarUtbetalingResponse(
    val tittel: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val utbetalingEllerForfall: LocalDate?,
    val status: String
)
