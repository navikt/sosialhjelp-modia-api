package no.nav.sosialhjelp.modia.soknadoversikt

import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.OppgaveStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Sak
import no.nav.sosialhjelp.modia.digisossak.domain.SaksStatus
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Utbetaling
import no.nav.sosialhjelp.modia.digisossak.domain.UtfallVedtak
import no.nav.sosialhjelp.modia.digisossak.domain.Vedtak
import no.nav.sosialhjelp.modia.digisossak.domain.Vilkar
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.event.SAK_DEFAULT_TITTEL
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.soknad.dokumentasjonkrav.DokumentasjonkravResponse
import no.nav.sosialhjelp.modia.soknad.dokumentasjonkrav.DokumentasjonkravService
import no.nav.sosialhjelp.modia.soknad.oppgave.OppgaveResponse
import no.nav.sosialhjelp.modia.soknad.oppgave.OppgaveService
import no.nav.sosialhjelp.modia.tilgang.TilgangskontrollService
import no.nav.sosialhjelp.modia.utils.Ident
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.KILDE_INNSYN_API
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime

internal class SoknadsoversiktControllerTest {

    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val oppgaveService: OppgaveService = mockk()
    private val dokumentasjonkravService: DokumentasjonkravService = mockk()
    private val tilgangskontrollService: TilgangskontrollService = mockk()

    private val controller = SoknadsoversiktController(fiksClient, eventService, oppgaveService, dokumentasjonkravService, tilgangskontrollService)

    private val digisosSak1: DigisosSak = mockk()
    private val digisosSak2: DigisosSak = mockk()
    private val digisosSak3: DigisosSak = mockk()
    private val digisosSak4: DigisosSak = mockk()
    private val digisosSak5: DigisosSak = mockk()

    private val model1: InternalDigisosSoker = mockk()
    private val model2: InternalDigisosSoker = mockk()
    private val model3: InternalDigisosSoker = mockk()
    private val model4: InternalDigisosSoker = mockk()
    private val model5: InternalDigisosSoker = mockk()

    private val sak1: Sak = mockk()
    private val sak2: Sak = mockk()
    private val sak3: Sak = mockk()

    private val utbetaling1: Utbetaling = mockk()
    private val utbetaling2: Utbetaling = mockk()
    private val utbetaling3: Utbetaling = mockk()

    private val oppgaveResponseMock: OppgaveResponse = mockk()
    private val dokumentasjonkravMock: DokumentasjonkravResponse = mockk()

    private val fnr = "11111111111"
    private val id_1 = "123"
    private val id_2 = "456"
    private val id_3 = "789"
    private val id_4 = "101"
    private val id_5 = "101"

    private val hendelseTidspunktSoknad1 = LocalDateTime.now().minusDays(2)
    private val hendelseTidspunktSoknad2 = LocalDateTime.now().minusDays(2)
    private val hendelseTidspunktSoknad3 = LocalDateTime.now().minusDays(4)
    private val hendelseTidspunktSoknad4 = LocalDateTime.now().minusDays(6)
    private val hendelseTidspunktSoknad5 = LocalDateTime.now().minusDays(2)

    private val soknad3Sak1DatoOpprettet = LocalDate.now().minusDays(2)
    private val soknad4Sak1DatoOpprettet = LocalDate.now().minusDays(4)
    private val soknad4Sak2DatoOpprettet = LocalDate.now().minusDays(3)
    private val soknad5Sak1DatoOpprettet = LocalDate.now().minusDays(2)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { tilgangskontrollService.harTilgang(any(), any(), any(), any()) } just Runs

        every { digisosSak1.fiksDigisosId } returns id_1
        every { digisosSak1.sistEndret } returns 0L
        every { digisosSak1.digisosSoker } returns null
        every { digisosSak1.originalSoknadNAV } returns null
        every { eventService.createModel(digisosSak1) } returns model1

        every { digisosSak2.fiksDigisosId } returns id_2
        every { digisosSak2.sistEndret } returns 1000L
        every { digisosSak2.digisosSoker } returns mockk()
        every { digisosSak2.originalSoknadNAV?.timestampSendt } returns System.currentTimeMillis()
        every { eventService.createModel(digisosSak2) } returns model2

        every { digisosSak3.fiksDigisosId } returns id_3
        every { digisosSak3.sistEndret } returns 2000L
        every { digisosSak3.digisosSoker } returns mockk()
        every { digisosSak3.originalSoknadNAV?.timestampSendt } returns System.currentTimeMillis()
        every { eventService.createModel(digisosSak3) } returns model3

