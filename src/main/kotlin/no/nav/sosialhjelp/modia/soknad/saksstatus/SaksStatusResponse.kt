package no.nav.sosialhjelp.modia.soknad.saksstatus

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.sosialhjelp.modia.digisossak.domain.SaksStatus
import no.nav.sosialhjelp.modia.digisossak.domain.UtfallVedtak
import java.time.LocalDate

data class SaksStatusResponse(
    val tittel: String,
    val status: SaksStatus?,
    val vedtak: List<Vedtak>?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val datoOpprettet: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val datoAvsluttet: LocalDate?
) {

    data class Vedtak(
        @JsonFormat(pattern = "yyyy-MM-dd")
        val vedtakDato: LocalDate,
        val utfall: UtfallVedtak?
    )
}
