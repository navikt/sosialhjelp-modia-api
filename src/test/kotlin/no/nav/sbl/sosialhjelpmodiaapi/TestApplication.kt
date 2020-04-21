package no.nav.sbl.sosialhjelpmodiaapi


import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(TokenGeneratorConfiguration::class)
class TestApplication

private const val DEFAULT_PORT = 8383

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
    {
        setDefaultProperties(mapOf("server.port" to getPort()))
        setRegisterShutdownHook(true)
    }
}

private fun getPort(): Int {
    if (isRunningOnGCP()) {
        return Integer.parseInt(System.getenv("GCP_PORT"))
    }
    return DEFAULT_PORT
}

private fun isRunningOnGCP(): Boolean {
    return System.getenv().containsKey("GCP_PORT")
}