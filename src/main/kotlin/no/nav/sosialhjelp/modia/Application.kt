package no.nav.sosialhjelp.modia

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
class Application : SpringBootServletInitializer()

fun main(args: Array<String>) {
    runApplication<Application>(*args).registerShutdownHook()
}
