package no.nav.sbl.sosialhjelpmodiaapi.oppgave

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.EttersendtInfoNAV
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService.InternalVedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class OppgaveServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val vedleggService: VedleggService = mockk()
    private val service = OppgaveService(fiksClient, eventService, vedleggService)

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
        clearAllMocks()
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
        every { vedleggService.hentEttersendteVedlegg(any(), any(), any(), any()) } returns emptyList()

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver[0].dokumenttype).isEqualTo(type)
        assertThat(oppgaver[0].tilleggsinformasjon).isEqualTo(tillegg)
        assertThat(oppgaver[0].innsendelsesfrist).isEqualTo(frist.toLocalDate())
    }

    @Test
    fun `Should return oppgave without tilleggsinformasjon`() {
        val model = InternalDigisosSoker()
        model.oppgaver.add(Oppgave(type, null, frist, tidspunktForKrav, true))

        every { eventService.createModel(any(), any()) } returns model
        every { vedleggService.hentEttersendteVedlegg(any(), any(), any(), any()) } returns emptyList()

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver[0].dokumenttype).isEqualTo(type)
        assertThat(oppgaver[0].tilleggsinformasjon).isNull()
        assertThat(oppgaver[0].innsendelsesfrist).isEqualTo(frist.toLocalDate())
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
        every { vedleggService.hentEttersendteVedlegg(any(), any(), any(), any()) } returns emptyList()

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver.size == 4)
        assertThat(oppgaver[0].dokumenttype).isEqualTo(type)
        assertThat(oppgaver[0].tilleggsinformasjon).isEqualTo(tillegg)
        assertThat(oppgaver[0].innsendelsesfrist).isEqualTo(frist.toLocalDate())

        assertThat(oppgaver[1].dokumenttype).isEqualTo(type2)
        assertThat(oppgaver[1].tilleggsinformasjon).isEqualTo(tillegg2)
        assertThat(oppgaver[1].innsendelsesfrist).isEqualTo(frist2.toLocalDate())

        assertThat(oppgaver[2].dokumenttype).isEqualTo(type3)
        assertThat(oppgaver[2].tilleggsinformasjon).isEqualTo(tillegg3)
        assertThat(oppgaver[2].innsendelsesfrist).isEqualTo(frist3.toLocalDate())

        assertThat(oppgaver[3].dokumenttype).isEqualTo(type4)
        assertThat(oppgaver[3].tilleggsinformasjon).isEqualTo(tillegg4)
        assertThat(oppgaver[3].innsendelsesfrist).isEqualTo(frist4.toLocalDate())
    }

    @Test
    fun `skal vise info om dokumenter bruker har lastet opp tilknyttet en oppgave`() {
        val model = InternalDigisosSoker()
        model.oppgaver.addAll(listOf(
                Oppgave(type, tillegg, frist, tidspunktForKrav, true),
                Oppgave(type2, null, frist2, tidspunktForKrav, true),
                Oppgave(type3, tillegg3, frist3, tidspunktForKrav, true)))

        every { eventService.createModel(any(), any()) } returns model
        every { vedleggService.hentEttersendteVedlegg(any(), any(), any(), any()) } returns listOf(
                InternalVedlegg(type, tillegg, frist, 1, tidspunktEtterKrav),
                InternalVedlegg(type2, null, frist2, 1, tidspunktEtterKrav),
                InternalVedlegg(type3, tillegg3, frist3, 1, tidspunktFoerKrav),
                InternalVedlegg(type3, null, frist3, 1, tidspunktEtterKrav))

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver).hasSize(3)

        assertThat(oppgaver[0].dokumenttype).isEqualTo(type)
        assertThat(oppgaver[0].tilleggsinformasjon).isEqualTo(tillegg)
        assertThat(oppgaver[0].innsendelsesfrist).isEqualTo(frist.toLocalDate())
        assertThat(oppgaver[0].antallVedlegg).isEqualTo(1)
        assertThat(oppgaver[0].vedleggDatoLagtTil).isEqualTo(tidspunktEtterKrav.toLocalDate())

        assertThat(oppgaver[1].dokumenttype).isEqualTo(type2)
        assertThat(oppgaver[1].tilleggsinformasjon).isNull()
        assertThat(oppgaver[1].innsendelsesfrist).isEqualTo(frist2.toLocalDate())
        assertThat(oppgaver[1].antallVedlegg).isEqualTo(1)
        assertThat(oppgaver[1].vedleggDatoLagtTil).isEqualTo(tidspunktEtterKrav.toLocalDate())

        assertThat(oppgaver[2].dokumenttype).isEqualTo(type3)
        assertThat(oppgaver[2].tilleggsinformasjon).isEqualTo(tillegg3)
        assertThat(oppgaver[2].innsendelsesfrist).isEqualTo(frist3.toLocalDate())
        assertThat(oppgaver[2].antallVedlegg).isEqualTo(0)
        assertThat(oppgaver[2].vedleggDatoLagtTil).isNull()
    }

    // FIXME:
    //  2 oppgaver med samme type og tillegg, men 2 ulike tidspunkt for krav. Hva skjer hvis det finnes 1 vedlegg som matcher begge?
    //  Hvordan vite hvilken oppgave ett opplastet vedlegg hører til?
    //  Slik det er nå, vil ett vedlegg med som matcher 2 oppgaver knyttes til begge oppgaver.
    @Test
    internal fun `2 oppgaver med samme type og tillegg - hva skjer med vedlegg som matcher begge`() {
        val model = InternalDigisosSoker()
        model.oppgaver.addAll(listOf(
                Oppgave(type, tillegg, frist, tidspunktForKrav, true),
                Oppgave(type, tillegg, frist2, tidspunktForKrav.plusDays(1), true)))

        every { eventService.createModel(any(), any()) } returns model
        every { vedleggService.hentEttersendteVedlegg(any(), any(), any(), any()) } returns listOf(
                InternalVedlegg(type, tillegg, frist, 2, tidspunktEtterKrav))

        val oppgaver = service.hentOppgaver("123", token)

        assertThat(oppgaver).isNotNull
        assertThat(oppgaver).hasSize(2)
    }
}