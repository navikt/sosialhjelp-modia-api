package no.nav.sosialhjelp.modia.fodselsnummer

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.modia.utils.Ident
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

internal class FodselsnummerControllerTest {

    private val fodselsnummerService: FodselsnummerService = mockk()
    private val modiaBaseUrl = "http://localhost:3000/sosialhjelp/modia"
    private val controller = FodselsnummerController(fodselsnummerService, modiaBaseUrl)

    private val ident = Ident("11111111111")
    private val fnrId = "abfcc2e8-9986-48c0-9952-eb6b724df6ce"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `setFodselsnummer - skal lagre fodselsnummer og returnere modia url med fodselsnummer id`() {
        every { fodselsnummerService.setFnrForSalesforce(ident.fnr) } returns fnrId
        val response = controller.setFodselsnummer(ident)

        assertThat(response.body).isNotNull
        assertThat(response.body).isInstanceOf(SetFodselsnummerResponse::class.java)
        val setFodselsnummerResponse = response.body as SetFodselsnummerResponse
        assertThat(setFodselsnummerResponse.modiaSosialhjelpUrl).isEqualTo(modiaBaseUrl)
        assertThat(setFodselsnummerResponse.modiaSosialhjelpFnrUuid).isEqualTo(fnrId)
    }

    @Test
    fun `setFodselsnummer - sette med tomt string og returnere bad request`() {
        val response = controller.setFodselsnummer(Ident(" "))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).isEqualTo("Mangler fødselsnummer!")
    }

    @Test
    fun `hentFodselsnummer - skal returnere fodselsnummer fra cachen`() {
        every { fodselsnummerService.getFnr(fnrId) } returns ident.fnr
        val response = controller.hentFodselsnummer(fnrId)

        assertThat(response.body).isNotNull
        assertThat(response.body).isEqualTo(ident.fnr)
    }

    @Test
    fun `hentFodselsnummer - finne ikke fodselsnummer og returnere not found`() {
        every { fodselsnummerService.getFnr(fnrId) } returns null
        val response = controller.hentFodselsnummer(fnrId)

        assertThat(response.body).isNull()
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}
