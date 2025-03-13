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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

@Composable
fun LeaveScreen() {
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
                            data + mapOf("id" to doc.id)
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Submit Leave Request", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave") },
                        modifier = Modifier.fillMaxWidth()
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
                            }
                            .addOnFailureListener {
                                error = "Failed to submit leave request: ${it.message}"
                            }
                    } else {
                        error = "Reason cannot be empty"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Submit Leave Request", style = MaterialTheme.typography.labelLarge)
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

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
                        elevation = CardDefaults.cardElevation(2.dp),
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