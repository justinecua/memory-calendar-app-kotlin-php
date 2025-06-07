package com.example.sample.com.example.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.sample.com.example.sample.viewmodel.MemoriesViewModel
import com.example.sample.com.example.sample.viewmodel.MemoriesUiState
import com.example.sample.com.example.sample.viewmodel.Memory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.sample.com.example.sample.utils.BASE_URL
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign


@Composable
fun MemoryScreen(viewModel: MemoriesViewModel, userId: Int) {
    val memoriesState by viewModel.memoriesState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.fetchMemories(userId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = memoriesState) {
            is MemoriesUiState.Loading -> {
                CenterLoadingIndicator()
            }
            is MemoriesUiState.Success -> {
                if (state.memories.isEmpty()) {
                    EmptyMemoriesPlaceholder()
                } else {
                    MemoryList(memories = state.memories)
                }
            }
            is MemoriesUiState.Error -> {
                ErrorMessage(message = state.message)
            }
            else -> {}
        }
    }
}

@Composable
private fun MemoryList(memories: List<Memory>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(memories) { memory ->
            MemoryCard(memory = memory)
        }
    }
}

@Composable
private fun MemoryCard(memory: Memory) {
    val fullImageUrl = BASE_URL + memory.image_path

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(fullImageUrl),
                    contentDescription = "Memory Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp
                            )
                        ),
                    contentScale = ContentScale.Crop
                )

                // Date badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = formatRelativeDate(memory.memory_date),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                memory.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,

                    )
                }


            }
        }
    }
}

fun formatRelativeDate(dateString: String): String {
    val formatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )

    val now = LocalDateTime.now()

    val dateTime = formatters.firstNotNullOfOrNull { formatter ->
        try {
            LocalDateTime.parse(dateString, formatter)
        } catch (e: Exception) {
            try {
                val localDate = java.time.LocalDate.parse(dateString, formatter)
                localDate.atStartOfDay()
            } catch (e2: Exception) {
                null
            }
        }
    } ?: return dateString

    val daysBetween = ChronoUnit.DAYS.between(dateTime, now)
    val weeksBetween = ChronoUnit.WEEKS.between(dateTime, now)
    val monthsBetween = ChronoUnit.MONTHS.between(dateTime, now)
    val yearsBetween = ChronoUnit.YEARS.between(dateTime, now)

    return when {
        daysBetween == 0L -> "Today"
        daysBetween == 1L -> "Yesterday"
        daysBetween < 7 -> "$daysBetween days ago"
        weeksBetween == 1L -> "1 week ago"
        weeksBetween < 4 -> "$weeksBetween weeks ago"
        monthsBetween == 1L -> "1 month ago"
        monthsBetween < 12 -> "$monthsBetween months ago"
        yearsBetween == 1L -> "1 year ago"
        else -> "$yearsBetween years ago"
    }
}


@Composable
private fun CenterLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyMemoriesPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoLibrary,
            contentDescription = "No memories",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Memories Yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = "Add your first memory by selecting a date in the calendar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error Loading Memories",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
