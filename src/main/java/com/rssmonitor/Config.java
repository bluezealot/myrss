package com.rssmonitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    
    private String opmlPath = "feeds.opml";
    private int maxArticles = 10;
    private List<String> keywords = Arrays.asList("AI", "robot", "android", "LLM", "OpenAI", 
            "artificial intelligence", "machine learning", "neural network", "deep learning");
    private boolean aiEnabled = true;
    private String dataFile = "data/articles.json";
    
    public static Config load(String configPath) {
        ObjectMapper mapper = new ObjectMapper();
        File configFile = new File(configPath);
        
        if (configFile.exists()) {
            try {
                Config config = mapper.readValue(configFile, Config.class);
                logger.info("Loaded configuration from {}", configPath);
                return config;
            } catch (IOException e) {
                logger.error("Error loading config file, using defaults: {}", e.getMessage());
            }
        } else {
            logger.info("Config file not found, using defaults");
        }
        
        return new Config();
    }
    
    public String getOpmlPath() {
        return opmlPath;
    }
    
    public void setOpmlPath(String opmlPath) {
        this.opmlPath = opmlPath;
    }
    
    public int getMaxArticles() {
        return maxArticles;
    }
    
    public void setMaxArticles(int maxArticles) {
        this.maxArticles = maxArticles;
    }
    
    public List<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    public boolean isAiEnabled() {
        return aiEnabled;
    }
    
    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }
    
    public String getDataFile() {
        return dataFile;
    }
    
    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }
}
