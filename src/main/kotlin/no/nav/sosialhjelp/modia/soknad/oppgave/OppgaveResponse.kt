package no.nav.sosialhjelp.modia.soknad.oppgave

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class OppgaveResponse(
    val dokumenttype: String,
    val tilleggsinformasjon: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val innsendelsesfrist: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val vedleggDatoLagtTil: LocalDate?,
    val antallVedlegg: Int,
    val erFraInnsyn: Boolean,
)
