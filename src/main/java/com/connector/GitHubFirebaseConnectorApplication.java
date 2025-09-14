package com.connector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot main application class for GitHub-Firebase connector
 * Demonstrates production-quality code with proper design patterns
 */
@SpringBootApplication
@Slf4j
public class GitHubFirebaseConnectorApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GitHubFirebaseConnectorApplication.class, args);
        log.info("GitHub-Firebase Connector Application started successfully");
        log.info("Available endpoints:");
        log.info("  POST /api/v1/sync - Sync GitHub issues to Firestore");
        log.info("  GET  /api/v1/issues - Get all issues from Firestore");
        log.info("  GET  /api/v1/issues/{id}/exists - Check if issue exists");
        log.info("  GET  /api/v1/health - Application health status");
    }
}
