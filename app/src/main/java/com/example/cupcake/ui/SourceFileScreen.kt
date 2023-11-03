package com.example.cupcake.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import coil.compose.rememberAsyncImagePainter
import com.example.cupcake.R

@Composable
fun SourceFileScreen(
    uri:Uri?,
    onImageSelected: (Uri) -> Unit = {},
    onCancelButtonClicked: () -> Unit = {},
    onNextButtonClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedValue by rememberSaveable { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) {
            onImageSelected(it)
        }
    }
    val context = LocalContext.current
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
            if (uri != null) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier=Modifier.height(300.dp).width(300.dp)
                )
            }
            Button(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }, modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
                Text("Select Image")
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
                Text(stringResource(R.string.cancel))
            }
            Button(
                modifier = Modifier.weight(1f),
                enabled = uri != null,
                onClick = onNextButtonClicked
            ) {
                Text(stringResource(R.string.analyse_image))
            }
        }
    }
}

