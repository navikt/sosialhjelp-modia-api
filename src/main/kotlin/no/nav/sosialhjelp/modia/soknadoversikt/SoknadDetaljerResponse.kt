package no.nav.sosialhjelp.modia.soknadoversikt

import no.nav.sosialhjelp.modia.domain.SoknadsStatus

data class SoknadDetaljerResponse(
    val fiksDigisosId: String,
    val soknadTittel: String,
    val status: SoknadsStatus,
    val harNyeOppgaver: Boolean,
    val harVilkar: Boolean
)
