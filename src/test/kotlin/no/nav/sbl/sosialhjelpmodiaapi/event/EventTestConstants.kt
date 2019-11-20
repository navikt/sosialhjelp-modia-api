package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonAvsender
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonForvaltningsbrev
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonDokumentlagerFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonSvarUtFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


const val dokumentlagerId_1 = "1"
const val dokumentlagerId_2 = "2"
const val svarUtId = "42"
const val svarUtNr = 42

const val soknadsmottaker = "The Office"
const val enhetsnr = "2317"

const val navKontor = "1337"
const val navKontor2 = "2222"

const val tittel_1 = "tittel"
const val tittel_2 = "tittel2"

const val referanse_1 = "sak1"
const val referanse_2 = "sak2"

const val utbetaling_ref_1 = "utbetaling 1"

const val vilkar_ref_1 = "ulike vilkar"

const val dokumentasjonkrav_ref_1 = "dette må du gjøre for å få pengene"

const val dokumenttype = "dokumentasjonstype"
const val tilleggsinfo = "ekstra info"

val avsender = JsonAvsender().withSystemnavn("test").withSystemversjon("123")

private val now = ZonedDateTime.now()
val tidspunkt_soknad = now.minusHours(11).toEpochSecond() * 1000L
val tidspunkt_1 = now.minusHours(10).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_2 = now.minusHours(9).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_3 = now.minusHours(8).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_4 = now.minusHours(7).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_5 = now.minusHours(6).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_6 = now.minusHours(5).format(DateTimeFormatter.ISO_DATE_TIME)
val innsendelsesfrist = now.plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME)

val DOKUMENTLAGER_1 = JsonDokumentlagerFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(dokumentlagerId_1)
val DOKUMENTLAGER_2 = JsonDokumentlagerFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(dokumentlagerId_2)
val SVARUT_1 = JsonSvarUtFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(svarUtId).withNr(svarUtNr)

val SOKNADS_STATUS_MOTTATT = JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withStatus(JsonSoknadsStatus.Status.MOTTATT)

val SOKNADS_STATUS_UNDERBEHANDLING = JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withStatus(JsonSoknadsStatus.Status.UNDER_BEHANDLING)

val SOKNADS_STATUS_FERDIGBEHANDLET = JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withStatus(JsonSoknadsStatus.Status.FERDIGBEHANDLET)

val TILDELT_NAV_KONTOR = JsonTildeltNavKontor()
        .withType(JsonHendelse.Type.TILDELT_NAV_KONTOR)
        .withNavKontor(navKontor)

val TILDELT_NAV_KONTOR_2 = JsonTildeltNavKontor()
        .withType(JsonHendelse.Type.TILDELT_NAV_KONTOR)
        .withNavKontor(navKontor2)

val SAK1_SAKS_STATUS_UNDERBEHANDLING = JsonSaksStatus()
        .withType(JsonHendelse.Type.SAKS_STATUS)
        .withStatus(JsonSaksStatus.Status.UNDER_BEHANDLING)
        .withTittel(tittel_1)
        .withReferanse(referanse_1)

val SAK1_UTEN_SAKS_STATUS_ELLER_TITTEL = JsonSaksStatus()
        .withType(JsonHendelse.Type.SAKS_STATUS)
        .withReferanse(referanse_1)

val SAK1_SAKS_STATUS_IKKEINNSYN = JsonSaksStatus()
        .withType(JsonHendelse.Type.SAKS_STATUS)
        .withStatus(JsonSaksStatus.Status.IKKE_INNSYN)
        .withTittel(tittel_1)
        .withReferanse(referanse_1)

val SAK2_SAKS_STATUS_UNDERBEHANDLING = JsonSaksStatus()
        .withType(JsonHendelse.Type.SAKS_STATUS)
        .withStatus(JsonSaksStatus.Status.UNDER_BEHANDLING)
        .withTittel(tittel_2)
        .withReferanse(referanse_2)

val SAK1_VEDTAK_FATTET_INNVILGET = JsonVedtakFattet()
        .withType(JsonHendelse.Type.VEDTAK_FATTET)
        .withSaksreferanse(referanse_1)
        .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_1))
        .withUtfall(JsonVedtakFattet.Utfall.INNVILGET)

val SAK1_VEDTAK_FATTET_UTEN_UTFALL = JsonVedtakFattet()
        .withType(JsonHendelse.Type.VEDTAK_FATTET)
        .withSaksreferanse(referanse_1)
        .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_1))

