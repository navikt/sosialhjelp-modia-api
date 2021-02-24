package no.nav.sosialhjelp.modia.mock.responses

import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import no.nav.sosialhjelp.api.fiks.Ettersendelse
import no.nav.sosialhjelp.api.fiks.EttersendtInfoNAV
import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV
import no.nav.sosialhjelp.api.fiks.Tilleggsinformasjon
import java.time.LocalDateTime
import java.time.ZoneOffset

val defaultDigisosSak = DigisosSak(
        fiksDigisosId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        sokerFnr = "string",
        fiksOrgId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        kommunenummer = "1111",
        sistEndret = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(),
        originalSoknadNAV = OriginalSoknadNAV(
                navEksternRefId = "string",
                metadata = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                vedleggMetadata = "mock-soknad-vedlegg-metadata",
                soknadDokument = DokumentInfo(
                        filnavn = "string",
                        dokumentlagerDokumentId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                        storrelse = 0),
                vedlegg = listOf(
                        DokumentInfo(
                                filnavn = "soknad vedlegg filnavn 1",
                                dokumentlagerDokumentId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                storrelse = 0)),
                timestampSendt = 0
        ),
        ettersendtInfoNAV = EttersendtInfoNAV(
                ettersendelser = listOf(
                        Ettersendelse(
                                navEksternRefId = "string",
                                vedleggMetadata = "mock-ettersendelse-vedlegg-metadata",
                                vedlegg = listOf(
                                        DokumentInfo(
                                                filnavn = "ettersendelse vedlegg filnavn 1",
                                                dokumentlagerDokumentId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                storrelse = 0
                                        )
                                ),
                                timestampSendt = 1539432000000
                        ),
                        Ettersendelse(
                                navEksternRefId = "string",
                                vedleggMetadata = "mock-ettersendelse-vedlegg-metadata-2",
                                vedlegg = listOf(
                                        DokumentInfo(
                                                filnavn = "ettersendelse vedlegg filnavn 2",
                                                dokumentlagerDokumentId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                storrelse = 0
                                        )
                                ),
                                timestampSendt = 1539296542000
                        )
                )
        ),
        digisosSoker = DigisosSoker(
                metadata = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                dokumenter = listOf(
                        DokumentInfo(
                                filnavn = "string",
                                dokumentlagerDokumentId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                storrelse = 0
                        )
                ),
                timestampSistOppdatert = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        ),
        tilleggsinformasjon = Tilleggsinformasjon(
                enhetsnummer = "1234"
        )
)

val minimalPapirsoknad = DigisosSak(
        fiksDigisosId = "ff7a4826-01a1-11eb-adc1-0242ac120002-minimal-papir",
        sokerFnr = "string",
        fiksOrgId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        kommunenummer = "0301",
        sistEndret = 0,
        originalSoknadNAV = null,
        ettersendtInfoNAV = null,
        digisosSoker = DigisosSoker(
                metadata = "mock-digisossoker-minimal",
                dokumenter = emptyList(),
                timestampSistOppdatert = 1601309557
        ),
        tilleggsinformasjon = null
)

val minimalDigitalsoknad = DigisosSak(
        fiksDigisosId = "a2460722-01a5-11eb-adc1-0242ac120002-minimal-digital",
        sokerFnr = "string",
        fiksOrgId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        kommunenummer = "0301",
        sistEndret = 0,
        originalSoknadNAV = OriginalSoknadNAV(
                navEksternRefId = "11000001",
                metadata = "mock-soknad-minimal",
                vedleggMetadata = "mock-soknad-vedlegg-metadata",
                soknadDokument = DokumentInfo(
                        filnavn = "Soknad.pdf",
                        dokumentlagerDokumentId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                        storrelse = 0),
                vedlegg = emptyList(),
                timestampSendt = 1601309557
        ),
        ettersendtInfoNAV = null,
        digisosSoker = null,
        tilleggsinformasjon = null
)