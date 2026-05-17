package com.transactiongraph.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for transaction creation requests
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
public record TransactionRequest(
        @NotNull(message = "Amount is required")
        Double amount,
        
        @NotNull(message = "Type is required")
        String type,
        
        Long parentId
) {
}
