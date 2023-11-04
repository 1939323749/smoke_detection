package app.smoke.data

import android.net.Uri

data class AnalyseUiState(
    var source:Int=0,
    val fileUri: Uri?=null,
    val camaraUri: Uri?=null,
    val sourceOptions: List<String> = listOf(),
    var result: RecResult?=null
)
