package no.nav.sosialhjelp.modia.soknadoversikt

import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus

data class SoknadDetaljerResponse(
    val fiksDigisosId: String,
    val soknadTittel: String,
    val status: SoknadsStatus,
    val manglerOpplysninger: Boolean,
    val harNyeOppgaver: Boolean,
    val harDokumentasjonkrav: Boolean,
    val harVilkar: Boolean
)
