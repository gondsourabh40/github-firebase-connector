package com.connector.service;

import com.connector.model.ConnectorConfig;
import com.connector.model.Issue;
import com.connector.repository.GitHubApiClient;
import com.connector.repository.IssueRepository;
import com.connector.repository.RepositoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class orchestrating the GitHub to Firestore sync process
 * Implements the Service Layer pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectorService {
    
    private final GitHubApiClient githubClient;
    private final IssueRepository issueRepository;
    private final ConnectorConfig config;
    
    /**
     * Execute the complete sync process
     * @return SyncResult containing statistics about the sync operation
     * @throws ConnectorException if sync operation fails
     */
    public SyncResult syncIssues() throws ConnectorException {
        log.info("Starting GitHub to Firestore sync for repository: {}", config.getGithubRepository());
        
        try {
            // Step 1: Fetch issues from GitHub
            List<Issue> issues = fetchIssuesFromGitHub();
            log.info("Fetched {} issues from GitHub", issues.size());
            
            // Step 2: Save issues to Firestore (with duplicate handling)
            List<Issue> savedIssues = saveIssuesToFirestore(issues);
            log.info("Saved {} new issues to Firestore", savedIssues.size());
            
            // Step 3: Create sync result
            SyncResult result = new SyncResult(
                issues.size(),
                savedIssues.size(),
                issues.size() - savedIssues.size(),
                System.currentTimeMillis()
            );
            
            log.info("Sync completed successfully: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("Sync operation failed: {}", e.getMessage(), e);
            throw new ConnectorException("Sync operation failed", e);
        }
    }
    
    /**
     * Fetch issues from GitHub API
     */
    private List<Issue> fetchIssuesFromGitHub() throws ConnectorException {
        try {
            return githubClient.fetchRecentIssues();
        } catch (RepositoryException e) {
            throw new ConnectorException("Failed to fetch issues from GitHub", e);
        }
    }
    
    /**
     * Save issues to Firestore with duplicate handling
     */
    private List<Issue> saveIssuesToFirestore(List<Issue> issues) throws ConnectorException {
        try {
            return issueRepository.saveAll(issues);
        } catch (RepositoryException e) {
            throw new ConnectorException("Failed to save issues to Firestore", e);
        }
    }
    
    /**
     * Get all issues from Firestore
     */
    public List<Issue> getAllIssues() throws ConnectorException {
        try {
            return issueRepository.findAll();
        } catch (RepositoryException e) {
            throw new ConnectorException("Failed to retrieve issues from Firestore", e);
        }
    }
    
    /**
     * Check if an issue exists in Firestore
     */
    public boolean issueExists(Long issueId) throws ConnectorException {
        try {
            return issueRepository.existsById(issueId);
        } catch (RepositoryException e) {
            throw new ConnectorException("Failed to check issue existence", e);
        }
    }
}

