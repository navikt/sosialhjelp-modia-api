package no.nav.sosialhjelp.modia.utbetalinger

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.sosialhjelp.modia.domain.UtbetalingsStatus
import java.time.LocalDate

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

data class NavKontor(
    val enhetsNavn: String,
    val enhetsNr: String
)