val SAK1_VEDTAK_FATTET_AVSLATT = JsonVedtakFattet()
        .withType(JsonHendelse.Type.VEDTAK_FATTET)
        .withSaksreferanse(referanse_1)
        .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_2))
        .withUtfall(JsonVedtakFattet.Utfall.AVSLATT)

val SAK2_VEDTAK_FATTET = JsonVedtakFattet()
        .withType(JsonHendelse.Type.VEDTAK_FATTET)
        .withSaksreferanse(referanse_2)
        .withVedtaksfil(JsonVedtaksfil().withReferanse(SVARUT_1))
        .withUtfall(JsonVedtakFattet.Utfall.INNVILGET)

val DOKUMENTASJONETTERSPURT = JsonDokumentasjonEtterspurt()
        .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
        .withDokumenter(mutableListOf(JsonDokumenter().withInnsendelsesfrist(innsendelsesfrist).withDokumenttype(dokumenttype).withTilleggsinformasjon(tilleggsinfo)))
        .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(DOKUMENTLAGER_1))

val DOKUMENTASJONETTERSPURT_TOM_DOKUMENT_LISTE = JsonDokumentasjonEtterspurt()
        .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
        .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(DOKUMENTLAGER_1))

val DOKUMENTASJONETTERSPURT_UTEN_FORVALTNINGSBREV = JsonDokumentasjonEtterspurt()
        .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
        .withDokumenter(mutableListOf(JsonDokumenter().withInnsendelsesfrist(innsendelsesfrist).withDokumenttype(dokumenttype).withTilleggsinformasjon(tilleggsinfo)))

val FORELOPIGSVAR = JsonForelopigSvar()
        .withType(JsonHendelse.Type.FORELOPIG_SVAR)
        .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(SVARUT_1))

val UTBETALING = JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withUtbetalingsreferanse(utbetaling_ref_1)
        .withSaksreferanse(referanse_1)
        .withRammevedtaksreferanse(null)
        .withStatus(JsonUtbetaling.Status.UTBETALT)
        .withBelop(1234.56)
        .withBeskrivelse(tittel_1)
        .withForfallsdato("2019-12-31")
        .withStonadstype("type")
        .withUtbetalingsdato("2019-12-24")
        .withFom(null)
        .withTom(null)
        .withAnnenMottaker(false)
        .withMottaker("fnr")
        .withKontonummer("kontonummer")
        .withUtbetalingsmetode("pose med krølla femtilapper")


val VILKAR_OPPFYLT = JsonVilkar()
        .withType(JsonHendelse.Type.VILKAR)
        .withVilkarreferanse(vilkar_ref_1)
        .withUtbetalingsreferanse(listOf(utbetaling_ref_1))
        .withBeskrivelse("beskrivelse")
        .withStatus(JsonVilkar.Status.OPPFYLT)

val DOKUMENTASJONKRAV_OPPFYLT = JsonDokumentasjonkrav()
        .withType(JsonHendelse.Type.DOKUMENTASJONKRAV)
        .withDokumentasjonkravreferanse(dokumentasjonkrav_ref_1)
        .withUtbetalingsreferanse(listOf(utbetaling_ref_1))
        .withBeskrivelse("beskrivelse")
        .withStatus(JsonDokumentasjonkrav.Status.OPPFYLT)

fun resetHendelser() {
    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(null)
    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(null)
    SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(null)
    TILDELT_NAV_KONTOR.withHendelsestidspunkt(null)
    TILDELT_NAV_KONTOR_2.withHendelsestidspunkt(null)
    SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(null)
    SAK1_SAKS_STATUS_IKKEINNSYN.withHendelsestidspunkt(null)
    SAK2_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(null)
    SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(null)
    SAK1_VEDTAK_FATTET_AVSLATT.withHendelsestidspunkt(null)
    SAK2_VEDTAK_FATTET.withHendelsestidspunkt(null)
    DOKUMENTASJONETTERSPURT.withHendelsestidspunkt(null)
    DOKUMENTASJONETTERSPURT_UTEN_FORVALTNINGSBREV.withHendelsestidspunkt(null)
    DOKUMENTASJONETTERSPURT_TOM_DOKUMENT_LISTE.withHendelsestidspunkt(null)
    FORELOPIGSVAR.withHendelsestidspunkt(null)
    UTBETALING.withHendelsestidspunkt(null)
    DOKUMENTASJONKRAV_OPPFYLT.withHendelsestidspunkt(null)
    VILKAR_OPPFYLT.withHendelsestidspunkt(null)
}