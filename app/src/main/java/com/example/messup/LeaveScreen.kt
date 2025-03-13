package com.example.messup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(navController: NavController) { // Added NavController parameter
    var reason by remember { mutableStateOf("") }
    var userLeaveRequests by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Fetch user's leave requests in real-time
    DisposableEffect(Unit) {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            val listener = db.collection("leave_requests")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Error fetching leave requests: ${e.message}")
                        }
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
        onDispose {
            listenerRegistration?.remove()
        }
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
                    IconButton(onClick = { /* Add drawer logic if needed */ }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Submit Leave Request Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Submit Leave Request", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false // Allow multiline for reason
                    )
                }
            }
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
                                scope.launch {
                                    snackbarHostState.showSnackbar("Leave request submitted successfully!")
                                }
                            }
                            .addOnFailureListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Failed to submit leave request: ${it.message}")
                                }
                            }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Reason cannot be empty")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Submit Leave Request", style = MaterialTheme.typography.labelLarge)
            }
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Display User's Leave Requests
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Leave Requests", style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userLeaveRequests) { leave ->
                    val status = leave["status"]?.toString() ?: "Pending"
                    val cardColor = when (status) {
                        "Accepted" -> Color(0xFF4CAF50) // Green
                        "Rejected" -> Color(0xFFE53935) // Red
                        else -> MaterialTheme.colorScheme.surface // Default for Pending
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Reason: ${leave["reason"]?.toString() ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (status == "Pending") MaterialTheme.colorScheme.onSurface else Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Status: $status",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (status == "Pending") MaterialTheme.colorScheme.onSurface else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}