package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.SubjectHandlerUtils
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("mock | mock-alt")
@Configuration
class MockConfig {

    init {
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

}