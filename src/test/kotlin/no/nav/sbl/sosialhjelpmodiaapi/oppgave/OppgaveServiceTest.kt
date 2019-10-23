package no.nav.sbl.sosialhjelpmodiaapi.oppgave

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.EttersendtInfoNAV
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class OppgaveServiceTest {

    private val eventService: EventService = mockk()
    private val fiksClient: FiksClient = mockk()
    private val service = OppgaveService(eventService, fiksClient)

    private val mockDigisosSak: DigisosSak = mockk()
    private val mockEttersendtInfoNAV: EttersendtInfoNAV = mockk()

    private val type = "brukskonto"
    private val tillegg = "fraarm"
    private val type2 = "sparekonto"
    private val tillegg2 = "sparegris"
    private val type3 = "bsu"
    private val tillegg3 = "bes svare umiddelbart"
    private val type4 = "pengebinge"
    private val tillegg4 = "Onkel Skrue penger"
    private val tidspunktForKrav = LocalDateTime.now().minusDays(5)
    private val tidspunktFoerKrav = LocalDateTime.now().minusDays(7)
    private val tidspunktEtterKrav = LocalDateTime.now().minusDays(3)
    private val frist = LocalDateTime.now()
    private val frist2 = LocalDateTime.now().plusDays(1)
    private val frist3 = LocalDateTime.now().plusDays(2)
    private val frist4 = LocalDateTime.now().plusDays(3)

    private val token = "token"

    @BeforeEach
    fun init() {
        clearMocks(eventService)
        every { fiksClient.hentDigisosSak(any(), any()) } returns mockDigisosSak
        every { mockDigisosSak.ettersendtInfoNAV } returns mockEttersendtInfoNAV
    }

    @Test
    fun `Should return emptylist`() {
        val model = InternalDigisosSoker()

        every { eventService.createModel(any(), any()) } returns model

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver).isEmpty()
    }

    @Test
    fun `Should return oppgave`() {
        val model = InternalDigisosSoker()
        model.oppgaver.add(Oppgave(type, tillegg, frist, tidspunktForKrav, true))

        every { eventService.createModel(any(), any()) } returns model

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver[0].dokumenttype).isEqualTo(type)
        assertThat(oppgaver[0].tilleggsinformasjon).isEqualTo(tillegg)
        assertThat(oppgaver[0].innsendelsesfrist).isEqualTo(frist.toString())
    }

    @Test
    fun `Should return oppgave without tilleggsinformasjon`() {
        val model = InternalDigisosSoker()
        model.oppgaver.add(Oppgave(type, null, frist, tidspunktForKrav, true))

        every { eventService.createModel(any(), any()) } returns model

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver[0].dokumenttype).isEqualTo(type)
        assertThat(oppgaver[0].tilleggsinformasjon).isNull()
        assertThat(oppgaver[0].innsendelsesfrist).isEqualTo(frist.toString())
    }

    @Test
    fun `Should return list of oppgaver sorted by frist`() {
        val model = InternalDigisosSoker()
        model.oppgaver.addAll(listOf(
                Oppgave(type, tillegg, frist, tidspunktForKrav, true),
                Oppgave(type3, tillegg3, frist3, tidspunktForKrav, true),
                Oppgave(type4, tillegg4, frist4, tidspunktForKrav, true),
                Oppgave(type2, tillegg2, frist2, tidspunktForKrav, true)))

        every { eventService.createModel(any(), any()) } returns model

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver.size == 4)
        assertThat(oppgaver[0].dokumenttype).isEqualTo(type)
        assertThat(oppgaver[0].tilleggsinformasjon).isEqualTo(tillegg)
        assertThat(oppgaver[0].innsendelsesfrist).isEqualTo(frist.toString())

        assertThat(oppgaver[1].dokumenttype).isEqualTo(type2)
        assertThat(oppgaver[1].tilleggsinformasjon).isEqualTo(tillegg2)
        assertThat(oppgaver[1].innsendelsesfrist).isEqualTo(frist2.toString())

        assertThat(oppgaver[2].dokumenttype).isEqualTo(type3)
        assertThat(oppgaver[2].tilleggsinformasjon).isEqualTo(tillegg3)
        assertThat(oppgaver[2].innsendelsesfrist).isEqualTo(frist3.toString())

        assertThat(oppgaver[3].dokumenttype).isEqualTo(type4)
        assertThat(oppgaver[3].tilleggsinformasjon).isEqualTo(tillegg4)
        assertThat(oppgaver[3].innsendelsesfrist).isEqualTo(frist4.toString())
    }

}