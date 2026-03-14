package com.rssmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class RankingEngine {
    private static final Logger logger = LoggerFactory.getLogger(RankingEngine.class);
    
    private final List<String> keywords;
    private final int maxArticles;
    
    private static final double RECENCY_WEIGHT = 1.0;
    private static final double KEYWORD_WEIGHT = 1.0;
    private static final double CLUSTER_WEIGHT = 1.0;
    
    public RankingEngine() {
        this(Arrays.asList("AI", "robot", "android", "LLM", "OpenAI", "artificial intelligence", 
                "machine learning", "neural network"), 10);
    }
    
    public RankingEngine(List<String> keywords, int maxArticles) {
        this.keywords = keywords.stream()
                .map(String::toLowerCase)
                .toList();
        this.maxArticles = maxArticles;
    }
    
    public List<Article> rank(List<Article> articles) {
        Map<String, Integer> topicCounts = countTopics(articles);
        Instant now = Instant.now();
        
        for (Article article : articles) {
            double score = calculateScore(article, topicCounts, now);
            article.setScore(score);
        }
        
        List<Article> rankedArticles = new ArrayList<>(articles);
        rankedArticles.sort((a1, a2) -> Double.compare(a2.getScore(), a1.getScore()));
        
        int limit = Math.min(maxArticles, rankedArticles.size());
        List<Article> topArticles = rankedArticles.subList(0, limit);
        
        logger.info("Ranked {} articles, returning top {}", articles.size(), topArticles.size());
        return topArticles;
    }
    
    private double calculateScore(Article article, Map<String, Integer> topicCounts, Instant now) {
        double score = 0.0;
        
        // Recency score
        long hoursAgo = Duration.between(article.getPublishTime(), now).toHours();
        double recencyScore = Math.max(0, 24 - hoursAgo) / 24.0 * 10;
        score += recencyScore * RECENCY_WEIGHT;
        
        // Keyword match score
        String titleAndDesc = (article.getTitle() + " " + article.getDescription()).toLowerCase();
        int keywordMatches = 0;
        for (String keyword : keywords) {
            if (titleAndDesc.contains(keyword)) {
                keywordMatches++;
            }
        }
        double keywordScore = keywordMatches * 5;
        score += keywordScore * KEYWORD_WEIGHT;
        
        // Cluster/topic popularity score
        for (Map.Entry<String, Integer> entry : topicCounts.entrySet()) {
            if (titleAndDesc.contains(entry.getKey())) {
                score += entry.getValue() * CLUSTER_WEIGHT;
            }
        }
        
        return score;
    }
    
    private Map<String, Integer> countTopics(List<Article> articles) {
        Map<String, Integer> topicCounts = new HashMap<>();
        
        for (Article article : articles) {
            String titleAndDesc = (article.getTitle() + " " + article.getDescription()).toLowerCase();
            
            for (String keyword : keywords) {
                if (titleAndDesc.contains(keyword)) {
                    topicCounts.merge(keyword, 1, Integer::sum);
                }
            }
        }
        
        return topicCounts;
    }
}
