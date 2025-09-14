package com.connector.service;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Result object containing statistics about a sync operation
 */
@Data
@AllArgsConstructor
public class SyncResult {
    private int totalIssuesFetched;
    private int newIssuesSaved;
    private int duplicateIssuesSkipped;
    private long syncTimestamp;
}
