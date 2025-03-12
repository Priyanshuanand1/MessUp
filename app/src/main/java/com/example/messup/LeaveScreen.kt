package com.example.messup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun LeaveScreen() {
    var reason by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for leave") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }
        }
        Button(
            onClick = {
                if (reason.isNotEmpty()) {
                    val leaveData = hashMapOf("reason" to reason, "user" to auth.currentUser?.email)
                    db.collection("leave_requests").add(leaveData)
                        .addOnSuccessListener {
                            reason = ""
                            showSuccessMessage = true
                        }
                        .addOnFailureListener {
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to submit leave")
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Submit Leave", style = MaterialTheme.typography.labelLarge)
        }
    }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            scope.launch {
                snackbarHostState.showSnackbar("Leave request submitted!")
                showSuccessMessage = false
            }
        }
    }
}