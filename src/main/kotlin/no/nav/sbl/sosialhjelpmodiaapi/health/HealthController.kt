package no.nav.sbl.sosialhjelpmodiaapi.health

import no.nav.sbl.sosialhjelpmodiaapi.utils.Miljo
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.selftest.DependencyCheck
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
        private val dependencyChecks: List<DependencyCheck>
) {

    val selftestService = SelftestService()

    val isAlive: String
        @ResponseBody
        @GetMapping(value = ["/isAlive"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = APPLICATION_LIVENESS

    val isReady: String
        @ResponseBody
        @GetMapping(value = ["/isReady"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = APPLICATION_READY

    @ResponseBody
    @GetMapping("/selftest")
    fun nySelftest(): SelftestResult {
        return selftestService.getSelftest("sosialhjelp-modia-api", Miljo.getAppImage(), dependencyChecks)
    }

}