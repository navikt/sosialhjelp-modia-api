package no.nav.sbl.sosialhjelpmodiaapi.health

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.AbstractDependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyCheckResult
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Result
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.SelftestResult
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

const val APPLICATION_LIVENESS = "Application is alive!"
const val APPLICATION_READY = "Application is ready!"

@Unprotected
@RestController
@RequestMapping(value = ["/internal"])
class HealthController(private val dependencyCheckList: List<AbstractDependencyCheck>) {

    val isAlive: String
        @ResponseBody
        @GetMapping(value = ["/isAlive"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = APPLICATION_LIVENESS

    val isReady: String
        @ResponseBody
        @GetMapping(value = ["/isReady"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = APPLICATION_READY

    @FlowPreview
    @GetMapping("/selftest")
    @ResponseBody
    fun selftest(): SelftestResult {
        val results = ArrayList<DependencyCheckResult>()
        runBlocking { checkDependencies(results) }
        return SelftestResult(
                "sosialhjelp-modia-api",
                "version",
                getOverallSelftestResult(results),
                results
        )
    }

    // Hvis appen skal hindres fra å starte dersom kritiske avhengigheter er nede
//    private fun isAnyVitalDependencyUnhealthy(results: List<Result>): Boolean {
//        return results.stream().anyMatch { result -> result == Result.ERROR }
//    }

    private fun getOverallSelftestResult(results: List<DependencyCheckResult>): Result {
        if (results.stream().anyMatch { result -> result.result == Result.ERROR }) {
            return Result.ERROR
        } else if (results.stream().anyMatch { result -> result.result == Result.WARNING }) {
            return Result.WARNING
        }
        return Result.OK
    }
    // Hvis appen skal hindres fra å starte dersom kritiske avhengigheter er nede
//    private fun checkCriticalDependencies(results: MutableList<DependencyCheckResult>) {
//        Flowable.fromIterable(dependencyCheckList)
//                .filter { it.importance ==  Importance.CRITICAL }
//                .parallel()
//                .runOn(Schedulers.io())
//                .map { it.check().get() }
//                .sequential().blockingSubscribe{ results.add(it) }
//    }

    @FlowPreview
    private suspend fun checkDependencies(results: MutableList<DependencyCheckResult>) {
        dependencyCheckList
                .asFlow()
                .collect { results.add(it.check().get()) }
    }
}