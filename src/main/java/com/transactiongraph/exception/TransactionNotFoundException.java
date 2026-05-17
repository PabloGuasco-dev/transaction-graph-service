package com.transactiongraph.exception;

/**
 * Exception thrown when a transaction is not found
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long transactionId) {
        super("Transaction not found with id: " + transactionId);
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }
}
