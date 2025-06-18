package com.example.watchxapp.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.watchxapp.data.model.Chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareTweetDialog(
    chats: List<Chat>,
    onDismissRequest: () -> Unit,
    onChatSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Share Tweet to...") },
        text = {
            if (chats.isEmpty()) {
                Text("You are not a member of any workspaces.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(chats) { chat ->
                        Text(
                            text = chat.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChatSelected(chat.id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}