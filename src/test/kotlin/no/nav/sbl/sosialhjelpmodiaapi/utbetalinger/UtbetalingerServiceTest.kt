package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class UtbetalingerServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()

    private val service = UtbetalingerService(fiksClient, eventService)

    private val mockDigisosSak: DigisosSak = mockk()

    private val token = "token"

    private val tittel = "tittel"
    private val referanse = "referanse"

    @BeforeEach
    fun init() {
        clearMocks(fiksClient, eventService)

        every { mockDigisosSak.fiksDigisosId } returns "some id"
    }

    @Test
    fun `Skal returnere emptyList når model_saker er null`() {
        val model = InternalDigisosSoker()
        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns emptyList()

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isEmpty()
    }

    @Test
    fun `Skal returnere response med 1 utbetaling`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("Sak1", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Nødhjelp", null,
                                LocalDate.of(2019, 8, 10), null, null, null, null, mutableListOf()))
                , vilkar = mutableListOf()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response[0].utbetalinger[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")
    }

    @Test
    fun `Skal returnere response med 2 utbetalinger for 1 måned`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("referanse", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Nødhjelp", null, LocalDate.of(2019, 8, 10), null, null, null, null, mutableListOf()),
                        Utbetaling("Sak2", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Tannlege", null, LocalDate.of(2019, 8, 12), null, null, null, null, mutableListOf())
                ),
                vilkar = mutableListOf()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response[0].utbetalinger[0].utbetalinger).hasSize(2)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")

        assertThat(response[0].utbetalinger[0].utbetalinger[1].tittel).isEqualTo("Tannlege")
        assertThat(response[0].utbetalinger[0].utbetalinger[1].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].utbetalinger[1].utbetalingsdato).isEqualTo("2019-08-12")
    }

    @Test
    fun `Skal returnere response med 1 utbetalinger for 2 måneder`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("referanse", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Nødhjelp", null, LocalDate.of(2019, 8, 10), null, null, null, null, mutableListOf()),
                        Utbetaling("Sak2", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Tannlege", null, LocalDate.of(2019, 9, 12), null, null, null, null, mutableListOf())
                ),
                vilkar = mutableListOf()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotNull
        assertThat(response[0].utbetalinger).hasSize(2)
        assertThat(response[0].utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response[0].utbetalinger[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")

        assertThat(response[0].utbetalinger[1].tittel.toLowerCase()).isEqualTo("september")
        assertThat(response[0].utbetalinger[1].utbetalinger[0].tittel).isEqualTo("Tannlege")
        assertThat(response[0].utbetalinger[1].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[1].utbetalinger[0].utbetalingsdato).isEqualTo("2019-09-12")
    }

    @Test
    fun `Skal returnere response med 1 utbetaling med vilkår`() {
        val model = InternalDigisosSoker()
        val vilkar = Vilkar("vilkar1", mutableListOf(), "Skal hoppe", false)
        val utbetaling1 = Utbetaling("referanse", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Nødhjelp",
                null, LocalDate.of(2019, 8, 10), null, null, null, null, mutableListOf(vilkar))
        vilkar.utbetalinger.add(utbetaling1)
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        utbetaling1,
                        Utbetaling("Sak2", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Tannlege", null,
                                LocalDate.of(2019, 9, 12), null, null, null, null, mutableListOf(vilkar))
                ),
                vilkar = mutableListOf(
                        vilkar
                )
        )
        )

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotNull
        assertThat(response[0].utbetalinger).hasSize(2)
        assertThat(response[0].utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response[0].utbetalinger[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")

        assertThat(response[0].utbetalinger[1].tittel.toLowerCase()).isEqualTo("september")
        assertThat(response[0].utbetalinger[1].utbetalinger[0].tittel).isEqualTo("Tannlege")
        assertThat(response[0].utbetalinger[1].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[1].utbetalinger[0].utbetalingsdato).isEqualTo("2019-09-12")

        assertThat(response[0].utbetalinger[0].utbetalinger[0].vilkar).hasSize(1)
    }

    @Test
    fun `Skal returnere utbetalinger for alle digisosSaker`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("Sak1", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Nødhjelp", null,
                                LocalDate.of(2019, 8, 10), null, null, null, null, mutableListOf()))
                , vilkar = mutableListOf()
        ))

        val model2 = InternalDigisosSoker()
        model2.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.FERDIGBEHANDLET,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.ONE, "Barnehage og SFO", null,
                                LocalDate.of(2019, 9, 10), null, null, null, null, mutableListOf()))
                , vilkar = mutableListOf()
        ))

        val mockDigisosSak2: DigisosSak = mockk()
        val id1 = "some id"
        val id2 = "other id"
        every { mockDigisosSak.fiksDigisosId } returns id1
        every { mockDigisosSak2.fiksDigisosId } returns id2
        every { eventService.createModel(mockDigisosSak, any()) } returns model
        every { eventService.createModel(mockDigisosSak2, any()) } returns model2
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak, mockDigisosSak2)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(2)
        assertThat(response[0].fiksDigisosId).isEqualTo(id1)
        assertThat(response[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response[0].utbetalinger[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")

        assertThat(response[1].fiksDigisosId).isEqualTo(id2)
        assertThat(response[1].utbetalinger).hasSize(1)
        assertThat(response[1].utbetalinger[0].tittel.toLowerCase()).isEqualTo("september")
        assertThat(response[1].utbetalinger[0].utbetalinger).hasSize(1)
        assertThat(response[1].utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Barnehage og SFO")
        assertThat(response[1].utbetalinger[0].utbetalinger[0].belop).isEqualTo(1.0)
        assertThat(response[1].utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-09-10")
    }
}