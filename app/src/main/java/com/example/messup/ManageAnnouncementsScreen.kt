package com.example.messup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAnnouncementsScreen(navController: NavController, drawerState: DrawerState) {
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var announcements by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    val scopeDrawer = rememberCoroutineScope()

    // Fetch announcements from Firestore
    DisposableEffect(Unit) {
        val listener = db.collection("announcements")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    scope.launch { snackbarHostState.showSnackbar("Error fetching announcements: ${e.message}") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    announcements = snapshot.documents.map { doc ->
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
                            "Manage Announcements",
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
            // Section to create a new announcement
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Create Announcement", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = announcementTitle,
                            onValueChange = { announcementTitle = it },
                            label = { Text("Title", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = announcementMessage,
                            onValueChange = { announcementMessage = it },
                            label = { Text("Message", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (announcementTitle.isNotEmpty() && announcementMessage.isNotEmpty()) {
                                    val announcementData = hashMapOf(
                                        "title" to announcementTitle,
                                        "message" to announcementMessage,
                                        "timestamp" to FieldValue.serverTimestamp() // Add server timestamp
                                    )
                                    db.collection("announcements").add(announcementData)
                                        .addOnSuccessListener {
                                            announcementTitle = ""
                                            announcementMessage = ""
                                            scope.launch { snackbarHostState.showSnackbar("Announcement created successfully!") }
                                        }
                                        .addOnFailureListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to create announcement: ${it.message}")
                                            }
                                        }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Title and message are required")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Create Announcement", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Section to display existing announcements
            item {
                Text("Existing Announcements", style = MaterialTheme.typography.titleLarge, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (announcements.isEmpty()) {
                    Text("No announcements available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(announcements) { announcement ->
                            val announcementId = announcement["id"]?.toString() ?: ""
                            val timestamp = announcement["timestamp"]?.let { it as? com.google.firebase.Timestamp }
                            val formattedDate = timestamp?.toDate()?.let { date ->
                                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
                            } ?: "N/A"

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
                                    Column {
                                        Text(
                                            text = "Title: ${announcement["title"]?.toString() ?: "N/A"}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Message: ${announcement["message"]?.toString() ?: "N/A"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Posted: $formattedDate",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            if (announcementId.isNotEmpty()) {
                                                db.collection("announcements").document(announcementId)
                                                    .delete()
                                                    .addOnSuccessListener {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Announcement deleted successfully!")
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Failed to delete announcement: ${e.message}")
                                                        }
                                                    }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Announcement",
                                            tint = Color.Red
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
}