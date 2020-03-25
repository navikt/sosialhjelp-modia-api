//package no.nav.sbl.sosialhjelpmodiaapi.abac.annotation
//
//import io.mockk.every
//import io.mockk.mockk
//import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
//import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.assertThatCode
//import org.junit.jupiter.api.Test
//
//internal class ModiaSosialhjelpTilgangTest {
//
//    private val abacService: AbacService = mockk()
//
//    private val tilgang = ModiaSosialhjelpTilgang(abacService)
//
//    @Test
//    internal fun `isValid returnerer true hvis abacService returnerer true`() {
//        every { abacService.harTilgang(any()) } returns true
//
//        val result = tilgang.isValid("token", mockk())
//
//        assertThat(result).isTrue()
//    }
//
//    @Test
//    internal fun `isValid returnerer false hvis abacService returnerer false`() {
//        every { abacService.harTilgang(any()) } returns false
//
//        val result = tilgang.isValid("token", mockk())
//
//        assertThat(result).isFalse()
//    }
//
//    @Test
//    internal fun `isValid returnerer false hvis abacService kaster feil`() {
//        every { abacService.harTilgang(any()) } throws TilgangskontrollException("Ukjent decision", null)
//
//        assertThatCode { tilgang.isValid("token", mockk()) }
//                .isInstanceOf(TilgangskontrollException::class.java)
//                .hasMessage("Ukjent decision")
//    }
//}