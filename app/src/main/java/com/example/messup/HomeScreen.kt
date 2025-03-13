package com.example.messup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val email = auth.currentUser?.email
        if (email != null) {
            db.collection("users").document(email).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists() && doc.getString("role") == "admin") {
                        isAdmin = true
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Failed to check user role: ${exception.message}"
                    isLoading = false
                }
        } else {
            errorMessage = "User not logged in"
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = errorMessage ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else if (isAdmin) {
        AdminScreen(navController)
    } else {
        val nestedNavController = rememberNavController()
        val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text("MessUp") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = currentRoute == "menu",
                        onClick = { nestedNavController.navigate("menu") },
                        icon = { Icon(Icons.Default.Restaurant, contentDescription = "Menu") },
                        label = { Text("Menu") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "order",
                        onClick = { nestedNavController.navigate("order") },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Order") },
                        label = { Text("Order") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "announcements",
                        onClick = { nestedNavController.navigate("announcements") },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Announcements") },
                        label = { Text("Announcements") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "leave",
                        onClick = { nestedNavController.navigate("leave") },
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Leave") },
                        label = { Text("Leave") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "feedback",
                        onClick = { nestedNavController.navigate("feedback") },
                        icon = { Icon(Icons.Default.Feedback, contentDescription = "Feedback") },
                        label = { Text("Feedback") }
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Box(modifier = Modifier.weight(1f)) {
                    NavHost(nestedNavController, startDestination = "menu") {
                        composable("menu") { MenuScreen() }
                        composable("order") { OrderScreen() }
                        composable("announcements") { AnnouncementsScreen() }
                        composable("leave") { LeaveScreen() }
                        composable("feedback") { FeedbackScreen() }
                    }
                }
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") { popUpTo(0) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Changed to red
                ) {
                    Text("Logout", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}