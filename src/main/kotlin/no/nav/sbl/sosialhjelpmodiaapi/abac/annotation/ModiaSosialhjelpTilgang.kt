package no.nav.sbl.sosialhjelpmodiaapi.abac.annotation

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

@Component
class ModiaSosialhjelpTilgang(private val abacService: AbacService) : ConstraintValidator<Abac, String> {

    companion object {
        private val log by logger()
    }

    override fun isValid(token: String?, context: ConstraintValidatorContext?): Boolean {
        if (token != null){
            log.info("Sjekker tilgang mot abac")
            return abacService.harTilgang(token)
        }
        return false
    }

}