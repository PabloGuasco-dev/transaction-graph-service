package com.transactiongraph.model;

import java.util.Objects;

/**
 * Domain model representing a transaction in the graph
 * 
 * Transactions form a hierarchical structure through parent_id relationships,
 * enabling transitive sum calculations across the entire hierarchy.
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
public class Transaction {

    private final Long id;
    private final Double amount;
    private final String type;
    private final Long parentId;

    /**
     * Creates a new transaction
     * 
     * @param id Unique transaction identifier
     * @param amount Transaction amount (required)
     * @param type Transaction type (required)
     * @param parentId Parent transaction ID (optional, for hierarchical relationships)
     */
    public Transaction(Long id, Double amount, String type, Long parentId) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.parentId = parentId;
    }

    public Long getId() {
        return id;
    }

    public Double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
