package com.example.watchxapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.watchxapp.data.model.Chat
import com.example.watchxapp.data.model.Message
import com.example.watchxapp.data.model.Tweet
import com.example.watchxapp.data.model.Workspace
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val functions = Firebase.functions

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var messagesListener: ListenerRegistration? = null
    private var chatMetadataListener: ListenerRegistration? = null

    init {
        fetchUserChats()
    }

    // --- Create, Join, Delete Functions ---
    fun createChat(workspaceName: String) {
        val currentUser = auth.currentUser ?: return
        val newWorkspaceRef = db.collection("workspaces").document()
        val newChatRef = db.collection("chats").document()
        val initialMembers = listOf(currentUser.uid)

        val workspace = Workspace(id = newWorkspaceRef.id, name = workspaceName, members = initialMembers, chatId = newChatRef.id)
        val chat = Chat(id = newChatRef.id, name = workspaceName, members = initialMembers, workspaceId = newWorkspaceRef.id)

        db.batch().apply {
            set(newWorkspaceRef, workspace)
            set(newChatRef, chat)
        }.commit()
    }

    fun joinChat(chatId: String, memberEmail: String, onResult: (String) -> Unit) {
        val data = hashMapOf("chatId" to chatId.trim(), "memberEmail" to memberEmail.trim().lowercase())
        functions.getHttpsCallable("joinChat").call(data)
            .addOnSuccessListener { result ->
                val message = (result.data as? Map<*, *>)?.get("message") as? String
                onResult(message ?: "Success!")
            }
            .addOnFailureListener { e -> onResult("Failed: ${e.message}") }
    }

    fun deleteChat(chat: Chat, onResult: (String) -> Unit) {
        val data = hashMapOf("chatId" to chat.id, "workspaceId" to chat.workspaceId)
        functions.getHttpsCallable("deleteChat").call(data)
            .addOnSuccessListener { onResult("Workspace deleted.") }
            .addOnFailureListener { e -> onResult("Failed: ${e.message}") }
    }

    fun clearChatForUser(chatId: String, onResult: (String) -> Unit) {
        val data = hashMapOf("chatId" to chatId)
        functions.getHttpsCallable("clearChatForUser").call(data)
            .addOnSuccessListener {
                // The listeners will now handle the UI update automatically
                onResult("Chat cleared.")
            }
            .addOnFailureListener { e -> onResult("Failed: ${e.message}") }
    }


    // --- Data Fetching Functions ---
    private fun fetchUserChats() {
        val currentUserUid = auth.currentUser?.uid

        // --- CRUCIAL DEBUGGING STEP ---
        // This will print the exact User ID the app is using for the query.
        Log.d("DEBUG_VIEWMODEL", "Attempting to fetch chats for user UID: $currentUserUid")

        if (currentUserUid == null) {
            Log.e("DEBUG_VIEWMODEL", "User is NOT logged in. Cannot fetch chats.")
            _isLoading.value = false // Stop loading if the user is null
            return
        }

        _isLoading.value = true
        db.collection("chats")
            .whereArrayContains("members", currentUserUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("DEBUG_VIEWMODEL", "Listen failed.", e)
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                _chats.value = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                _isLoading.value = false
            }
    }

    // MODIFIED: This function is now fully reactive to solve the "Clear Chat" bug
    fun listenForMessages(chatId: String) {
        // Clean up previous listeners before starting new ones
        messagesListener?.remove()
        chatMetadataListener?.remove()

        val currentUserUid = auth.currentUser?.uid ?: return
        val userChatDataRef = db.collection("users").document(currentUserUid)
            .collection("chatMetadata").document(chatId)

        // Listen for changes to the 'clearedAt' timestamp in real-time
        chatMetadataListener = userChatDataRef.addSnapshotListener { doc, error ->
            if (error != null) {
                Log.w("ChatViewModel", "Metadata listen failed.", error)
                // If we can't get metadata, just load the whole chat
                attachMessagesListener(chatId, null)
                return@addSnapshotListener
            }

            val clearedAt = doc?.getTimestamp("clearedAt")
            // Re-attach the message listener with the new timestamp
            attachMessagesListener(chatId, clearedAt)
        }
    }

    private fun attachMessagesListener(chatId: String, clearedAt: com.google.firebase.Timestamp?) {
        messagesListener?.remove() // Clean up the old message listener

        var query = db.collection("chats").document(chatId)
            .collection("messages").orderBy("timestamp")

        if (clearedAt != null) {
            query = query.whereGreaterThan("timestamp", clearedAt)
        }

        messagesListener = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("ChatViewModel", "Messages listen failed.", e)
                return@addSnapshotListener
            }
            _messages.value = snapshot?.toObjects(Message::class.java) ?: emptyList()
        }
    }

    // ADDED: This is the function that was missing
    fun shareTweetToChat(tweet: Tweet, chatId: String, onResult: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val message = Message(
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "User",
            type = "SHARED_TWEET",
            text = "${currentUser.displayName ?: "User"} shared a tweet.", // Fallback text
            sharedTweetAuthor = tweet.author,
            sharedTweetHandle = tweet.handle,
            sharedTweetContent = tweet.content,
            sharedTweetSentiment = tweet.sentiment
        )
        // Call the private sendMessage function to send the specialized message
        sendMessage(chatId, message)
        onResult("Tweet shared!")
    }

    // Public function for sending a simple text message from the ChatScreen
    fun sendMessage(chatId: String, text: String) {
        val currentUser = auth.currentUser ?: return
        val message = Message(
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "User",
            type = "TEXT",
            text = text,
        )
        sendMessage(chatId, message)
    }

    // Private function that handles sending any type of Message object to Firestore
    private fun sendMessage(chatId: String, message: Message) {
        val chatRef = db.collection("chats").document(chatId)
        db.collection("chats").document(chatId).collection("messages")
            .add(message)
            .addOnSuccessListener {
                chatRef.update(
                    "lastMessage", message.text ?: "Shared a tweet",
                    "lastMessageSender", message.senderName,
                    "lastMessageTimestamp", com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error sending message", e)
            }
    }


    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
        chatMetadataListener?.remove()
    }
}