package com.connector.controller;

import com.connector.model.Issue;
import com.connector.service.ConnectorService;
import com.connector.service.SyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for GitHub-Firebase connector operations
 * Provides HTTP endpoints for syncing issues and retrieving data
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ConnectorController {
    
    private final ConnectorService connectorService;
    
    /**
     * Sync GitHub issues to Firestore
     * POST /api/v1/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<SyncResult>> syncIssues() {
        try {
            log.info("REST API: Starting manual sync operation");
            SyncResult result = connectorService.syncIssues();
            log.info("REST API: Sync completed successfully - {} issues processed", result.getTotalIssuesFetched());
            return ResponseEntity.ok(ApiResponse.success("Sync completed successfully", result));
        } catch (Exception e) {
            log.error("REST API: Sync operation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Sync operation failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get all issues from Firestore
     * GET /api/v1/issues
     */
    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<List<Issue>>> getAllIssues() {
        try {
            log.info("REST API: Retrieving all issues from Firestore");
            List<Issue> issues = connectorService.getAllIssues();
            log.info("REST API: Retrieved {} issues from Firestore", issues.size());
            return ResponseEntity.ok(ApiResponse.success("Issues retrieved successfully", issues));
        } catch (Exception e) {
            log.error("REST API: Failed to retrieve issues", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve issues: " + e.getMessage()));
        }
    }
    
    /**
     * Check if a specific issue exists in Firestore
     * GET /api/v1/issues/{issueId}/exists
     */
    @GetMapping("/issues/{issueId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkIssueExists(@PathVariable Long issueId) {
        try {
            log.info("REST API: Checking existence of issue {}", issueId);
            boolean exists = connectorService.issueExists(issueId);
            log.info("REST API: Issue {} exists: {}", issueId, exists);
            return ResponseEntity.ok(ApiResponse.success("Issue existence checked", exists));
        } catch (Exception e) {
            log.error("REST API: Failed to check issue existence for ID {}", issueId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check issue existence: " + e.getMessage()));
        }
    }
    
    /**
     * Get application health status
     * GET /api/v1/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthStatus>> getHealth() {
        try {
            log.debug("REST API: Health check requested");
            HealthStatus health = new HealthStatus("UP", "GitHub-Firebase Connector is running");
            return ResponseEntity.ok(ApiResponse.success("Health check completed", health));
        } catch (Exception e) {
            log.error("REST API: Health check failed", e);
            HealthStatus health = new HealthStatus("DOWN", "Application error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Health check failed"));
        }
    }
    
    /**
     * Health status response model
     */
    public static class HealthStatus {
        private String status;
        private String message;
        
        public HealthStatus(String status, String message) {
            this.status = status;
            this.message = message;
        }
        
        public String getStatus() {
            return status;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
