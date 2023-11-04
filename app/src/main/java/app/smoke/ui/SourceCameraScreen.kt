package app.smoke.ui

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import app.smoke.R
import java.io.File
import java.util.*

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SourceCameraScreen(
    imageUri:Uri?,
    onPhotoShot: @Composable () -> Unit,
    onOpenCameraClicked:()->Unit={},
    onCancelButtonClicked: () -> Unit = {},
    onNextButtonClicked: () -> Unit = {},
    onSaveButtonClicked:()->Unit={},
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
                .verticalScroll(rememberScrollState()
        )){
            onPhotoShot()

                Row(
                    modifier=Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick =onOpenCameraClicked,
                        modifier=Modifier.padding(dimensionResource(R.dimen.padding_medium))
                    ){
                        Text(stringResource(R.string.open_camera))
                    }
                }
                Row(
                    modifier=Modifier.align(Alignment.CenterHorizontally)
                ){
                    Button(
                        onClick = onSaveButtonClicked,
                        modifier=Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                        enabled = imageUri!=null
                    ){
                        Text(stringResource(R.string.save))
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
        ){
            OutlinedButton(modifier = Modifier.weight(1f), onClick = onCancelButtonClicked) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                modifier = Modifier.weight(1f),
                // the button is enabled when the user makes a selection
                enabled = imageUri!=null,
                onClick = onNextButtonClicked
            ) {
                Text(stringResource(R.string.analyse_image))
            }
        }
    }
}
@SuppressLint("SimpleDateFormat")
fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}