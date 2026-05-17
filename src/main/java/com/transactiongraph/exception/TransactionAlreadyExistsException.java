package com.transactiongraph.exception;

/**
 * Exception thrown when attempting to create a transaction with a duplicate ID
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
public class TransactionAlreadyExistsException extends RuntimeException {

    public TransactionAlreadyExistsException(Long transactionId) {
        super("Transaction already exists with id: " + transactionId);
    }
}
