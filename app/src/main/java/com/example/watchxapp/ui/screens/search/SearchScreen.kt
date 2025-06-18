package com.example.watchxapp.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.watchxapp.data.model.Tweet
import com.example.watchxapp.data.repository.DataSource
import com.example.watchxapp.ui.components.SummaryCard
import com.example.watchxapp.ui.components.TweetCard
import com.example.watchxapp.ui.screens.home.ShareTweetDialog
import com.example.watchxapp.viewmodel.ChatViewModel
import com.example.watchxapp.viewmodel.DashboardViewModel
import com.example.watchxapp.viewmodel.SearchViewModel


@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    dashboardViewModel: DashboardViewModel,
    chatViewModel: ChatViewModel = viewModel()
) {
    val uiState by searchViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val pinnedTweets by dashboardViewModel.pinnedTweets.collectAsState()
    val chats by chatViewModel.chats.collectAsState()

    var tweetToShare by remember { mutableStateOf<Tweet?>(null) }

    if (tweetToShare != null) {
        ShareTweetDialog(
            chats = chats,
            onDismissRequest = { tweetToShare = null },
            onChatSelected = { chatId ->
                chatViewModel.shareTweetToChat(tweetToShare!!, chatId) { /* Handle result */ }
                tweetToShare = null
            }
        )
    }

    val performSearch = { query: String ->
        if (query.isNotBlank()) {
            searchQuery = query // Update search query state
            searchViewModel.search(query)
            keyboardController?.hide()
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // --- FIX: This code for the Search Bar and Chips was accidentally removed and is now restored. ---
            Card(shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = Brush.horizontalGradient(colors = listOf(Color.Black, Color(0xFF0D47A1))))
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Enter Hashtag or Keyword") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.LightGray,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Try searching for:", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DataSource.recommendedKeywords) { keyword ->
                    SuggestionChip(
                        onClick = { performSearch(keyword) },
                        label = { Text(keyword) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            // --- End of restored code ---

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = uiState.searchPerformed && !uiState.isLoading && uiState.error == null) {
                Column {
                    uiState.sentiment?.let { sentiment ->
                        SummaryCard(summary = uiState.summary, sentiment = sentiment)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.searchPerformed) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(uiState.tweets) { tweet ->
                        TweetCard(
                            tweet = tweet,
                            isPinned = pinnedTweets.contains(tweet),
                            onPinClick = { dashboardViewModel.toggleTweetPin(tweet) },
                            onShareClick = { tweetToShare = tweet }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Enter a search term or select a keyword to begin.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}