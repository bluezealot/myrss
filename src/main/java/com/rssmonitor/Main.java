package com.rssmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    private static final String DEFAULT_CONFIG_PATH = "config.json";
    
    public static void main(String[] args) {
        logger.info("Starting OPML RSS Trending Monitor");
        
        try {
            String configPath = args.length > 0 ? args[0] : DEFAULT_CONFIG_PATH;
            Config config = Config.load(configPath);
            
            Main pipeline = new Main(config);
            pipeline.run();
            
            logger.info("OPML RSS Trending Monitor completed successfully");
        } catch (Exception e) {
            logger.error("Pipeline failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    private final Config config;
    private final OpmlLoader opmlLoader;
    private final FeedFetcher feedFetcher;
    private final ArticleRepository articleRepository;
    private final UpdateDetector updateDetector;
    private final TopicClusterer topicClusterer;
    private final RankingEngine rankingEngine;
    private final AiSummarizer aiSummarizer;
    private final DigestGenerator digestGenerator;
    private final TelegramPublisher telegramPublisher;
    
    public Main(Config config) {
        this.config = config;
        this.opmlLoader = new OpmlLoader();
        this.feedFetcher = new FeedFetcher();
        this.articleRepository = new ArticleRepository(config.getDataFile());
        this.updateDetector = new UpdateDetector();
        this.topicClusterer = new TopicClusterer(config.getKeywords());
        this.rankingEngine = new RankingEngine(config.getKeywords(), config.getMaxArticles());
        this.aiSummarizer = new AiSummarizer();
        this.digestGenerator = new DigestGenerator();
        this.telegramPublisher = new TelegramPublisher();
    }
    
    public void run() {
        logger.info("=== Step 1: Loading OPML feeds ===");
        List<String> feedUrls = opmlLoader.loadFeeds(config.getOpmlPath());
        
        if (feedUrls.isEmpty()) {
            logger.warn("No feed URLs loaded from OPML file. Exiting.");
            return;
        }
        
        logger.info("=== Step 2: Fetching RSS feeds ===");
        List<Article> allFetchedArticles = new ArrayList<>();
        
        for (String feedUrl : feedUrls) {
            List<Article> articles = feedFetcher.fetch(feedUrl);
            allFetchedArticles.addAll(articles);
        }
        
        if (allFetchedArticles.isEmpty()) {
            logger.warn("No articles fetched from feeds. Exiting.");
            return;
        }
        
        logger.info("Total articles fetched: {}", allFetchedArticles.size());
        
        logger.info("=== Step 3: Loading stored articles ===");
        List<Article> storedArticles = articleRepository.loadArticles();
        
        logger.info("=== Step 4: Detecting new articles ===");
        List<Article> newArticles = updateDetector.detectNew(allFetchedArticles, storedArticles);
        
        if (newArticles.isEmpty()) {
            logger.info("No new articles detected. Nothing to report.");
            return;
        }
        
        logger.info("=== Step 5: Clustering topics ===");
        Map<String, List<Article>> clusters = topicClusterer.cluster(newArticles);
        
        logger.info("=== Step 6: Ranking articles ===");
        List<Article> rankedArticles = rankingEngine.rank(newArticles);
        
        logger.info("=== Step 7: Generating AI summary ===");
        String summary = aiSummarizer.summarize(rankedArticles);
        
        // Parse Chinese titles from summary and set to articles
        parseChineseTitles(summary, rankedArticles);
        
        logger.info("=== Step 8: Building digest ===");
        String digest = digestGenerator.generate(rankedArticles, summary);
        
        logger.info("=== Step 9: Sending Telegram message ===");
        telegramPublisher.sendMessage(digest);
        
        logger.info("=== Step 10: Updating stored articles ===");
        List<Article> updatedArticles = new ArrayList<>(storedArticles);
        
        // Add new articles that aren't already stored
        for (Article newArticle : newArticles) {
            if (!updatedArticles.contains(newArticle)) {
                updatedArticles.add(newArticle);
            }
        }
        
        // Limit stored articles to prevent unlimited growth (keep last 5000)
        if (updatedArticles.size() > 5000) {
            updatedArticles = updatedArticles.subList(updatedArticles.size() - 5000, updatedArticles.size());
        }
        
        articleRepository.saveArticles(updatedArticles);
        
        logger.info("Pipeline completed successfully!");
    }
    
    /**
     * Parse Chinese titles from the AI-generated summary and set them to the corresponding articles.
     */
    private void parseChineseTitles(String summary, List<Article> articles) {
        if (articles.isEmpty()) return;
        
        // Pattern to match ARTICLE_X_TITLE and ARTICLE_X_CHINESE_TITLE pairs
        Pattern pattern = Pattern.compile("ARTICLE_(\\d+)_TITLE: (.*?)\\s*ARTICLE_\\1_CHINESE_TITLE: (.*?)(?=\\s*(ARTICLE_|$))", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(summary);
        
        while (matcher.find()) {
            try {
                int articleIndex = Integer.parseInt(matcher.group(1)) - 1; // Convert to 0-based index
                String englishTitle = matcher.group(2).trim();
                String chineseTitle = matcher.group(3).trim();
                
                if (articleIndex >= 0 && articleIndex < articles.size()) {
                    Article article = articles.get(articleIndex);
                    // Only set if the English title matches
                    if (article.getTitle().equals(englishTitle)) {
                        article.setChineseTitle(chineseTitle);
                        logger.info("Set Chinese title for article: {} -> {}", englishTitle, chineseTitle);
                    }
                }
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse article index: {}", e.getMessage());
            }
        }
    }
}
