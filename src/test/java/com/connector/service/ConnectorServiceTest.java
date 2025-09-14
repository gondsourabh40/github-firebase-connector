package com.connector.service;

import com.connector.model.ConnectorConfig;
import com.connector.model.Issue;
import com.connector.repository.GitHubApiClient;
import com.connector.repository.IssueRepository;
import com.connector.repository.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConnectorService
 * Demonstrates testing approach for the connector
 */
@ExtendWith(MockitoExtension.class)
class ConnectorServiceTest {
    
    @Mock
    private GitHubApiClient githubClient;
    
    @Mock
    private IssueRepository issueRepository;
    
    private ConnectorService connectorService;
    private ConnectorConfig config;
    
    @BeforeEach
    void setUp() {
        config = ConnectorConfig.builder()
                .githubRepository("test/repo")
                .maxIssues(5)
                .build();
        
        connectorService = new ConnectorService(githubClient, issueRepository, config);
    }
    
    @Test
    void testSyncIssues_Success() throws Exception {
        // Arrange
        List<Issue> mockIssues = createMockIssues();
        when(githubClient.fetchRecentIssues()).thenReturn(mockIssues);
        when(issueRepository.saveAll(any())).thenReturn(mockIssues);
        
        // Act
        SyncResult result = connectorService.syncIssues();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalIssuesFetched());
        assertEquals(2, result.getNewIssuesSaved());
        assertEquals(0, result.getDuplicateIssuesSkipped());
        
        verify(githubClient).fetchRecentIssues();
        verify(issueRepository).saveAll(mockIssues);
    }
    
    @Test
    void testSyncIssues_GitHubApiFailure() throws Exception {
        // Arrange
        when(githubClient.fetchRecentIssues()).thenThrow(new RepositoryException("API Error"));
        
        // Act & Assert
        ConnectorException exception = assertThrows(ConnectorException.class, 
            () -> connectorService.syncIssues());
        
        assertTrue(exception.getMessage().contains("Sync operation failed"));
        verify(githubClient).fetchRecentIssues();
        verify(issueRepository, never()).saveAll(any());
    }
    
    @Test
    void testSyncIssues_FirestoreFailure() throws Exception {
        // Arrange
        List<Issue> mockIssues = createMockIssues();
        when(githubClient.fetchRecentIssues()).thenReturn(mockIssues);
        when(issueRepository.saveAll(any())).thenThrow(new RepositoryException("Firestore Error"));
        
        // Act & Assert
        ConnectorException exception = assertThrows(ConnectorException.class, 
            () -> connectorService.syncIssues());
        
        assertTrue(exception.getMessage().contains("Sync operation failed"));
        verify(githubClient).fetchRecentIssues();
        verify(issueRepository).saveAll(mockIssues);
    }
    
    @Test
    void testGetAllIssues_Success() throws Exception {
        // Arrange
        List<Issue> mockIssues = createMockIssues();
        when(issueRepository.findAll()).thenReturn(mockIssues);
        
        // Act
        List<Issue> result = connectorService.getAllIssues();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(issueRepository).findAll();
    }
    
    @Test
    void testIssueExists_True() throws Exception {
        // Arrange
        Long issueId = 123L;
        when(issueRepository.existsById(issueId)).thenReturn(true);
        
        // Act
        boolean exists = connectorService.issueExists(issueId);
        
        // Assert
        assertTrue(exists);
        verify(issueRepository).existsById(issueId);
    }
    
    @Test
    void testIssueExists_False() throws Exception {
        // Arrange
        Long issueId = 123L;
        when(issueRepository.existsById(issueId)).thenReturn(false);
        
        // Act
        boolean exists = connectorService.issueExists(issueId);
        
        // Assert
        assertFalse(exists);
        verify(issueRepository).existsById(issueId);
    }
    
    private List<Issue> createMockIssues() {
        return Arrays.asList(
            new Issue(1L, "Test Issue 1", LocalDateTime.now(), "open", "http://example.com/1", "test/repo"),
            new Issue(2L, "Test Issue 2", LocalDateTime.now(), "closed", "http://example.com/2", "test/repo")
        );
    }
}
