package app.smoke

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.smoke.common.saveImage
import app.smoke.common.shareResult
import app.smoke.data.*
import app.smoke.ui.*
import coil.compose.rememberAsyncImagePainter
import com.tencent.yolov5ncnn.YoloV5Ncnn
import com.tencent.yolov5ncnn.decodeUri
import com.tencent.yolov5ncnn.showObjects
import java.util.*

enum class SmokeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    SourceFile(title = R.string.source_file),
    SourceCamera(title = R.string.source_camera),
    Result(title = R.string.result),
}

@Composable
fun SmokeAppBar(
    currentScreen: SmokeScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SmokeApp(
    viewModel: SmokeViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = SmokeScreen.valueOf(
        backStackEntry?.destination?.route ?: SmokeScreen.Start.name
    )
    Scaffold(
        topBar = {
            SmokeAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()
        val res by remember { mutableStateOf(uiState.result) }
        NavHost(
            navController = navController,
            startDestination = SmokeScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = SmokeScreen.Start.name) {
                var next by remember { mutableStateOf(SmokeScreen.SourceFile.name) }
                StartAnalyseScreen(
                    quantityOptions = DataSource.sourceOptions,
                    onNextButtonClicked = {
                        viewModel.setSource(it)
                        if (it== FILE){
                            next=SmokeScreen.SourceFile.name
                        }else if (it== CAMARA){
                            next=SmokeScreen.SourceCamera.name
                        }
                        navController.navigate(next)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }
            composable(route = SmokeScreen.SourceFile.name) {
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                    if (it != null) {
                        viewModel.setFileImage(it)
                    }
                }
                SourceFileScreen(
                    uri=uiState.fileUri,
                    onSelectImageButtonClicked = {
                        launcher.launch(
                        PickVisualMediaRequest(
                            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )},
                    onNextButtonClicked = { navController.navigate(SmokeScreen.Result.name) },
                    onCancelButtonClicked = {
                        cancelAnalyseAndNavigateToStart(viewModel, navController)
                    },
                    onImageSelected = {
                        if (uiState.fileUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(uiState.fileUri),
                            contentDescription = null,
                            modifier=Modifier.height(300.dp).width(300.dp)
                        )
                    } },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = SmokeScreen.SourceCamera.name) {
                val context= LocalContext.current

                val file = context.createImageFile()
                val uri = FileProvider.getUriForFile(
                    Objects.requireNonNull(context),
                    BuildConfig.APPLICATION_ID + ".provider", file
                )

                val cameraLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
                        viewModel.setCamaraImage(uri)
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
                SourceCameraScreen(
                    imageUri = uiState.camaraUri,
                    onOpenCameraClicked = {
                        val permissionCheckResult =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onNextButtonClicked = { navController.navigate(SmokeScreen.Result.name) },
                    onCancelButtonClicked = {
                        cancelAnalyseAndNavigateToStart(viewModel, navController)
                    },
                    onSaveButtonClicked = { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        viewModel.setCamaraImage(saveImage(context, decodeUri(uiState.camaraUri,context),"jpeg",Bitmap.CompressFormat.JPEG,100))
                    }
                    },
                    onPhotoShot = { OnPhotoShot(uiState.camaraUri) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = SmokeScreen.Result.name) {
                val context= LocalContext.current
                ResultScreen(
                    onNextButtonClicked = {

                        var maxPollutionLevel=0
                        if(res!=null){
                            for (level in res!!.pers){
                                maxPollutionLevel= maxOf(getPollutionLevel(level),maxPollutionLevel)
                            }
                        }
                        shareResult(context = context,uri = if(uiState.source== FILE){
                            uiState.fileUri
                        }else{
                            uiState.camaraUri
                        },"最大污染等级为$maxPollutionLevel")
                    },
                    onCancelButtonClicked = {
                        cancelAnalyseAndNavigateToStart(viewModel, navController)
                    },
                    onGetResult = {
                        val imageUri= when (uiState.source) {
                            FILE -> {
                                uiState.fileUri
                            }
                            CAMARA -> {
                                uiState.camaraUri
                            }
                            else -> {
                                null
                            }
                        }
                        if (imageUri != null) {
                        if (imageUri.path?.isNotEmpty() == true) {
                            val assetManager: AssetManager = context.assets
                            val yolov5ncnn = YoloV5Ncnn()
                            yolov5ncnn.Init(assetManager)
                            val objects = yolov5ncnn.Detect(decodeUri(imageUri, context), false)
                            val bms = showObjects(objects, decodeUri(imageUri, context))
                            uiState.result=bms
                            Column{
                                Image(
                                    painter = rememberAsyncImagePainter(bms.bms[0]),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(300.dp)
                                        .width(300.dp)
                                        .padding(dimensionResource(R.dimen.padding_medium))
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
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(dimensionResource(R.dimen.padding_medium))
                                    ) {
                                        Text("没有检测到烟雾！")
                                    }

                                }
                            }

                        }
                    }
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }

}
@Composable
private fun OnPhotoShot(imageUri:Uri?){
    if (imageUri != null) {
        if (imageUri.path?.isNotEmpty() == true) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier.height(300.dp).height(300.dp)
            )
        }
    }
}

private fun cancelAnalyseAndNavigateToStart(
    viewModel: SmokeViewModel,
    navController: NavHostController
) {
    viewModel.resetImageSource()
    navController.popBackStack(SmokeScreen.Start.name, inclusive = false)
}
