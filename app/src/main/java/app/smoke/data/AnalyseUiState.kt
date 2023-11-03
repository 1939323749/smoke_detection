package app.smoke.data

import android.net.Uri

data class AnalyseUiState(
    val source:Int=0,
    val uri: Uri?=null,
    val pickupOptions: List<String> = listOf(),
)
