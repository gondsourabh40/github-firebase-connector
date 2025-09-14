package com.connector.config;

import com.connector.model.ConnectorConfig;
import com.connector.repository.RetryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for connector components
 */
@Configuration
@Slf4j
public class ConnectorConfiguration {
    
    @Value("${connector.github.repository}")
    private String githubRepository;
    
    @Value("${connector.max.issues:5}")
    private int maxIssues;
    
    @Value("${connector.firestore.collection:github_issues}")
    private String firestoreCollection;
    
    @Value("${connector.service.account.path}")
    private String serviceAccountPath;
    
    @Value("${connector.max.retries:3}")
    private int maxRetries;
    
    @Value("${connector.retry.delay.ms:1000}")
    private long retryDelayMs;
    
    @Bean
    public ConnectorConfig connectorConfig() {
        ConnectorConfig config = ConnectorConfig.builder()
                .githubRepository(githubRepository)
                .maxIssues(maxIssues)
                .firestoreCollection(firestoreCollection)
                .serviceAccountPath(serviceAccountPath)
                .maxRetries(maxRetries)
                .retryDelayMs(retryDelayMs)
                .build();
        
        log.info("Connector configuration loaded: repository={}, maxIssues={}, collection={}", 
                config.getGithubRepository(), config.getMaxIssues(), config.getFirestoreCollection());
        
        return config;
    }
    
    @Bean
    public RetryHandler retryHandler(ConnectorConfig config) {
        return new RetryHandler(config.getMaxRetries(), config.getRetryDelayMs());
    }
}
