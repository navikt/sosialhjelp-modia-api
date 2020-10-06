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

const val enhetsnavn = "The Office"
const val enhetsnr = "2317"

const val navKontor = "1337"
const val navKontor2 = "2244"

const val tittel_1 = "tittel"
const val tittel_2 = "tittel2"

const val referanse_1 = "sak1"
const val referanse_2 = "sak2"

const val utbetaling_ref_1 = "utbetaling 1"

const val vilkar_ref_1 = "ulike vilkar"

const val dokumentasjonkrav_ref_1 = "dette må du gjøre for å få pengene"

const val dokumenttype = "dokumentasjonstype"
const val tilleggsinfo = "ekstra info"

val avsender: JsonAvsender = JsonAvsender().withSystemnavn("test").withSystemversjon("123")

private val now = ZonedDateTime.now()
val tidspunkt_soknad = now.minusHours(11).toEpochSecond() * 1000L
val tidspunkt_1: String = now.minusHours(10).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_2: String = now.minusHours(9).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_3: String = now.minusHours(8).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_4: String = now.minusHours(7).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_5: String = now.minusHours(6).format(DateTimeFormatter.ISO_DATE_TIME)
val tidspunkt_6: String = now.minusHours(5).format(DateTimeFormatter.ISO_DATE_TIME)
val innsendelsesfrist: String = now.plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME)

val DOKUMENTLAGER_1: JsonDokumentlagerFilreferanse = JsonDokumentlagerFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(dokumentlagerId_1)
val DOKUMENTLAGER_2: JsonDokumentlagerFilreferanse = JsonDokumentlagerFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(dokumentlagerId_2)
val SVARUT_1: JsonSvarUtFilreferanse = JsonSvarUtFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(svarUtId).withNr(svarUtNr)

val SOKNADS_STATUS_MOTTATT: JsonSoknadsStatus = JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withStatus(JsonSoknadsStatus.Status.MOTTATT)

val SOKNADS_STATUS_UNDERBEHANDLING: JsonSoknadsStatus = JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withStatus(JsonSoknadsStatus.Status.UNDER_BEHANDLING)

val SOKNADS_STATUS_FERDIGBEHANDLET: JsonSoknadsStatus = JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withStatus(JsonSoknadsStatus.Status.FERDIGBEHANDLET)

val TILDELT_NAV_KONTOR: JsonTildeltNavKontor = JsonTildeltNavKontor()
        .withType(JsonHendelse.Type.TILDELT_NAV_KONTOR)
        .withNavKontor(navKontor)

val TILDELT_NAV_KONTOR_2: JsonTildeltNavKontor = JsonTildeltNavKontor()
        .withType(JsonHendelse.Type.TILDELT_NAV_KONTOR)
        .withNavKontor(navKontor2)

val TILDELT_EMPTY_NAV_KONTOR: JsonTildeltNavKontor = JsonTildeltNavKontor()
        .withType(JsonHendelse.Type.TILDELT_NAV_KONTOR)
        .withNavKontor("")

val SAK1_SAKS_STATUS_UNDERBEHANDLING: JsonSaksStatus = JsonSaksStatus()
        .withType(JsonHendelse.Type.SAKS_STATUS)
        .withStatus(JsonSaksStatus.Status.UNDER_BEHANDLING)
        .withTittel(tittel_1)
        .withReferanse(referanse_1)

val SAK1_UTEN_SAKS_STATUS_ELLER_TITTEL: JsonSaksStatus = JsonSaksStatus()
        .withType(JsonHendelse.Type.SAKS_STATUS)
        .withReferanse(referanse_1)

val SAK1_SAKS_STATUS_IKKEINNSYN: JsonSaksStatus = JsonSaksStatus()
        .withType(JsonHendelse.Type.SAKS_STATUS)
        .withStatus(JsonSaksStatus.Status.IKKE_INNSYN)
        .withTittel(tittel_1)
        .withReferanse(referanse_1)

val SAK2_SAKS_STATUS_UNDERBEHANDLING: JsonSaksStatus = JsonSaksStatus()
        .withType(JsonHendelse.Type.SAKS_STATUS)
        .withStatus(JsonSaksStatus.Status.UNDER_BEHANDLING)
        .withTittel(tittel_2)
        .withReferanse(referanse_2)

