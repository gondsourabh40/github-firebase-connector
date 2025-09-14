package com.connector.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Retry handler for handling transient failures
 * Implements exponential backoff strategy
 */
@Component
@Slf4j
public class RetryHandler {
    
    private final int maxRetries;
    private final long baseDelayMs;
    
    public RetryHandler(int maxRetries, long baseDelayMs) {
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;
    }
    
    /**
     * Execute operation with retry logic
     * @param operation the operation to execute
     * @param <T> return type
     * @return result of the operation
     * @throws Exception if all retries fail
     */
    public <T> T executeWithRetry(Supplier<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt == maxRetries) {
                    log.error("Operation failed after {} attempts. Last error: {}", maxRetries + 1, e.getMessage());
                    break;
                }
                
                long delay = calculateDelay(attempt);
                log.warn("Operation failed (attempt {}/{}). Retrying in {}ms. Error: {}", 
                        attempt + 1, maxRetries + 1, delay, e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        
        throw new RuntimeException("Operation failed after all retries", lastException);
    }
    
    /**
     * Calculate delay with exponential backoff
     */
    private long calculateDelay(int attempt) {
        return baseDelayMs * (long) Math.pow(2, attempt);
    }
}

