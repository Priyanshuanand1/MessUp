package com.example.messup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MessUpTheme {
                MessUpApp()
            }
        }
    }
}

@Composable
fun MessUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF0288D1),
            onPrimary = Color.White,
            secondary = Color(0xFF4CAF50),
            onSecondary = Color.White,
            surface = Color(0xFFF5F5F5),
            onSurface = Color.Black
        ),
        typography = Typography(
            titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            bodyMedium = TextStyle(fontSize = 16.sp)
        ),
        content = content
    )
}

@Composable
fun MessUpApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    NavHost(navController = navController, startDestination = if (auth.currentUser == null) "login" else "home") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("admin") { AdminScreen(navController) }
        composable("admin_signup") { AdminSignupScreen(navController) }
    }
}