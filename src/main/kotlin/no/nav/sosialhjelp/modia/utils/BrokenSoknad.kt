package no.nav.sosialhjelp.modia.utils

import no.nav.sosialhjelp.modia.logger

object BrokenSoknad {
    private val log by logger()

    private val brokenEksternRefIds =
        this::class.java
            .getResource("/soknadermedmanglendevedlegg/feilede_eksternref_med_vedlegg.csv")
            .openStream()
            .bufferedReader()
            .readLines()
            .toSet()
            .also { log.info("Lastet inn ${it.size} feilede eksternrefids") }

    fun isBrokenSoknad(eksternRefId: String): Boolean =
        brokenEksternRefIds.contains(eksternRefId).also {
            if (it) {
                log.info("Søknad med feilet eksternrefid: $eksternRefId")
            }
        }
}
