package no.nav.sosialhjelp.modia.digisossak.event

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.mockk
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.Soknadsmottaker
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.task.AsyncTaskExecutor

internal class HendelseFoldServiceTest {
    private val meterRegistry = SimpleMeterRegistry()
    private val service = HendelseFoldService(meterRegistry, mockk<SoknadVedleggService>(), mockk<AsyncTaskExecutor>())

    @Test
    fun `records match for unchanged empty model`() {
        service.fold(
            defaultDigisosSak,
            null,
            InternalDigisosSoker(soknadsmottaker = Soknadsmottaker(ENHETSNR, ENHETSNAVN)),
            { emptyList() },
        )

        assertThat(
            meterRegistry
                .get("hendelser_fold_total")
                .tag("result", "match")
                .counter()
                .count(),
        ).isEqualTo(1.0)
    }

    @Test
    fun `records fields for mismatched model`() {
        service.fold(defaultDigisosSak, null, InternalDigisosSoker(), { emptyList() })

        assertThat(
            meterRegistry
                .get("hendelser_fold_total")
                .tag("result", "mismatch")
                .counter()
                .count(),
        ).isEqualTo(1.0)
        assertThat(
            meterRegistry
                .get("hendelser_fold_field_diff_total")
                .tag("path", "soknadsmottaker")
                .counter()
                .count(),
        ).isEqualTo(1.0)
    }
}
