package com.rssmonitor;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TelegramPublisher {
    private static final Logger logger = LoggerFactory.getLogger(TelegramPublisher.class);
    
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final int MAX_MESSAGE_LENGTH = 4096;
    private static final int MAX_RETRIES = 3;
    
    private final String botToken;
    private final String chatId;
    private final boolean enabled;
    private final OkHttpClient httpClient;
    
    public TelegramPublisher() {
        this(System.getenv("TELEGRAM_TOKEN"), System.getenv("TELEGRAM_CHAT_ID"));
    }
    
    public TelegramPublisher(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.enabled = botToken != null && !botToken.isEmpty() && 
                      chatId != null && !chatId.isEmpty();
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public void sendMessage(String message) {
        if (!enabled) {
            logger.warn("Telegram publisher is not enabled. Set TELEGRAM_TOKEN and TELEGRAM_CHAT_ID environment variables.");
            logger.info("Message that would have been sent:\n{}", message);
            return;
        }
        
        // Split message if it exceeds Telegram's max length
        List<String> messageParts = splitMessage(message, MAX_MESSAGE_LENGTH);
        
        for (int i = 0; i < messageParts.size(); i++) {
            String part = messageParts.get(i);
            boolean isLastPart = (i == messageParts.size() - 1);
            
            sendMessagePart(part, i + 1, messageParts.size());
            
            // Add small delay between messages to avoid rate limiting
            if (!isLastPart) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    private void sendMessagePart(String message, int partNumber, int totalParts) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String url = TELEGRAM_API_BASE + botToken + "/sendMessage";
                
                String displayMessage = message;
                if (totalParts > 1) {
                    displayMessage = "(Part " + partNumber + "/" + totalParts + ")\n\n" + message;
                }
                
                String requestBody = String.format(
                    "{\"chat_id\": \"%s\", \"text\": %s, \"parse_mode\": \"HTML\", \"disable_web_page_preview\": false}",
                    chatId,
                    escapeJson(displayMessage)
                );
                
                Request request = new Request.Builder()
                        .url(url)
                        .header("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        throw new IOException("HTTP " + response.code() + ": " + errorBody);
                    }
                    
                    logger.info("Successfully sent message part {}/{}", partNumber, totalParts);
                    return;
                }
                
            } catch (Exception e) {
                logger.warn("Attempt {}/{} failed to send Telegram message part {}: {}", 
                        attempt, MAX_RETRIES, partNumber, e.getMessage());
                
                if (attempt == MAX_RETRIES) {
                    logger.error("Failed to send Telegram message part {} after {} attempts", 
                            partNumber, MAX_RETRIES);
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
    }
    
    private List<String> splitMessage(String message, int maxLength) {
        List<String> parts = new ArrayList<>();
        
        if (message.length() <= maxLength) {
            parts.add(message);
            return parts;
        }
        
        int start = 0;
        while (start < message.length()) {
            int end = Math.min(start + maxLength, message.length());
            
            // Try to split at a newline if possible
            if (end < message.length()) {
                int lastNewline = message.lastIndexOf('\n', end);
                if (lastNewline > start && lastNewline > end - 100) {
                    end = lastNewline;
                }
            }
            
            parts.add(message.substring(start, end));
            start = end;
            
            // Skip leading newline for next part
            if (start < message.length() && message.charAt(start) == '\n') {
                start++;
            }
        }
        
        return parts;
    }
    
    private String escapeJson(String str) {
        if (str == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            sb.append('0');
                        }
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        
        sb.append('"');
        return sb.toString();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
