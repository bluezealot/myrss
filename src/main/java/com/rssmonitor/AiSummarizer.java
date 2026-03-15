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
            return "No articles to summarize.\n\n没有文章可以总结。";
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
        prompt.append("IMPORTANT: Provide the summary in both English and Chinese (Simplified).\n");
        prompt.append("First write the English summary, then the Chinese summary.\n\n");
        prompt.append("Additionally, for each article, provide a Chinese translation of the title.\n");
        prompt.append("Format the article translations as follows:\n");
        prompt.append("ARTICLE_1_TITLE: [English title]\n");
        prompt.append("ARTICLE_1_CHINESE_TITLE: [Chinese translation]\n\n");
        prompt.append("Articles:\n\n");
        
        int count = 0;
        for (Article article : articles) {
            if (count >= 10) break;
            prompt.append("ARTICLE_").append(count + 1).append("_TITLE: ").append(article.getTitle()).append("\n");
            prompt.append("ARTICLE_").append(count + 1).append("_DESCRIPTION: ").append(article.getDescription()).append("\n\n");
            count++;
        }
        
        return prompt.toString();
    }
    
    private String callOpenAiApi(String prompt) throws IOException {
        String requestBody = String.format(
            "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": %s}], \"max_tokens\": 1000, \"temperature\": 0.7}",
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
            
            String content = jsonNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();
            
            return content;
        }
    }
    
    private String generateFallbackSummary(List<Article> articles) {
        if (articles.isEmpty()) {
            return "No new articles to report.\n\n没有新文章要报告。";
        }
        
        StringBuilder summary = new StringBuilder();
        
        // English summary
        summary.append("📰 Latest News Summary\n\n");
        summary.append("Key articles from this update:\n\n");
        
        int count = 0;
        for (Article article : articles) {
            if (count >= 5) break;
            summary.append("• " + article.getTitle() + "\n");
            count++;
        }
        
        if (articles.size() > 5) {
            summary.append("\n...and " + (articles.size() - 5) + " more articles.");
        }
        
        // Chinese summary
        summary.append("\n\n📰 最新新闻摘要\n\n");
        summary.append("本次更新的重点文章：\n\n");
        
        count = 0;
        for (Article article : articles) {
            if (count >= 5) break;
            summary.append("• " + article.getTitle() + "\n");
            count++;
        }
        
        if (articles.size() > 5) {
            summary.append("\n...以及 " + (articles.size() - 5) + " 篇更多文章。");
        }
        
        return summary.toString();
    }
}
