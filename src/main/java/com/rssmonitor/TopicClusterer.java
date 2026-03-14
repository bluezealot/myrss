package com.rssmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TopicClusterer {
    private static final Logger logger = LoggerFactory.getLogger(TopicClusterer.class);
    
    private final List<String> keywords;
    
    public TopicClusterer() {
        this(Arrays.asList("AI", "robot", "android", "LLM", "OpenAI", "artificial intelligence", 
                "machine learning", "neural network", "deep learning", "chatbot", "GPT", "NLP"));
    }
    
    public TopicClusterer(List<String> keywords) {
        this.keywords = keywords.stream()
                .map(String::toLowerCase)
                .toList();
    }
    
    public Map<String, List<Article>> cluster(List<Article> articles) {
        Map<String, List<Article>> clusters = new HashMap<>();
        
        for (Article article : articles) {
            Set<String> matchedTopics = findTopics(article);
            
            for (String topic : matchedTopics) {
                clusters.computeIfAbsent(topic, k -> new ArrayList<>()).add(article);
            }
        }
        
        logger.info("Clustered {} articles into {} topics", articles.size(), clusters.size());
        return clusters;
    }
    
    private Set<String> findTopics(Article article) {
        Set<String> matchedTopics = new HashSet<>();
        
        String titleAndDesc = (article.getTitle() + " " + article.getDescription()).toLowerCase();
        
        for (String keyword : keywords) {
            if (titleAndDesc.contains(keyword)) {
                matchedTopics.add(keyword);
            }
        }
        
        return matchedTopics;
    }
}
