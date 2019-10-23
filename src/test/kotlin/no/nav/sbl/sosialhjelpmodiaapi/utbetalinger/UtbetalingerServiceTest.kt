package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class UtbetalingerServiceTest {
    private val eventService: EventService = mockk()

    private val service = UtbetalingerService(eventService)

    private val token = "token"

    private val tittel = "tittel"
    private val referanse = "referanse"
    private val vedtaksfilUrl = "url"

    @BeforeEach
    fun init() {
        clearMocks(eventService)
    }

    @Test
    fun `Skal returnere emptyList når model_saker er null`() {
        val model = InternalDigisosSoker()
        every { eventService.createModel(any(), any()) } returns model

        val response: UtbetalingerResponse = service.hentUtbetalinger("123", token)

        assertThat(response.utbetalinger).isEmpty()
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

        val response: UtbetalingerResponse = service.hentUtbetalinger("123", token)

        assertThat(response).isNotNull
        assertThat(response.utbetalinger).hasSize(1)
        assertThat(response.utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response.utbetalinger[0].utbetalinger).hasSize(1)
        assertThat(response.utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response.utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response.utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")
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

        val response: UtbetalingerResponse = service.hentUtbetalinger("123", token)

        assertThat(response).isNotNull
        assertThat(response.utbetalinger).hasSize(1)
        assertThat(response.utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response.utbetalinger[0].utbetalinger).hasSize(2)
        assertThat(response.utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response.utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response.utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")

        assertThat(response.utbetalinger[0].utbetalinger[1].tittel).isEqualTo("Tannlege")
        assertThat(response.utbetalinger[0].utbetalinger[1].belop).isEqualTo(10.0)
        assertThat(response.utbetalinger[0].utbetalinger[1].utbetalingsdato).isEqualTo("2019-08-12")
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

        val response: UtbetalingerResponse = service.hentUtbetalinger("123", token)

        assertThat(response).isNotNull
        assertThat(response.utbetalinger).hasSize(2)
        assertThat(response.utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response.utbetalinger[0].utbetalinger).hasSize(1)
        assertThat(response.utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response.utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response.utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")

        assertThat(response.utbetalinger).hasSize(2)
        assertThat(response.utbetalinger[1].tittel.toLowerCase()).isEqualTo("september")
        assertThat(response.utbetalinger[1].utbetalinger[0].tittel).isEqualTo("Tannlege")
        assertThat(response.utbetalinger[1].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response.utbetalinger[1].utbetalinger[0].utbetalingsdato).isEqualTo("2019-09-12")
        println(response)
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

        val response: UtbetalingerResponse = service.hentUtbetalinger("123", token)

        assertThat(response).isNotNull
        assertThat(response.utbetalinger).hasSize(2)
        assertThat(response.utbetalinger[0].tittel.toLowerCase()).isEqualTo("august")
        assertThat(response.utbetalinger[0].utbetalinger).hasSize(1)
        assertThat(response.utbetalinger[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response.utbetalinger[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response.utbetalinger[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")

        assertThat(response.utbetalinger).hasSize(2)
        assertThat(response.utbetalinger[1].tittel.toLowerCase()).isEqualTo("september")
        assertThat(response.utbetalinger[1].utbetalinger[0].tittel).isEqualTo("Tannlege")
        assertThat(response.utbetalinger[1].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response.utbetalinger[1].utbetalinger[0].utbetalingsdato).isEqualTo("2019-09-12")

        assertThat(response.utbetalinger[0].utbetalinger[0].vilkar).hasSize(1)
    }
}