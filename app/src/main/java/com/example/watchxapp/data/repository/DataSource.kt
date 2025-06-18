package com.example.watchxapp.data.repository

import com.example.watchxapp.R
import com.example.watchxapp.data.model.Tweet
import com.example.watchxapp.data.model.TwitterAccount
import java.util.Locale
import kotlin.random.Random

object DataSource {
    val topPeople = listOf(
        TwitterAccount("Elon Musk", "@elonmusk", R.drawable.elon_icon, "188.1M"),
        TwitterAccount("Barack Obama", "@BarackObama", R.drawable.obama_icon, "131.9M"),
        TwitterAccount("Cristiano Ronaldo", "@Cristiano", R.drawable.ronaldo_icon, "111.4M")
    )
    val topOrgs = listOf(
        TwitterAccount("NASA", "@NASA", R.drawable.nasa_icon, "87.1M"),
        TwitterAccount("Google", "@Google", R.drawable.google_icon, "28.5M"),
        TwitterAccount("Netflix", "@netflix", R.drawable.netflix_icon, "78.4M")
    )
    val recommendedKeywords = listOf("AI", "Jetpack Compose", "War", "Disaster", "World Cup")
    private val allAccounts = topPeople + topOrgs
    fun findAccountByHandle(handle: String): TwitterAccount? { return allAccounts.find { it.handle == handle } }

    // ADDED BACK: This function was accidentally deleted.
    fun getDummyTweetsFor(account: TwitterAccount): List<Tweet> {
        return when (account.handle) {
            "@elonmusk" -> listOf(Tweet(account.name, account.handle, "Starship is ready for launch!", "Positive"), Tweet(account.name, account.handle, "Thinking about buying Twitter... again.", "Neutral"), Tweet(account.name, account.handle, "Doge to the moon! 🚀", "Positive"))
            "@BarackObama" -> listOf(Tweet(account.name, account.handle, "Hope is a powerful thing. Keep believing.", "Positive"), Tweet(account.name, account.handle, "Reading a great book. What are you reading?", "Neutral"), Tweet(account.name, account.handle, "Inspiring to see so many young people making a difference.", "Positive"))
            "@Cristiano" -> listOf(Tweet(account.name, account.handle, "Another win! Great team effort. Siuuu!", "Positive"), Tweet(account.name, account.handle, "Hard work pays off. #blessed", "Positive"), Tweet(account.name, account.handle, "Family time is the best time.", "Positive"))
            "@NASA" -> listOf(Tweet(account.name, account.handle, "The James Webb Telescope has captured another stunning image of a distant galaxy.", "Positive"), Tweet(account.name, account.handle, "Our Artemis I mission is paving the way for future human exploration of the Moon.", "Positive"), Tweet(account.name, account.handle, "Curiosity rover checking in from Mars. The views are out of this world!", "Positive"))
            "@Google" -> listOf(Tweet(account.name, account.handle, "Check out the new features in Android 15!", "Neutral"), Tweet(account.name, account.handle, "Happy to announce our latest AI model, it's our most capable yet.", "Positive"), Tweet(account.name, account.handle, "It's I/O day! Tune in for the latest news from Google.", "Neutral"))
            "@netflix" -> listOf(Tweet(account.name, account.handle, "New season of Stranger Things is coming soon!", "Neutral"), Tweet(account.name, account.handle, "What are you watching this weekend?", "Neutral"), Tweet(account.name, account.handle, "Our new documentary just won an award!", "Positive"))
            else -> listOf(Tweet(account.name, account.handle, "This is a sample tweet! #Sample", "Neutral"), Tweet(account.name, account.handle, "Just setting up my WatchX profile.", "Neutral"), Tweet(account.name, account.handle, "What's happening? #FirstTweet", "Neutral"))
        }
    }

    private fun determineSentiment(text: String): String {
        val positiveWords = listOf("love", "amazing", "great", "excellent", "happy", "success", "fascinating", "celebration", "excited")
        val negativeWords = listOf("hate", "bad", "terrible", "awful", "disaster", "sad", "war", "concerned", "failure", "controversy")
        return when {
            positiveWords.any { text.contains(it, ignoreCase = true) } -> "Positive"
            negativeWords.any { text.contains(it, ignoreCase = true) } -> "Negative"
            else -> "Neutral"
        }
    }

    fun getTweetsForQuery(query: String): List<Tweet> {
        val generatedTweets = mutableListOf<Tweet>()
        val authors = listOf(
            Pair("Tech Insider", "@TechIn"), Pair("Global News Network", "@GNN"),
            Pair("Market Watchers", "@MarketWatch"), Pair("Everyday Commenter", "@JustAComment"),
            Pair("Dr. Anna Cruz", "@DrAnnaCruz"), Pair("Future Forward", "@FutureFWD"),
            Pair("The Cynic", "@CynicWeekly"), Pair("Optimist Prime", "@OptimusPrime")
        )

        val openers = listOf("Breaking:", "Just in:", "My thoughts on", "A deep dive into", "Let's talk about", "I can't believe", "The new report on", "Amazing news from")
        val topics = listOf("the future of", "the impact of", "the debate around", "the success of", "the recent failure of", "the controversy surrounding")
        val closers = listOf("is truly mind-blowing.", "is something we should all be watching.", "could change everything.", "is a complete disaster.", "is cause for great celebration.", "is deeply concerning.")

        repeat(15) {
            val author = authors.random()
            val opener = openers.random()
            val topic = topics.random()
            val closer = closers.random()
            val hashtag = "#${query.replace(" ", "")}"

            val content = "$opener $topic $query. $closer $hashtag"
            val sentiment = determineSentiment(content)

            generatedTweets.add(
                Tweet(
                    author = author.first,
                    handle = author.second,
                    content = content,
                    sentiment = sentiment
                )
            )
        }
        return generatedTweets.shuffled()
    }

    fun getLatestTweets(): List<Tweet> {
        val generatedTweets = mutableListOf<Tweet>()
        val authors = listOf(
            Pair("Tech Insider", "@TechIn"), Pair("Global News Network", "@GNN"),
            Pair("Market Watchers", "@MarketWatch"), Pair("Everyday Commenter", "@JustAComment")
        )
        val topics = listOf("the new AI models", "this year's election", "the state of the economy", "advances in space travel", "the latest foldable phones")
        val actions = listOf("is absolutely mind-blowing.", "is more complicated than people think.", "deserves more attention.", "is a cause for concern.", "is something to be excited about!")

        repeat(5) {
            val author = authors.random()
            val topic = topics.random()
            val action = actions.random()
            val content = "My latest take: ${topic} ${action} #${topic.split(" ").last()}"
            val sentiment = determineSentiment(content)

            generatedTweets.add(
                Tweet(
                    author = author.first,
                    handle = author.second,
                    content = content,
                    sentiment = sentiment
                )
            )
        }
        return generatedTweets.shuffled()
    }
}