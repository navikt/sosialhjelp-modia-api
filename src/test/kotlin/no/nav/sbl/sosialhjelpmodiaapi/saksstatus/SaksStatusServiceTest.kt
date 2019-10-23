package no.nav.sbl.sosialhjelpmodiaapi.saksstatus

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SaksStatusServiceTest {
    private val eventService: EventService = mockk()

    private val service = SaksStatusService(eventService)

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

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123", token)

        assertThat(response).isEmpty()
    }

    @Test
    fun `Skal returnere response med status = UNDER_BEHANDLING`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(),
                vilkar = mutableListOf()
        ))

        every { eventService.createModel(any(), any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123", token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].status).isEqualTo(SaksStatus.UNDER_BEHANDLING)
        assertThat(response[0].tittel).isEqualTo(tittel)
        assertThat(response[0].vedtaksfilUrlList).isNull()
    }

    @Test
    fun `Skal returnere response med status = FERDIGBEHANDLET ved vedtakFattet uavhengig av utfallet til vedtakFattet`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = tittel,
                vedtak = mutableListOf(Vedtak(
                        utfall = UtfallVedtak.INNVILGET,
                        vedtaksFilUrl = vedtaksfilUrl
                )),
                utbetalinger = mutableListOf(),
                vilkar = mutableListOf()
        ))

        every { eventService.createModel(any(), any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123", token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].status).isEqualTo(SaksStatus.FERDIGBEHANDLET)
        assertThat(response[0].tittel).isEqualTo(tittel)
        assertThat(response[0].vedtaksfilUrlList).hasSize(1)
        assertThat(response[0].vedtaksfilUrlList?.get(0)).isEqualTo(vedtaksfilUrl)
    }

    @Test
    fun `Skal returnere response med status = FERDIGBEHANDLET og vedtaksfilUrl og DEFAULT_TITTEL`() {
        val model = InternalDigisosSoker()
        model.saker.add(Sak(
                referanse = referanse,
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = DEFAULT_TITTEL,
                vedtak = mutableListOf(Vedtak(
                        utfall = UtfallVedtak.INNVILGET,
                        vedtaksFilUrl = vedtaksfilUrl
                )),
                utbetalinger = mutableListOf(),
                vilkar = mutableListOf()
        ))

        every { eventService.createModel(any(), any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123", token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(1)
        assertThat(response[0].status).isEqualTo(SaksStatus.FERDIGBEHANDLET)
        assertThat(response[0].tittel).isEqualTo(DEFAULT_TITTEL)
        assertThat(response[0].vedtaksfilUrlList).hasSize(1)
        assertThat(response[0].vedtaksfilUrlList?.get(0)).isEqualTo(vedtaksfilUrl)
    }

    @Test
    fun `Skal returnere response med 2 elementer ved 2 Saker`() {
        val model = InternalDigisosSoker()
        model.saker.addAll(listOf(
                Sak(
                        referanse = referanse,
                        saksStatus = SaksStatus.UNDER_BEHANDLING,
                        tittel = tittel,
                        vedtak = mutableListOf(
                                Vedtak(
                                        utfall = UtfallVedtak.INNVILGET,
                                        vedtaksFilUrl = vedtaksfilUrl),
                                Vedtak(
                                        utfall = UtfallVedtak.INNVILGET,
                                        vedtaksFilUrl = vedtaksfilUrl)),
                        utbetalinger = mutableListOf(),
                        vilkar = mutableListOf()),
                Sak(
                        referanse = referanse,
                        saksStatus = SaksStatus.IKKE_INNSYN,
                        tittel = DEFAULT_TITTEL,
                        vedtak = mutableListOf(),
                        utbetalinger = mutableListOf(),
                        vilkar = mutableListOf()
                )
        ))

        every { eventService.createModel(any(), any()) } returns model

        val response: List<SaksStatusResponse> = service.hentSaksStatuser("123", token)

        assertThat(response).isNotNull
        assertThat(response).hasSize(2)
        assertThat(response[0].tittel).isEqualTo(tittel)
        assertThat(response[1].tittel).isEqualTo(DEFAULT_TITTEL)

        assertThat(response[0].vedtaksfilUrlList).hasSize(2)
        assertThat(response[1].vedtaksfilUrlList).isNull()
    }
}