        every { digisosSak4.fiksDigisosId } returns id_4
        every { digisosSak4.sistEndret } returns 2000L
        every { digisosSak4.digisosSoker } returns mockk()
        every { digisosSak4.originalSoknadNAV } returns null
        every { eventService.createModel(digisosSak4) } returns model4

        every { digisosSak5.fiksDigisosId } returns id_5
        every { digisosSak5.sistEndret } returns 2000L
        every { digisosSak5.digisosSoker } returns mockk()
        every { digisosSak5.originalSoknadNAV } returns null
        every { eventService.createModel(digisosSak5) } returns model5

        every { oppgaveService.hentOppgaver(id_1) } returns listOf(oppgaveResponseMock, oppgaveResponseMock) // 2 oppgaver
        every { oppgaveService.hentOppgaver(id_2) } returns listOf(oppgaveResponseMock) // 1 oppgave
        every { dokumentasjonkravService.hentDokumentasjonkrav(id_3) } returns listOf(dokumentasjonkravMock) // 1 oppgave
        every { dokumentasjonkravService.hentDokumentasjonkrav(id_4) } returns listOf(dokumentasjonkravMock) // 1 oppgave
        every { dokumentasjonkravService.hentDokumentasjonkrav(id_5) } returns listOf(dokumentasjonkravMock) // 1 oppgave

        every { model1.historikk } returns mutableListOf(
            Hendelse("Tittel", "Beskrivelse", hendelseTidspunktSoknad1, "fil")
        )
        every { model2.historikk } returns mutableListOf(
            Hendelse("Tittel", "Beskrivelse", hendelseTidspunktSoknad2, "fil")
        )
        every { model3.historikk } returns mutableListOf(
            Hendelse("Tittel", "Beskrivelse", hendelseTidspunktSoknad3, "fil")
        )
        every { model4.historikk } returns mutableListOf(
            Hendelse("Tittel", "Beskrivelse", hendelseTidspunktSoknad4, "fil")
        )
        every { model5.historikk } returns mutableListOf(
            Hendelse("Tittel", "Beskrivelse", hendelseTidspunktSoknad5, "fil")
        )

        every { model1.saker } returns mutableListOf()

        every { model2.saker } returns mutableListOf()

        every { model3.saker } returns mutableListOf(
            Sak(
                referanse = "vanlig2",
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = SAK_DEFAULT_TITTEL,
                vedtak = mutableListOf(
                    Vedtak(
                        utfall = UtfallVedtak.DELVIS_INNVILGET,
                        datoFattet = LocalDate.now().plusDays(2)
                    )
                ),
                utbetalinger = mutableListOf(),
                datoOpprettet = soknad3Sak1DatoOpprettet
            )
        )

        every { model4.saker } returns mutableListOf(
            Sak(
                referanse = "papirsøknad med 2 sak",
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = SAK_DEFAULT_TITTEL,
                vedtak = mutableListOf(
                    Vedtak(
                        utfall = UtfallVedtak.INNVILGET,
                        datoFattet = LocalDate.now().minusDays(2)
                    ),
                    Vedtak(
                        utfall = UtfallVedtak.DELVIS_INNVILGET,
                        datoFattet = LocalDate.now().plusDays(2)
                    )
                ),
                utbetalinger = mutableListOf(),
                datoOpprettet = soknad4Sak1DatoOpprettet
            ),
            Sak(
                referanse = "referanse",
                saksStatus = SaksStatus.IKKE_INNSYN,
                tittel = SAK_DEFAULT_TITTEL,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(),
                datoOpprettet = soknad4Sak2DatoOpprettet
            )
        )

        every { model5.saker } returns mutableListOf(
            Sak(
                referanse = "papirsøknad med 1 sak",
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = SAK_DEFAULT_TITTEL,
                vedtak = mutableListOf(
                    Vedtak(
                        utfall = UtfallVedtak.DELVIS_INNVILGET,
                        datoFattet = LocalDate.now().plusDays(2)
                    )
                ),
                utbetalinger = mutableListOf(),
                datoOpprettet = soknad5Sak1DatoOpprettet
            )
        )
    }

    @Test
    fun `getSoknader - skal mappe fra DigisosSak til SoknadResponse`() {
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak1, digisosSak2)

