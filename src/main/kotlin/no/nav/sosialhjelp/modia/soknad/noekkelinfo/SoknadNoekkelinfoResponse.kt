package no.nav.sosialhjelp.modia.soknad.noekkelinfo

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.sosialhjelp.modia.digisossak.domain.SendingType
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class SoknadNoekkelinfoResponse(
    val tittel: String,
    val status: SoknadsStatus,
    @param:JsonFormat(pattern = "yyyy-MM-dd")
    val sistOppdatert: LocalDate,
    @param:JsonFormat(pattern = "yyyy-MM-dd")
    val sendtEllerMottattTidspunkt: LocalDate?,
    val navKontor: NavKontor?,
    val kommunenavn: String,
    val videresendtHistorikk: List<VideresendtInfo>?,
    @param:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val tidspunktForelopigSvar: LocalDateTime?,
    val papirSoknad: Boolean,
    val kommunenummer: String,
)

data class VideresendtInfo(
    val type: SendingType,
    @param:JsonFormat(pattern = "yyyy-MM-dd")
    val tidspunkt: LocalDate,
    val navKontor: NavKontor,
)

data class NavKontor(
    val enhetsNavn: String,
    val enhetsNr: String,
)
