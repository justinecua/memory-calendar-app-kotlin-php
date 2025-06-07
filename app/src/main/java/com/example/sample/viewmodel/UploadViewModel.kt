package com.example.sample.com.example.sample.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sample.com.example.sample.http.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModel : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uploadState: StateFlow<UploadUiState> = _uploadState

    fun uploadImage(userId: Int, imageFile: File, memoryDate: String, description: String?) {
        Log.d("SelectedDate", "UploadView: $memoryDate")
        _uploadState.value = UploadUiState.Loading

        viewModelScope.launch {
            val imageBytes = imageFile.readBytes()
            val imageFileName = imageFile.name
            ApiService.uploadImage(userId, imageBytes, imageFileName, memoryDate, description) { response ->
                _uploadState.value = when {
                    response == null -> UploadUiState.Error("Network error")
                    response.contains("success", ignoreCase = true) -> UploadUiState.Success
                    else -> UploadUiState.Error(response)
                }
            }
        }
    }


}

sealed class UploadUiState {
    object Idle : UploadUiState()
    object Loading : UploadUiState()
    object Success : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}
