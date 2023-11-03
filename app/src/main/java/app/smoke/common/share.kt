package app.smoke.common

import android.content.Context
import android.content.Intent
import android.net.Uri



fun shareResult(context: Context, uri: Uri?,text:String?):Boolean {
    if(uri==null){
        return false
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM,uri)
        putExtra(Intent.EXTRA_SUBJECT,"share the image")
        if(text!=null){
            putExtra(Intent.EXTRA_TEXT,text)
        }
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            "share image"
        )
    )
    return true
}



