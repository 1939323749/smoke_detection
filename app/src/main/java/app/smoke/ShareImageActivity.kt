package app.smoke

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import app.smoke.data.getPollutionLevel
import app.smoke.ui.theme.SmokeTheme
import com.tencent.yolov5ncnn.YoloV5Ncnn
import com.tencent.yolov5ncnn.decodeUri
import com.tencent.yolov5ncnn.showObjects

class ShareImageActivity : ComponentActivity() {
    private var sharedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                } else if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent) // Handle single image being sent
                }
            }
            intent?.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                handleSendMultipleImages(intent) // Handle multiple images being sent
            }
            else -> {
                finish()
            }
        }
        sharedImageUri?.let { uri ->
            setContent {
                SmokeTheme {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        DisplayImage(sharedImageUri!!)
                    }
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            // Update UI to reflect text being shared
        }
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            sharedImageUri=it
        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            // Update UI to reflect multiple images being shared
        }
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DisplayImage(uri: Uri){
    val context= LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Analyse")
                }
            )
        },
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.padding_medium))
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ){
                val assetManager: AssetManager = context.assets
                val yolov5ncnn = YoloV5Ncnn()
                yolov5ncnn.Init(assetManager)
                val objects = yolov5ncnn.Detect(decodeUri(uri, context), false)
                val bms = showObjects(objects, decodeUri(uri, context))
                Image(
                    painter = rememberAsyncImagePainter(bms.bms[0]),
                    contentDescription = null,
                    modifier = Modifier
                        .width(300.dp)
                        .height(300.dp)
                )
                for (i in 0 until bms.pers.size) {
                    Image(
                        painter = rememberAsyncImagePainter(bms.bms[i + 1]),
                        contentDescription = null,
                        modifier = Modifier
                            .width(300.dp)
                            .height(300.dp)
                    )
                    Text(
                        text = "该烟雾的污染程度为${getPollutionLevel(bms.pers[i])}级",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Button(
                        modifier = Modifier.weight(1f).padding(top = 30.dp),
                        onClick = {}
                    ) {
                        Text("Share")
                    }
                }
            }
        }
    }
}