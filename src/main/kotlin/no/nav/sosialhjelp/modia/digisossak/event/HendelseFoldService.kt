package no.nav.sosialhjelp.modia.digisossak.event

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Instant
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.digisos.hendelser.fold.SoknadMetadata
import no.nav.sosialhjelp.digisos.hendelser.fold.fold
import no.nav.sosialhjelp.filformat.digisos.soker.DigisosSoker
import no.nav.sosialhjelp.filformat.filformatJson
import no.nav.sosialhjelp.filformat.vedlegg.Vedlegg
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.stereotype.Component

@Component
class HendelseFoldService(
    private val meterRegistry: MeterRegistry,
    private val soknadVedleggService: SoknadVedleggService,
    @Qualifier("hendelseFoldExecutor") private val hendelseFoldExecutor: AsyncTaskExecutor,
) {
    fun foldAsync(
        digisosSak: DigisosSak,
        jsonDigisosSoker: JsonDigisosSoker?,
        oldModel: InternalDigisosSoker,
    ) {
        hendelseFoldExecutor.execute {
            fold(digisosSak, jsonDigisosSoker, oldModel) {
                soknadVedleggService.hentSoknadVedleggMedStatus(digisosSak, VEDLEGG_KREVES_STATUS).map {
                    Vedlegg(type = it.type, tilleggsinfo = it.tilleggsinfo)
                }
            }
        }
    }

    fun fold(
        digisosSak: DigisosSak,
        jsonDigisosSoker: JsonDigisosSoker?,
        oldModel: InternalDigisosSoker,
        vedleggProvider: () -> List<Vedlegg>,
    ) {
        try {
            meterRegistry.timer("hendelser_fold_duration").record(
                Runnable {
                    val result =
                        fold(
                            jsonDigisosSoker?.let {
                                filformatJson.decodeFromString<DigisosSoker>(sosialhjelpJsonMapper.writeValueAsString(it))
                            },
                            SoknadMetadata(
                                fiksDigisosId = digisosSak.fiksDigisosId,
                                kommunenummer = digisosSak.kommunenummer,
                                erPapirsoknad = digisosSak.originalSoknadNAV == null,
                                sistEndret = Instant.fromEpochMilliseconds(digisosSak.sistEndret),
                                timestampSendt = digisosSak.originalSoknadNAV?.timestampSendt?.let(Instant::fromEpochMilliseconds),
                                navEksternRefId = digisosSak.originalSoknadNAV?.navEksternRefId,
                                originalSoknadDokumentlagerId = digisosSak.originalSoknadNAV?.soknadDokument?.dokumentlagerDokumentId,
                                vedleggMetadataDokumentlagerId = digisosSak.originalSoknadNAV?.vedleggMetadata,
                                fagsystemNavn = null,
                                fagsystemVersjon = null,
                                mottakerEnhetsnummer = digisosSak.tilleggsinformasjon?.enhetsnummer,
                                mottakerEnhetsnavn = null,
                            ),
                            paakrevdeVedleggProvider = vedleggProvider,
                        )

                    val differences = differences(oldModel, result.soknad)
                    meterRegistry
                        .counter(
                            "hendelser_fold_total",
                            "result",
                            if (differences.isEmpty()) "match" else "mismatch",
                        ).increment()
                    differences.forEach {
                        meterRegistry.counter("hendelser_fold_field_diff_total", "path", it).increment()
                    }

                    if (differences.isNotEmpty()) {
                        log.info(
                            "Hendelser fold mismatch fiksDigisosId={} kommunenummer={} fields={}",
                            digisosSak.fiksDigisosId,
                            digisosSak.kommunenummer,
                            differences,
                        )
                    }
                },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            meterRegistry.counter("hendelser_fold_total", "result", "error").increment()
            log.warn("Hendelser fold failed fiksDigisosId={}", digisosSak.fiksDigisosId, e)
        }
    }

    private fun differences(
        oldModel: InternalDigisosSoker,
        soknad: no.nav.sosialhjelp.digisos.hendelser.domain.Soknad,
    ): Set<String> =
        buildSet {
            if (oldModel.status.name != soknad.status.name) add("status")
            val oldSaker = oldModel.saker.associateBy { it.referanse }
            val foldedSaker = soknad.saker.associateBy { it.referanse }
            // Old model creates a "default" sak for vedtak without saksreferanse. New model puts them in vedtakUtenSak.
            oldSaker.keys
                .filterNot { it in foldedSaker }
                .forEach { add(if (it == "default" && soknad.vedtakUtenSak.isNotEmpty()) "saker.default" else "saker.manglerINy") }
            foldedSaker.keys.filterNot { it in oldSaker }.forEach { referanse ->
                add(if (foldedSaker.getValue(referanse).erSyntetisk()) "saker.syntetisk" else "saker.manglerIGammel")
            }
            oldSaker.keys.intersect(foldedSaker.keys).forEach { referanse ->
                if ((oldSaker.getValue(referanse).saksStatus?.name ?: "UNDER_BEHANDLING") !=
                    (foldedSaker.getValue(referanse).saksStatus?.name ?: "UNDER_BEHANDLING")
                ) {
                    add("saker.status")
                }
            }
            if (oldModel.saker
                    .flatMap { it.vedtak }
                    .map { it.utfall?.name to it.datoFattet.toString() }
                    .sortedBy { it.toString() } !=
                (soknad.saker.flatMap { it.vedtak } + soknad.vedtakUtenSak)
                    .map { it.utfall?.name to it.dato?.toString() }
                    .sortedBy {
                        it
                            .toString()
                    }
            ) {
                add("vedtak")
            }
            if (oldModel.utbetalinger.map { it.referanse to it.status.name }.sortedBy { it.first } !=
                (
                    soknad.saker.flatMap {
                        it.utbetalinger
                    } + soknad.utbetalingerUtenSak
                ).map { it.referanse to it.status.name }.sortedBy { it.first }
            ) {
                add("utbetalinger")
            }
            if (oldModel.oppgaver
                    .map { Triple(it.tittel, it.tilleggsinfo, it.erFraInnsyn) }
                    .sortedBy { it.toString() } !=
                soknad.dokumentasjonEtterspurt
                    .map {
                        Triple(
                            it.tittel,
                            it.beskrivelse,
                            it.kilde == no.nav.sosialhjelp.digisos.hendelser.domain.DokumentasjonEtterspurt.Kilde.DOKUMENTASJON_ETTERSPURT,
                        )
                    }.sortedBy { it.toString() }
            ) {
                add("oppgaver")
            }
            if (oldModel.vilkar.map { it.referanse to it.status.name }.sortedBy { it.first } !=
                (soknad.saker.flatMap { it.vilkar } + soknad.vilkarUtenSak)
                    .map { it.referanse to it.status.name }
                    .sortedBy { it.first }
            ) {
                add("vilkar")
            }
            if (oldModel.dokumentasjonkrav.map { it.dokumentasjonkravId to it.status?.name }.sortedBy { it.first } !=
                (soknad.saker.flatMap { it.dokumentasjonkrav } + soknad.dokumentasjonkravUtenSak)
                    .map { it.referanse to it.status.name }
                    .sortedBy { it.first }
            ) {
                add("dokumentasjonkrav")
            }
            if ((oldModel.forelopigSvar != null) != (soknad.forelopigSvar != null)) add("forelopigSvar")
            if (oldModel.soknadsmottaker?.navEnhetsnummer != soknad.mottaker?.enhetsnummer) add("soknadsmottaker")
        }

    // A sak the library creates only because a vilkår or dokumentasjonkrav references it. The old model has no equivalent.
    private fun no.nav.sosialhjelp.digisos.hendelser.domain.Sak.erSyntetisk(): Boolean =
        saksStatus == null && tittel == null && vedtak.isEmpty() && utbetalinger.isEmpty()

    companion object {
        private val log by logger()
    }
}
