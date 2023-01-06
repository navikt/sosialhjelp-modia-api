package no.nav.sosialhjelp.modia.kommune.kartverket

data class KommunenavnProperties(
    val containeditems: List<ContainedItem>,
)

data class ContainedItem(
//    val ValidFrom: String?,
//    val ValidTo: String?,
    val codevalue: String,
//    val dateAccepted: String?,
//    val dateSubmitted: String,
    val description: String,
//    val id: String,
//    val itemclass: String,
//    val label: String,
//    val lang: String,
//    val lastUpdated: String,
//    val owner: String,
//    val seoname: String,
//    val status: String,
//    val uuid: String,
//    val versionNumber: Int
)
