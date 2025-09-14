package com.connector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for connector settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorConfig {
    private String githubRepository;
    @Builder.Default
    private int maxIssues = 5;
    @Builder.Default
    private String firestoreCollection = "github_issues";
    @Builder.Default
    private String serviceAccountPath = "";
    @Builder.Default
    private int maxRetries = 3;
    @Builder.Default
    private long retryDelayMs = 1000;
}
