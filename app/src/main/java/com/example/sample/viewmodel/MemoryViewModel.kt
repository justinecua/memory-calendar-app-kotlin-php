package com.example.sample.com.example.sample.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sample.com.example.sample.http.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable

class MemoriesViewModel : ViewModel() {

    private val _memoriesState = MutableStateFlow<MemoriesUiState>(MemoriesUiState.Idle)
    val memoriesState: StateFlow<MemoriesUiState> = _memoriesState

    fun fetchMemories(userId: Int) {
        _memoriesState.value = MemoriesUiState.Loading

        viewModelScope.launch {
            ApiService.fetchMemories(userId) { response ->
                if (response == null) {
                    _memoriesState.value = MemoriesUiState.Error("Network error")
                    return@fetchMemories
                }

                try {
                    val json = Json { ignoreUnknownKeys = true }
                    val jsonObject = json.parseToJsonElement(response).jsonObject
                    val success = jsonObject["success"]?.jsonPrimitive?.booleanOrNull ?: false

                    if (success) {
                        val memoriesJsonElement = jsonObject["memories"] ?: JsonArray(emptyList())

                        // DEBUG: log the raw memories JSON
                        println("Raw memories JSON: $memoriesJsonElement")

                        val cleanedMemoriesJsonArray = JsonArray(
                            memoriesJsonElement.jsonArray.map { memoryJsonElement ->
                                val memoryObj = memoryJsonElement.jsonObject
                                val filteredContent = memoryObj.filterKeys { key ->
                                    key in listOf("id", "user_id", "image_path", "memory_date", "description", "created_at")
                                }
                                JsonObject(filteredContent)
                            }
                        )

                        // DEBUG: log the cleaned memories JSON
                        println("Cleaned memories JSON: $cleanedMemoriesJsonArray")

                        val memories = json.decodeFromJsonElement<List<Memory>>(cleanedMemoriesJsonArray)
                        _memoriesState.value = MemoriesUiState.Success(memories)
                    } else {
                        val msg = jsonObject["message"]?.jsonPrimitive?.content ?: "Unknown error"
                        _memoriesState.value = MemoriesUiState.Error(msg)
                    }
                } catch (e: Exception) {
                    _memoriesState.value = MemoriesUiState.Error("Parsing error: ${e.localizedMessage}")
                    // Log full stacktrace for debugging
                    e.printStackTrace()
                }

            }
        }
    }

}


@Serializable
data class Memory(
    val id: Int,
    val user_id: Int,
    val image_path: String?,
    val memory_date: String,
    val description: String?,
    val created_at: String
)


sealed class MemoriesUiState {
    object Idle : MemoriesUiState()
    object Loading : MemoriesUiState()
    data class Success(val memories: List<Memory>) : MemoriesUiState()
    data class Error(val message: String) : MemoriesUiState()
}
