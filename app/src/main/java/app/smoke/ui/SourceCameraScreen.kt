package app.smoke.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import app.smoke.BuildConfig
import app.smoke.R
import app.smoke.common.saveImage
import java.io.File
import java.io.FileOutputStream
import java.util.*

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SourceCameraScreen(
    imageUri:Uri?,
    onPhotoShot: (Uri) -> Unit = {},
    onCancelButtonClicked: () -> Unit = {},
    onNextButtonClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context= LocalContext.current

    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        BuildConfig.APPLICATION_ID + ".provider", file
    )

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            onPhotoShot(uri)
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)).align(Alignment.CenterHorizontally).verticalScroll(
            rememberScrollState()
        )){
            if (imageUri != null) {
                if (imageUri.path?.isNotEmpty() == true) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.height(300.dp).height(300.dp)
                    )
                }
            }
            Button(onClick = {
                val permissionCheckResult =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(uri)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },modifier=Modifier.padding(dimensionResource(R.dimen.padding_medium))){
                Text("Open Camera")
            }
            Button(
                onClick = {
                if(saveImage(context,imageUri,null)){
                    Toast.makeText(context, "image saved successfully", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "image save failed", Toast.LENGTH_SHORT).show()
                }
            },
                modifier=Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                enabled = imageUri!=null
            ){
                Text("Save")
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
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm ss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}


@SuppressLint("SimpleDateFormat")
fun getCurrentDate(): String {
    val currentDate = Date()
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    return formatter.format(currentDate)
}