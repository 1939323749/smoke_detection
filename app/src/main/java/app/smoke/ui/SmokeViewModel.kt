package app.smoke.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import app.smoke.data.AnalyseUiState
import app.smoke.data.RecResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SmokeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyseUiState())
    val uiState: StateFlow<AnalyseUiState> = _uiState.asStateFlow()

    fun setSource(source: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                source = source
            )
        }
    }

    fun setFileImage(uri: Uri){
        _uiState.update { currentState->
            currentState.copy(
                fileUri=uri
            )
        }
    }

    fun setCamaraImage(uri: Uri){
        _uiState.update { currentState->
            currentState.copy(
                camaraUri=uri
            )
        }
    }


    fun resetImageSource() {
        _uiState.value = AnalyseUiState()
    }

    fun setResult(bms: RecResult) {
        _uiState.update {
            it.copy(
                result = bms
            )
        }
    }
}

