package no.nav.sbl.sosialhjelpmodiaapi.client.kommunenavn

data class KommunenavnProperties(
    val ContainedItemsResult: ContainedItemsResult,
    val SelectedDOKMunicipality: String,
    val containedItemClass: String,
    val containedSubRegisters: List<Any>,
    val containeditems: List<ContainedItem>,
    val contentsummary: String,
    val id: String,
    val label: String,
    val lang: String,
    val lastUpdated: String,
    val manager: String,
    val owner: String,
    val uuid: String
)

data class ContainedItemsResult(
    val Count: Int,
    val Limit: Int,
    val Offset: Int,
    val Total: Int
)

data class ContainedItem(
    val ValidFrom: String?,
    val ValidTo: String?,
    val codevalue: String,
    val dateAccepted: String?,
    val dateSubmitted: String,
    val description: String,
    val id: String,
    val itemclass: String,
    val label: String,
    val lang: String,
    val lastUpdated: String,
    val owner: String,
    val seoname: String,
    val status: String,
    val uuid: String,
    val versionNumber: Int
)
