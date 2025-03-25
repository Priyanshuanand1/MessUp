package com.example.messup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.messup.ui.theme.MessUpTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MessUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser == null) "login" else {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(userEmail).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            navController.navigate("admin") { popUpTo(0) }
                        } else {
                            navController.navigate("home") { popUpTo(0) }
                        }
                    }
                }
            "login"
        } else {
            "login"
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("menu") { MenuScreen(navController) }
        composable("order") { OrderScreen(navController) }
        composable("feedback") { FeedbackScreen(navController) }
        composable("leave") { LeaveScreen(navController) }
        composable("announcements") { AnnouncementsScreen(navController) }
        composable("admin") { AdminScreen(navController) }
        composable("manage_feedbacks") {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            ManageFeedbacksScreen(navController, drawerState)
        }
        composable("manage_leave_requests") {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            ManageLeaveRequestsScreen(navController, drawerState)
        }
        composable("manage_announcements") {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            ManageAnnouncementsScreen(navController, drawerState)
        }
    }
}