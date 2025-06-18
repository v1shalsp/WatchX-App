package com.example.watchxapp.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.watchxapp.R
import com.example.watchxapp.data.model.PinnedHashtag
import com.example.watchxapp.data.model.Tweet
import com.example.watchxapp.data.model.TwitterAccount
import com.example.watchxapp.data.repository.DataSource
import com.example.watchxapp.ui.components.TweetCard
import com.example.watchxapp.viewmodel.ChatViewModel
import com.example.watchxapp.viewmodel.DashboardViewModel

@Composable
fun WatchXHomeScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel,
    chatViewModel: ChatViewModel = viewModel() // Get instance of ChatViewModel
) {
    val pinnedHashtags by dashboardViewModel.pinnedHashtags.collectAsState()
    val pinnedTweets by dashboardViewModel.pinnedTweets.collectAsState()
    val latestTweets by dashboardViewModel.latestTweets.collectAsState()
    val followerCounts by dashboardViewModel.followerCounts.collectAsState()
    val chats by chatViewModel.chats.collectAsState()

    // State to manage which tweet is being shared
    var tweetToShare by remember { mutableStateOf<Tweet?>(null) }

    // When tweetToShare is not null, show the dialog
    if (tweetToShare != null) {
        ShareTweetDialog(
            chats = chats,
            onDismissRequest = { tweetToShare = null },
            onChatSelected = { chatId ->
                chatViewModel.shareTweetToChat(tweetToShare!!, chatId) { /* Handle result if needed */ }
                tweetToShare = null
            }
        )
    }

    LaunchedEffect(Unit) {
        dashboardViewModel.refreshAll()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { DashboardHeader() }

        if (pinnedHashtags.isNotEmpty()) {
            item { PinnedHashtagsSection(pinnedHashtags) }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest Tweets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { dashboardViewModel.refreshAll() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }

        items(latestTweets) { tweet ->
            TweetCard(
                tweet = tweet,
                isPinned = pinnedTweets.contains(tweet),
                onPinClick = { dashboardViewModel.toggleTweetPin(tweet) },
                onShareClick = { tweetToShare = tweet } // Trigger the share dialog
            )
        }

        item {
            TopAccountsSection(
                title = "Most Followed People",
                accounts = DataSource.topPeople,
                liveCounts = followerCounts,
                iconResId = R.drawable.social_leaderboard_icon,
                navController = navController
            )
        }
        item {
            TopAccountsSection(
                title = "Most Followed Organizations",
                accounts = DataSource.topOrgs,
                liveCounts = followerCounts,
                iconResId = R.drawable.leaderboard_icon,
                navController = navController
            )
        }
    }
}

@Composable
fun TopAccountsSection(
    title: String,
    accounts: List<TwitterAccount>,
    liveCounts: Map<String, String>, // Accepts the map of live data
    @DrawableRes iconResId: Int,
    navController: NavController
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.7f), tonalElevation = 2.dp) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.Black),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$title Icon",
                modifier = Modifier.size(28.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp)) {
                accounts.forEach { account ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { navController.navigate("account_detail/${account.handle}") }
                            .padding(vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = account.iconResId),
                            contentDescription = "${account.name} profile picture",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(account.name, fontWeight = FontWeight.Bold)
                            Text(account.handle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            // Use the live count from the map, or the hardcoded default if not yet loaded
                            Text(
                                text = liveCounts[account.handle] ?: account.followerCount,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }
                    if (account != accounts.last()) Divider(Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}


@Composable
fun DashboardHeader() {
    var filterMenuExpanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box {
                DropdownMenu(
                    expanded = filterMenuExpanded,
                    onDismissRequest = { filterMenuExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("Trending") }, onClick = { filterMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Latest") }, onClick = { filterMenuExpanded = false })
                }
            }
        }
    }
}

@Composable
fun PinnedHashtagsSection(pinnedHashtags: List<PinnedHashtag>) {
    Column {
        Text("Pinned Hashtags", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (pinnedHashtags.isEmpty()) {
                    Text("No hashtags pinned yet.", color = Color.Gray)
                } else {
                    pinnedHashtags.forEach { pinned ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "#${pinned.hashtag}",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("${pinned.tweetCount} tweets", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}