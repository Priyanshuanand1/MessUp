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
fun OrderScreen() {
    var order by remember { mutableStateOf("") }
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
                    value = order,
                    onValueChange = { order = it },
                    label = { Text("Enter your order") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        Button(
            onClick = {
                if (order.isNotEmpty()) {
                    val orderData = hashMapOf("order" to order, "user" to auth.currentUser?.email)
                    db.collection("orders").add(orderData)
                        .addOnSuccessListener {
                            order = ""
                            showSuccessMessage = true
                        }
                        .addOnFailureListener {
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to place order")
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Submit Order", style = MaterialTheme.typography.labelLarge)
        }
    }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            scope.launch {
                snackbarHostState.showSnackbar("Order placed successfully!")
                showSuccessMessage = false
            }
        }
    }
}