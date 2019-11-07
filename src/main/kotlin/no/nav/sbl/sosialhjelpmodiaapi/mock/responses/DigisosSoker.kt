package no.nav.sbl.sosialhjelpmodiaapi.mock.responses

import no.nav.sbl.soknadsosialhjelp.digisos.soker.*
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonDokumentlagerFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonSvarUtFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.*

val digisosSoker = JsonDigisosSoker()
        .withVersion("1.0.0")
        .withAvsender(
                JsonAvsender()
                        .withSystemnavn("Testsystemet")
                        .withSystemversjon("1.0.0"))
        .withHendelser(
                listOf(
                        JsonSoknadsStatus()
                                .withType(JsonHendelse.Type.SOKNADS_STATUS)
                                .withHendelsestidspunkt("2018-10-08T11:00:00.000Z")
                                .withStatus(JsonSoknadsStatus.Status.MOTTATT),

                        JsonTildeltNavKontor()
                                .withType(JsonHendelse.Type.TILDELT_NAV_KONTOR)
                                .withHendelsestidspunkt("2018-10-08T21:47:00.134Z")
                                .withNavKontor("0301"),

                        JsonDokumentasjonEtterspurt()
                                .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
                                .withHendelsestidspunkt("2018-10-11T13:42:00.134Z")
                                .withForvaltningsbrev(
                                        JsonForvaltningsbrev()
                                                .withReferanse(
                                                        JsonDokumentlagerFilreferanse()
                                                                .withType(JsonFilreferanse.Type.DOKUMENTLAGER)
                                                                .withId("12345678-9abc-def0-1234-56789abcdeb1")
                                                )
                                )
                                .withVedlegg(
                                        listOf(
                                                JsonVedlegg()
                                                        .withTittel("dokumentasjon etterspurt dokumentlager")
                                                        .withReferanse(
                                                                JsonDokumentlagerFilreferanse()
                                                                        .withType(JsonFilreferanse.Type.DOKUMENTLAGER)
                                                                        .withId("12345678-9abc-def0-1234-56789abcdea2")
                                                        ),
                                                JsonVedlegg()
                                                        .withTittel("dokumentasjon etterspurt svarut")
                                                        .withReferanse(
                                                                JsonSvarUtFilreferanse()
                                                                        .withType(JsonFilreferanse.Type.SVARUT)
                                                                        .withId("12345678-9abc-def0-1234-56789abcdea3")
                                                                        .withNr(1)
                                                        )
                                        )
                                )
                                .withDokumenter(
                                        listOf(
                                                JsonDokumenter()
                                                        .withDokumenttype("Strømfaktura")
                                                        .withTilleggsinformasjon("For periode 01.01.2019 til 01.02.2019")
                                                        .withInnsendelsesfrist("2018-10-20T07:37:00.134Z"),
                                                JsonDokumenter()
                                                        .withDokumenttype("Kopi av depositumskonto")
                                                        .withTilleggsinformasjon("Signert av både deg og utleier")
                                                        .withInnsendelsesfrist("2018-10-20T07:37:30.000Z")
                                        )
                                ),

                        JsonForelopigSvar()
                                .withType(JsonHendelse.Type.FORELOPIG_SVAR)
                                .withHendelsestidspunkt("2018-10-12T07:37:00.134Z")
                                .withForvaltningsbrev(
                                        JsonForvaltningsbrev()
                                                .withReferanse(
                                                        JsonDokumentlagerFilreferanse()
                                                                .withType(JsonFilreferanse.Type.DOKUMENTLAGER)
                                                                .withId("12345678-9abc-def0-1234-56789abcdeb1")
                                                )
                                )
                                .withVedlegg(
                                        listOf(
                                                JsonVedlegg()
                                                        .withTittel("foreløpig svar dokumentlager")
                                                        .withReferanse(
                                                                JsonDokumentlagerFilreferanse()
                                                                        .withType(JsonFilreferanse.Type.DOKUMENTLAGER)
                                                                        .withId("12345678-9abc-def0-1234-56789abcdeb2")
                                                        ),
                                                JsonVedlegg()
                                                        .withTittel("foreløpig svar svarut")
                                                        .withReferanse(
                                                                JsonSvarUtFilreferanse()
                                                                        .withType(JsonFilreferanse.Type.SVARUT)
                                                                        .withId("12345678-9abc-def0-1234-56789abcdeb3")
                                                                        .withNr(1)
                                                        )
                                        )
                                ),

                        JsonVedtakFattet()
                                .withType(JsonHendelse.Type.VEDTAK_FATTET)
                                .withHendelsestidspunkt("2018-10-12T13:37:00.134Z")
                                .withVedtaksfil(
                                        JsonVedtaksfil()
                                                .withReferanse(
                                                        JsonDokumentlagerFilreferanse()
                                                                .withType(JsonFilreferanse.Type.DOKUMENTLAGER)
                                                                .withId("12345678-9abc-def0-1234-56789abcdef0")
                                                )
                                )
                                .withSaksreferanse("SAK1")
                                .withUtfall(JsonVedtakFattet.Utfall.INNVILGET)
                                .withVedlegg(
                                        listOf(
                                                JsonVedlegg()
                                                        .withTittel("Foobar")
                                                        .withReferanse(
                                                                JsonDokumentlagerFilreferanse()
                                                                        .withType(JsonFilreferanse.Type.DOKUMENTLAGER)
                                                                        .withId("12345678-9abc-def0-1234-56789abcdef0")
                                                        ),
                                                JsonVedlegg()
                                                        .withTittel("Test")
                                                        .withReferanse(
                                                                JsonSvarUtFilreferanse()
                                                                        .withType(JsonFilreferanse.Type.SVARUT)
                                                                        .withId("12345678-9abc-def0-1234-56789abcdef0")
                                                                        .withNr(1)
                                                        )
                                        )
                                ),

                        JsonSaksStatus()
                                .withType(JsonHendelse.Type.SAKS_STATUS)
                                .withHendelsestidspunkt("2018-10-12T13:37:00.134Z")
                                .withStatus(JsonSaksStatus.Status.UNDER_BEHANDLING)
                                .withReferanse("SAK1")
                                .withTittel("Nødhjelp"),

                        JsonUtbetaling()
                                .withType(JsonHendelse.Type.UTBETALING)
                                .withHendelsestidspunkt("2018-10-12T13:37:00.134Z")
                                .withUtbetalingsreferanse("Betaling 1")
                                .withSaksreferanse("SAK1")
                                .withStatus(JsonUtbetaling.Status.PLANLAGT_UTBETALING)
                                .withBeskrivelse("Nødhjelp")
                                .withUtbetalingsdato("2019-08-01")
                                .withBelop(12000.0)
                )
        )!!