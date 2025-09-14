package com.connector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model representing a GitHub issue
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private String state;
    private String htmlUrl;
    private String repository;
}

