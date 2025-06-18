package com.example.watchxapp.ui.screens.auth

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watchxapp.R
import com.example.watchxapp.data.model.CredentialsState
import com.example.watchxapp.data.model.UserProfile
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun LoginSignupScreen(onAuthSuccess: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var credentials by remember { mutableStateOf(CredentialsState()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var showSignupSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    val db = Firebase.firestore

    // Google Sign-In
    val oneTapClient = Identity.getSignInClient(context)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val googleIdToken = credential.googleIdToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            if (user != null) {
                                val userRef = db.collection("users").document(user.uid)
                                userRef.get().addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        val newUserProfile = UserProfile(
                                            name = user.displayName ?: "User",
                                            email = user.email ?: "",
                                            phone = user.phoneNumber ?: ""
                                        )
                                        userRef.set(newUserProfile)
                                    }
                                }
                            }
                            onAuthSuccess()
                        } else {
                            errorMessage = "Google Sign-In failed: ${task.exception?.message}"
                        }
                    }
            } catch (e: ApiException) {
                errorMessage = "Google Sign-In failed: ${e.localizedMessage}"
            }
        }
    }

    if (showSignupSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSignupSuccessDialog = false
                onAuthSuccess()
            },
            title = { Text("Success") },
            text = { Text("Sign-up successful!") },
            confirmButton = {
                Button(onClick = {
                    showSignupSuccessDialog = false
                    onAuthSuccess()
                }) { Text("OK") }
            }
        )
    }

    // FIX: Added the 'login@' label to the lambda definition
    val handleLogin: () -> Unit = login@{
        if (credentials.email.isBlank() || credentials.password.isBlank()) {
            errorMessage = "Email and Password must not be empty"
            return@login
        }
        isLoading = true
        auth.signInWithEmailAndPassword(credentials.email, credentials.password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onAuthSuccess()
                } else {
                    errorMessage = "Login failed: ${task.exception?.localizedMessage}"
                }
            }
    }

    // FIX: Added the 'signup@' label to the lambda definition
    val handleSignup: () -> Unit = signup@{
        if (credentials.name.isBlank() || credentials.phone.isBlank() || credentials.email.isBlank() || credentials.password.isBlank()) {
            errorMessage = "All fields must be filled"
            return@signup
        }
        isLoading = true
        auth.createUserWithEmailAndPassword(credentials.email, credentials.password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val userMap = UserProfile(
                        name = credentials.name,
                        phone = credentials.phone,
                        email = credentials.email
                    )
                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener { showSignupSuccessDialog = true }
                        .addOnFailureListener { errorMessage = "Failed to save user data" }
                } else {
                    errorMessage = "Signup failed: ${task.exception?.localizedMessage}"
                }
            }
    }

    val handleGoogleSignIn: () -> Unit = {
        isLoading = true
        val signInRequest = BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                scope.launch {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        googleSignInLauncher.launch(intentSenderRequest)
                    } catch (e: IntentSender.SendIntentException) {
                        errorMessage = "Google Sign-In launch failed: ${e.localizedMessage}"
                    }
                }
            }
            .addOnFailureListener { e ->
                errorMessage = "Google Sign-In request failed: ${e.localizedMessage}"
            }
            .addOnCompleteListener { isLoading = false }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = "Login Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (isLogin) "Login to WatchX" else "Create Account",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    if (!isLogin) {
                        AuthTextField(
                            value = credentials.name,
                            onValueChange = { credentials = credentials.copy(name = it) },
                            label = "Full Name",
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AuthTextField(
                            value = credentials.phone,
                            onValueChange = { credentials = credentials.copy(phone = it) },
                            label = "Phone Number",
                            keyboardType = KeyboardType.Phone,
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    AuthTextField(
                        value = credentials.email,
                        onValueChange = { credentials = credentials.copy(email = it) },
                        label = "Email",
                        keyboardType = KeyboardType.Email,
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AuthTextField(
                        value = credentials.password,
                        onValueChange = { credentials = credentials.copy(password = it) },
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { if (isLogin) handleLogin() else handleSignup() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(text = if (isLogin) "Login" else "Sign Up", color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = handleGoogleSignIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                        enabled = !isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.google_icon),
                                contentDescription = "Google Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sign in with Google", color = Color.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { isLogin = !isLogin }, enabled = !isLoading, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(
                            text = if (isLogin) "Don't have an account? Sign up" else "Already have an account? Log in",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.DarkGray,
            focusedContainerColor = Color.White.copy(alpha = 0.8f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
        )
    )
}