package no.nav.sosialhjelp.modia.soknad.vedlegg

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.sosialhjelp.modia.soknad.vedlegg.VedleggController.VedleggResponse
import no.nav.sosialhjelp.modia.tilgang.TilgangskontrollService
import no.nav.sosialhjelp.modia.utils.Ident
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

internal class VedleggControllerTest {
    private val vedleggService: VedleggService = mockk()
    private val tilgangskontrollService: TilgangskontrollService = mockk()

    private val controller = VedleggController(vedleggService, tilgangskontrollService)

    private val fnr = "11111111111"
    private val id = "123"

    private val dokumenttype = "type"
    private val dokumenttype2 = "type_2"
    private val tilleggsinfo = "tilleggsinfo"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { tilgangskontrollService.harTilgang(any(), any(), any(), any()) } just Runs
    }

    @Test
    suspend fun `skal mappe fra InternalVedleggList til VedleggResponseList`() {
        val frist = LocalDateTime.now().plusDays(7)
        val datoLagtTil = LocalDateTime.now()

        coEvery { vedleggService.hentAlleOpplastedeVedlegg(any()) } returns
            listOf(
                InternalVedlegg(dokumenttype, tilleggsinfo, frist, 1, datoLagtTil, LocalDateTime.now()),
            )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token", Ident(fnr))

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (!body.isNullOrEmpty()) {
            assertThat(body).hasSize(1)

            assertThat(body[0].type).isEqualTo(dokumenttype)
            assertThat(body[0].tilleggsinfo).isEqualTo(tilleggsinfo)
            assertThat(body[0].innsendelsesfrist).isEqualTo(frist)
            assertThat(body[0].datoLagtTil).isEqualTo(datoLagtTil)
        }
    }

    @Test
    suspend fun `skal handtere flere vedlegg`() {
        val frist = LocalDateTime.now().plusDays(7)
        val datoLagtTil = LocalDateTime.now()

        coEvery { vedleggService.hentAlleOpplastedeVedlegg(any()) } returns
            listOf(
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil, LocalDateTime.now()),
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil, LocalDateTime.now()),
            )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token", Ident(fnr))

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (!body.isNullOrEmpty()) {
            assertThat(body).hasSize(2)
        }
    }

    @Test
    suspend fun `skal filtrere bort vedlegg med antall filer som er 0`() {
        val frist = LocalDateTime.now().plusDays(7)
        val datoLagtTil = LocalDateTime.now()

        coEvery { vedleggService.hentAlleOpplastedeVedlegg(any()) } returns
            listOf(
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil, LocalDateTime.now()),
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil, LocalDateTime.now()),
                InternalVedlegg(dokumenttype, null, frist, 0, datoLagtTil, LocalDateTime.now()),
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil, LocalDateTime.now()),
                InternalVedlegg(dokumenttype, null, frist, 0, datoLagtTil, LocalDateTime.now()),
            )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token", Ident(fnr))

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (!body.isNullOrEmpty()) {
            assertThat(body).hasSize(3)
        }
    }

    @Test
    suspend fun `skal sortere pa innsendelsesfrist forst og deretter datoLagtTil`() {
        val frist = LocalDateTime.now().plusDays(7)
        val frist2 = LocalDateTime.now().plusDays(6)
        val datoLagtTil = LocalDateTime.now().plusDays(1)
        val datolagttil2 = LocalDateTime.now()

        coEvery { vedleggService.hentAlleOpplastedeVedlegg(any()) } returns
            listOf(
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil, LocalDateTime.now()),
                InternalVedlegg(dokumenttype, tilleggsinfo, frist2, 1, datolagttil2, LocalDateTime.now()),
                InternalVedlegg(dokumenttype, tilleggsinfo, frist2, 1, datoLagtTil, LocalDateTime.now()),
                InternalVedlegg(dokumenttype2, null, null, 1, datolagttil2, LocalDateTime.now()),
                InternalVedlegg(dokumenttype2, tilleggsinfo, frist, 1, datolagttil2, LocalDateTime.now()),
            )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token", Ident(fnr))

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (!body.isNullOrEmpty()) {
            assertThat(body).hasSize(5)

            assertThat(body[0].innsendelsesfrist).isEqualTo(frist)
            assertThat(body[0].datoLagtTil).isEqualTo(datoLagtTil)

            assertThat(body[1].innsendelsesfrist).isEqualTo(frist)
            assertThat(body[1].datoLagtTil).isEqualTo(datolagttil2)

            assertThat(body[2].innsendelsesfrist).isEqualTo(frist2)
            assertThat(body[2].datoLagtTil).isEqualTo(datoLagtTil)

            assertThat(body[3].innsendelsesfrist).isEqualTo(frist2)
            assertThat(body[3].datoLagtTil).isEqualTo(datolagttil2)

            assertThat(body[4].innsendelsesfrist).isNull()
            assertThat(body[4].datoLagtTil).isEqualTo(datolagttil2)
        }
    }

    @Test
    suspend fun `vedlegg med antallFiler lik 0 og ingen datoLagtTil`() {
        val frist = LocalDateTime.now().plusDays(7)

        coEvery { vedleggService.hentAlleOpplastedeVedlegg(any()) } returns
            listOf(
                InternalVedlegg(dokumenttype, null, frist, 0, null, LocalDateTime.now()),
            )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token", Ident(fnr))

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (!body.isNullOrEmpty()) {
            assertThat(body).hasSize(1)

            assertThat(body[0].type).isEqualTo(dokumenttype)
            assertThat(body[0].tilleggsinfo).isNull()
            assertThat(body[0].innsendelsesfrist).isEqualTo(frist)
            assertThat(body[0].datoLagtTil).isNull()
        }
    }
}
