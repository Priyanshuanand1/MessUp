package com.example.messup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.example.messup.ui.theme.MessUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MessUpTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("admin_login") { AdminLoginScreen(navController) }
        composable("admin_signup") { AdminSignupScreen(navController) } // Correct route
        composable("home") { HomeScreen(navController) }
        composable("admin") { AdminScreen(navController) } // Correct route
        composable("menu") { MenuScreen() }
        composable("order") { OrderScreen() }
        composable("feedback") { FeedbackScreen() }
        composable("leave") { LeaveScreen() }
        composable("manage_feedbacks") { FeedbacksScreen(navController) }
        composable("manage_leave_requests") { LeaveRequestsScreen(navController) }
        composable("announcements") { AnnouncementsScreen() }
    }
}