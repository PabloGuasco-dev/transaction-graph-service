package com.transactiongraph.exception;

/**
 * Exception thrown when a parent transaction reference is invalid
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
public class InvalidParentTransactionException extends RuntimeException {

    public InvalidParentTransactionException(Long parentId) {
        super("Parent transaction not found with id: " + parentId);
    }
}
