package com.transactiongraph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Transaction Graph Service
 *
 * This service provides an in-memory transaction graph API with:
 * - Transaction creation with parent-child relationships
 * - Query transactions by type
 * - Calculate transitive sums of transaction hierarchies
 *
 * Built with Spring Boot 3 and Java 17
 *
 * @author Pablo Guasco
 * @version 1.0.0
 */
@SpringBootApplication
public class TransactionGraphServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionGraphServiceApplication.class, args);
    }
}
