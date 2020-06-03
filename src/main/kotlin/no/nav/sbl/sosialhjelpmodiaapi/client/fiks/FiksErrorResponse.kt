package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

data class FiksErrorResponse(
        val error: String?,
        val errorCode: Any?,
        val errorId: String?,
        val errorJson: Any?,
        val message: String?,
        val originalPath: String?,
        val path: String?,
        val status: Int?,
        val timestamp: Long?
)