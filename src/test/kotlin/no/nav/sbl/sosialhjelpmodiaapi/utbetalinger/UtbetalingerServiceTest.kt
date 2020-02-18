package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class UtbetalingerServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()

    private val service = UtbetalingerService(fiksClient, eventService)

    private val mockDigisosSak: DigisosSak = mockk()

    private val token = "token"

    private val digisosId = "some id"

    private val tittel = "tittel"
    private val referanse = "referanse"

    @BeforeEach
    fun init() {
        clearAllMocks()

        every { mockDigisosSak.fiksDigisosId } returns digisosId
    }

    @Test
    fun `Skal returnere emptyList hvis soker ikke har noen digisosSaker`() {
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
                        Utbetaling(
                                referanse = "Sak1",
                                status = UtbetalingsStatus.UTBETALT,
                                belop = BigDecimal.TEN,
                                beskrivelse = "Nødhjelp",
                                forfallsDato = null,
                                utbetalingsDato = LocalDate.of(2019, 8, 10),
                                fom = LocalDate.of(2019, 8, 1),
                                tom = LocalDate.of(2019, 8, 31),
                                mottaker = "utleier",
                                kontonummer = "kontonr",
                                utbetalingsmetode = "utbetalingsmetode",
                                vilkar = mutableListOf(),
                                dokumentasjonkrav = mutableListOf())),
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(),
                datoOpprettet = LocalDate.now()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].ar).isEqualTo(2019)
        assertThat(response[0].maned).isEqualToIgnoringCase("august")
        assertThat(response[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")
        assertThat(response[0].utbetalinger[0].fom).isEqualTo("2019-08-01")
        assertThat(response[0].utbetalinger[0].tom).isEqualTo("2019-08-31")
        assertThat(response[0].utbetalinger[0].mottaker).isEqualTo("utleier")
        assertThat(response[0].utbetalinger[0].kontonummer).isEqualTo("kontonr")
        assertThat(response[0].utbetalinger[0].utbetalingsmetode).isEqualTo("utbetalingsmetode")
    }

    @Test
    fun `Skal returnere response med 2 utbetalinger for 1 maned`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, LocalDate.of(2019, 8, 10), null, null, null, null, null, mutableListOf(), mutableListOf()),
                        Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Tannlege", null, LocalDate.of(2019, 8, 12), null, null, null, null, null, mutableListOf(), mutableListOf())
                ),
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(),
                datoOpprettet = LocalDate.now()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].ar).isEqualTo(2019)
        assertThat(response[0].maned).isEqualToIgnoringCase("august")
        assertThat(response[0].utbetalinger).hasSize(2)
        assertThat(response[0].utbetalinger[0].tittel).isEqualTo("Tannlege")
        assertThat(response[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-12")
        assertThat(response[0].utbetalinger[1].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].utbetalinger[1].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[1].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalinger[1].utbetalingsdato).isEqualTo("2019-08-10")
    }

    @Test
    fun `Skal returnere response med 1 utbetaling for 2 maneder`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, LocalDate.of(2019, 8, 10), null, null, null, null, null, mutableListOf(), mutableListOf()),
                        Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Tannlege", null, LocalDate.of(2019, 9, 12), null, null, null, null, null, mutableListOf(), mutableListOf())
                ),
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(),
                datoOpprettet = LocalDate.now()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(2)
        assertThat(response[0].ar).isEqualTo(2019)
        assertThat(response[0].maned).isEqualToIgnoringCase("september")
        assertThat(response[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].tittel).isEqualTo("Tannlege")
        assertThat(response[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-09-12")

        assertThat(response[1].ar).isEqualTo(2019)
        assertThat(response[1].maned).isEqualToIgnoringCase("august")
        assertThat(response[1].utbetalinger).hasSize(1)
        assertThat(response[1].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[1].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[1].utbetalinger[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[1].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")
    }

    @Test
    fun `Skal returnere response med 1 utbetaling med vilkar`() {
        val model = InternalDigisosSoker()
        val vilkar = Vilkar("vilkar1", mutableListOf(), "Skal hoppe", false)
        val utbetaling1 = Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp",
                null, LocalDate.of(2019, 8, 10), null, null, null, null, null, mutableListOf(vilkar), mutableListOf())
        vilkar.utbetalinger.add(utbetaling1)
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(utbetaling1),
                vilkar = mutableListOf(vilkar),
                dokumentasjonkrav = mutableListOf(),
                datoOpprettet = LocalDate.now()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].harVilkar).isTrue()
    }

    @Disabled("disabled frem til det blir bekreftet om dokumentasjonkrav skal være med i response")
    @Test
    fun `Skal returnere response med 1 utbetaling med dokumentasjonkrav`() {
        val model = InternalDigisosSoker()
        val dokumentasjonkrav = Dokumentasjonkrav("dokumentasjonskrav", mutableListOf(), "Skal hoppe", false)
        val utbetaling1 = Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp",
                null, LocalDate.of(2019, 8, 10), null, null, null, null, null, mutableListOf(), mutableListOf(dokumentasjonkrav))
        dokumentasjonkrav.utbetalinger.add(utbetaling1)
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(utbetaling1),
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(dokumentasjonkrav),
                datoOpprettet = LocalDate.now()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].utbetalinger).hasSize(1)
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
                        Utbetaling("Sak1", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null,
                                LocalDate.of(2019, 8, 10), null, null, null, null, null, mutableListOf(), mutableListOf())),
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(),
                datoOpprettet = LocalDate.now()
        ))

        val model2 = InternalDigisosSoker()
        model2.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.FERDIGBEHANDLET,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.ONE, "Barnehage og SFO", null,
                                LocalDate.of(2019, 9, 12), null, null, null, null, null, mutableListOf(), mutableListOf())),
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(),
                datoOpprettet = LocalDate.now()
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

        assertThat(response[0].ar).isEqualTo(2019)
        assertThat(response[0].maned).isEqualToIgnoringCase("september")
        assertThat(response[0].sum).isEqualTo(1.0)
        assertThat(response[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].tittel).isEqualTo("Barnehage og SFO")
        assertThat(response[0].utbetalinger[0].belop).isEqualTo(1.0)
        assertThat(response[0].utbetalinger[0].fiksDigisosId).isEqualTo(id2)
        assertThat(response[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-09-12")

        assertThat(response[1].ar).isEqualTo(2019)
        assertThat(response[1].maned).isEqualToIgnoringCase("august")
        assertThat(response[1].sum).isEqualTo(10.0)
        assertThat(response[1].utbetalinger).hasSize(1)
        assertThat(response[1].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[1].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[1].utbetalinger[0].fiksDigisosId).isEqualTo(id1)
        assertThat(response[1].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")
    }

    @Test
    internal fun `hentUtbetalinger for DigisosSak`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling(
                                referanse = "Sak1",
                                status = UtbetalingsStatus.UTBETALT,
                                belop = BigDecimal.TEN,
                                beskrivelse = "Nødhjelp",
                                forfallsDato = null,
                                utbetalingsDato = LocalDate.of(2019, 8, 10),
                                fom = LocalDate.of(2019, 8, 1),
                                tom = LocalDate.of(2019, 8, 31),
                                mottaker = "utleier",
                                kontonummer = "kontonr",
                                utbetalingsmetode = "utbetalingsmetode",
                                vilkar = mutableListOf(),
                                dokumentasjonkrav = mutableListOf())),
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(),
                datoOpprettet = LocalDate.now()
        ))

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentDigisosSak(any(), any()) } returns mockDigisosSak

        val response: List<UtbetalingerResponse> = service.hentUtbetalinger(mockDigisosSak, token)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].ar).isEqualTo(2019)
        assertThat(response[0].maned).isEqualToIgnoringCase("august")
        assertThat(response[0].utbetalinger).hasSize(1)
        assertThat(response[0].utbetalinger[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].utbetalinger[0].belop).isEqualTo(10.0)
        assertThat(response[0].utbetalinger[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalinger[0].utbetalingsdato).isEqualTo("2019-08-10")
        assertThat(response[0].utbetalinger[0].fom).isEqualTo("2019-08-01")
        assertThat(response[0].utbetalinger[0].tom).isEqualTo("2019-08-31")
        assertThat(response[0].utbetalinger[0].mottaker).isEqualTo("utleier")
        assertThat(response[0].utbetalinger[0].kontonummer).isEqualTo("kontonr")
        assertThat(response[0].utbetalinger[0].utbetalingsmetode).isEqualTo("utbetalingsmetode")
    }
}