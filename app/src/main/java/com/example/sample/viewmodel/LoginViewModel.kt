package com.example.sample.com.example.sample.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sample.com.example.sample.http.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject


class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun login(context: Context, email: String, password: String) {
        _loginState.value = LoginUiState.Loading

        viewModelScope.launch {
            ApiService.login(email, password) { response ->
                if (response == null) {
                    _loginState.value = LoginUiState.Error("Network error")
                } else {
                    if (response.contains("success", ignoreCase = true)) {
                        Log.d("LoginViewModel", "Response: $response")
                        try {
                            val json = JSONObject(response)
                            val user = json.getJSONObject("user")

                            // SharedPreferences
                            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            sharedPref.edit().apply {
                                putInt("user_id", user.getInt("id"))
                                putString("username", user.getString("username"))
                                putString("email", user.getString("email"))
                                apply()
                            }

                            _loginState.value = LoginUiState.Success
                        } catch (e: Exception) {
                            _loginState.value = LoginUiState.Error("Parsing error")
                        }
                    } else {
                        try {
                            val json = JSONObject(response)
                            val message = json.optString("error", "Unknown error")
                            _loginState.value = LoginUiState.Error(message)
                        } catch (e: Exception) {
                            _loginState.value = LoginUiState.Error("Unknown response")
                        }
                    }
                }
            }
        }
    }

}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
