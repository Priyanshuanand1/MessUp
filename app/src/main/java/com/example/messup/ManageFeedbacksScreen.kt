package com.example.messup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFeedbacksScreen(navController: NavController, drawerState: DrawerState) {
    var feedbacks by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    val scopeDrawer = rememberCoroutineScope()

    // Fetch feedbacks from Firestore
    DisposableEffect(Unit) {
        val listener = db.collection("feedbacks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    scope.launch { snackbarHostState.showSnackbar("Error fetching feedbacks: ${e.message}") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    feedbacks = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap()
                        data + mapOf("id" to (doc.id ?: ""))
                    }
                }
            }
        listenerRegistration = listener
        onDispose { listenerRegistration?.remove() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Manage Feedbacks",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { scopeDrawer.launch { drawerState.open() } },
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open Drawer",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") { popUpTo(0) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.9f)),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text("Logout", fontSize = 14.sp, color = Color.White)
                        }
                    }
                )
            }
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
            item {
                Text("Feedbacks", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (feedbacks.isEmpty()) {
                    Text("No feedbacks available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(feedbacks) { feedback ->
                            val feedbackId = feedback["id"]?.toString() ?: ""
                            val status = feedback["status"]?.toString() ?: "Pending"
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
                                        text = "User: ${feedback["userEmail"]?.toString() ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        color = if (status == "Pending") MaterialTheme.colorScheme.onSurface else Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Feedback: ${feedback["feedback"]?.toString() ?: "N/A"}",
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
                                    if (status == "Pending") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (feedbackId.isNotEmpty()) {
                                                        db.collection("feedbacks").document(feedbackId)
                                                            .update("status", "Accepted")
                                                            .addOnSuccessListener {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar("Feedback accepted!")
                                                                }
                                                            }
                                                            .addOnFailureListener { e ->
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar("Failed to accept feedback: ${e.message}")
                                                                }
                                                            }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            ) {
                                                Text("Accept", fontSize = 14.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    if (feedbackId.isNotEmpty()) {
                                                        db.collection("feedbacks").document(feedbackId)
                                                            .update("status", "Rejected")
                                                            .addOnSuccessListener {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar("Feedback rejected!")
                                                                }
                                                            }
                                                            .addOnFailureListener { e ->
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar("Failed to reject feedback: ${e.message}")
                                                                }
                                                            }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 8.dp)
                                            ) {
                                                Text("Reject", fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}