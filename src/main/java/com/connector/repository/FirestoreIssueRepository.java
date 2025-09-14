package com.connector.repository;

import com.connector.model.Issue;
import com.connector.model.ConnectorConfig;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import com.google.auth.oauth2.ServiceAccountCredentials;

/**
 * Firestore implementation of IssueRepository
 * Handles duplicate detection and batch operations
 */
@Component
@Slf4j
public class FirestoreIssueRepository implements IssueRepository {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private Firestore firestore;
    private String collectionName;
    
    @Autowired
    private ConnectorConfig config;
    
    @PostConstruct
    public void initialize() {
        this.collectionName = config.getFirestoreCollection();
        try {
            this.firestore = initializeFirestore(config);
        } catch (RepositoryException e) {
            throw new RuntimeException("Failed to initialize Firestore repository", e);
        }
    }
    
    /**
     * Initialize Firestore connection using Firebase Admin SDK
     */
    private Firestore initializeFirestore(ConnectorConfig config) throws RepositoryException {
        try {
            // Initialize Firebase if not already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(config.getServiceAccountPath());
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                
                // Extract project ID from credentials
                String projectId = extractProjectIdFromCredentials(credentials);
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build();
                FirebaseApp.initializeApp(options);
            }
            
            return FirestoreClient.getFirestore();
            
        } catch (IOException e) {
            log.error("Failed to initialize Firestore: {}", e.getMessage(), e);
            throw new RepositoryException("Failed to initialize Firestore. Please check your service account file.", e);
        } catch (Exception e) {
            log.error("Invalid service account configuration: {}", e.getMessage(), e);
            throw new RepositoryException("Invalid service account configuration. Please verify your serviceAccount.json file has valid credentials.", e);
        }
    }
    
    @Override
    public Issue save(Issue issue) throws RepositoryException {
        try {
            DocumentReference docRef = firestore.collection(collectionName).document(issue.getId().toString());
            
            Map<String, Object> data = convertIssueToMap(issue);
            ApiFuture<WriteResult> result = docRef.set(data);
            
            WriteResult writeResult = result.get();
            log.info("Successfully saved issue {} at {}", issue.getId(), writeResult.getUpdateTime());
            
            return issue;
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to save issue {}: {}", issue.getId(), e.getMessage(), e);
            throw new RepositoryException("Failed to save issue", e);
        }
    }
    
    @Override
    public Optional<Issue> findById(Long id) throws RepositoryException {
        try {
            DocumentReference docRef = firestore.collection(collectionName).document(id.toString());
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            
            if (document.exists()) {
                Issue issue = convertMapToIssue(document.getData(), id);
                return Optional.of(issue);
            } else {
                return Optional.empty();
            }
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to find issue {}: {}", id, e.getMessage(), e);
            throw new RepositoryException("Failed to find issue", e);
        }
    }
    
    @Override
    public boolean existsById(Long id) throws RepositoryException {
        try {
            DocumentReference docRef = firestore.collection(collectionName).document(id.toString());
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            
            return document.exists();
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to check existence of issue {}: {}", id, e.getMessage(), e);
            throw new RepositoryException("Failed to check existence of issue", e);
        }
    }
    
    @Override
    public List<Issue> saveAll(List<Issue> issues) throws RepositoryException {
        List<Issue> savedIssues = new ArrayList<>();
        int duplicateCount = 0;
        
        for (Issue issue : issues) {
            try {
                // Check if issue already exists
                if (existsById(issue.getId())) {
                    log.info("Skipping duplicate issue: {} - {}", issue.getId(), issue.getTitle());
                    duplicateCount++;
                    continue;
                }
                
                // Save new issue
                save(issue);
                savedIssues.add(issue);
                log.info("Saved new issue: {} - {}", issue.getId(), issue.getTitle());
            } catch (RepositoryException e) {
                log.warn("Failed to save issue {}: {}", issue.getId(), e.getMessage());
                // Continue with other issues even if one fails
            }
        }
        
        log.info("Batch save completed. Saved {} new issues, skipped {} duplicates out of {} total issues", 
                savedIssues.size(), duplicateCount, issues.size());
        return savedIssues;
    }
    
    @Override
    public List<Issue> findAll() throws RepositoryException {
        try {
            ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = 
                firestore.collection(collectionName).get();
            
            List<Issue> issues = new ArrayList<>();
            for (DocumentSnapshot document : future.get().getDocuments()) {
                Issue issue = convertMapToIssue(document.getData(), Long.parseLong(document.getId()));
                issues.add(issue);
            }
            
            return issues;
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to find all issues: {}", e.getMessage(), e);
            throw new RepositoryException("Failed to find all issues", e);
        }
    }
    
    /**
     * Convert Issue object to Firestore Map
     */
    private Map<String, Object> convertIssueToMap(Issue issue) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", issue.getId());
        data.put("title", issue.getTitle());
        data.put("created_at", issue.getCreatedAt().format(DATE_TIME_FORMATTER));
        data.put("state", issue.getState());
        data.put("html_url", issue.getHtmlUrl());
        data.put("repository", issue.getRepository());
        return data;
    }
    
    /**
     * Convert Firestore Map to Issue object
     */
    private Issue convertMapToIssue(Map<String, Object> data, Long id) {
        String title = (String) data.get("title");
        String createdAtStr = (String) data.get("created_at");
        String state = (String) data.get("state");
        String htmlUrl = (String) data.get("html_url");
        String repository = (String) data.get("repository");
        
        java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(createdAtStr, DATE_TIME_FORMATTER);
        
        return new Issue(id, title, createdAt, state, htmlUrl, repository);
    }
    
    /**
     * Extract project ID from Google credentials
     */
    private String extractProjectIdFromCredentials(GoogleCredentials credentials) throws RepositoryException {
        if (credentials instanceof ServiceAccountCredentials) {
            ServiceAccountCredentials serviceAccountCredentials = (ServiceAccountCredentials) credentials;
            return serviceAccountCredentials.getProjectId();
        } else {
            throw new RepositoryException("Invalid credentials type. Expected ServiceAccountCredentials.");
        }
    }
}
