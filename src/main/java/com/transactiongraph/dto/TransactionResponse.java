package com.transactiongraph.dto;

/**
 * DTO for transaction responses
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
public record TransactionResponse(
        Long id,
        Double amount,
        String type,
        Long parentId
) {
}
