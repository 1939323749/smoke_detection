package app.smoke

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.smoke.common.shareResult
import app.smoke.data.*
import app.smoke.ui.*

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
                SourceFileScreen(
                    uri=uiState.fileUri,
                    onNextButtonClicked = { navController.navigate(SmokeScreen.Result.name) },
                    onCancelButtonClicked = {
                        cancelAnalyseAndNavigateToStart(viewModel, navController)
                    },
                    onImageSelected = { viewModel.setFileImage(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = SmokeScreen.SourceCamera.name) {
                SourceCameraScreen(
                    imageUri = uiState.camaraUri,
                    onNextButtonClicked = { navController.navigate(SmokeScreen.Result.name) },
                    onCancelButtonClicked = {
                        cancelAnalyseAndNavigateToStart(viewModel, navController)
                    },
                    onPhotoShot = { viewModel.setCamaraImage(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = SmokeScreen.Result.name) {
                val context= LocalContext.current
                ResultScreen(
                    context = context,
                    imageUri = if(uiState.source== FILE){
                        uiState.fileUri
                    }else{
                        uiState.camaraUri
                    },
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
                    onGetResult = {res->
                        uiState.result=res
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
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
