package com.rssmonitor;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
            import java.util.List;
import java.util.concurrent.TimeUnit;

public class FeedFetcher {
    private static final Logger logger = LoggerFactory.getLogger(FeedFetcher.class);
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_SECONDS = 30;
    
    private final OkHttpClient httpClient;
    
    public FeedFetcher() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }
    
    public List<Article> fetch(String feedUrl) {
        List<Article> articles = new ArrayList<>();
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                logger.debug("Fetching feed from {} (attempt {}/{}})", feedUrl, attempt, MAX_RETRIES);
                
                Request request = new Request.Builder()
                        .url(feedUrl)
                        .header("User-Agent", "OPML-RSS-Monitor/1.0")
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new Exception("HTTP error: " + response.code());
                    }
                    
                    String body = response.body().string();
                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(new StringReader(body));
                    
                    String source = feed.getTitle() != null ? feed.getTitle() : feedUrl;
                    
                    for (SyndEntry entry : feed.getEntries()) {
                        Article article = convertToArticle(entry, source);
                        articles.add(article);
                    }
                    
                    logger.info("Fetched {} articles from {}", articles.size(), feedUrl);
                    return articles;
                }
                
            } catch (Exception e) {
                logger.warn("Attempt {}/{} failed for {}: {}", attempt, MAX_RETRIES, feedUrl, e.getMessage());
                
                if (attempt == MAX_RETRIES) {
                    logger.error("Failed to fetch feed after {} attempts: {}", MAX_RETRIES, feedUrl);
                } else {
                    try {
                        Thread.sleep(1000 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        return articles;
    }
    
    private Article convertToArticle(SyndEntry entry, String source) {
        String title = entry.getTitle() != null ? entry.getTitle() : "Untitled";
        String link = entry.getLink() != null ? entry.getLink() : "";
        String description = "";
        
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            description = entry.getDescription().getValue();
        }
        
        Instant publishTime = Instant.now();
        if (entry.getPublishedDate() != null) {
            publishTime = entry.getPublishedDate().toInstant();
        } else if (entry.getUpdatedDate() != null) {
            publishTime = entry.getUpdatedDate().toInstant();
        }
        
        return new Article(title, link, publishTime, source, description);
    }
}
