package com.example.messup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    var userEmail by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userRoomNo by remember { mutableStateOf("") }
    var menuItem by remember { mutableStateOf("") }
    var menuItems by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var feedbacks by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var leaveRequests by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var orders by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showAdminMode by remember { mutableStateOf(true) }
    var menuListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    var feedbackListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    var leaveListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    var ordersListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Real-time listener for menu items
    DisposableEffect(Unit) {
        val menuListener = db.collection("menu")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error fetching menu items: ${e.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    menuItems = snapshot.documents.map { it.data ?: emptyMap() }
                }
            }
        menuListenerRegistration = menuListener

        // Real-time listener for feedbacks
        val feedbackListener = db.collection("feedbacks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error fetching feedbacks: ${e.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    feedbacks = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap()
                        data + mapOf("id" to doc.id) // Include document ID for later updates
                    }
                }
            }
        feedbackListenerRegistration = feedbackListener

        // Real-time listener for leave requests
        val leaveListener = db.collection("leave_requests")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error fetching leave requests: ${e.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    leaveRequests = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap()
                        data + mapOf("id" to doc.id) // Include document ID for later updates
                    }
                }
            }
        leaveListenerRegistration = leaveListener

        // Real-time listener for orders
        val ordersListener = db.collection("orders")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error fetching orders: ${e.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    orders = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap()
                        data + mapOf("id" to doc.id)
                    }
                }
            }
        ordersListenerRegistration = ordersListener

        onDispose {
            menuListenerRegistration?.remove()
            feedbackListenerRegistration?.remove()
            leaveListenerRegistration?.remove()
            ordersListenerRegistration?.remove()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (showAdminMode) "Admin Panel" else "MessUp") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Button(
                        onClick = { showAdminMode = !showAdminMode },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(if (showAdminMode) "User Mode" else "Admin Mode")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showAdminMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add User Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add User", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = userEmail,
                            onValueChange = { userEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = userRoomNo,
                            onValueChange = { userRoomNo = it },
                            label = { Text("Room No") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                Button(
                    onClick = {
                        if (userEmail.isNotEmpty() && userName.isNotEmpty() && userRoomNo.isNotEmpty()) {
                            val userData = hashMapOf(
                                "name" to userName,
                                "email" to userEmail,
                                "roomNo" to userRoomNo,
                                "role" to "user"
                            )
                            db.collection("users").document(userEmail).set(userData)
                                .addOnSuccessListener {
                                    userEmail = ""
                                    userName = ""
                                    userRoomNo = ""
                                    showSuccessMessage = true
                                }
                                .addOnFailureListener {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Failed to add user")
                                    }
                                }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("All fields are required")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Add User", style = MaterialTheme.typography.labelLarge)
                }

                // Add Menu Item Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Menu Item", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = menuItem,
                            onValueChange = { menuItem = it },
                            label = { Text("Menu Item") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                Button(
                    onClick = {
                        if (menuItem.isNotEmpty()) {
                            val menuData = hashMapOf("item" to menuItem)
                            db.collection("menu").add(menuData)
                                .addOnSuccessListener {
                                    menuItem = ""
                                    showSuccessMessage = true
                                }
                                .addOnFailureListener {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Failed to add menu item")
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Add Menu Item", style = MaterialTheme.typography.labelLarge)
                }

                // Display All Menu Items
                Spacer(modifier = Modifier.height(16.dp))
                Text("Menu Items", style = MaterialTheme.typography.titleLarge)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(menuItems) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Text(
                                text = item["item"].toString(),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Display Feedbacks
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Feedbacks", style = MaterialTheme.typography.titleLarge)
                    Button(
                        onClick = { navController.navigate("manage_feedbacks") },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Manage Feedbacks", style = MaterialTheme.typography.labelMedium)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(feedbacks) { feedback ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "User: ${feedback["userEmail"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Feedback: ${feedback["feedback"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Status: ${feedback["status"]?.toString() ?: "Pending"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Display Leave Requests
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Leave Requests", style = MaterialTheme.typography.titleLarge)
                    Button(
                        onClick = { navController.navigate("manage_leave_requests") },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Manage Leave Requests", style = MaterialTheme.typography.labelMedium)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(leaveRequests) { leave ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "User: ${leave["userEmail"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Reason: ${leave["reason"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Status: ${leave["status"]?.toString() ?: "Pending"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Display Orders
                Spacer(modifier = Modifier.height(16.dp))
                Text("Orders", style = MaterialTheme.typography.titleLarge)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "User: ${order["userEmail"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Item: ${order["item"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Status: ${order["status"]?.toString() ?: "Pending"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Logout Button
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Logout", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else {
            HomeScreen(navController, modifier = Modifier.padding(paddingValues))
        }
    }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            scope.launch {
                snackbarHostState.showSnackbar("Action successful!")
                showSuccessMessage = false
            }
        }
    }
}