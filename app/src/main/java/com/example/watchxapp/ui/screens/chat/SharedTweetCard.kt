package com.example.watchxapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.watchxapp.data.model.Message

@Composable
fun SharedTweetMessageCard(message: Message, isFromCurrentUser: Boolean) {
    val boxAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val columnAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start // FIX: Correct horizontal alignment
    val backgroundColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = boxAlignment
    ) {
        Column(horizontalAlignment = columnAlignment) { // FIX: Using the corrected alignment here
            // "You shared a tweet" or "User shared a tweet" label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 4.dp, start = 8.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Shared tweet",
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Text(
                    text = if (isFromCurrentUser) "You shared a tweet" else "${message.senderName} shared a tweet",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            // The actual tweet card
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (isFromCurrentUser) 16.dp else 0.dp,
                    bottomEnd = if (isFromCurrentUser) 0.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Text(
                                message.sharedTweetAuthor?.firstOrNull()?.toString()?.uppercase() ?: "?",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(message.sharedTweetAuthor ?: "", fontWeight = FontWeight.Bold)
                            Text(message.sharedTweetHandle ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(message.sharedTweetContent ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}