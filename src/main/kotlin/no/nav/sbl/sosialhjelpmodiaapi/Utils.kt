package no.nav.sbl.sosialhjelpmodiaapi

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.companionObject

const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"
const val NAIS_NAMESPACE = "NAIS_NAMESPACE"

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

fun toLocalDateTime(hendelsetidspunkt: String): LocalDateTime {
    return ZonedDateTime.parse(hendelsetidspunkt, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()
}

fun unixToLocalDateTime(tidspunkt: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(tidspunkt), ZoneId.of("Europe/Oslo"))
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }
}

// unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}

fun isRunningInProd(): Boolean {
    return System.getenv(NAIS_CLUSTER_NAME) == "prod-fss" && System.getenv(NAIS_NAMESPACE) == "default"
}

fun resolveSrvUser(): String {
    return getFileAsString("/secrets/serviceuser/srvsosialhjelp-mod/username")
}

fun resolveSrvPassword(): String {
    return getFileAsString("/secrets/serviceuser/srvsosialhjelp-mod/password")
}

private fun getFileAsString(path: String): String {
    return String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8)
}

private fun getProperty(propertyName: String): String {
    return System.getProperty(propertyName, System.getenv(propertyName))
}