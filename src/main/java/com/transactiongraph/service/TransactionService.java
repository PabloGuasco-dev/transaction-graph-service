package com.transactiongraph.service;

import com.transactiongraph.dto.SumResponse;
import com.transactiongraph.dto.TransactionRequest;
import com.transactiongraph.dto.TransactionResponse;
import com.transactiongraph.exception.InvalidParentTransactionException;
import com.transactiongraph.exception.TransactionAlreadyExistsException;
import com.transactiongraph.exception.TransactionNotFoundException;
import com.transactiongraph.model.Transaction;
import com.transactiongraph.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for transaction business logic
 * 
 * Handles transaction creation, type-based queries, and transitive sum calculations.
 * Uses iterative DFS to avoid stack overflow in sum calculations.
 * 
 * Time Complexity:
 * - createTransaction: O(1) average case
 * - getTransactionIdsByType: O(1) average case
 * - calculateTransitiveSum: O(n) where n is the number of connected transactions
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new transaction
     * 
     * @param transactionId The unique transaction ID
     * @param request The transaction request data
     * @return The created transaction response
     * @throws TransactionAlreadyExistsException if transaction ID already exists
     * @throws InvalidParentTransactionException if parent transaction doesn't exist
     */
    public TransactionResponse createTransaction(Long transactionId, TransactionRequest request) {
        // Check for duplicate transaction ID
        if (repository.existsById(transactionId)) {
            throw new TransactionAlreadyExistsException(transactionId);
        }

        // Validate parent transaction if provided
        if (request.parentId() != null && !repository.existsById(request.parentId())) {
            throw new InvalidParentTransactionException(request.parentId());
        }

        Transaction transaction = new Transaction(
                transactionId,
                request.amount(),
                request.type(),
                request.parentId()
        );

        boolean saved = repository.save(transaction);
        if (!saved) {
            throw new TransactionAlreadyExistsException(transactionId);
        }

        return toResponse(transaction);
    }

    /**
     * Gets all transaction IDs for a given type
     * 
     * @param type The transaction type
     * @return List of transaction IDs
     */
    public List<Long> getTransactionIdsByType(String type) {
        Set<Long> ids = repository.findIdsByType(type);
        return new ArrayList<>(ids);
    }

    /**
     * Calculates the transitive sum of a transaction and all its descendants
     * 
     * Uses iterative DFS to avoid stack overflow for deep hierarchies.
     * Time Complexity: O(n) where n is the number of connected transactions
     * 
     * @param transactionId The transaction ID
     * @return The transitive sum response
     * @throws TransactionNotFoundException if transaction doesn't exist
     */
    public SumResponse calculateTransitiveSum(Long transactionId) {
        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        double sum = calculateTransitiveSumIterative(transaction);
        return new SumResponse(sum);
    }

    /**
     * Calculates transitive sum using iterative DFS
     *
     * This approach avoids stack overflow for deep hierarchies by using an explicit stack.
     * Also includes cycle detection using a visited set to prevent infinite loops.
     *
     * @param root The root transaction
     * @return The sum of the transaction and all its descendants
     */
    private double calculateTransitiveSumIterative(Transaction root) {
        double sum = 0.0;
        Deque<Long> stack = new java.util.ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        stack.push(root.getId());

        while (!stack.isEmpty()) {
            Long currentId = stack.pop();

            // Skip if already visited (cycle detection)
            if (visited.contains(currentId)) {
                continue;
            }
            visited.add(currentId);

            Transaction current = repository.findById(currentId)
                    .orElseThrow(() -> new TransactionNotFoundException(currentId));

            sum += current.getAmount();

            // Add children to stack for processing
            Set<Long> childIds = repository.findChildIds(currentId);
            for (Long childId : childIds) {
                stack.push(childId);
            }
        }

        return sum;
    }

    /**
     * Converts a Transaction entity to a TransactionResponse DTO
     * 
     * @param transaction The transaction entity
     * @return The transaction response DTO
     */
    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getParentId()
        );
    }
}
