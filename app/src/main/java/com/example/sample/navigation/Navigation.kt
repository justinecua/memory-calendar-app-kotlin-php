package com.example.sample.navigation

import androidx.compose.runtime.*
import com.example.sample.MainScreen
import com.example.sample.com.example.sample.screens.LoginScreen
import com.example.sample.com.example.sample.screens.RegisterScreen

@Composable
fun AuthNavigation() {
    var isLoginScreen by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }

    if (isAuthenticated) {
        MainScreen()
    } else {
        if (isLoginScreen) {
            LoginScreen(
                onLoginClick = { email, password ->
                },
                onSignupClick = {
                    isLoginScreen = false
                }, onLoginSuccess = {
                    isAuthenticated = true
                }
            )
        } else {
            RegisterScreen(
                onRegisterClick = { username, email, password ->
                },
                onLoginClick = {
                    isLoginScreen = true
                },
                onRegisterSuccess = {
                    isAuthenticated = true
                }
            )
        }
    }
}

