package com.example.sample

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Memory : Screen("memory", "Memory", Icons.Default.Add)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}
