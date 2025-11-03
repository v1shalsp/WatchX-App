# 🕵️‍♂️ WatchX – Twitter Monitoring App  
### *Real-Time Social Media Intelligence & Analytics Dashboard*  

---

## 🧠 Overview

**WatchX** is an **AI-powered Twitter monitoring and analytics system** that tracks trending topics, user sentiments, and influencer activity in real time.  
It combines **social listening**, **natural language processing (NLP)**, and **data visualization** to detect **public opinion shifts**, **anomalies**, and **emerging narratives** across Twitter.  

Whether for research, brand reputation, or political analysis — **WatchX** provides actionable insights from millions of tweets in one place.

---

## 🧩 Key Features

### 🐦 1. Real-Time Tweet Monitoring
- Tracks tweets on specific **topics, hashtags, or users** using the **Twitter API (v2)**.  
- Supports filters (language, region, verified users).  
- Updates dashboards automatically with new data.

### 🧠 2. Sentiment & Topic Analysis
- Uses **BERT / RoBERTa / Transformers** for real-time sentiment classification.  
- Clusters tweets with **BERTopic / LDA** for topic modeling.  
- Generates **LLM-powered summaries** (Gemini / Mistral / GPT-based).

### 🔍 3. Trend & Anomaly Detection
- Detects **sudden shifts in sentiment or tweet volume** using rolling averages.  
- Flags possible **coordinated activity or bot amplification**.  
- Generates alerts in the dashboard or via API.

### 🧬 4. User & Hashtag Network Analysis
- Builds **retweet, mention, and hashtag networks** using **NetworkX**.  
- Identifies **key influencers** through PageRank and centrality measures.  
- Displays visual graphs and word clouds.

### 📊 5. Interactive Analytics Dashboard
- Built with **Streamlit** for dynamic visuals:  
  - 🕒 Live Tweet Feed  
  - 💬 Sentiment Distribution Charts  
  - 🔥 Trending Hashtags  
  - 🧠 Topic Insights (LLM-generated)  
  - 🧍‍♂️ Influencer Leaderboard  
  - 🚨 Anomaly Alerts

### ⚙️ 6. FastAPI Backend
- Provides structured REST endpoints:
  - `/api/tweets?topic=AI` → Returns recent tweets.  
  - `/api/sentiment?topic=AI` → Returns aggregated sentiment stats.  
  - `/api/trends` → Lists trending hashtags and their metrics.  
- Supports caching and background tasks.

---

## 🧱 Database Schema

| Table | Description |
|--------|-------------|
| `MonitoredTopic` | Topics being tracked (e.g., “AI”, “Elections”) |
| `Tweet` | Stores tweet text, user, timestamp, and sentiment |
| `UserProfile` | Key influencer data and engagement metrics |
| `TrendRecord` | Time-series trends and anomalies |
