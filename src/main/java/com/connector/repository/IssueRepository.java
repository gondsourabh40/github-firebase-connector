package com.connector.repository;

import com.connector.model.Issue;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for issue data access
 * Follows Repository pattern for data access abstraction
 */
public interface IssueRepository {
    
    /**
     * Save an issue to the repository
     * @param issue the issue to save
     * @return the saved issue
     * @throws RepositoryException if save operation fails
     */
    Issue save(Issue issue) throws RepositoryException;
    
    /**
     * Find an issue by its ID
     * @param id the issue ID
     * @return Optional containing the issue if found
     * @throws RepositoryException if find operation fails
     */
    Optional<Issue> findById(Long id) throws RepositoryException;
    
    /**
     * Check if an issue exists by its ID
     * @param id the issue ID
     * @return true if issue exists, false otherwise
     * @throws RepositoryException if check operation fails
     */
    boolean existsById(Long id) throws RepositoryException;
    
    /**
     * Save multiple issues in batch
     * @param issues list of issues to save
     * @return list of saved issues
     * @throws RepositoryException if batch save operation fails
     */
    List<Issue> saveAll(List<Issue> issues) throws RepositoryException;
    
    /**
     * Find all issues
     * @return list of all issues
     * @throws RepositoryException if find operation fails
     */
    List<Issue> findAll() throws RepositoryException;
}

