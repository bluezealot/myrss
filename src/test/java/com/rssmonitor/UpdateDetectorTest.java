package com.rssmonitor;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpdateDetectorTest {

    @Test
    void testDetectNewArticles() {
        UpdateDetector detector = new UpdateDetector();
        
        // Create stored articles
        List<Article> stored = new ArrayList<>();
        stored.add(new Article("Old Article 1", "http://example.com/old1", Instant.now(), "Test", "Description"));
        stored.add(new Article("Old Article 2", "http://example.com/old2", Instant.now(), "Test", "Description"));
        
        // Create fetched articles (mix of old and new)
        List<Article> fetched = new ArrayList<>();
        fetched.add(new Article("Old Article 1", "http://example.com/old1", Instant.now(), "Test", "Description"));
        fetched.add(new Article("New Article 1", "http://example.com/new1", Instant.now(), "Test", "Description"));
        fetched.add(new Article("New Article 2", "http://example.com/new2", Instant.now(), "Test", "Description"));
        
        List<Article> newArticles = detector.detectNew(fetched, stored);
        
        assertEquals(2, newArticles.size());
        assertTrue(newArticles.stream().anyMatch(a -> a.getLink().equals("http://example.com/new1")));
        assertTrue(newArticles.stream().anyMatch(a -> a.getLink().equals("http://example.com/new2")));
    }

    @Test
    void testDetectNewArticlesWithEmptyStored() {
        UpdateDetector detector = new UpdateDetector();
        
        List<Article> stored = new ArrayList<>();
        
        List<Article> fetched = new ArrayList<>();
        fetched.add(new Article("Article 1", "http://example.com/1", Instant.now(), "Test", "Description"));
        fetched.add(new Article("Article 2", "http://example.com/2", Instant.now(), "Test", "Description"));
        
        List<Article> newArticles = detector.detectNew(fetched, stored);
        
        assertEquals(2, newArticles.size());
    }

    @Test
    void testDetectNewArticlesWithNoNewArticles() {
        UpdateDetector detector = new UpdateDetector();
        
        List<Article> stored = new ArrayList<>();
        stored.add(new Article("Article 1", "http://example.com/1", Instant.now(), "Test", "Description"));
        
        List<Article> fetched = new ArrayList<>();
        fetched.add(new Article("Article 1", "http://example.com/1", Instant.now(), "Test", "Description"));
        
        List<Article> newArticles = detector.detectNew(fetched, stored);
        
        assertTrue(newArticles.isEmpty());
    }
}
