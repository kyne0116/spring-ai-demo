package com.example.springai;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

/**
 * Spring AI Demo Application
 * 
 * This application demonstrates the basic usage of Spring AI with OpenAI.
 * It provides a simple chat interface to interact with OpenAI models.
 * 
 * Configuration is read from environment variables:
 * - AI_API_KEY: Your OpenAI API key (required)
 * - AI_BASE_URL: OpenAI API base URL (default: api.openai.com)
 * - AI_MODEL: OpenAI model name (default: gpt-4o-mini)
 * 
 * @author Spring AI Demo
 */
@SpringBootApplication
public class SpringAiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemoApplication.class, args);
    }

    /**
     * CommandLineRunner to demonstrate basic AI functionality on application startup.
     * This will send a request to OpenAI and print the response.
     */
    @Bean
    public CommandLineRunner runner() {
        return args -> {
            System.out.println("=== Spring AI Demo Application Started ===");
        };
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        String rawKey = System.getenv("AI_API_KEY");
        String apiKey = rawKey != null
                ? (rawKey.length() >= 8
                    ? rawKey.substring(0, 4) + "..." + rawKey.substring(rawKey.length() - 4)
                    : rawKey)
                : "***NOT SET***";
        String baseUrl = System.getenv("AI_BASE_URL") != null ? System.getenv("AI_BASE_URL") : "api.openai.com (default)";
        String model = System.getenv("AI_MODEL") != null ? System.getenv("AI_MODEL") : "gpt-4o-mini (default)";

        System.out.println("Current configuration from environment variables:");
        System.out.println("- AI_API_KEY: " + apiKey);
        System.out.println("- AI_BASE_URL: " + baseUrl);
        System.out.println("- AI_MODEL: " + model);

        int port = 8080;
        try {
            ServletWebServerApplicationContext ctx = (ServletWebServerApplicationContext) event.getApplicationContext();
            port = ctx.getWebServer().getPort();
        } catch (Exception ignored) {}

        System.out.println("Home URLs:");
        System.out.println("- http://localhost:" + port + "/");
        System.out.println("- 非流式（一次性响应）UI: http://localhost:" + port + "/chat.html");
        System.out.println("- 流式（SSE）UI: http://localhost:" + port + "/chat-stream.html");
    }
}
