package no.nav.sosialhjelp.modia.utbetalinger

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.digisossak.domain.Dokumentasjonkrav
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.digisossak.domain.SendingType
import no.nav.sosialhjelp.modia.digisossak.domain.Utbetaling
import no.nav.sosialhjelp.modia.digisossak.domain.UtbetalingsStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Vilkar
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class UtbetalingerServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()

    private val service = UtbetalingerService(fiksClient, eventService)

    private val mockDigisosSak: DigisosSak = mockk()

    private val fnr = "fnr"

    private val digisosId = "some id"

    private val tittel = "tittel"
    private val referanse = "referanse"

    private val enhetsnr = "1337"
    private val enhetsnavn = "NAV nav nav"

    @BeforeEach
    fun init() {
        clearAllMocks()

        coEvery { mockDigisosSak.fiksDigisosId } returns digisosId
        coEvery { mockDigisosSak.kommunenummer } returns "0001"
        coEvery { mockDigisosSak.sistEndret } returns ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere emptyList hvis soker ikke har noen digisosSaker`() {
        val model = InternalDigisosSoker()
        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns emptyList()

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 12, null, null)

        assertThat(response).isEmpty()
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere response med 1 utbetaling`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val fom = LocalDate.now().withDayOfMonth(1).minusMonths(1)
        val tom = LocalDate.now().withDayOfMonth(1).minusDays(1)

        val model = InternalDigisosSoker()
        model.utbetalinger.add(
            Utbetaling(
                referanse = "Sak1",
                status = UtbetalingsStatus.UTBETALT,
                belop = BigDecimal.TEN,
                beskrivelse = "Nødhjelp",
                forfallsDato = null,
                utbetalingsDato = utbetalingsdato,
                fom = fom,
                tom = tom,
                mottaker = "utleier",
                annenMottaker = false,
                kontonummer = "kontonr",
                utbetalingsmetode = "utbetalingsmetode",
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(),
                datoHendelse = LocalDateTime.now()
            )
        )
        model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now(), enhetsnr, enhetsnavn))

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, null)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].belop).isEqualTo(10.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
        assertThat(response[0].fom).isEqualTo(fom)
        assertThat(response[0].tom).isEqualTo(tom)
        assertThat(response[0].mottaker).isEqualTo("utleier")
        assertThat(response[0].kontonummer).isEqualTo("kontonr")
        assertThat(response[0].utbetalingsmetode).isEqualTo("utbetalingsmetode")
        assertThat(response[0].navKontor?.enhetsNr).isEqualTo(enhetsnr)
        assertThat(response[0].navKontor?.enhetsNavn).isEqualTo(enhetsnavn)
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere response med 2 utbetalinger for 1 maned`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val utbetalingsdato2 = LocalDate.now().withDayOfMonth(10).minusMonths(1)

        val model = InternalDigisosSoker()
        model.utbetalinger.addAll(
            mutableListOf(
                Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, utbetalingsdato, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Tannlege", null, utbetalingsdato2, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, null)

        assertThat(response).isNotNull
        assertThat(response).hasSize(2)
        assertThat(response[0].tittel).isEqualTo("Tannlege")
        assertThat(response[0].belop).isEqualTo(10.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato2)
        assertThat(response[1].tittel).isEqualTo("Nødhjelp")
        assertThat(response[1].belop).isEqualTo(10.0)
        assertThat(response[1].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[1].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere response med 1 utbetaling for 2 maneder`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(2)
        val utbetalingsdato2 = LocalDate.now().withDayOfMonth(5).minusMonths(1)

        val model = InternalDigisosSoker()
        model.utbetalinger.addAll(
            mutableListOf(
                Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, utbetalingsdato, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Tannlege", null, utbetalingsdato2, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, null)

        assertThat(response).isNotNull
        assertThat(response).hasSize(2)
        assertThat(response[0].tittel).isEqualTo("Tannlege")
        assertThat(response[0].belop).isEqualTo(10.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato2)

        assertThat(response[1].tittel).isEqualTo("Nødhjelp")
        assertThat(response[1].belop).isEqualTo(10.0)
        assertThat(response[1].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[1].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere response med 1 utbetaling med vilkar`() {
        val model = InternalDigisosSoker()
        val vilkar = Vilkar("vilkar1", "Skal hoppe", false, LocalDateTime.now(), LocalDateTime.now())
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val utbetaling1 = Utbetaling(
            "referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp",
            null, utbetalingsdato, null, null, null, false, null, null, mutableListOf(vilkar), mutableListOf(), LocalDateTime.now()
        )
        model.utbetalinger.add(utbetaling1)

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, null)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].harVilkar).isTrue
    }

    @Disabled("disabled frem til det blir bekreftet om dokumentasjonkrav skal være med i response")
    @Test
    fun `hentAlleUtbetalinger skal returnere response med 1 utbetaling med dokumentasjonkrav`() {
        val model = InternalDigisosSoker()
        val dokumentasjonkrav = Dokumentasjonkrav("dokumentasjonskrav", "Skal hoppe", false)
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val utbetaling1 = Utbetaling(
            "referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp",
            null, utbetalingsdato, null, null, null, false, null, null, mutableListOf(), mutableListOf(dokumentasjonkrav), LocalDateTime.now()
        )
        model.utbetalinger.add(utbetaling1)

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, null)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere utbetalinger for alle digisosSaker`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val utbetalingsdato2 = LocalDate.now().withDayOfMonth(10).minusMonths(1)
        val model = InternalDigisosSoker()
        model.utbetalinger.add(
            Utbetaling(
                "Sak1", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null,
                utbetalingsdato, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()
            )
        )

        val model2 = InternalDigisosSoker()
        model2.utbetalinger.add(
            Utbetaling(
                "Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.ONE, "Barnehage og SFO", null,
                utbetalingsdato2, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()
            )
        )

        val mockDigisosSak2: DigisosSak = mockk()
        val id1 = "some id"
        val id2 = "other id"

        coEvery { mockDigisosSak.fiksDigisosId } returns id1
        coEvery { mockDigisosSak2.fiksDigisosId } returns id2
        coEvery { mockDigisosSak.kommunenummer } returns "1111"
        coEvery { mockDigisosSak2.kommunenummer } returns "2222"
        coEvery { mockDigisosSak.sistEndret } returns ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli()
        coEvery { mockDigisosSak2.sistEndret } returns ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli()
        coEvery { eventService.createModel(mockDigisosSak) } returns model
        coEvery { eventService.createModel(mockDigisosSak2) } returns model2
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak, mockDigisosSak2)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, null)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(2)

        assertThat(response[0].tittel).isEqualTo("Barnehage og SFO")
        assertThat(response[0].belop).isEqualTo(1.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(id2)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato2)

        assertThat(response[1].tittel).isEqualTo("Nødhjelp")
        assertThat(response[1].belop).isEqualTo(10.0)
        assertThat(response[1].fiksDigisosId).isEqualTo(id1)
        assertThat(response[1].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
    }

    @Test
    internal fun `hentUtbetalingerForDigisosSak skal hente alle utbetalinger for 1 DigisosSak`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val fom = LocalDate.now().withDayOfMonth(1).minusMonths(1)
        val tom = LocalDate.now().withDayOfMonth(1).minusDays(1)

        val model = InternalDigisosSoker()
        model.utbetalinger.add(
            Utbetaling(
                referanse = "Sak1",
                status = UtbetalingsStatus.UTBETALT,
                belop = BigDecimal.TEN,
                beskrivelse = "Nødhjelp",
                forfallsDato = null,
                utbetalingsDato = utbetalingsdato,
                fom = fom,
                tom = tom,
                mottaker = "utleier",
                annenMottaker = true,
                kontonummer = "kontonr",
                utbetalingsmetode = "utbetalingsmetode",
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf(),
                datoHendelse = LocalDateTime.now()
            )
        )

        every { eventService.createModel(any()) } returns model
        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak

        val response: List<UtbetalingerResponse> = service.hentUtbetalingerForDigisosSak(mockDigisosSak)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].belop).isEqualTo(10.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
        assertThat(response[0].fom).isEqualTo(fom)
        assertThat(response[0].tom).isEqualTo(tom)
        assertThat(response[0].mottaker).isEqualTo("utleier")
        assertThat(response[0].annenMottaker).isTrue
        assertThat(response[0].kontonummer).isEqualTo("kontonr")
        assertThat(response[0].utbetalingsmetode).isEqualTo("utbetalingsmetode")
    }

    @Test
    fun `hentAlleUtbetalinger skal filtrere vekk annullerte utbetalinger`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(2).minusMonths(1)
        val utbetalingsdato2 = LocalDate.now().withDayOfMonth(3).minusMonths(1)
        val utbetalingsdato3 = LocalDate.now().withDayOfMonth(4).minusMonths(1)
        val utbetalingsdato4 = LocalDate.now().withDayOfMonth(5).minusMonths(1)

        val model = InternalDigisosSoker()
        model.utbetalinger.addAll(
            mutableListOf(
                Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, utbetalingsdato, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                Utbetaling("Sak2", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Tannlege", utbetalingsdato2, null, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                Utbetaling("Sak3", UtbetalingsStatus.STOPPET, BigDecimal.TEN, "Depositum", null, utbetalingsdato3, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                Utbetaling("Sak4", UtbetalingsStatus.ANNULLERT, BigDecimal.TEN, "Kinopenger", null, utbetalingsdato4, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, null)

        assertThat(response).isNotNull
        assertThat(response).hasSize(3)
        assertThat(response[0].tittel).isEqualTo("Depositum")
        assertThat(response[0].status).isEqualTo(UtbetalingsStatus.STOPPET)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato3)

        assertThat(response[1].tittel).isEqualTo("Tannlege")
        assertThat(response[1].status).isEqualTo(UtbetalingsStatus.PLANLAGT_UTBETALING)
        assertThat(response[1].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato2)

        assertThat(response[2].tittel).isEqualTo("Nødhjelp")
        assertThat(response[2].status).isEqualTo(UtbetalingsStatus.UTBETALT)
        assertThat(response[2].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
    }

    @Test
    fun `hentAlleUtbetalinger skal filtrere ned til 0 utbetalinger`() {
        val model = InternalDigisosSoker()
        val vilkar = Vilkar("vilkar1", "Skal hoppe", false, LocalDateTime.now(), LocalDateTime.now())
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val utbetaling1 = Utbetaling(
            "referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp",
            null, utbetalingsdato, null, null, null, false, null, null, mutableListOf(vilkar), mutableListOf(), LocalDateTime.now()
        )
        model.utbetalinger.add(utbetaling1)

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 0, null, null)

        assertThat(response).isNotNull
        assertThat(response).hasSize(0)
    }

    @Test
    fun `hentAlleUtbetalinger skal filtrere bort utbetalinger til gamle saker`() {
        val model = InternalDigisosSoker()
        val vilkar = Vilkar("vilkar1", "Skal hoppe", false, LocalDateTime.now(), LocalDateTime.now())
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val utbetaling1 = Utbetaling(
            "referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp",
            null, utbetalingsdato, null, null, null, false, null, null, mutableListOf(vilkar), mutableListOf(), LocalDateTime.now()
        )
        model.utbetalinger.add(utbetaling1)

        coEvery { eventService.createModel(any()) } returns model
        coEvery { mockDigisosSak.sistEndret } returns ZonedDateTime.now(ZoneId.of("UTC")).minusMonths(5).toInstant().toEpochMilli()
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, null)

        assertThat(response).isNotNull
        assertThat(response).hasSize(0)
    }

    @Test
    fun `bare fom er satt`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val fom = LocalDate.now().withDayOfMonth(1).minusMonths(1)
        val sistEndretFørFom = ZonedDateTime.of(fom.minusDays(1).atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli()

        val model = InternalDigisosSoker()
        model.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak1", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, utbetalingsdato, null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )
        model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now(), enhetsnr, enhetsnavn))

        val modelFørFom = InternalDigisosSoker()
        modelFørFom.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, fom.minusDays(1), null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )

        val digisosSak: DigisosSak = mockk()
        coEvery { digisosSak.fiksDigisosId } returns digisosId
        coEvery { digisosSak.kommunenummer } returns "0001"
        coEvery { digisosSak.sistEndret } returns ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli()
        coEvery { eventService.createModel(digisosSak) } returns model

        val digisosSakFørFom: DigisosSak = mockk()
        coEvery { digisosSakFørFom.fiksDigisosId } returns digisosId
        coEvery { digisosSakFørFom.kommunenummer } returns "0001"
        coEvery { digisosSakFørFom.sistEndret } returns sistEndretFørFom
        coEvery { eventService.createModel(digisosSakFørFom) } returns modelFørFom

        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak, digisosSakFørFom)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, fom, null)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
    }

    @Test
    fun `bare tom er satt`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val tom = LocalDate.now().withDayOfMonth(1).minusDays(1)
        val sistEndretFørTom = ZonedDateTime.of(tom.minusDays(1).atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli()
        val sistEndretEtterTom = ZonedDateTime.of(tom.plusDays(1).atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli()

        val model = InternalDigisosSoker()
        model.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak1", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, utbetalingsdato, null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )
        model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now(), enhetsnr, enhetsnavn))

        val modelEtterTom = InternalDigisosSoker()
        modelEtterTom.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, tom.plusDays(1), null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )

        val digisosSak: DigisosSak = mockk()
        coEvery { digisosSak.fiksDigisosId } returns digisosId
        coEvery { digisosSak.kommunenummer } returns "0001"
        coEvery { digisosSak.sistEndret } returns sistEndretFørTom
        coEvery { eventService.createModel(digisosSak) } returns model

        val digisosSakEtterTom: DigisosSak = mockk()
        coEvery { digisosSakEtterTom.fiksDigisosId } returns digisosId
        coEvery { digisosSakEtterTom.kommunenummer } returns "0001"
        coEvery { digisosSakEtterTom.sistEndret } returns sistEndretEtterTom
        coEvery { eventService.createModel(digisosSakEtterTom) } returns modelEtterTom

        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak, digisosSakEtterTom)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, null, tom)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
    }

    @Test
    fun `fom og tom er satt`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val fom = LocalDate.now().withDayOfMonth(1).minusMonths(1)
        val tom = LocalDate.now().withDayOfMonth(1).minusDays(1)
        val sistEndretFørFom = ZonedDateTime.of(fom.minusDays(1).atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli()
        val sistEndretMellomFomOgTom = ZonedDateTime.of(fom.plusDays(1).atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli()
        val sistEndretEtterTom = ZonedDateTime.of(tom.plusDays(1).atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli()

        val model = InternalDigisosSoker()
        model.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak1", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, utbetalingsdato, null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )
        model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now(), enhetsnr, enhetsnavn))

        val modelFørFom = InternalDigisosSoker()
        modelFørFom.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, fom.minusDays(1), null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )

        val modelEtterTom = InternalDigisosSoker()
        modelEtterTom.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, tom.plusDays(1), null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )

        val digisosSak: DigisosSak = mockk()
        coEvery { digisosSak.fiksDigisosId } returns digisosId
        coEvery { digisosSak.kommunenummer } returns "0001"
        coEvery { digisosSak.sistEndret } returns sistEndretMellomFomOgTom
        coEvery { eventService.createModel(digisosSak) } returns model

        val digisosSakFørFom: DigisosSak = mockk()
        coEvery { digisosSakFørFom.fiksDigisosId } returns digisosId
        coEvery { digisosSakFørFom.sistEndret } returns sistEndretFørFom
        coEvery { digisosSakFørFom.kommunenummer } returns "0001"
        coEvery { eventService.createModel(digisosSakFørFom) } returns modelFørFom

        val digisosSakEtterTom: DigisosSak = mockk()
        coEvery { digisosSakEtterTom.fiksDigisosId } returns digisosId
        coEvery { digisosSakEtterTom.sistEndret } returns sistEndretEtterTom
        coEvery { digisosSakEtterTom.kommunenummer } returns "0001"
        coEvery { eventService.createModel(digisosSakEtterTom) } returns modelEtterTom

        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak, digisosSakFørFom, digisosSakEtterTom)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, fom, tom)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo(utbetalingsdato)
    }

    @Test
    fun `fom og tom er samme dag og utbetalingsdato er utenfor intervall`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(1).plusDays(1)
        val fom = LocalDate.now().withDayOfMonth(1)
        val tom = LocalDate.now().withDayOfMonth(1)
        val sistEndret = ZonedDateTime.of(fom.plusDays(1).atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli()

        val model = InternalDigisosSoker()
        model.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak1", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, utbetalingsdato, null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )
        model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now(), enhetsnr, enhetsnavn))

        val digisosSak: DigisosSak = mockk()
        coEvery { digisosSak.fiksDigisosId } returns digisosId
        coEvery { digisosSak.kommunenummer } returns "0001"
        coEvery { digisosSak.sistEndret } returns sistEndret
        coEvery { eventService.createModel(digisosSak) } returns model

        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr, 3, fom, tom)

        assertThat(response).isEmpty()
    }

    @Test
    fun `skal gi feil - fom er etter tom`() {
        val utbetalingsdato = LocalDate.now().withDayOfMonth(5).minusMonths(1)
        val fom = LocalDate.now().withDayOfMonth(1).minusMonths(1)

        val model = InternalDigisosSoker()
        model.utbetalinger.addAll(
            arrayOf(
                Utbetaling("Sak1", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, utbetalingsdato, null, null, "utleier", false, "kontonr", "utbetalingsmetode", mutableListOf(), mutableListOf(), LocalDateTime.now())
            )
        )
        model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now(), enhetsnr, enhetsnavn))

        val digisosSak: DigisosSak = mockk()

        coEvery { digisosSak.fiksDigisosId } returns digisosId
        coEvery { digisosSak.kommunenummer } returns "0001"
        coEvery { digisosSak.sistEndret } returns ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli()

        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(digisosSak)

        assertThatThrownBy { service.hentAlleUtbetalinger(fnr, 3, fom, fom.minusDays(1)) }
            .isInstanceOf(IllegalStateException::class.java)
    }
}
