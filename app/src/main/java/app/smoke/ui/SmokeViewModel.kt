package app.smoke.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import app.smoke.data.AnalyseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SmokeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyseUiState(sourceOptions = pickupOptions()))
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

    private fun pickupOptions(): List<String> {
        val dateOptions = mutableListOf<String>()
        val formatter = SimpleDateFormat("E MMM d", Locale.getDefault())
        val calendar = Calendar.getInstance()
        repeat(4) {
            dateOptions.add(formatter.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
        return dateOptions
    }

    fun resetImageSource() {
        _uiState.value = AnalyseUiState(sourceOptions = pickupOptions())
    }
}

