package app.smoke.ui

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import app.smoke.R
import app.smoke.data.RecResult
import app.smoke.data.getPollutionLevel
import com.tencent.yolov5ncnn.YoloV5Ncnn
import com.tencent.yolov5ncnn.decodeUri
import com.tencent.yolov5ncnn.showObjects

@Composable
fun ResultScreen(
    context:Context,
    imageUri:Uri?,
    onCancelButtonClicked: () -> Unit = {},
    onNextButtonClicked: () -> Unit = {},
    onGetResult:(RecResult)->Unit={},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.padding_medium))
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState())
        ) {
            if (imageUri != null) {
                if (imageUri.path?.isNotEmpty() == true) {
                    val assetManager: AssetManager = context.assets
                    val yolov5ncnn = YoloV5Ncnn()
                    yolov5ncnn.Init(assetManager)
                    val objects = yolov5ncnn.Detect(decodeUri(imageUri, context), false)
                    val bms = showObjects(objects, decodeUri(imageUri, context))
                    onGetResult(bms)
                    Image(
                        painter = rememberAsyncImagePainter(bms.bms[0]),
                        contentDescription = null,
                        modifier = Modifier.height(300.dp).width(300.dp)
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
                            text = "该烟雾的污染程度为${getPollutionLevel(bms.pers[i])}级（仅供参考）",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    if (bms.pers.size == 0) {
                        Text("没有检测到烟雾！")
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
                .weight(1f, false),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedButton(modifier = Modifier.weight(1f), onClick = onCancelButtonClicked) {
                Text("Return")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onNextButtonClicked
            ) {
                Text(stringResource(R.string.share))
            }
        }
    }
}