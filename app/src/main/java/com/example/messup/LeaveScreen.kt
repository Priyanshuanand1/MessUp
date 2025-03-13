package com.example.messup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(navController: NavController) {
    var reason by remember { mutableStateOf("") }
    var userLeaveRequests by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

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

    DisposableEffect(Unit) {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            val listener = db.collection("leave_requests")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        scope.launch { snackbarHostState.showSnackbar("Error fetching leave requests: ${e.message}") }
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        userLeaveRequests = snapshot.documents.map { doc ->
                            val data = doc.data ?: emptyMap()
                            data + mapOf("id" to (doc.id ?: ""))
                        }
                    }
                }
            listenerRegistration = listener
        }
        onDispose { listenerRegistration?.remove() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Leave", fontSize = 20.sp) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Request Leave", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val userEmail = auth.currentUser?.email
                            if (userEmail != null && reason.isNotEmpty()) {
                                val leaveData = hashMapOf(
                                    "userEmail" to userEmail,
                                    "reason" to reason,
                                    "status" to "Pending"
                                )
                                db.collection("leave_requests").add(leaveData)
                                    .addOnSuccessListener {
                                        reason = ""
                                        scope.launch { snackbarHostState.showSnackbar("Leave request submitted successfully!") }
                                    }
                                    .addOnFailureListener {
                                        scope.launch { snackbarHostState.showSnackbar("Failed to submit leave request: ${it.message}") }
                                    }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Reason cannot be empty") }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Submit Request", fontSize = 16.sp)
                    }
                }
            }

            Text("Your Leave Requests", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (userLeaveRequests.isEmpty()) {
                Text("No leave requests available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userLeaveRequests) { leave ->
                        val status = leave["status"]?.toString() ?: "Pending"
                        val cardColor = when (status) {
                            "Accepted" -> Color(0xFF4CAF50)
                            "Rejected" -> Color(0xFFE53935)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "Reason: ${leave["reason"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp,
                                    color = if (status == "Pending") MaterialTheme.colorScheme.onSurface else Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Status: $status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp,
                                    color = if (status == "Pending") MaterialTheme.colorScheme.onSurface else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}