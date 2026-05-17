package com.transactiongraph.repository;

import com.transactiongraph.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for transaction storage
 * 
 * Uses efficient data structures for O(1) lookups:
 * - Map<Long, Transaction>: Fast transaction lookup by ID
 * - Map<String, Set<Long>>: Fast transaction lookup by type
 * - Map<Long, Set<Long>>: Fast child lookup for parent-child relationships
 * 
 * Thread-safe implementation using ConcurrentHashMap
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
@Repository
public class TransactionRepository {

    // Main storage: transaction ID -> Transaction
    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    
    // Type index: type -> set of transaction IDs
    private final Map<String, Set<Long>> typeIndex = new ConcurrentHashMap<>();
    
    // Parent-child index: parent ID -> set of child transaction IDs
    private final Map<Long, Set<Long>> parentChildIndex = new ConcurrentHashMap<>();

    /**
     * Saves a transaction to the repository
     * 
     * @param transaction The transaction to save
     * @return true if saved successfully, false if transaction ID already exists
     */
    public boolean save(Transaction transaction) {
        if (transactions.containsKey(transaction.getId())) {
            return false;
        }

        transactions.put(transaction.getId(), transaction);

        // Update type index
        typeIndex.computeIfAbsent(transaction.getType(), k -> ConcurrentHashMap.newKeySet())
                .add(transaction.getId());

        // Update parent-child index if parent exists
        if (transaction.getParentId() != null) {
            parentChildIndex.computeIfAbsent(transaction.getParentId(), k -> ConcurrentHashMap.newKeySet())
                    .add(transaction.getId());
        }

        return true;
    }

    /**
     * Finds a transaction by ID
     * 
     * @param id The transaction ID
     * @return Optional containing the transaction if found
     */
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(transactions.get(id));
    }

    /**
     * Checks if a transaction exists by ID
     * 
     * @param id The transaction ID
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        return transactions.containsKey(id);
    }

    /**
     * Finds all transaction IDs by type
     * 
     * @param type The transaction type
     * @return Set of transaction IDs with the given type
     */
    public Set<Long> findIdsByType(String type) {
        return typeIndex.getOrDefault(type, Collections.emptySet());
    }

    /**
     * Finds all child transaction IDs for a given parent
     * 
     * @param parentId The parent transaction ID
     * @return Set of child transaction IDs
     */
    public Set<Long> findChildIds(Long parentId) {
        return parentChildIndex.getOrDefault(parentId, Collections.emptySet());
    }

    /**
     * Gets the total number of transactions
     * 
     * @return Total transaction count
     */
    public int count() {
        return transactions.size();
    }

    /**
     * Clears all transactions (useful for testing)
     */
    public void clear() {
        transactions.clear();
        typeIndex.clear();
        parentChildIndex.clear();
    }
}
