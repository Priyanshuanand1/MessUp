package com.example.messup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var menuItem by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var menuItems by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showAdminMode by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                users = result.map { it.data }
            }
        db.collection("menu").get()
            .addOnSuccessListener { result ->
                menuItems = result.map { it.data }
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
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("User Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                Button(
                    onClick = {
                        if (email.isNotEmpty()) {
                            val userData = hashMapOf("email" to email, "role" to "user")
                            db.collection("users").document(email).set(userData)
                                .addOnSuccessListener {
                                    email = ""
                                    showSuccessMessage = true
                                }
                                .addOnFailureListener {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Failed to add user")
                                    }
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

                // Display All Users
                Spacer(modifier = Modifier.height(16.dp))
                Text("All Users", style = MaterialTheme.typography.titleLarge)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Email: ${user["email"].toString()}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Role: ${user["role"].toString()}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Name: ${user["name"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Room No: ${user["roomNo"]?.toString() ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
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