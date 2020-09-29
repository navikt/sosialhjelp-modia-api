package no.nav.sbl.sosialhjelpmodiaapi.service.utbetalinger

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.Dokumentasjonkrav
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontorInformasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.Sak
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.SendingType
import no.nav.sbl.sosialhjelpmodiaapi.domain.Utbetaling
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingsStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.Vilkar
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.SubjectHandlerUtils.setNewSubjectHandlerImpl
import no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines.RequestContextServiceImpl
import no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines.RequestContextServiceMock
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertTrue

internal class UtbetalingerServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val requestContextService = RequestContextServiceImpl()

    private val service = UtbetalingerService(fiksClient, eventService, requestContextService)

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

        setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        coEvery { mockDigisosSak.fiksDigisosId } returns digisosId
        coEvery { mockDigisosSak.kommunenummer } returns "0001"
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere emptyList hvis soker ikke har noen digisosSaker`() {
        val model = InternalDigisosSoker()
        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns emptyList()

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr)

        assertThat(response).isEmpty()
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere response med 1 utbetaling`() {
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
                                annenMottaker = false,
                                kontonummer = "kontonr",
                                utbetalingsmetode = "utbetalingsmetode",
                                vilkar = mutableListOf(),
                                dokumentasjonkrav = mutableListOf(),
                                datoHendelse = LocalDateTime.now()
                        )),
                datoOpprettet = LocalDate.now()
        ))
        model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now(), enhetsnr, enhetsnavn))

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].belop).isEqualTo(10.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-08-10")
        assertThat(response[0].fom).isEqualTo("2019-08-01")
        assertThat(response[0].tom).isEqualTo("2019-08-31")
        assertThat(response[0].mottaker).isEqualTo("utleier")
        assertThat(response[0].kontonummer).isEqualTo("kontonr")
        assertThat(response[0].utbetalingsmetode).isEqualTo("utbetalingsmetode")
        assertThat(response[0].navKontor?.enhetsNr).isEqualTo(enhetsnr)
        assertThat(response[0].navKontor?.enhetsNavn).isEqualTo(enhetsnavn)
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere response med 2 utbetalinger for 1 maned`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, LocalDate.of(2019, 8, 10), null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                        Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Tannlege", null, LocalDate.of(2019, 8, 12), null, null, null,  false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now())
                ),
                datoOpprettet = LocalDate.now()
        ))

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr)

        assertThat(response).isNotNull
        assertThat(response).hasSize(2)
        assertThat(response[0].tittel).isEqualTo("Tannlege")
        assertThat(response[0].belop).isEqualTo(10.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-08-12")
        assertThat(response[1].tittel).isEqualTo("Nødhjelp")
        assertThat(response[1].belop).isEqualTo(10.0)
        assertThat(response[1].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[1].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-08-10")
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere response med 1 utbetaling for 2 maneder`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, LocalDate.of(2019, 8, 10), null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                        Utbetaling("Sak2", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Tannlege", null, LocalDate.of(2019, 9, 12), null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now())
                ),
                datoOpprettet = LocalDate.now()
        ))

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr)

        assertThat(response).isNotNull
        assertThat(response).hasSize(2)
        assertThat(response[0].tittel).isEqualTo("Tannlege")
        assertThat(response[0].belop).isEqualTo(10.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-09-12")

        assertThat(response[1].tittel).isEqualTo("Nødhjelp")
        assertThat(response[1].belop).isEqualTo(10.0)
        assertThat(response[1].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[1].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-08-10")
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere response med 1 utbetaling med vilkar`() {
        val model = InternalDigisosSoker()
        val vilkar = Vilkar("vilkar1", "Skal hoppe", false, LocalDateTime.now(), LocalDateTime.now())
        val utbetaling1 = Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp",
                null, LocalDate.of(2019, 8, 10), null, null, null, false, null, null, mutableListOf(vilkar), mutableListOf(), LocalDateTime.now())
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(utbetaling1),
                datoOpprettet = LocalDate.now()
        ))

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertTrue(response[0].harVilkar)
    }

    @Disabled("disabled frem til det blir bekreftet om dokumentasjonkrav skal være med i response")
    @Test
    fun `hentAlleUtbetalinger skal returnere response med 1 utbetaling med dokumentasjonkrav`() {
        val model = InternalDigisosSoker()
        val dokumentasjonkrav = Dokumentasjonkrav("dokumentasjonskrav", "Skal hoppe", false)
        val utbetaling1 = Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp",
                null, LocalDate.of(2019, 8, 10), null, null, null, false, null, null, mutableListOf(), mutableListOf(dokumentasjonkrav), LocalDateTime.now())
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(utbetaling1),
                datoOpprettet = LocalDate.now()
        ))

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
    }

    @Test
    fun `hentAlleUtbetalinger skal returnere utbetalinger for alle digisosSaker`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("Sak1", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null,
                                LocalDate.of(2019, 8, 10), null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now())),
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
                                LocalDate.of(2019, 9, 12), null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now())),
                datoOpprettet = LocalDate.now()
        ))

        val mockDigisosSak2: DigisosSak = mockk()
        val id1 = "some id"
        val id2 = "other id"

        coEvery { mockDigisosSak.fiksDigisosId } returns id1
        coEvery { mockDigisosSak2.fiksDigisosId } returns id2
        coEvery { mockDigisosSak.kommunenummer } returns "1111"
        coEvery { mockDigisosSak2.kommunenummer } returns "2222"
        coEvery { eventService.createModel(mockDigisosSak) } returns model
        coEvery { eventService.createModel(mockDigisosSak2) } returns model2
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak, mockDigisosSak2)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(2)

        assertThat(response[0].tittel).isEqualTo("Barnehage og SFO")
        assertThat(response[0].belop).isEqualTo(1.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(id2)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-09-12")

        assertThat(response[1].tittel).isEqualTo("Nødhjelp")
        assertThat(response[1].belop).isEqualTo(10.0)
        assertThat(response[1].fiksDigisosId).isEqualTo(id1)
        assertThat(response[1].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-08-10")
    }

    @Test
    internal fun `hentUtbetalingerForDigisosSak skal hente alle utbetalinger for 1 DigisosSak`() {
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
                                annenMottaker = true,
                                kontonummer = "kontonr",
                                utbetalingsmetode = "utbetalingsmetode",
                                vilkar = mutableListOf(),
                                dokumentasjonkrav = mutableListOf(),
                                datoHendelse = LocalDateTime.now()
                        )),
                datoOpprettet = LocalDate.now()
        ))

        every { eventService.createModel(any()) } returns model
        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak

        val response: List<UtbetalingerResponse> = service.hentUtbetalingerForDigisosSak(mockDigisosSak)

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].tittel).isEqualTo("Nødhjelp")
        assertThat(response[0].belop).isEqualTo(10.0)
        assertThat(response[0].fiksDigisosId).isEqualTo(digisosId)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-08-10")
        assertThat(response[0].fom).isEqualTo("2019-08-01")
        assertThat(response[0].tom).isEqualTo("2019-08-31")
        assertThat(response[0].mottaker).isEqualTo("utleier")
        assertThat(response[0].annenMottaker).isTrue()
        assertThat(response[0].kontonummer).isEqualTo("kontonr")
        assertThat(response[0].utbetalingsmetode).isEqualTo("utbetalingsmetode")
    }

    @Test
    fun `hentAlleUtbetalinger skal filtrere vekk annullerte utbetalinger`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(
                        Utbetaling("referanse", UtbetalingsStatus.UTBETALT, BigDecimal.TEN, "Nødhjelp", null, LocalDate.of(2019, 8, 1), null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                        Utbetaling("Sak2", UtbetalingsStatus.PLANLAGT_UTBETALING, BigDecimal.TEN, "Tannlege", LocalDate.of(2019, 9, 1), null, null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                        Utbetaling("Sak3", UtbetalingsStatus.STOPPET, BigDecimal.TEN, "Depositum", null, LocalDate.of(2019, 10, 1), null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now()),
                        Utbetaling("Sak4", UtbetalingsStatus.ANNULLERT, BigDecimal.TEN, "Kinopenger", null, LocalDate.of(2019, 11, 1), null, null, null, false, null, null, mutableListOf(), mutableListOf(), LocalDateTime.now())
                ),
                datoOpprettet = LocalDate.now()
        ))

        coEvery { eventService.createModel(any()) } returns model
        every { fiksClient.hentAlleDigisosSaker(any()) } returns listOf(mockDigisosSak)

        val response: List<UtbetalingerResponse> = service.hentAlleUtbetalinger(fnr)

        assertThat(response).isNotNull
        assertThat(response).hasSize(3)
        assertThat(response[0].tittel).isEqualTo("Depositum")
        assertThat(response[0].status).isEqualTo(UtbetalingsStatus.STOPPET)
        assertThat(response[0].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-10-01")

        assertThat(response[1].tittel).isEqualTo("Tannlege")
        assertThat(response[1].status).isEqualTo(UtbetalingsStatus.PLANLAGT_UTBETALING)
        assertThat(response[1].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-09-01")

        assertThat(response[2].tittel).isEqualTo("Nødhjelp")
        assertThat(response[2].status).isEqualTo(UtbetalingsStatus.UTBETALT)
        assertThat(response[2].utbetalingEllerForfallDigisosSoker).isEqualTo("2019-08-01")
    }
}