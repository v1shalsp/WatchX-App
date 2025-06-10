package com.example.watchxapp

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest // NEW: Import IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts // Keep this
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.example.watchxapp.ui.theme.WatchXAppTheme
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.identity.BeginSignInRequest // NEW: Import BeginSignInRequest

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: SignInClient
    private lateinit var auth: FirebaseAuth

    // FIXED: Change contract to StartIntentSenderForResult
    private lateinit var googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        googleSignInClient = Identity.getSignInClient(this)

        // FIXED: Register for ActivityResult with StartIntentSenderForResult
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = googleSignInClient.getSignInCredentialFromIntent(result.data)
                    firebaseAuthWithGoogle(credential)
                } catch (e: ApiException) {
                    Log.w("GOOGLE_AUTH", "Google sign in failed: ${e.statusCode}", e)
                    Toast.makeText(this, "Google Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("GOOGLE_AUTH", "Error processing Google sign-in result", e)
                    Toast.makeText(this, "Error during Google Sign-In", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.d("GOOGLE_AUTH", "Google Sign-In cancelled or failed with result code: ${result.resultCode}")
                Toast.makeText(this, "Google Sign-In cancelled or failed", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            WatchXAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") {
                        LoginSignupScreen(
                            onAuthSuccess = {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            },
                            googleSignInLauncher = googleSignInLauncher,
                            auth = auth,
                            activity = this@MainActivity,
                            gisSignInClient = googleSignInClient
                        )
                    }
                    composable("home") {
                        WatchXHomeScreen()
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(credential: SignInCredential) {
        val idToken = credential.googleIdToken
        if (idToken == null) {
            Log.w("GOOGLE_AUTH", "Google ID Token not found in credential")
            Toast.makeText(this, "Google ID Token missing", Toast.LENGTH_LONG).show()
            return
        }
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("GOOGLE_AUTH", "signInWithCredential:success (GIS)")
                    Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("GOOGLE_AUTH", "signInWithCredential:failure (GIS)", task.exception)
                    Toast.makeText(this, "Firebase Auth failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}

@Composable
fun LoginSignupScreen(
    onAuthSuccess: () -> Unit,
    googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>, // FIXED: Launcher type
    auth: FirebaseAuth,
    activity: MainActivity,
    gisSignInClient: SignInClient
) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLogin) "Login to WatchX" else "Create Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!isLogin) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true

                // Validation
                if (!isLogin) {
                    if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMessage = "All fields must be filled"
                        isLoading = false
                        return@Button
                    }
                    if (!phone.matches(Regex("^\\d{10}$"))) {
                        errorMessage = "Enter a valid 10-digit phone number"
                        isLoading = false
                        return@Button
                    }
                } else {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email and Password must not be empty"
                        isLoading = false
                        return@Button
                    }
                }

                if (isLogin) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onAuthSuccess()
                            } else {
                                errorMessage = "Login failed: ${task.exception?.localizedMessage}"
                            }
                        }
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: ""
                                val db = FirebaseFirestore.getInstance()
                                val userMap = hashMapOf(
                                    "name" to name,
                                    "phone" to phone,
                                    "email" to email
                                )
                                db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onAuthSuccess()
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        errorMessage = "Failed to save user data"
                                    }
                            } else {
                                isLoading = false
                                errorMessage = "Signup failed: ${task.exception?.localizedMessage}"
                            }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5BFF)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = if (isLogin) "Login" else "Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // FIXED: Corrected BeginSignInRequest.Builder usage and asynchronous handling
                gisSignInClient.beginSignIn(
                    BeginSignInRequest.Builder()
                        .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                                .setSupported(true)
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                        )
                        .setAutoSelectEnabled(false)
                        .build()
                ).addOnSuccessListener { result ->
                    try {
                        val signInIntentSender = result.pendingIntent.intentSender
                        // Launch the IntentSenderRequest
                        googleSignInLauncher.launch(IntentSenderRequest.Builder(signInIntentSender).build())
                    } catch (e: IntentSender.SendIntentException) {
                        errorMessage = "Google Sign-In launch failed: ${e.localizedMessage}"
                        Log.e("GOOGLE_AUTH", "Error launching sign-in UI", e)
                    } catch (e: Exception) {
                        errorMessage = "An unexpected error occurred: ${e.localizedMessage}"
                        Log.e("GOOGLE_AUTH", "Unexpected error during beginSignIn", e)
                    }
                }.addOnFailureListener { e ->
                    // Handle cases where beginSignIn itself fails (e.g., no compatible Google Play Services)
                    errorMessage = "Google Sign-In request failed: ${e.localizedMessage}"
                    Log.e("GOOGLE_AUTH", "beginSignIn failed", e)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            enabled = !isLoading
        ) {
            Text("Sign in with Google", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { isLogin = !isLogin }, enabled = !isLoading) {
            Text(
                text = if (isLogin) "Don't have an account? Sign up" else "Already have an account? Log in",
                color = Color.Gray
            )
        }
    }
}

@Composable
fun WatchXHomeScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FE))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to WatchX",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Enter hashtag or keyword...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD0D0D0),
                unfocusedBorderColor = Color(0xFFD0D0D0)
            ),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        Toast.makeText(context, "Searching for: $searchQuery (X API)", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enter a hashtag or keyword above to start monitoring Twitter in real-time. Get instant insights on sentiment, geography, and demographics.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        FeatureItem(title = "Real-time Monitoring", description = "Track hashtags and keywords instantly")
        FeatureItem(title = "Sentiment Analysis", description = "Understand public opinion and mood")
        FeatureItem(title = "Global Insights", description = "Geographic and demographic analytics")
    }
}

@Composable
fun FeatureItem(title: String, description: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Text(text = description, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}