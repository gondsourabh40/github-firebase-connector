package com.connector.repository;

import com.connector.model.Issue;
import com.connector.model.ConnectorConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Content;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub API client for fetching issues
 * Implements retry mechanism and proper error handling
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubApiClient {
    
    private static final String GITHUB_API_BASE = "https://api.github.com/repos/";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private final ConnectorConfig config;
    private final RetryHandler retryHandler;
    
    /**
     * Fetch recent issues from GitHub repository
     * @return list of recent issues
     * @throws RepositoryException if fetch operation fails
     */
    public List<Issue> fetchRecentIssues() throws RepositoryException {
        String url = GITHUB_API_BASE + config.getGithubRepository() + 
                    "/issues?per_page=" + config.getMaxIssues() + "&sort=created&direction=desc";
        
        try {
            String response = retryHandler.executeWithRetry(() -> {
                log.info("Fetching issues from GitHub API: {}", url);
                try {
                    Content content = Request.Get(url)
                            .addHeader("Accept", "application/vnd.github.v3+json")
                            .addHeader("User-Agent", "GitHub-Firebase-Connector/1.0")
                            .execute().returnContent();
                    return content.asString();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to execute HTTP request", e);
                }
            });
            
            return parseIssuesFromResponse(response, config.getGithubRepository());
            
        } catch (Exception e) {
            log.error("Failed to fetch issues from GitHub: {}", e.getMessage(), e);
            throw new RepositoryException("Failed to fetch issues from GitHub", e);
        }
    }
    
    /**
     * Parse issues from GitHub API response
     */
    private List<Issue> parseIssuesFromResponse(String response, String repository) throws RepositoryException {
        try {
            JSONArray issuesArray = new JSONArray(response);
            List<Issue> issues = new ArrayList<>();
            
            for (int i = 0; i < issuesArray.length(); i++) {
                JSONObject issueJson = issuesArray.getJSONObject(i);
                Issue issue = parseIssueFromJson(issueJson, repository);
                issues.add(issue);
            }
            
            log.info("Successfully parsed {} issues from GitHub response", issues.size());
            return issues;
            
        } catch (Exception e) {
            log.error("Failed to parse GitHub response: {}", e.getMessage(), e);
            throw new RepositoryException("Failed to parse GitHub response", e);
        }
    }
    
    /**
     * Parse a single issue from JSON
     */
    private Issue parseIssueFromJson(JSONObject issueJson, String repository) throws RepositoryException {
        try {
            Long id = issueJson.getLong("id");
            String title = issueJson.getString("title");
            String createdAtStr = issueJson.getString("created_at");
            String state = issueJson.getString("state");
            String htmlUrl = issueJson.getString("html_url");
            
            // Parse the ISO 8601 date string
            LocalDateTime createdAt = LocalDateTime.parse(createdAtStr.replace("Z", ""), DATE_TIME_FORMATTER);
            
            return new Issue(id, title, createdAt, state, htmlUrl, repository);
            
        } catch (Exception e) {
            log.error("Failed to parse issue from JSON: {}", e.getMessage(), e);
            throw new RepositoryException("Failed to parse issue from JSON", e);
        }
    }
}
