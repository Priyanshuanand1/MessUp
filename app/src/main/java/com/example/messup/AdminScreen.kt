package com.example.messup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var menuListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    var feedbackListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    var leaveListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    var ordersListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    var drawerState = rememberDrawerState(DrawerValue.Closed)
    val scopeDrawer = rememberCoroutineScope()

    // Real-time listeners with robust error handling
    DisposableEffect(Unit) {
        try {
            val menuListener = db.collection("menu")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Error fetching menu items: ${e.message}")
                        }
                        println("Error fetching menu items: ${e.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val updatedMenuItems = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data?.takeIf { it.containsKey("item") } ?: emptyMap()
                            data + mapOf("id" to (doc.id ?: "")) // Include document ID
                        }
                        menuItems = updatedMenuItems
                        println("Updated menuItems size: ${menuItems.size}")
                    } else {
                        println("No menu items snapshot received")
                    }
                }
            menuListenerRegistration = menuListener

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
                            data + mapOf("id" to (doc.id ?: ""))
                        }
                    }
                }
            feedbackListenerRegistration = feedbackListener

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
                            data + mapOf("id" to (doc.id ?: ""))
                        }
                    }
                }
            leaveListenerRegistration = leaveListener

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
                            data + mapOf("id" to (doc.id ?: ""))
                        }
                    }
                }
            ordersListenerRegistration = ordersListener
        } catch (e: Exception) {
            println("Error setting up Firestore listeners: ${e.message}")
        }

        onDispose {
            menuListenerRegistration?.remove()
            feedbackListenerRegistration?.remove()
            leaveListenerRegistration?.remove()
            ordersListenerRegistration?.remove()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(250.dp)
            ) {
                NavigationDrawerItem(
                    label = { Text("Manage Feedbacks", fontSize = 16.sp) },
                    selected = false,
                    onClick = {
                        scopeDrawer.launch { drawerState.close() }
                        navController.navigate("manage_feedbacks")
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Manage Leave Requests", fontSize = 16.sp) },
                    selected = false,
                    onClick = {
                        scopeDrawer.launch { drawerState.close() }
                        navController.navigate("manage_leave_requests")
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Admin Panel", fontSize = 20.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = { // Explicitly placed in top left
                        IconButton(
                            onClick = { scopeDrawer.launch { drawerState.open() } },
                            modifier = Modifier.padding(start = 8.dp) // Optional padding for clarity
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open Drawer",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
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
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Add User Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Add User", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("Name", fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = userEmail,
                                onValueChange = { userEmail = it },
                                label = { Text("Email", fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = userRoomNo,
                                onValueChange = { userRoomNo = it },
                                label = { Text("Room No", fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
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
                                                    snackbarHostState.showSnackbar("Failed to add user: ${it.message}")
                                                }
                                            }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("All fields are required")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Add User", fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Add Menu Item Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Add Menu Item", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = menuItem,
                                onValueChange = { menuItem = it },
                                label = { Text("Menu Item", fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (menuItem.isNotEmpty()) {
                                        val menuData = hashMapOf("item" to menuItem)
                                        db.collection("menu").add(menuData)
                                            .addOnSuccessListener {
                                                menuItem = ""
                                                showSuccessMessage = true
                                                println("Menu item added successfully: $menuItem")
                                            }
                                            .addOnFailureListener {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Failed to add menu item: ${it.message}")
                                                }
                                                println("Failed to add menu item: ${it.message}")
                                            }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Add Menu Item", fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Display All Menu Items with Delete Option
                item {
                    Text("Menu Items", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (menuItems.isEmpty()) {
                        Text("No menu items available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp), // Constrain height
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(menuItems) { item ->
                                val itemId = item["id"]?.toString() ?: ""
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item["item"]?.toString() ?: "Unknown Item",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontSize = 16.sp
                                        )
                                        IconButton(
                                            onClick = {
                                                if (itemId.isNotEmpty()) {
                                                    db.collection("menu").document(itemId)
                                                        .delete()
                                                        .addOnSuccessListener {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Menu item deleted successfully!")
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Failed to delete menu item: ${e.message}")
                                                            }
                                                        }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Menu Item",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Display Feedbacks
                item {
                    Text("Feedbacks", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (feedbacks.isEmpty()) {
                        Text("No feedbacks available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp), // Constrain height
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(feedbacks) { feedback ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "User: ${feedback["userEmail"]?.toString() ?: "N/A"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Feedback: ${feedback["feedback"]?.toString() ?: "N/A"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Status: ${feedback["status"]?.toString() ?: "Pending"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Display Leave Requests
                item {
                    Text("Leave Requests", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (leaveRequests.isEmpty()) {
                        Text("No leave requests available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp), // Constrain height
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(leaveRequests) { leave ->
                                val status = leave["status"]?.toString() ?: "Pending"
                                val cardColor = when (status) {
                                    "Accepted" -> Color(0xFF4CAF50) // Green
                                    "Rejected" -> Color(0xFFE53935) // Red
                                    else -> MaterialTheme.colorScheme.surface // Default for Pending
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
                                            text = "User: ${leave["userEmail"]?.toString() ?: "N/A"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp,
                                            color = if (status == "Pending") MaterialTheme.colorScheme.onSurface else Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
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

                // Display Orders
                item {
                    Text("Orders", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (orders.isEmpty()) {
                        Text("No orders available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp), // Constrain height
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(orders) { order ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "User: ${order["userEmail"]?.toString() ?: "N/A"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Item: ${order["item"]?.toString() ?: "N/A"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Status: ${order["status"]?.toString() ?: "Pending"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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