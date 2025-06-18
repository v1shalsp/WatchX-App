package com.example.watchxapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.watchxapp.ui.navigation.MainScreenView
import com.example.watchxapp.ui.screens.auth.LoginSignupScreen
import com.example.watchxapp.ui.theme.WatchXAppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIX: Add this line to make the app draw below the status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            WatchXAppTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val startDestination = if (Firebase.auth.currentUser != null) "main_app" else "auth_root"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("auth_root") {
                        LoginSignupScreen(
                            onAuthSuccess = {
                                navController.navigate("main_app") {
                                    popUpTo("auth_root") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("main_app") {
                        MainScreenView(
                            rootNavController = navController,
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it }
                        )
                    }
                }
            }
        }
    }
}
