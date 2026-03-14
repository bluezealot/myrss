# MASTER_PROMPT.md

## Project Specification for Trae Coding Agent

## Project Name

OPML RSS Trending Monitor

## Goal

Build a Java application that:

1. Loads RSS feeds from an OPML file
2. Fetches RSS articles
3. Detects newly published articles
4. Detects trending topics across feeds
5. Generates an AI-powered summary of the most important articles
6. Sends a digest to a Telegram channel
7. Runs automatically on a schedule using GitHub Actions
8. Add unit tests for each module

Technologies used:

* OPML feed list (Outline Processor Markup Language)
* RSS / Atom feeds
* Telegram Bot API
* GitHub Actions scheduler
* Optional AI summarization via OpenAI API

The system should run without requiring a dedicated server.
All scheduled execution should occur via GitHub Actions.

---

# Architecture Overview

Pipeline:

OPML file
↓
Extract feed URLs
↓
Fetch RSS feeds
↓
Parse articles
↓
Detect new articles
↓
Cluster topics
↓
Rank trending content
↓
Generate AI summary
↓
Build digest message
↓
Send Telegram message

---

# Repository Structure

Create the following project layout:

```
opml-rss-trending-monitor

/src/main/java/com/rssmonitor
/src/main/resources
/src/test/java

/data
/.github/workflows

feeds.opml
config.json
README.md
```

---

# Required Dependencies

Use Maven and add dependencies for:

RSS parsing

* Rome

HTTP client

* OkHttp

JSON parsing

* Jackson

Logging

* slf4j-simple

Testing

* JUnit

---

# Core Data Model

Create the following model:

Article

Fields:

* title
* link
* publishTime
* source
* description
* score

---

# Module 1: OPML Loader

Class name:

OpmlLoader

Function:

```
List<String> loadFeeds(String opmlPath)
```

Responsibilities:

* parse OPML XML
* extract `xmlUrl` attributes
* return list of RSS URLs

Acceptance:

Input: feeds.opml
Output: list of feed URLs

---

# Module 2: RSS Feed Fetcher

Class:

FeedFetcher

Function:

```
List<Article> fetch(String feedUrl)
```

Responsibilities:

* download RSS feed
* parse entries using Rome
* convert entries to Article objects

Handle network failures gracefully.

---

# Module 3: Article Repository

Create persistent storage:

data/articles.json

Structure:

```
{
  "articles": []
}
```

Class:

ArticleRepository

Functions:

```
List<Article> loadArticles()
void saveArticles(List<Article>)
```

---

# Module 4: Update Detector

Class:

UpdateDetector

Function:

```
List<Article> detectNew(List<Article> fetched, List<Article> stored)
```

Logic:

If article link does not exist in stored database
→ mark as new article.

---

# Module 5: Topic Clustering

Purpose:

Detect topics mentioned across multiple feeds.

Example topics:

* AI
* robotics
* Android
* LLM
* OpenAI

Class:

TopicClusterer

Function:

```
Map<String, List<Article>> cluster(List<Article>)
```

Initial implementation:

Keyword-based clustering.

Optional improvement:

Use TF-IDF similarity between titles.

---

# Module 6: Ranking Engine

Class:

RankingEngine

Function:

```
List<Article> rank(List<Article>)
```

Score formula:

score =

recencyWeight

* keywordWeight
* clusterWeight

Example scoring:

recent article → +10
keyword match → +5
multiple feeds mention topic → +5

Return top 10 articles.

---

# Module 7: AI Summarizer

Class:

AiSummarizer

Function:

```
String summarize(List<Article>)
```

Use OpenAI API if enabled.

Prompt example:

"Summarize the following news articles into a short technology digest.
Include key themes and 3–5 bullet highlights."

Input:

Article titles and descriptions.

Output:

Concise summary paragraph.

If AI API is unavailable:

Fallback to simple bullet list summary.

---

# Module 8: Digest Generator

Class:

DigestGenerator

Function:

```
String generate(List<Article> ranked, String summary)
```

Output format:

🔥 Tech Digest

## Summary

<AI generated summary>

## Trending Articles

1️⃣ Title
Link

2️⃣ Title
Link

3️⃣ Title
Link

---

# Module 9: Telegram Publisher

Create a Telegram bot using BotFather.

Environment variables:

```
TELEGRAM_TOKEN
TELEGRAM_CHAT_ID
```

Class:

TelegramPublisher

Function:

```
void sendMessage(String message)
```

Endpoint:

```
https://api.telegram.org/bot<TOKEN>/sendMessage
```

---

# Module 10: Main Pipeline

Class:

Main

Execution flow:

1 Load OPML feeds
2 Fetch RSS feeds
3 Extract articles
4 Detect new articles
5 Cluster topics
6 Rank trending articles
7 Generate AI summary
8 Build digest
9 Send Telegram message
10 Update stored article database

---

# GitHub Actions Scheduler

Create workflow:

.github/workflows/rss-monitor.yml

Schedule example:

```
0 */2 * * *
```

This runs every 2 hours.

Workflow steps:

1 Checkout repository
2 Setup Java
3 Build project
4 Run application

Example:

```
- uses: actions/checkout@v3

- uses: actions/setup-java@v3
  with:
    distribution: temurin
    java-version: 17

- run: mvn package

- run: java -jar target/rss-monitor.jar
```

---

# Configuration File

Create config.json

Fields:

```
{
  "opmlPath": "feeds.opml",
  "maxArticles": 10,
  "keywords": ["AI","robot","android","LLM","OpenAI"],
  "aiEnabled": true
}
```

---

# Logging

Log the following events:

* feed download failures
* telegram API errors
* AI summarization errors
* OPML parsing errors

Retry network operations up to 3 times.

---

# Testing

Create unit tests for:

* OPML parsing
* RSS feed fetching
* update detection
* ranking engine

---

# README

Include instructions for:

* configuring Telegram bot
* adding RSS feeds
* enabling GitHub Actions
* configuring environment variables

---

# Example Output

Telegram message:

🔥 Tech Digest

## Summary

AI continues dominating tech news with new LLM benchmarks and robotics navigation improvements.

## Trending Articles

1️⃣ OpenAI releases new benchmark
https://example.com

2️⃣ Robotics navigation breakthrough
https://example.com

3️⃣ Android AI toolkit update
https://example.com

---

# Future Enhancements

Optional improvements:

* duplicate news detection
* semantic topic clustering
* daily summary mode
* user topic filtering
* RSS feed health monitoring

---

# Implementation Rules

The coding agent should:

1. Implement modules incrementally
2. Ensure code compiles successfully after each module
3. Add logging for all network operations
4. Write unit tests where possible
5. Ensure the application runs correctly in GitHub Actions

Stop when the full pipeline works and successfully sends a Telegram message.
