package com.example.messup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.messup.ui.theme.MessUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MessUpTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") { LoginScreen(navController) }
                        composable("signup") { SignupScreen(navController) }
                        composable("admin_login") { AdminLoginScreen(navController) }
                        composable("admin_signup") { AdminSignupScreen(navController) }
                        composable("home") { HomeScreen(navController) }
                        composable("admin") { AdminScreen(navController) }
                        composable("menu") { MenuScreen(navController) }
                        composable("order") { OrderScreen(navController) }
                        composable("feedback") { FeedbackScreen(navController) }
                        composable("leave") { LeaveScreen(navController) }
                        composable("manage_feedbacks") { FeedbacksScreen(navController) }
                        composable("manage_leave_requests") { LeaveRequestsScreen(navController) }
                        composable("announcements") { AnnouncementsScreen(navController) }
                    }
                }
            }
        }
    }
}