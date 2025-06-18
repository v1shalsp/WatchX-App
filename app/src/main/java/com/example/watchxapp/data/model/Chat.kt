package com.example.watchxapp.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Workspace(
    @DocumentId val id: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val chatId: String = ""
)

data class Chat(
    @DocumentId val id: String = "",
    val name: String = "",
    val workspaceId: String = "",
    val members: List<String> = emptyList(),
    val lastMessage: String? = null,
    val lastMessageSender: String? = null,
    @ServerTimestamp val lastMessageTimestamp: Date? = null
)

data class Message(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    @ServerTimestamp val timestamp: Date? = null,

    // ADDED: New fields to handle different message types
    val type: String = "TEXT", // Can be "TEXT" or "SHARED_TWEET"
    val text: String? = null, // Text is now optional

    // Fields specifically for shared tweets
    val sharedTweetAuthor: String? = null,
    val sharedTweetHandle: String? = null,
    val sharedTweetContent: String? = null,
    val sharedTweetSentiment: String? = null
)