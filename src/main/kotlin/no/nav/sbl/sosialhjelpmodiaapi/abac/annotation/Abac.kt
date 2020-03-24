package no.nav.sbl.sosialhjelpmodiaapi.abac.annotation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass


@MustBeDocumented
@Constraint(validatedBy = [ModiaSosialhjelpTilgang::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Abac(
        val message: String = "Ikke tilgang til Modia Sosialhjelp",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)