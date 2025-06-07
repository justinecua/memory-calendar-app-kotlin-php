package com.example.sample.com.example.sample.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sample.com.example.sample.http.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


private fun extractMessageFromJson(json: String): String {
    return try {
        val jsonObject = org.json.JSONObject(json)
        when {
            jsonObject.has("error") -> jsonObject.getString("error")
            jsonObject.has("success") -> jsonObject.getString("success")
            else -> "Unknown error"
        }
    } catch (e: Exception) {
        "Invalid server response"
    }
}


class RegisterViewModel : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState

    fun register(username: String, email: String, password: String) {
        _registerState.value = RegisterUiState.Loading

        viewModelScope.launch {
            ApiService.register(username, email, password) { response ->
                _registerState.value = when {
                    response == null -> RegisterUiState.Error("Network error")
                    response.contains("success", ignoreCase = true) -> RegisterUiState.Success
                    else -> {
                        val message = extractMessageFromJson(response)
                        RegisterUiState.Error(message)
                    }
                }
            }
        }

    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

