package com.example.watchxapp.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box // <--- FIX: Added missing import
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.watchxapp.R
import com.example.watchxapp.ui.components.ProfileOptionRow

@Composable
fun ProfileScreen(rootNavController: NavController, isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    val auth = Firebase.auth
    var showLogoutDialog by remember { mutableStateOf(false) }

    // This listener automatically navigates to the auth screen on logout
    LaunchedEffect(auth) {
        auth.addAuthStateListener {
            if (it.currentUser == null) {
                rootNavController.navigate("auth_root") {
                    popUpTo("main_app") { inclusive = true }
                }
            }
        }
    }


    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        auth.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // FIX: The Box composable is now correctly referenced
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color.White else Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.account_circle_icon),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(110.dp),
                colorFilter = ColorFilter.tint(if (isDarkTheme) Color.Black else Color.White)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = auth.currentUser?.displayName ?: "User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = auth.currentUser?.email ?: "", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            // FIX: The composable invocations are now correctly inside the Column scope
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileOptionRow(title = "Change Password", onClick = { /* TODO */ })
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileOptionRow(title = "Dark Mode", hasToggle = true, isChecked = isDarkTheme, onCheckedChange = onThemeChange)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Logout")
        }
    }
}