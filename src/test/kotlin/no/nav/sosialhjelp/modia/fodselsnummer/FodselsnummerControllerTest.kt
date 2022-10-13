package no.nav.sosialhjelp.modia.fodselsnummer

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.modia.utils.Ident
import no.nav.sosialhjelp.modia.utils.MiljoUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

internal class FodselsnummerControllerTest {

    private val miljoUtils: MiljoUtils = mockk()
    private val fodselsnummerService: FodselsnummerService = mockk()
    private val controller = FodselsnummerController(miljoUtils, fodselsnummerService)

    private val ident = Ident("11111111111")
    private val fnrId = "abfcc2e8-9986-48c0-9952-eb6b724df6ce"
    private val modiaUrl = "http://localhost:3000/sosialhjelp/modia/abfcc2e8-9986-48c0-9952-eb6b724df6ce"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        every { miljoUtils.isProfileLocal() } returns true
    }

    @Test
    fun `setFodselsnummer - skal lagre fodselsnummer og returnere modia url med fodselsnummer id`() {
        every { fodselsnummerService.set(ident.fnr) } returns fnrId
        val response = controller.setFodselsnummer(ident)
        assertNotNull(response.body)
        assertEquals(modiaUrl, response.body?.melding)
    }

    @Test
    fun `setFodselsnummer - sette med tomt string og returnere bad request`() {
        val response = controller.setFodselsnummer(Ident(" "))
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Mangler fødselsnummer!", response.body?.melding)
    }

    @Test
    fun `hentFodselsnummer - skal returnere fodselsnummer fra cachen`() {
        every { fodselsnummerService.get(fnrId) } returns ident.fnr
        val response = controller.hentFodselsnummer(fnrId)
        assertNotNull(response.body)
        assertEquals(ident.fnr, response.body?.melding)
    }

    @Test
    fun `hentFodselsnummer - finne ikke fodselsnummer og returnere not found`() {
        every { fodselsnummerService.get(fnrId) } returns null
        val response = controller.hentFodselsnummer(fnrId)
        assertNull(response.body)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