val SAK1_VEDTAK_FATTET_INNVILGET: JsonVedtakFattet = JsonVedtakFattet()
        .withType(JsonHendelse.Type.VEDTAK_FATTET)
        .withSaksreferanse(referanse_1)
        .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_1))
        .withUtfall(JsonVedtakFattet.Utfall.INNVILGET)

val SAK1_VEDTAK_FATTET_UTEN_UTFALL: JsonVedtakFattet = JsonVedtakFattet()
        .withType(JsonHendelse.Type.VEDTAK_FATTET)
        .withSaksreferanse(referanse_1)
        .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_1))

val SAK1_VEDTAK_FATTET_AVSLATT: JsonVedtakFattet = JsonVedtakFattet()
        .withType(JsonHendelse.Type.VEDTAK_FATTET)
        .withSaksreferanse(referanse_1)
        .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_2))
        .withUtfall(JsonVedtakFattet.Utfall.AVSLATT)

val SAK2_VEDTAK_FATTET: JsonVedtakFattet = JsonVedtakFattet()
        .withType(JsonHendelse.Type.VEDTAK_FATTET)
        .withSaksreferanse(referanse_2)
        .withVedtaksfil(JsonVedtaksfil().withReferanse(SVARUT_1))
        .withUtfall(JsonVedtakFattet.Utfall.INNVILGET)

val DOKUMENTASJONETTERSPURT: JsonDokumentasjonEtterspurt = JsonDokumentasjonEtterspurt()
        .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
        .withDokumenter(mutableListOf(JsonDokumenter().withInnsendelsesfrist(innsendelsesfrist).withDokumenttype(dokumenttype).withTilleggsinformasjon(tilleggsinfo)))
        .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(DOKUMENTLAGER_1))

val DOKUMENTASJONETTERSPURT_TOM_DOKUMENT_LISTE: JsonDokumentasjonEtterspurt = JsonDokumentasjonEtterspurt()
        .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
        .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(DOKUMENTLAGER_1))

val DOKUMENTASJONETTERSPURT_UTEN_FORVALTNINGSBREV: JsonDokumentasjonEtterspurt = JsonDokumentasjonEtterspurt()
        .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
        .withDokumenter(mutableListOf(JsonDokumenter().withInnsendelsesfrist(innsendelsesfrist).withDokumenttype(dokumenttype).withTilleggsinformasjon(tilleggsinfo)))

val FORELOPIGSVAR: JsonForelopigSvar = JsonForelopigSvar()
        .withType(JsonHendelse.Type.FORELOPIG_SVAR)
        .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(SVARUT_1))

val UTBETALING: JsonUtbetaling = JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withUtbetalingsreferanse(utbetaling_ref_1)
        .withSaksreferanse(referanse_1)
        .withRammevedtaksreferanse(null)
        .withStatus(JsonUtbetaling.Status.UTBETALT)
        .withBelop(1234.56)
        .withBeskrivelse(tittel_1)
        .withForfallsdato("2019-12-31")
        .withUtbetalingsdato("2019-12-24")
        .withFom("2019-12-01")
        .withTom("2019-12-31")
        .withAnnenMottaker(false)
        .withMottaker("fnr")
        .withKontonummer("kontonummer")
        .withUtbetalingsmetode("pose med krølla femtilapper")

val UTBETALING_ANNEN_MOTTAKER: JsonUtbetaling = JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withUtbetalingsreferanse(utbetaling_ref_1)
        .withSaksreferanse(referanse_1)
        .withRammevedtaksreferanse(null)
        .withStatus(JsonUtbetaling.Status.UTBETALT)
        .withBelop(1234.56)
        .withBeskrivelse(tittel_1)
        .withForfallsdato("2019-12-31")
        .withUtbetalingsdato("2019-12-24")
        .withFom(null)
        .withTom(null)
        .withAnnenMottaker(true)
        .withMottaker("utleier")
        .withKontonummer(null)
        .withUtbetalingsmetode("pose med krølla femtilapper")

val VILKAR_OPPFYLT: JsonVilkar = JsonVilkar()
        .withType(JsonHendelse.Type.VILKAR)
        .withVilkarreferanse(vilkar_ref_1)
        .withUtbetalingsreferanse(listOf(utbetaling_ref_1))
        .withBeskrivelse("beskrivelse")
        .withStatus(JsonVilkar.Status.OPPFYLT)

val DOKUMENTASJONKRAV_OPPFYLT: JsonDokumentasjonkrav = JsonDokumentasjonkrav()
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