# OPML RSS Trending Monitor

A Java application that monitors RSS feeds, detects trending topics, and sends a daily digest to a Telegram channel.

## Features

- 📡 **RSS Feed Monitoring**: Load and monitor multiple RSS feeds from an OPML file
- 🔍 **Trending Detection**: Automatically detect trending topics across feeds
- 🤖 **AI-Powered Summaries**: Generate AI summaries of the most important articles (optional)
- 📱 **Telegram Integration**: Send formatted digests to a Telegram channel
- ⏰ **Scheduled Execution**: Run automatically via GitHub Actions

## Project Structure

```
opml-rss-trending-monitor/
├── .github/workflows/     # GitHub Actions workflows
├── data/                  # Persistent data storage
├── src/main/java/com/rssmonitor/
│   ├── Article.java           # Core data model
│   ├── Config.java            # Configuration management
│   ├── OpmlLoader.java        # OPML file parsing
│   ├── FeedFetcher.java       # RSS feed fetching
│   ├── ArticleRepository.java # Article persistence
│   ├── UpdateDetector.java    # New article detection
│   ├── TopicClusterer.java    # Topic clustering
│   ├── RankingEngine.java     # Article ranking
│   ├── AiSummarizer.java      # AI summary generation
│   ├── DigestGenerator.java   # Digest generation
│   ├── TelegramPublisher.java # Telegram integration
│   └── Main.java              # Main pipeline
├── config.json            # Configuration file
├── feeds.opml            # RSS feeds list
└── pom.xml               # Maven configuration
```

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- (Optional) OpenAI API key for AI summaries
- (Optional) Telegram bot token and chat ID

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/opml-rss-trending-monitor.git
cd opml-rss-trending-monitor
```

2. Build the project:
```bash
mvn clean package
```

3. Configure your feeds in `feeds.opml` or use the default feeds.

4. (Optional) Set environment variables:
```bash
export TELEGRAM_TOKEN="your_bot_token"
export TELEGRAM_CHAT_ID="your_chat_id"
export OPENAI_API_KEY="your_openai_api_key"  # Optional
```

5. Run the application:
```bash
java -jar target/opml-rss-trending-monitor-1.0-SNAPSHOT.jar
```

### GitHub Actions Setup

1. Fork this repository

2. Add the following secrets to your GitHub repository:
   - `TELEGRAM_TOKEN`: Your Telegram bot token
   - `TELEGRAM_CHAT_ID`: Your Telegram chat ID
   - `OPENAI_API_KEY`: (Optional) Your OpenAI API key

3. The workflow will run automatically every 2 hours, or you can trigger it manually from the Actions tab.

## Configuration

The `config.json` file allows you to customize the application:

```json
{
  "opmlPath": "feeds.opml",
  "maxArticles": 10,
  "keywords": ["AI", "robot", "android", "LLM", "OpenAI"],
  "aiEnabled": true,
  "dataFile": "data/articles.json"
}
```

- `opmlPath`: Path to the OPML file containing RSS feeds
- `maxArticles`: Maximum number of articles to include in the digest
- `keywords`: Keywords used for topic clustering and ranking
- `aiEnabled`: Enable AI-powered summaries (requires OpenAI API key)
- `dataFile`: Path to the persistent article database

## Setting up Telegram Bot

1. Message [@BotFather](https://t.me/botfather) on Telegram
2. Create a new bot with `/newbot`
3. Copy the bot token provided
4. Get your chat ID by messaging [@userinfobot](https://t.me/userinfobot) or by visiting `https://api.telegram.org/bot<TOKEN>/getUpdates` after sending a message to your bot

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- [Rome](https://github.com/rometools/rome) - For RSS/Atom feed parsing
- [OkHttp](https://square.github.io/okhttp/) - For HTTP requests
- [Jackson](https://github.com/FasterXML/jackson) - For JSON processing
