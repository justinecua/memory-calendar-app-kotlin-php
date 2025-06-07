package com.example.sample

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sample.com.example.sample.navigation.BottomNavigationBar
import com.example.sample.com.example.sample.screens.CalendarScreen
import com.example.sample.com.example.sample.screens.HomeScreen
import com.example.sample.com.example.sample.screens.MemoryScreen
import com.example.sample.com.example.sample.screens.ProfileScreen
import com.example.sample.com.example.sample.viewmodel.MemoriesViewModel

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val db_userId = sharedPref.getInt("user_id", -1)

    val navController = rememberNavController()
    val memoriesViewModel = MemoriesViewModel()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen(userId = db_userId)
            }
            composable(Screen.Memory.route) {
                MemoryScreen(viewModel = memoriesViewModel, userId = db_userId)
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
