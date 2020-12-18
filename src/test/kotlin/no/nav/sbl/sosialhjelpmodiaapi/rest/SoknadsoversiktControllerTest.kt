package no.nav.sbl.sosialhjelpmodiaapi.rest

import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.OppgaveResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.Sak
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.Utbetaling
import no.nav.sbl.sosialhjelpmodiaapi.domain.Vilkar
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.service.oppgave.OppgaveService
import no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.KILDE_INNSYN_API
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class SoknadsoversiktControllerTest {

    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val oppgaveService: OppgaveService = mockk()
    private val abacService: AbacService = mockk()

    private val controller = SoknadsoversiktController(fiksClient, eventService, oppgaveService, abacService)

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

        every { abacService.harTilgang(any(), any()) } just Runs

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
    fun `hentAlleSaker - skal mappe fra DigisosSak til SakResponse`() {
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak1, digisosSak2)

        every { model1.status } returns SoknadsStatus.MOTTATT
        every { model2.status } returns SoknadsStatus.UNDER_BEHANDLING

        every { model1.oppgaver } returns mutableListOf(mockk())
        every { model2.oppgaver } returns mutableListOf(mockk())

        every { sak1.tittel } returns "Livsopphold"
        every { sak2.tittel } returns "Strøm"

        every { model2.saker } returns mutableListOf(sak1, sak2)

        val response = controller.hentAlleSaker("token", Ident(fnr))

        val saker = response.body
        assertThat(saker).isNotNull
        assertThat(saker).hasSize(2)

        if (saker != null && saker.size == 2) {
            val first = saker[0]
            assertThat(first.soknadTittel).isEqualTo("Søknad om økonomisk sosialhjelp")
            assertThat(first.kilde).isEqualTo(KILDE_INNSYN_API)
            assertNotNull(first.sendt)

            val second = saker[1]
            assertThat(second.soknadTittel).isEqualTo("Søknad om økonomisk sosialhjelp")
            assertThat(second.kilde).isEqualTo(KILDE_INNSYN_API)
            assertNull(second.sendt)
        }
    }

    @Test
    fun `hentSaksDetaljer - skal mappe fra DigisosSak til SakResponse for detaljer`() {
        every { fiksClient.hentDigisosSak(id_1) } returns digisosSak1
        every { fiksClient.hentDigisosSak(id_2) } returns digisosSak2
        every { eventService.createSoknadsoversiktModel(digisosSak1) } returns model1
        every { eventService.createSoknadsoversiktModel(digisosSak2) } returns model2

        every { model1.status } returns SoknadsStatus.MOTTATT
        every { model2.status } returns SoknadsStatus.UNDER_BEHANDLING

        every { model1.oppgaver } returns mutableListOf(mockk())
        every { model2.oppgaver } returns mutableListOf(mockk())

        every { sak1.tittel } returns "Livsopphold"
        every { sak1.saksStatus } returns SaksStatus.UNDER_BEHANDLING
        every { sak1.utbetalinger } returns mutableListOf(utbetaling1)
        every { utbetaling1.vilkar } returns mutableListOf(Vilkar("referanse", "beskrivelse", true, LocalDateTime.now(), LocalDateTime.now()))

        every { sak2.tittel } returns "Strøm"
        every { sak2.saksStatus } returns SaksStatus.UNDER_BEHANDLING
        every { sak1.utbetalinger } returns mutableListOf(utbetaling2)
        every { utbetaling2.vilkar } returns mutableListOf(Vilkar("referanse2", "beskrivelse2", false, LocalDateTime.now(), LocalDateTime.now()))

        every { model1.saker } returns mutableListOf()
        every { model2.saker } returns mutableListOf(sak1, sak2)

        val response1 = controller.hentSaksDetaljer(id_1, "token", Ident(fnr))
        val digisosSak1 = response1.body

        assertThat(response1.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(digisosSak1).isNotNull
        assertThat(digisosSak1?.soknadTittel).isEqualTo("Søknad om økonomisk sosialhjelp")
        assertThat(digisosSak1?.harNyeOppgaver).isTrue
        assertThat(digisosSak1?.harVilkar).isFalse

        val response2 = controller.hentSaksDetaljer(id_2, "token", Ident(fnr))
        val digisosSak2 = response2.body

        assertThat(response2.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(digisosSak2).isNotNull
        assertThat(digisosSak2?.soknadTittel).contains("Livsopphold", "Strøm")
        assertThat(digisosSak2?.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(digisosSak2?.harNyeOppgaver).isTrue
        assertThat(digisosSak2?.harVilkar).isTrue
    }

    @Test
    fun `hentSaksDetaljer - hvis model ikke har noen oppgaver, skal ikke oppgaveService kalles`() {
        every { fiksClient.hentDigisosSak(id_1) } returns digisosSak1
        every { eventService.createSoknadsoversiktModel(digisosSak1) } returns model1

        every { model1.status } returns SoknadsStatus.MOTTATT
        every { model1.oppgaver } returns mutableListOf()
        every { model1.saker } returns mutableListOf()

        val response = controller.hentSaksDetaljer(id_1, "token", Ident(fnr))
        val sak = response.body

        assertThat(sak).isNotNull
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        verify { oppgaveService wasNot Called }

        assertThat(sak?.harNyeOppgaver).isFalse
    }
}
