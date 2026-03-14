package com.rssmonitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArticleRepository {
    private static final Logger logger = LoggerFactory.getLogger(ArticleRepository.class);
    private static final String DEFAULT_DATA_FILE = "data/articles.json";
    
    private final ObjectMapper mapper;
    private final String dataFilePath;
    
    public ArticleRepository() {
        this(DEFAULT_DATA_FILE);
    }
    
    public ArticleRepository(String dataFilePath) {
        this.dataFilePath = dataFilePath;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        ensureDataDirectoryExists();
    }
    
    public List<Article> loadArticles() {
        File file = new File(dataFilePath);
        
        if (!file.exists()) {
            logger.info("No existing article database found. Starting with empty database.");
            return new ArrayList<>();
        }
        
        try {
            ArticleDatabase db = mapper.readValue(file, ArticleDatabase.class);
            logger.info("Loaded {} articles from database", db.getArticles().size());
            return db.getArticles();
        } catch (IOException e) {
            logger.error("Error loading articles from database: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void saveArticles(List<Article> articles) {
        try {
            ArticleDatabase db = new ArticleDatabase();
            db.setArticles(articles);
            
            mapper.writeValue(new File(dataFilePath), db);
            logger.info("Saved {} articles to database", articles.size());
        } catch (IOException e) {
            logger.error("Error saving articles to database: {}", e.getMessage());
        }
    }
    
    private void ensureDataDirectoryExists() {
        File file = new File(dataFilePath);
        File parentDir = file.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            logger.info("Created data directory: {}", parentDir.getAbsolutePath());
        }
    }
    
    private static class ArticleDatabase {
        private List<Article> articles = new ArrayList<>();
        
        public List<Article> getArticles() {
            return articles;
        }
        
        public void setArticles(List<Article> articles) {
            this.articles = articles;
        }
    }
}
