package com.example.sample.com.example.sample.screens

import android.net.Uri
import android.os.FileUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.sample.com.example.sample.viewmodel.UploadUiState
import com.example.sample.com.example.sample.viewmodel.UploadViewModel
import com.example.sample.utils.getFileFromUri
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import androidx.compose.material3.TextFieldDefaults
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    userId: Int,
    uploadViewModel: UploadViewModel = viewModel()
) {
    val currentMonth = YearMonth.now()
    val today = LocalDate.now()
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    val firstDayOfMonth = displayedMonth.atDay(1)
    val daysInMonth = displayedMonth.lengthOfMonth()

    var selectedDate by remember { mutableStateOf(today) }
    var imagesMap by remember { mutableStateOf<Map<LocalDate, Uri>>(emptyMap()) }
    var descriptionText by remember { mutableStateOf("") }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val uploadState by uploadViewModel.uploadState.collectAsState()

    // Modern color scheme
    val cardBackgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pickedImageUri = it
            showDescriptionDialog = true
        }
    }

    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false; pickedImageUri = null; descriptionText = "" },
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .background(cardBackgroundColor),
            confirmButton = {
                TextButton(
                    onClick = {
                        showDescriptionDialog = false
                        pickedImageUri?.let { uri ->
                            imagesMap = imagesMap + (selectedDate to uri)
                            val imageFile = getFileFromUri(context, uri)
                            if (imageFile != null) {
                                uploadViewModel.uploadImage(
                                    userId = userId,
                                    imageFile = imageFile,
                                    memoryDate = selectedDate.toString(),
                                    description = if (descriptionText.isBlank()) null else descriptionText
                                )
                            }
                        }
                        descriptionText = ""
                        pickedImageUri = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Text("UPLOAD", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDescriptionDialog = false
                        pickedImageUri = null
                        descriptionText = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = onSurfaceVariant
                    )
                ) {
                    Text("CANCEL")
                }
            },
            title = {
                Text("Add Description",
                    style = MaterialTheme.typography.titleLarge,
                    color = onSurface)
            },
            text = {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = { Text("Enter description (optional)", color = onSurfaceVariant) },
                    singleLine = false,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),

                    shape = MaterialTheme.shapes.medium
                )
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Month header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                color = cardBackgroundColor,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = onSurface
                        )
                    }

                    Text(
                        text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayedMonth.year}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Next Month",
                            tint = onSurface
                        )
                    }
                }
            }

            // Days of week
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            // Calendar
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                userScrollEnabled = false
            ) {
                val firstDayWeekday = firstDayOfMonth.dayOfWeek.value % 7
                items(firstDayWeekday) {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                items(daysInMonth) { dayIndex ->
                    val date = displayedMonth.atDay(dayIndex + 1)
                    val isSelected = date == selectedDate
                    val hasImage = imagesMap.containsKey(date)
                    val isToday = date == today

                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { selectedDate = date },
                        color = when {
                            isSelected -> primaryColor.copy(alpha = 0.2f)
                            isToday -> surfaceVariant.copy(alpha = 0.4f)
                            else -> Color.Transparent
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else if (isToday) 1.dp else 0.dp,
                            color = when {
                                isSelected -> primaryColor
                                isToday -> onSurfaceVariant
                                else -> Color.Transparent
                            }
                        ),
                        tonalElevation = if (hasImage) 4.dp else 0.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(
                                    if (hasImage) primaryColor.copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = when {
                                        isSelected -> primaryColor
                                        isToday -> primaryColor
                                        else -> onSurface
                                    },
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                if (hasImage) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(primaryColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected date
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                color = cardBackgroundColor,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                                    .uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (imagesMap[selectedDate] != null) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = primaryColor.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "MEMORY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Image section
                    val imageUri = imagesMap[selectedDate]
                    if (imageUri != null) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            tonalElevation = 4.dp
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Memory Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Add memory button
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Image")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD MEMORY", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LaunchedEffect(uploadState) {
                        when (uploadState) {
                            is UploadUiState.Loading -> {
                                Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()
                            }
                            is UploadUiState.Success -> {
                                Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                            }
                            is UploadUiState.Error -> {
                                Toast.makeText(context, "Upload failed!", Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}