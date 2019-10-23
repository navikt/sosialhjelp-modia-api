package no.nav.sbl.sosialhjelpmodiaapi.health.selftest

import com.fasterxml.jackson.annotation.JsonIgnore

data class DependencyCheckResult(
        val endpoint: String?,
        val result: Result?,
        val address: String?,
        val errorMessage: String?,
        val type: DependencyType?,
        val importance: Importance?,
        val responseTime: String?,
        @JsonIgnore
        val throwable: Throwable?
)

data class SelftestResult(
        val appName: String?,
        val version: String?,
        val result: Result?,
        val dependencyCheckResults: List<DependencyCheckResult>?
)

enum class DependencyType {
    REST, DB
}

enum class Importance {
    CRITICAL, WARNING
}

enum class Result {
    UNPINGABLE, OK, WARNING, ERROR
}