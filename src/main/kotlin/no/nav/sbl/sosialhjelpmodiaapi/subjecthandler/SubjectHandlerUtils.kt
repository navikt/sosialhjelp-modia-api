package no.nav.sbl.sosialhjelpmodiaapi.subjecthandler

import no.nav.sbl.sosialhjelpmodiaapi.isRunningInProd
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

object SubjectHandlerUtils {

    private val log by logger()
    private var subjectHandlerService: SubjectHandler = AzureADSubjectHandlerImpl(SpringTokenValidationContextHolder())

    fun getToken() : String {
        return subjectHandlerService.getToken()
    }

    fun setNewSubjectHandlerImpl(subjectHandlerImpl : SubjectHandler) {
        if (isRunningInProd()) {
            log.error("Forsøker å sette en annen SubjectHandlerImpl i prod!")
            throw RuntimeException("Forsøker å sette en annen SubjectHandlerImpl i prod!")
        } else {
            subjectHandlerService = subjectHandlerImpl
        }
    }

    fun resetSubjectHandlerImpl() {
        subjectHandlerService = AzureADSubjectHandlerImpl(SpringTokenValidationContextHolder())
    }
}