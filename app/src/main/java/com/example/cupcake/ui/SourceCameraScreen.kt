package com.example.cupcake.ui

import android.Manifest
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import com.example.cupcake.BuildConfig
import com.example.cupcake.R
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
                saveImageToFile(imageUri,context)
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
fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}

@RequiresApi(Build.VERSION_CODES.P)
private fun saveImageToFile(imageUri: Uri?,context: Context) {
    imageUri?.let { uri ->
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source)

        val externalStorageState = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == externalStorageState) {
            val imageDirectory = File(Environment.getExternalStorageDirectory(), "smoke")
            if (!imageDirectory.exists()) {
                imageDirectory.mkdirs()
            }

            val fileName =  getCurrentDate()+".jpg"
            val outputFile = File(imageDirectory, fileName)

            var outputStream: FileOutputStream? = null
            try {
                outputStream = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.d("[file save]",e.toString())
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                outputStream?.close()
            }
        } else {
            Toast.makeText(context, "External storage not available", Toast.LENGTH_SHORT).show()
        }
    }
}

fun getCurrentDate(): String {
    val currentDate = Date()
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    return formatter.format(currentDate)
}