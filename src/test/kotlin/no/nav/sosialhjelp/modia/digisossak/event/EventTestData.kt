package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonAvsender
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonForvaltningsbrev
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonDokumentlagerFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonSvarUtFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonEtterspurt
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonkrav
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumenter
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonForelopigSvar
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonTildeltNavKontor
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtakFattet
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtaksfil
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVilkar
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV
import no.nav.sosialhjelp.api.fiks.Tilleggsinformasjon
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

const val DOKUMENTLAGER_ID_1 = "1"
const val DOKUMENTLAGER_ID_2 = "2"
const val SVAR_UT_ID = "42"
const val SVAR_UT_NR = 42

const val ENHETSNAVN = "The Office"
const val ENHETSNR = "2317"

const val NAV_KONTOR = "1337"
const val NAV_KONTOR_2 = "2244"

const val TITTEL_1 = "tittel"
const val TITTEL_2 = "tittel2"

const val REFERANSE_1 = "sak1"
const val REFERANSE_2 = "sak2"

const val UTBETALING_REF_1 = "utbetaling 1"

const val VILKAR_REF_1 = "ulike vilkar"

const val DOKUMENTASJONSKRAV = "dette må du gjøre for å få pengene"

const val DOKUMENT_TYPE = "dokumentasjonstype"
const val TILLEGGSINFO = "ekstra info"

val avsender = JsonAvsender(systemnavn = "test", systemversjon = "123")
val baseJsonDigisosSoker = JsonDigisosSoker(version = "123", avsender = avsender, hendelser = emptyList())

private val now = ZonedDateTime.now()
val tidspunkt_soknad = now.minusHours(11).toEpochSecond() * 1000L
val tidspunkt_1: String = now.minusHours(10).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_2: String = now.minusHours(9).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_3: String = now.minusHours(8).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_4: String = now.minusHours(7).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_5: String = now.minusHours(6).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_6: String = now.minusHours(5).format(DateTimeFormatter.ISO_DATE_TIME)
val innsendelsesfrist: String = now.plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME)

val DOKUMENTLAGER_1 = JsonDokumentlagerFilreferanse(id = DOKUMENTLAGER_ID_1)
val DOKUMENTLAGER_2 = JsonDokumentlagerFilreferanse(id = DOKUMENTLAGER_ID_2)
val SVARUT_1 = JsonSvarUtFilreferanse(id = SVAR_UT_ID, nr = SVAR_UT_NR)

val SOKNADS_STATUS_MOTTATT = JsonSoknadsStatus(status = JsonSoknadsStatus.Status.MOTTATT, hendelsestidspunkt = "")

val SOKNADS_STATUS_UNDERBEHANDLING = JsonSoknadsStatus(status = JsonSoknadsStatus.Status.UNDER_BEHANDLING, hendelsestidspunkt = "")

val SOKNADS_STATUS_FERDIGBEHANDLET = JsonSoknadsStatus(status = JsonSoknadsStatus.Status.FERDIGBEHANDLET, hendelsestidspunkt = "")

val TILDELT_NAV_KONTOR = JsonTildeltNavKontor(navKontor = NAV_KONTOR, hendelsestidspunkt = "")

val TILDELT_NAV_KONTOR_2 = JsonTildeltNavKontor(navKontor = NAV_KONTOR_2, hendelsestidspunkt = "")

val TILDELT_EMPTY_NAV_KONTOR = JsonTildeltNavKontor(navKontor = "", hendelsestidspunkt = "")

val SAK1_SAKS_STATUS_UNDERBEHANDLING =
    JsonSaksStatus(referanse = REFERANSE_1, hendelsestidspunkt = "", tittel = TITTEL_1, status = JsonSaksStatus.Status.UNDER_BEHANDLING)

val SAK1_UTEN_SAKS_STATUS_ELLER_TITTEL = JsonSaksStatus(referanse = REFERANSE_1, hendelsestidspunkt = "")

val SAK1_SAKS_STATUS_IKKEINNSYN =
    JsonSaksStatus(referanse = REFERANSE_1, hendelsestidspunkt = "", tittel = TITTEL_1, status = JsonSaksStatus.Status.IKKE_INNSYN)

val SAK2_SAKS_STATUS_UNDERBEHANDLING =
    JsonSaksStatus(referanse = REFERANSE_2, hendelsestidspunkt = "", tittel = TITTEL_2, status = JsonSaksStatus.Status.UNDER_BEHANDLING)

val SAK1_VEDTAK_FATTET_INNVILGET =
    JsonVedtakFattet(
        saksreferanse = REFERANSE_1,
        vedtaksfil = JsonVedtaksfil(referanse = DOKUMENTLAGER_1),
        hendelsestidspunkt = "",
        utfall = JsonVedtakFattet.Utfall.INNVILGET,
    )

val SAK1_VEDTAK_FATTET_UTEN_UTFALL =
    JsonVedtakFattet(saksreferanse = REFERANSE_1, vedtaksfil = JsonVedtaksfil(referanse = DOKUMENTLAGER_1), hendelsestidspunkt = "")

val SAK1_VEDTAK_FATTET_AVSLATT =
    JsonVedtakFattet(
        saksreferanse = REFERANSE_1,
        vedtaksfil = JsonVedtaksfil(referanse = DOKUMENTLAGER_2),
        hendelsestidspunkt = "",
        utfall = JsonVedtakFattet.Utfall.AVSLATT,
    )

