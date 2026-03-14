package com.rssmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateDetector {
    private static final Logger logger = LoggerFactory.getLogger(UpdateDetector.class);
    
    public List<Article> detectNew(List<Article> fetched, List<Article> stored) {
        Set<String> storedLinks = stored.stream()
                .map(Article::getLink)
                .collect(Collectors.toSet());
        
        List<Article> newArticles = new ArrayList<>();
        
        for (Article article : fetched) {
            if (article.getLink() != null && !article.getLink().isEmpty()) {
                if (!storedLinks.contains(article.getLink())) {
                    newArticles.add(article);
                    logger.debug("New article detected: {}", article.getTitle());
                }
            }
        }
        
        logger.info("Detected {} new articles out of {} fetched articles", newArticles.size(), fetched.size());
        return newArticles;
    }
}
