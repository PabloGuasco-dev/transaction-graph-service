package com.transactiongraph.controller;

import com.transactiongraph.dto.SumResponse;
import com.transactiongraph.dto.TransactionRequest;
import com.transactiongraph.dto.TransactionResponse;
import com.transactiongraph.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for transaction operations
 * 
 * Provides endpoints for:
 * - Creating transactions with parent-child relationships
 * - Querying transactions by type
 * - Calculating transitive sums of transaction hierarchies
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
@Tag(name = "Transactions", description = "Transaction management API")
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Creates a new transaction
     * 
     * @param transactionId The unique transaction ID
     * @param request The transaction request data
     * @return The created transaction
     */
    @Operation(summary = "Create a transaction", description = "Creates a new transaction with optional parent relationship")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Transaction ID already exists")
    })
    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long transactionId,
            
            @Valid @RequestBody TransactionRequest request) {
        
        TransactionResponse response = transactionService.createTransaction(transactionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all transaction IDs for a given type
     * 
     * @param type The transaction type
     * @return List of transaction IDs
     */
    @Operation(summary = "Get transactions by type", description = "Returns all transaction IDs for a given type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction IDs retrieved successfully"),
            @ApiResponse(responseCode = "200", description = "Empty list if no transactions found")
    })
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getTransactionIdsByType(
            @Parameter(description = "Transaction type", required = true)
            @PathVariable String type) {
        
        List<Long> ids = transactionService.getTransactionIdsByType(type);
        return ResponseEntity.ok(ids);
    }

    /**
     * Calculates the transitive sum of a transaction and all its descendants
     * 
     * @param transactionId The transaction ID
     * @return The transitive sum
     */
    @Operation(summary = "Calculate transitive sum", description = "Calculates the sum of a transaction and all its descendants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sum calculated successfully",
                    content = @Content(schema = @Schema(implementation = SumResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/sum/{transactionId}")
    public ResponseEntity<SumResponse> calculateTransitiveSum(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long transactionId) {
        
        SumResponse response = transactionService.calculateTransitiveSum(transactionId);
        return ResponseEntity.ok(response);
    }
}
