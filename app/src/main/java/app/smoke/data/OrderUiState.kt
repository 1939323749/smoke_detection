package app.smoke.data

import android.net.Uri

data class OrderUiState(
    val source:Int=0,
    val uri: Uri?=null,
    val pickupOptions: List<String> = listOf(),
)
