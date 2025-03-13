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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestsScreen(navController: NavController) {
    var leaveRequests by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Real-time listener for leave requests
    DisposableEffect(Unit) {
        val listener = db.collection("leave_requests")
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
                        data + mapOf("id" to doc.id)
                    }
                }
            }
        listenerRegistration = listener
        onDispose {
            listenerRegistration?.remove()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manage Leave Requests") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    db.collection("leave_requests").document(leave["id"].toString())
                                        .update("status", "Accepted")
                                        .addOnSuccessListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Leave request accepted")
                                            }
                                        }
                                        .addOnFailureListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to update status")
                                            }
                                        }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = leave["status"]?.toString() == "Pending",
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Accept")
                            }
                            Button(
                                onClick = {
                                    db.collection("leave_requests").document(leave["id"].toString())
                                        .update("status", "Rejected")
                                        .addOnSuccessListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Leave request rejected")
                                            }
                                        }
                                        .addOnFailureListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to update status")
                                            }
                                        }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = leave["status"]?.toString() == "Pending",
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }
    }
}