package com.example.messup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavController) {
    var feedback by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var error by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Define the navigation items for NavigationBar
    val navItems = listOf(
        NavItem("Home", Icons.Default.Home, "home"),
        NavItem("Menu", Icons.Default.RestaurantMenu, "menu"),
        NavItem("Orders", Icons.Default.ShoppingCart, "order"),
        NavItem("Feedback", Icons.Default.Feedback, "feedback"),
        NavItem("Leave", Icons.Default.ExitToApp, "leave"),
        NavItem("Announcements", Icons.Default.Announcement, "announcements")
    )

    // Get the current back stack entry to highlight the selected item
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Feedback", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    Spacer(modifier = Modifier.width(8.dp)) // Placeholder, no navigation icon
                },
                actions = {
                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") { popUpTo(0) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(48.dp)
                    ) {
                        Text("Logout", fontSize = 14.sp)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Feedback", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val userEmail = auth.currentUser?.email
                    if (userEmail != null && feedback.isNotEmpty()) {
                        val feedbackData = hashMapOf(
                            "userEmail" to userEmail,
                            "feedback" to feedback,
                            "status" to "Pending"
                        )
                        db.collection("feedbacks").add(feedbackData)
                            .addOnSuccessListener {
                                feedback = ""
                                scope.launch { snackbarHostState.showSnackbar("Feedback submitted successfully!") }
                            }
                            .addOnFailureListener {
                                scope.launch { snackbarHostState.showSnackbar("Failed to submit feedback: ${it.message}") }
                            }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Feedback cannot be empty") }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Submit Feedback", style = MaterialTheme.typography.labelLarge)
            }
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}