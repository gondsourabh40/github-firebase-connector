package com.connector.service;

/**
 * Custom exception for connector service operations
 */
public class ConnectorException extends Exception {
    
    public ConnectorException(String message) {
        super(message);
    }
    
    public ConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}

