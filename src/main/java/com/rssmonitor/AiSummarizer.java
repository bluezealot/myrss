package com.rssmonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AiSummarizer {
    private static final Logger logger = LoggerFactory.getLogger(AiSummarizer.class);
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    
    private final String apiKey;
    private final String model;
    private final boolean enabled;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public AiSummarizer() {
        this(System.getenv("OPENAI_API_KEY"), DEFAULT_MODEL);
    }
    
    public AiSummarizer(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = apiKey != null && !apiKey.isEmpty();
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        
        this.objectMapper = new ObjectMapper();
    }
    
    public String summarize(List<Article> articles) {
        if (!enabled) {
            logger.info("AI summarization is disabled. Using fallback summary.");
            return generateFallbackSummary(articles);
        }
        
        if (articles.isEmpty()) {
            return "No articles to summarize.";
        }
        
        try {
            String prompt = buildPrompt(articles);
            String summary = callOpenAiApi(prompt);
            logger.info("Successfully generated AI summary");
            return summary;
        } catch (Exception e) {
            logger.error("Error generating AI summary: {}", e.getMessage(), e);
            return generateFallbackSummary(articles);
        }
    }
    
    private String buildPrompt(List<Article> articles) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Summarize the following news articles into a short technology digest. ");
        prompt.append("Include key themes and 3-5 bullet highlights.\n\n");
        prompt.append("Articles:\n\n");
        
        int count = 0;
        for (Article article : articles) {
            if (count >= 10) break;
            prompt.append("Title: ").append(article.getTitle()).append("\n");
            prompt.append("Description: ").append(article.getDescription()).append("\n\n");
            count++;
        }
        
        return prompt.toString();
    }
    
    private String callOpenAiApi(String prompt) throws IOException {
        String requestBody = String.format(
            "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": %s}], \"max_tokens\": 500, \"temperature\": 0.7}",
            model,
            objectMapper.writeValueAsString(prompt)
        );
        
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            return jsonNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();
        }
    }
    
    private String generateFallbackSummary(List<Article> articles) {
        if (articles.isEmpty()) {
            return "No new articles to report.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("📰 Latest News Summary\n\n");
        summary.append("Key articles from this update:\n\n");
        
        int count = 0;
        for (Article article : articles) {
            if (count >= 5) break;
            summary.append("• ").append(article.getTitle()).append("\n");
            count++;
        }
        
        if (articles.size() > 5) {
            summary.append("\n...and ").append(articles.size() - 5).append(" more articles.");
        }
        
        return summary.toString();
    }
}
