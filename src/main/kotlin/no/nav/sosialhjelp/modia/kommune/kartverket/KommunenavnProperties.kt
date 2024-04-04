package no.nav.sosialhjelp.modia.kommune.kartverket

data class KommunenavnProperties(
    val containeditems: List<ContainedItem>
)

data class ContainedItem(
    val codevalue: String,
    val description: String
)
