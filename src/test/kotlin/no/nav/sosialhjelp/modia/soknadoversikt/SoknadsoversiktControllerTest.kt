package no.nav.sosialhjelp.modia.soknadoversikt

import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.OppgaveStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Sak
import no.nav.sosialhjelp.modia.digisossak.domain.SaksStatus
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Utbetaling
import no.nav.sosialhjelp.modia.digisossak.domain.Vilkar
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.soknad.oppgave.OppgaveResponse
import no.nav.sosialhjelp.modia.soknad.oppgave.OppgaveService
import no.nav.sosialhjelp.modia.tilgang.TilgangskontrollService
import no.nav.sosialhjelp.modia.utils.Ident
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.KILDE_INNSYN_API
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

internal class SoknadsoversiktControllerTest {

    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val oppgaveService: OppgaveService = mockk()
    private val tilgangskontrollService: TilgangskontrollService = mockk()

    private val controller = SoknadsoversiktController(fiksClient, eventService, oppgaveService, tilgangskontrollService)

    private val digisosSak1: DigisosSak = mockk()
    private val digisosSak2: DigisosSak = mockk()

    private val model1: InternalDigisosSoker = mockk()
    private val model2: InternalDigisosSoker = mockk()

    private val sak1: Sak = mockk()
    private val sak2: Sak = mockk()

    private val utbetaling1: Utbetaling = mockk()
    private val utbetaling2: Utbetaling = mockk()

    private val oppgaveResponseMock: OppgaveResponse = mockk()

    private val fnr = "11111111111"
    private val id_1 = "123"
    private val id_2 = "456"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { tilgangskontrollService.harTilgang(any(), any(), any(), any()) } just Runs

        every { digisosSak1.fiksDigisosId } returns id_1
        every { digisosSak1.sistEndret } returns 0L
        every { digisosSak1.digisosSoker } returns null
        every { digisosSak1.originalSoknadNAV } returns null

        every { digisosSak2.fiksDigisosId } returns id_2
        every { digisosSak2.sistEndret } returns 1000L
        every { digisosSak2.digisosSoker } returns mockk()
        every { digisosSak2.originalSoknadNAV?.timestampSendt } returns System.currentTimeMillis()

        every { oppgaveService.hentOppgaver(id_1) } returns listOf(oppgaveResponseMock, oppgaveResponseMock) // 2 oppgaver
        every { oppgaveService.hentOppgaver(id_2) } returns listOf(oppgaveResponseMock) // 1 oppgave
    }

    @Test
    fun `getSoknader - skal mappe fra DigisosSak til SoknadResponse`() {
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak1, digisosSak2)

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

        every { fiksClient.hentDigisosSak(id_1) } returns digisosSak1
        every { fiksClient.hentDigisosSak(id_2) } returns digisosSak2
        every { eventService.createSoknadsoversiktModel(digisosSak1) } returns model1
        every { eventService.createSoknadsoversiktModel(digisosSak2) } returns model2

        every { model1.status } returns SoknadsStatus.MOTTATT
        every { model2.status } returns SoknadsStatus.UNDER_BEHANDLING

        every { model1.oppgaver } returns mutableListOf(mockk())
        every { model2.oppgaver } returns mutableListOf(mockk())

        every { model1.vilkar } returns mutableListOf(vilkar1)
        every { model2.vilkar } returns mutableListOf(vilkar2)

        every { sak1.tittel } returns "Livsopphold"
        every { sak1.saksStatus } returns SaksStatus.UNDER_BEHANDLING
        every { sak1.utbetalinger } returns mutableListOf(utbetaling1)

        every { utbetaling1.vilkar } returns mutableListOf(vilkar1)

        every { sak2.tittel } returns "Strøm"
        every { sak2.saksStatus } returns SaksStatus.UNDER_BEHANDLING
        every { sak2.utbetalinger } returns mutableListOf(utbetaling2)
        every { utbetaling2.vilkar } returns mutableListOf(vilkar2)

        every { model1.saker } returns mutableListOf()
        every { model2.saker } returns mutableListOf(sak1, sak2)

        val response1 = controller.getSoknadDetaljer(id_1, "token", Ident(fnr))
        val digisosSak1 = response1.body

        assertThat(response1.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(digisosSak1).isNotNull
        assertThat(digisosSak1?.soknadTittel).isEqualTo("Søknad om økonomisk sosialhjelp")
        assertThat(digisosSak1?.harNyeOppgaver).isTrue
        assertThat(digisosSak1?.harVilkar).isFalse

        val response2 = controller.getSoknadDetaljer(id_2, "token", Ident(fnr))
        val digisosSak2 = response2.body

        assertThat(response2.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(digisosSak2).isNotNull
        assertThat(digisosSak2?.soknadTittel).contains("Livsopphold", "Strøm")
        assertThat(digisosSak2?.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(digisosSak2?.harNyeOppgaver).isTrue
        assertThat(digisosSak2?.harVilkar).isTrue
    }

    @Test
    fun `getSoknadDetaljer - hvis model ikke har noen oppgaver, skal ikke oppgaveService kalles`() {
        every { fiksClient.hentDigisosSak(id_1) } returns digisosSak1
        every { eventService.createSoknadsoversiktModel(digisosSak1) } returns model1

        every { model1.status } returns SoknadsStatus.MOTTATT
        every { model1.oppgaver } returns mutableListOf()
        every { model1.saker } returns mutableListOf()
        every { model1.vilkar } returns mutableListOf()

        val response = controller.getSoknadDetaljer(id_1, "token", Ident(fnr))
        val sak = response.body

        assertThat(sak).isNotNull
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        verify { oppgaveService wasNot Called }

        assertThat(sak?.harNyeOppgaver).isFalse
    }
}
