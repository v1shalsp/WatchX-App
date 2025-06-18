package com.example.watchxapp.data.model

import androidx.annotation.DrawableRes

// MODIFIED: Added a sentiment field to the Tweet data class
data class Tweet(
    val author: String,
    val handle: String,
    val content: String,
    val sentiment: String // Can be "Positive", "Negative", or "Neutral"
)

data class TwitterAccount(
    val name: String,
    val handle: String,
    @DrawableRes val iconResId: Int,
    val followerCount: String
)

data class SearchUiState(
    val isLoading: Boolean = false,
    val tweets: List<Tweet> = emptyList(),
    val summary: String? = null,
    val sentiment: String? = null,
    val error: String? = null,
    val searchPerformed: Boolean = false
)

data class PinnedHashtag(val hashtag: String, val tweetCount: Int)