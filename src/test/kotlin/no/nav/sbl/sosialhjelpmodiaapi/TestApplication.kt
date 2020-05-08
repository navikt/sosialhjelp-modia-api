package no.nav.sbl.sosialhjelpmodiaapi


import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(TokenGeneratorConfiguration::class)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args).registerShutdownHook()
}