val SAK2_VEDTAK_FATTET =
    JsonVedtakFattet(
        saksreferanse = REFERANSE_2,
        vedtaksfil = JsonVedtaksfil(referanse = SVARUT_1),
        hendelsestidspunkt = "",
        utfall = JsonVedtakFattet.Utfall.INNVILGET,
    )

val DOKUMENTASJONETTERSPURT =
    JsonDokumentasjonEtterspurt(
        dokumenter =
            listOf(
                JsonDokumenter(dokumenttype = DOKUMENT_TYPE, innsendelsesfrist = innsendelsesfrist, tilleggsinformasjon = TILLEGGSINFO),
            ),
        hendelsestidspunkt = "",
        forvaltningsbrev = JsonForvaltningsbrev(referanse = DOKUMENTLAGER_1),
    )

val DOKUMENTASJONETTERSPURT_TOM_DOKUMENT_LISTE =
    JsonDokumentasjonEtterspurt(
        dokumenter = emptyList(),
        hendelsestidspunkt = "",
        forvaltningsbrev = JsonForvaltningsbrev(referanse = DOKUMENTLAGER_1),
    )

val DOKUMENTASJONETTERSPURT_UTEN_FORVALTNINGSBREV =
    JsonDokumentasjonEtterspurt(
        dokumenter =
            listOf(
                JsonDokumenter(dokumenttype = DOKUMENT_TYPE, innsendelsesfrist = innsendelsesfrist, tilleggsinformasjon = TILLEGGSINFO),
            ),
        hendelsestidspunkt = "",
    )

val FORELOPIGSVAR = JsonForelopigSvar(forvaltningsbrev = JsonForvaltningsbrev(referanse = SVARUT_1), hendelsestidspunkt = "")

val UTBETALING =
    JsonUtbetaling(
        utbetalingsreferanse = UTBETALING_REF_1,
        hendelsestidspunkt = "",
        saksreferanse = REFERANSE_1,
        status = JsonUtbetaling.Status.UTBETALT,
        belop = 1234.56,
        beskrivelse = TITTEL_1,
        forfallsdato = "2019-12-31",
        utbetalingsdato = "2019-12-24",
        fom = "2019-12-01",
        tom = "2019-12-31",
        annenMottaker = false,
        mottaker = "fnr",
        kontonummer = "kontonummer",
        utbetalingsmetode = "pose med krølla femtilapper",
    )

val UTBETALING_ANNEN_MOTTAKER =
    JsonUtbetaling(
        utbetalingsreferanse = UTBETALING_REF_1,
        hendelsestidspunkt = "",
        saksreferanse = REFERANSE_1,
        status = JsonUtbetaling.Status.UTBETALT,
        belop = 1234.56,
        beskrivelse = TITTEL_1,
        forfallsdato = "2019-12-31",
        utbetalingsdato = "2019-12-24",
        annenMottaker = true,
        mottaker = "utleier",
        utbetalingsmetode = "pose med krølla femtilapper",
    )

val VILKAR_OPPFYLT =
    JsonVilkar(
        vilkarreferanse = VILKAR_REF_1,
        hendelsestidspunkt = "",
        utbetalingsreferanse = listOf(UTBETALING_REF_1),
        beskrivelse = "beskrivelse",
        status = JsonVilkar.Status.RELEVANT,
    )

val DOKUMENTASJONKRAV_OPPFYLT =
    JsonDokumentasjonkrav(
        dokumentasjonkravreferanse = DOKUMENTASJONSKRAV,
        hendelsestidspunkt = "",
        utbetalingsreferanse = listOf(UTBETALING_REF_1),
        beskrivelse = "beskrivelse",
        status = JsonDokumentasjonkrav.Status.OPPFYLT,
    )

val DOKUMENTASJONKRAV_RELEVANT =
    JsonDokumentasjonkrav(
        dokumentasjonkravreferanse = DOKUMENTASJONSKRAV,
        hendelsestidspunkt = "",
        utbetalingsreferanse = listOf(UTBETALING_REF_1),
        beskrivelse = "beskrivelse",
        status = JsonDokumentasjonkrav.Status.RELEVANT,
    )

val defaultDigisosSak =
    DigisosSak(
        fiksDigisosId = "123",
        sokerFnr = "fnr",
        fiksOrgId = "",
        kommunenummer = "0301",
        sistEndret = 1L,
        originalSoknadNAV =
            OriginalSoknadNAV(
                navEksternRefId = "eksternRef",
                metadata = "some other id",
                vedleggMetadata = "",
                soknadDokument =
                    DokumentInfo(
                        filnavn = "soknad.json",
                        dokumentlagerDokumentId = "soknaddokumentlagerid",
                        storrelse = 99L,
                    ),
                vedlegg = emptyList(),
                timestampSendt = tidspunkt_soknad,
            ),
        ettersendtInfoNAV = null,
        digisosSoker =
            DigisosSoker(
                metadata = "some id",
                dokumenter = emptyList(),
                timestampSistOppdatert = 123L,
            ),
        tilleggsinformasjon =
            Tilleggsinformasjon(
                enhetsnummer = ENHETSNR,
            ),
    )
