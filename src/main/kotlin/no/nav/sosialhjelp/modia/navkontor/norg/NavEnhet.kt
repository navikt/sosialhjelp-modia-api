package no.nav.sosialhjelp.modia.navkontor.norg

data class SosialhjelpDigitalSonad(
    val lenke: String,
    val lenketekst: String
)
data class Sosialhelp(
    val digitaleSoeknader: List<SosialhjelpDigitalSonad>,
    val papirsoeknadInformasjon: String
)

data class PublikumsKanal (
    val beskrivelse: String,
    val telefon: String // Todo: trim for mellomrom
)

data class BrukerTjeneste (
    val tjeneste: List<String>,
    val ytterligereInformasjon: String?
)
data class BrukerKontakt (
    val sosialhjelp: Sosialhelp,
    val informasjonUtbetalinger: String?, // TODO: hvis null så skal det stå "Ingen informasjon"
    val publikumskanaler: List<PublikumsKanal>,
    val brukertjenesteTilbud: BrukerTjeneste
)
data class NavEnhetV1(
    val navn: String,
    val enhetNr: String,
    val status: String,
    val sosialeTjenester: String?,
    val type: String?
)
data class NavEnhet(
    val navn: String,
    val enhetNr: String,
    val status: String,
    val aktiveringsdato: String,
    val nedleggelsesdato: String?,
    val brukerKontakt: BrukerKontakt
)
