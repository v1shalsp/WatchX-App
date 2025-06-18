package com.example.watchxapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchxapp.data.model.PinnedHashtag
import com.example.watchxapp.data.model.Tweet
import com.example.watchxapp.data.repository.DataSource
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardViewModel : ViewModel() {
    private val _pinnedHashtags = MutableStateFlow<List<PinnedHashtag>>(emptyList())
    val pinnedHashtags = _pinnedHashtags.asStateFlow()

    private val _pinnedTweets = MutableStateFlow<Set<Tweet>>(emptySet())
    val pinnedTweets = _pinnedTweets.asStateFlow()

    private val _latestTweets = MutableStateFlow<List<Tweet>>(emptyList())
    val latestTweets = _latestTweets.asStateFlow()

    private val _followerCounts = MutableStateFlow<Map<String, String>>(emptyMap())
    val followerCounts = _followerCounts.asStateFlow()

    init {
        // Load all data when the ViewModel is first created
        refreshAll()
    }

    fun refreshAll() {
        refreshLatestTweets()
        fetchFollowerCounts()
    }

    private fun refreshLatestTweets() {
        _latestTweets.value = DataSource.getLatestTweets()
    }

    // Helper to format large numbers (e.g., 188123456 -> "188.1M")
    private fun formatFollowerCount(count: Long): String {
        return when {
            count >= 1_000_000 -> {
                val millions = count / 1_000_000.0
                String.format(Locale.US, "%.1fM", millions)
            }
            count >= 1_000 -> {
                val thousands = count / 1_000.0
                String.format(Locale.US, "%.1fK", thousands)
            }
            else -> count.toString()
        }
    }

    // Fetches live follower counts by calling our secure Cloud Function
    private fun fetchFollowerCounts() {
        viewModelScope.launch {
            val allAccounts = DataSource.topPeople + DataSource.topOrgs
            // Get usernames without the '@' symbol for the API call
            val usernames = allAccounts.map { it.handle.drop(1) }

            // Set initial state to "loading" for the UI
            val loadingMap = allAccounts.associate { it.handle to "..." }
            _followerCounts.value = loadingMap

            val data = hashMapOf("usernames" to usernames)

            Firebase.functions
                .getHttpsCallable("getLiveFollowerCounts")
                .call(data)
                .addOnSuccessListener { result ->
                    val counts = (result.data as? Map<String, Any>)?.get("counts") as? Map<String, Number>
                    if (counts != null) {
                        // Format the raw numbers into strings like "188.1M"
                        val formattedCounts = counts.mapValues { formatFollowerCount(it.value.toLong()) }
                        // Update the state with the new data
                        _followerCounts.update { currentCounts -> currentCounts + formattedCounts }
                    } else {
                        // Handle case where the 'counts' map might be missing in the response
                        val errorMap = allAccounts.associate { it.handle to "N/A" }
                        _followerCounts.value = errorMap
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardViewModel", "Failed to fetch follower counts", e)
                    val errorMap = allAccounts.associate { it.handle to "Error" }
                    _followerCounts.value = errorMap
                }
        }
    }

    fun isHashtagPinned(hashtag: String): Boolean {
        return _pinnedHashtags.value.any { it.hashtag.equals(hashtag, ignoreCase = true) }
    }

    fun pinHashtag(hashtag: String, tweetCount: Int): Boolean {
        if (_pinnedHashtags.value.size < 3 && !isHashtagPinned(hashtag)) {
            _pinnedHashtags.update { it + PinnedHashtag(hashtag, tweetCount) }
            return true
        }
        return false
    }

    fun toggleTweetPin(tweet: Tweet) {
        _pinnedTweets.update { currentPins ->
            if (currentPins.contains(tweet)) {
                currentPins - tweet
            } else {
                currentPins + tweet
            }
        }
    }
}