package no.nav.sbl.sosialhjelpmodiaapi.abac.annotation

import javax.validation.Constraint


@MustBeDocumented
@Constraint(validatedBy = [ModiaSosialhjelpTilgang::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Abac(
        val message: String = "Ikke tilgang til Modia Sosialhjelp"
)