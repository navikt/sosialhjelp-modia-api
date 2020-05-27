package no.nav.sbl.sosialhjelpmodiaapi.health

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyCheckResult
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Result
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.SelftestResult
import no.nav.sbl.sosialhjelpmodiaapi.utils.Miljo
import no.nav.security.token.support.core.api.Unprotected
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
        val results = runBlocking { checkDependencies() }
        return SelftestResult(
                appName = "sosialhjelp-innsyn-api",
                version = Miljo.getAppImage(),
                result = getOverallSelftestResult(results),
                dependencyCheckResults = results
        )
    }

    private fun getOverallSelftestResult(results: List<DependencyCheckResult>): Result {
        return when {
            results.any { it.result == Result.ERROR } -> Result.ERROR
            results.any { it.result == Result.WARNING } -> Result.WARNING
            else -> Result.OK
        }
    }

    private suspend fun checkDependencies(): List<DependencyCheckResult> {
        return coroutineScope {
            dependencyChecks.map {
                withContext(Dispatchers.Default) { it.check() }
            }
        }
    }
}