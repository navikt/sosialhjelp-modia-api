package no.nav.sosialhjelp.modia.app.health

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.selftest.SelftestResult
import no.nav.sosialhjelp.selftest.SelftestService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

const val APPLICATION_LIVENESS = "Application is alive!"
const val APPLICATION_READY = "Application is ready!"

@Unprotected
@RestController
@RequestMapping(value = ["/internal"])
class HealthController(
    private val selftestService: SelftestService
) {

    @ResponseBody
    @GetMapping(value = ["/isAlive"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun isAlive(): String = APPLICATION_LIVENESS

    @ResponseBody
    @GetMapping(value = ["/isReady"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun isReady(): String = APPLICATION_READY

    @ResponseBody
    @GetMapping("/selftest")
    fun selftest(): SelftestResult {
        return selftestService.getSelftest()
    }
}
