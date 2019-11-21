package no.nav.sbl.sosialhjelpmodiaapi.vedlegg

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.VedleggResponse
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService.InternalVedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

internal class VedleggControllerTest {

    private val vedleggService: VedleggService = mockk()

    private val controller = VedleggController(vedleggService)

    private val id = "123"

    private val dokumenttype = "type"
    private val dokumenttype_2 = "type_2"
    private val tilleggsinfo = "tilleggsinfo"

    @BeforeEach
    internal fun setUp() {
        clearMocks(vedleggService)
    }

    @Test
    fun `skal mappe fra InternalVedleggList til VedleggResponseList`() {
        val frist = LocalDateTime.now().plusDays(7)
        val datoLagtTil = LocalDateTime.now()

        every { vedleggService.hentAlleOpplastedeVedlegg(any(), any()) } returns listOf(
                InternalVedlegg(dokumenttype, tilleggsinfo, frist, 1, datoLagtTil)
        )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token")

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (body != null && body.isNotEmpty()) {
            assertThat(body).hasSize(1)

            assertThat(body[0].type).isEqualTo(dokumenttype)
            assertThat(body[0].tilleggsinfo).isEqualTo(tilleggsinfo)
            assertThat(body[0].innsendelsesfrist).isEqualTo(frist)
            assertThat(body[0].datoLagtTil).isEqualTo(datoLagtTil)
        }
    }

    @Test
    fun `skal handtere flere vedlegg`() {
        val frist = LocalDateTime.now().plusDays(7)
        val datoLagtTil = LocalDateTime.now()

        every { vedleggService.hentAlleOpplastedeVedlegg(any(), any()) } returns listOf(
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil),
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil)
        )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token")

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (body != null && body.isNotEmpty()) {
            assertThat(body).hasSize(2)
        }
    }

    @Test
    fun `skal sortere pa innsendelsesfrist forst og deretter datoLagtTil`() {
        val frist = LocalDateTime.now().plusDays(7)
        val frist_2 = LocalDateTime.now().plusDays(6)
        val datoLagtTil = LocalDateTime.now().plusDays(1)
        val datoLagtTil_2 = LocalDateTime.now()

        every { vedleggService.hentAlleOpplastedeVedlegg(any(), any()) } returns listOf(
                InternalVedlegg(dokumenttype, null, frist, 1, datoLagtTil),
                InternalVedlegg(dokumenttype, tilleggsinfo, frist_2, 1, datoLagtTil_2),
                InternalVedlegg(dokumenttype, tilleggsinfo, frist_2, 1, datoLagtTil),
                InternalVedlegg(dokumenttype_2, null, null, 1, datoLagtTil_2),
                InternalVedlegg(dokumenttype_2, tilleggsinfo, frist, 1, datoLagtTil_2)
        )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token")

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (body != null && body.isNotEmpty()) {
            assertThat(body).hasSize(5)

            assertThat(body[0].innsendelsesfrist).isEqualTo(frist)
            assertThat(body[0].datoLagtTil).isEqualTo(datoLagtTil)

            assertThat(body[1].innsendelsesfrist).isEqualTo(frist)
            assertThat(body[1].datoLagtTil).isEqualTo(datoLagtTil_2)

            assertThat(body[2].innsendelsesfrist).isEqualTo(frist_2)
            assertThat(body[2].datoLagtTil).isEqualTo(datoLagtTil)

            assertThat(body[3].innsendelsesfrist).isEqualTo(frist_2)
            assertThat(body[3].datoLagtTil).isEqualTo(datoLagtTil_2)

            assertThat(body[4].innsendelsesfrist).isNull()
            assertThat(body[4].datoLagtTil).isEqualTo(datoLagtTil_2)
        }
    }

    @Test
    fun `vedlegg med antallFiler lik 0 og ingen datoLagtTil`() {
        val frist = LocalDateTime.now().plusDays(7)

        every { vedleggService.hentAlleOpplastedeVedlegg(any(), any()) } returns listOf(
                InternalVedlegg(dokumenttype, null, frist, 0, null)
        )

        val vedleggResponses: ResponseEntity<List<VedleggResponse>> = controller.hentVedlegg(id, "token")

        val body = vedleggResponses.body

        assertThat(body).isNotNull
        if (body != null && body.isNotEmpty()) {
            assertThat(body).hasSize(1)

            assertThat(body[0].type).isEqualTo(dokumenttype)
            assertThat(body[0].tilleggsinfo).isNull()
            assertThat(body[0].innsendelsesfrist).isEqualTo(frist)
            assertThat(body[0].datoLagtTil).isNull()
        }
    }

}