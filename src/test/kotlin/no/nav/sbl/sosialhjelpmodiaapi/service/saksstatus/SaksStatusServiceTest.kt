package no.nav.sbl.sosialhjelpmodiaapi.service.saksstatus

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SaksStatusServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()

    private val service = SaksStatusService(fiksClient, eventService)

    private val mockDigisosSak: DigisosSak = mockk()

    private val tittel = "tittel"
    private val referanse = "referanse"

    @BeforeEach
    fun init() {
        clearMocks(fiksClient, eventService)

        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak
    }

    @Test
    fun `Skal returnere emptyList hvis model_saker er null`() {
        val model = InternalDigisosSoker()
        every { eventService.createModel(any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123")

        assertThat(response).isEmpty()
    }

    @Test
    fun `Skal returnere response med status = UNDER_BEHANDLING`() {
        val now = LocalDate.now()
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(),
                datoOpprettet = now
        ))

        every { eventService.createModel(any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123")

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].status).isEqualTo(SaksStatus.UNDER_BEHANDLING)
        assertThat(response[0].tittel).isEqualTo(tittel)
        assertThat(response[0].datoOpprettet).isEqualTo(now)
        assertThat(response[0].datoAvsluttet).isNull()
        assertThat(response[0].vedtak).isEmpty()
        assertThat(response[0].utfall).isNull()
    }

    @Test
    fun `Skal returnere response med status = FERDIGBEHANDLET ved vedtakFattet uavhengig av utfallet til vedtakFattet`() {
        val now = LocalDate.now()
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING, // overstyres når vedtak finnes
                tittel = tittel,
                vedtak = mutableListOf(Vedtak(
                        utfall = UtfallVedtak.INNVILGET,
                        datoFattet = now)),
                utbetalinger = mutableListOf(),
                datoOpprettet = now
        ))

        every { eventService.createModel(any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123")

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].status).isEqualTo(SaksStatus.FERDIGBEHANDLET)
        assertThat(response[0].tittel).isEqualTo(tittel)
        assertThat(response[0].datoOpprettet).isEqualTo(now)
        assertThat(response[0].datoAvsluttet).isEqualTo(now)
        assertThat(response[0].vedtak).hasSize(1)
        assertThat(response[0].vedtak!![0].vedtakDato).isEqualTo(now)
        assertThat(response[0].vedtak!![0].utfall).isEqualTo(UtfallVedtak.INNVILGET)
        assertThat(response[0].utfall).isEqualTo(UtfallVedtak.INNVILGET)
    }

    @Test
    fun `Skal returnere response med status = FERDIGBEHANDLET og vedtaksfilUrl og DEFAULT_TITTEL`() {
        val now = LocalDate.now()
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING, // overstyres når vedtak finnes
                tittel = DEFAULT_TITTEL,
                vedtak = mutableListOf(Vedtak(
                        utfall = UtfallVedtak.DELVIS_INNVILGET,
                        datoFattet = now)),
                utbetalinger = mutableListOf(),
                datoOpprettet = now
        ))

        every { eventService.createModel(any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123")

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].status).isEqualTo(SaksStatus.FERDIGBEHANDLET)
        assertThat(response[0].tittel).isEqualTo(DEFAULT_TITTEL)
        assertThat(response[0].datoOpprettet).isEqualTo(now)
        assertThat(response[0].datoAvsluttet).isEqualTo(now)
        assertThat(response[0].vedtak).hasSize(1)
        assertThat(response[0].vedtak!![0].vedtakDato).isEqualTo(now)
        assertThat(response[0].vedtak!![0].utfall).isEqualTo(UtfallVedtak.DELVIS_INNVILGET)
        assertThat(response[0].utfall).isEqualTo(UtfallVedtak.DELVIS_INNVILGET)
    }

    @Test
    fun `Skal returnere response med 2 elementer ved 2 Saker`() {
        val now = LocalDate.now()
        val model = InternalDigisosSoker()
        model.saker.addAll(listOf(
                Sak(
                        referanse = referanse,
                        saksStatus = SaksStatus.UNDER_BEHANDLING,
                        tittel = tittel,
                        vedtak = mutableListOf(
                                Vedtak(
                                        utfall = UtfallVedtak.INNVILGET,
                                        datoFattet = now.minusDays(2)),
                                Vedtak(
                                        utfall = UtfallVedtak.DELVIS_INNVILGET,
                                        datoFattet = now.plusDays(2))),
                        utbetalinger = mutableListOf(),
                        datoOpprettet = now),
                Sak(
                        referanse = referanse,
                        saksStatus = SaksStatus.IKKE_INNSYN,
                        tittel = DEFAULT_TITTEL,
                        vedtak = mutableListOf(),
                        utbetalinger = mutableListOf(),
                        datoOpprettet = now
                )
        ))

        every { eventService.createModel(any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123")

        assertThat(response).isNotNull
        assertThat(response).hasSize(2)

        assertThat(response[0].tittel).isEqualTo(tittel)
        assertThat(response[0].datoOpprettet).isEqualTo(now)
        assertThat(response[0].datoAvsluttet).isEqualTo(now.plusDays(2))
        assertThat(response[0].vedtak).hasSize(2)
        assertThat(response[0].vedtak!![0].vedtakDato).isEqualTo(now.minusDays(2))
        assertThat(response[0].vedtak!![0].utfall).isEqualTo(UtfallVedtak.INNVILGET)
        assertThat(response[0].vedtak!![1].vedtakDato).isEqualTo(now.plusDays(2))
        assertThat(response[0].vedtak!![1].utfall).isEqualTo(UtfallVedtak.DELVIS_INNVILGET)
        assertThat(response[0].utfall).isEqualTo(UtfallVedtak.DELVIS_INNVILGET)

        assertThat(response[1].tittel).isEqualTo(DEFAULT_TITTEL)
        assertThat(response[1].datoOpprettet).isEqualTo(now)
        assertThat(response[1].datoAvsluttet).isNull()
        assertThat(response[1].vedtak).isEmpty()
        assertThat(response[1].utfall).isNull()
    }
}