//        every { fiksClient.hentDigisosSak(id_1) } returns digisosSak1
//        every { fiksClient.hentDigisosSak(id_2) } returns digisosSak2

        val hendelse = Hendelse("Tittel", "Beskrivelse", LocalDateTime.now(), "fil")

        every { eventService.createModel(digisosSak1) } returns model1
        every { eventService.createModel(digisosSak2) } returns model2

        every { model1.historikk } returns mutableListOf(hendelse)
        every { model2.historikk } returns mutableListOf(hendelse)

        every { model1.status } returns SoknadsStatus.MOTTATT
        every { model2.status } returns SoknadsStatus.UNDER_BEHANDLING

        every { model1.oppgaver } returns mutableListOf(mockk())
        every { model2.oppgaver } returns mutableListOf(mockk())

        every { sak1.tittel } returns "Livsopphold"
        every { sak2.tittel } returns "Strøm"

        every { model2.saker } returns mutableListOf(sak1, sak2)

        val response = controller.getSoknader("token", Ident(fnr))

        val saker = response.body
        assertThat(saker).isNotNull
        assertThat(saker).hasSize(2)

        if (saker != null && saker.size == 2) {
            val first = saker[0]
            assertThat(first.soknadTittel).isEqualTo("Søknad om økonomisk sosialhjelp")
            assertThat(first.kilde).isEqualTo(KILDE_INNSYN_API)
            assertThat(first.sendt).isNotNull

            val second = saker[1]
            assertThat(second.soknadTittel).isEqualTo("Søknad om økonomisk sosialhjelp")
            assertThat(second.kilde).isEqualTo(KILDE_INNSYN_API)
            assertThat(second.sendt).isNull()
        }
    }

    @Test
    fun `getSoknadDetaljer - skal mappe fra DigisosSak til SoknadDetaljerResponse`() {
        val vilkar1 = Vilkar(
            "referanse",
            "beskrivelse",
            OppgaveStatus.ANNULLERT,
            LocalDateTime.now(),
            LocalDateTime.now(),
            emptyList(),
            null
        )
        val vilkar2 = Vilkar(
            "referanse2",
            "beskrivelse2",
            OppgaveStatus.RELEVANT,
            LocalDateTime.now(),
            LocalDateTime.now(),
            emptyList(),
            null
        )
        val vilkar3 = Vilkar(
            "referanse3",
            "beskrivelse3",
            OppgaveStatus.RELEVANT,
            LocalDateTime.now(),
            LocalDateTime.now(),
            emptyList(),
            null
        )

        every { fiksClient.hentDigisosSak(id_1) } returns digisosSak1
        every { fiksClient.hentDigisosSak(id_2) } returns digisosSak2
        every { fiksClient.hentDigisosSak(id_3) } returns digisosSak3
        every { eventService.createSoknadsoversiktModel(digisosSak1) } returns model1
        every { eventService.createSoknadsoversiktModel(digisosSak2) } returns model2
        every { eventService.createSoknadsoversiktModel(digisosSak3) } returns model3

        every { model1.status } returns SoknadsStatus.MOTTATT
        every { model2.status } returns SoknadsStatus.UNDER_BEHANDLING
        every { model3.status } returns SoknadsStatus.FERDIGBEHANDLET

        every { model1.oppgaver } returns mutableListOf(mockk())
        every { model2.oppgaver } returns mutableListOf(mockk())
        every { model3.oppgaver } returns mutableListOf()

        every { model1.dokumentasjonkrav } returns mutableListOf()
        every { model2.dokumentasjonkrav } returns mutableListOf()
        every { model3.dokumentasjonkrav } returns mutableListOf(mockk())

        every { model1.vilkar } returns mutableListOf(vilkar1)
        every { model2.vilkar } returns mutableListOf(vilkar2)
        every { model3.vilkar } returns mutableListOf(vilkar3)

        every { sak1.tittel } returns "Livsopphold"
        every { sak1.saksStatus } returns SaksStatus.UNDER_BEHANDLING
        every { sak1.utbetalinger } returns mutableListOf(utbetaling1)
        every { utbetaling1.vilkar } returns mutableListOf(vilkar1)

        every { sak2.tittel } returns "Strøm"
        every { sak2.saksStatus } returns SaksStatus.UNDER_BEHANDLING
        every { sak2.utbetalinger } returns mutableListOf(utbetaling2)
        every { utbetaling2.vilkar } returns mutableListOf(vilkar2)

        every { sak3.tittel } returns "Husleie"
        every { sak3.saksStatus } returns SaksStatus.UNDER_BEHANDLING
        every { sak3.utbetalinger } returns mutableListOf(utbetaling3)
        every { utbetaling3.vilkar } returns mutableListOf(vilkar3)

        every { model1.saker } returns mutableListOf()
        every { model2.saker } returns mutableListOf(sak1, sak2)
        every { model3.saker } returns mutableListOf(sak3)

        val response1 = controller.getSoknadDetaljer(id_1, "token", Ident(fnr))
        val digisosSak1 = response1.body

        assertThat(response1.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(digisosSak1).isNotNull
        assertThat(digisosSak1?.soknadTittel).isEqualTo("Søknad om økonomisk sosialhjelp")
        assertThat(digisosSak1?.harOppgaver).isTrue
        assertThat(digisosSak1?.harVilkar).isFalse

        val response2 = controller.getSoknadDetaljer(id_2, "token", Ident(fnr))
        val digisosSak2 = response2.body

        assertThat(response2.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(digisosSak2).isNotNull
        assertThat(digisosSak2?.soknadTittel).contains("Livsopphold", "Strøm")
        assertThat(digisosSak2?.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(digisosSak2?.harOppgaver).isTrue
        assertThat(digisosSak2?.harVilkar).isTrue

        val response3 = controller.getSoknadDetaljer(id_3, "token", Ident(fnr))
        val digisosSak3 = response3.body

        assertThat(response3.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(digisosSak3).isNotNull
        assertThat(digisosSak3?.soknadTittel).contains("Husleie")
        assertThat(digisosSak3?.status).isEqualTo(SoknadsStatus.FERDIGBEHANDLET)
        assertThat(digisosSak3?.harOppgaver).isFalse
        assertThat(digisosSak3?.harVilkar).isTrue
        assertThat(digisosSak3?.harDokumentasjonkrav).isTrue
    }

    @Test
    fun `getSoknadDetaljer - hvis model ikke har noen oppgaver, skal ikke oppgaveService kalles`() {
        every { fiksClient.hentDigisosSak(id_1) } returns digisosSak1
        every { eventService.createSoknadsoversiktModel(digisosSak1) } returns model1

        every { model1.status } returns SoknadsStatus.MOTTATT
        every { model1.oppgaver } returns mutableListOf()
        every { model1.saker } returns mutableListOf()
        every { model1.vilkar } returns mutableListOf()
        every { model1.dokumentasjonkrav } returns mutableListOf()

        val response = controller.getSoknadDetaljer(id_1, "token", Ident(fnr))
        val sak = response.body

        assertThat(sak).isNotNull
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        verify { oppgaveService wasNot Called }

        assertThat(sak?.harOppgaver).isFalse
    }

    @Test
    fun `papirSoknadDato - hvis papirsøknad, ingen sak, søknadsdato (første element i historikk) valgt`() {
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak1)

        assertThat(digisosSak1).isNotNull
        assertThat(model1).isNotNull
        assertThat(model1.saker).isNotNull
        assertThat(model1.saker).isEmpty()
        assertThat(digisosSak1.originalSoknadNAV).isNull()

        val dato1 = controller.papirSoknadDato(digisosSak1)
        assertThat(dato1).isEqualTo(hendelseTidspunktSoknad1.toLocalDate())
    }

    @Test
    fun `papirSoknadDato - hvis papirsøknad, 1 sak, velg saksdato`() {
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak5)
        assertThat(model5).isNotNull
        assertThat(model5.saker).isNotNull
        assertThat(model5.saker).isNotEmpty

        val dato5 = controller.papirSoknadDato(digisosSak5)
        assertThat(dato5).isEqualTo(soknad5Sak1DatoOpprettet)
    }

    @Test
    fun `papirSoknadDato - hvis papirsøknad, 2 sak, velg saksdato til sak 1`() {
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak4)
        assertThat(model4).isNotNull
        assertThat(model4.saker).isNotNull
        assertThat(model4.saker).isNotEmpty

        val dato4 = controller.papirSoknadDato(digisosSak4)
        assertThat(dato4).isEqualTo(soknad4Sak1DatoOpprettet)
        assertThat(dato4).isNotEqualTo(hendelseTidspunktSoknad4)
    }

    @Test
    fun `papirSoknadDato - ikke papirsøknad, 0 sak, returnerer null`() {
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak2)
        assertThat(digisosSak2.originalSoknadNAV).isNotNull
        assertThat(model2).isNotNull
        assertThat(model2.saker).isNotNull
        assertThat(model2.saker).isEmpty()

        val dato2 = controller.papirSoknadDato(digisosSak2)
        assertThat(dato2).isNull()
    }

    @Test
    fun `papirSoknadDato - ikke papirsøknad, 1 sak, returnerer null`() {
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak3)

        assertThat(model3).isNotNull
        assertThat(model3.saker).isNotNull
        assertThat(model3.saker).isNotEmpty
        val dato3 = controller.papirSoknadDato(digisosSak3)
        assertThat(dato3).isNull()
    }
}
