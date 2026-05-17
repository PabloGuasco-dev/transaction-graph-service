# Transaction Graph Service

A RESTful API for managing transaction graphs with in-memory storage and transitive sum calculations.

## Overview

This service provides a simple yet powerful API for:
- Creating transactions with hierarchical parent-child relationships
- Querying transactions by type
- Calculating transitive sums across transaction hierarchies

**Key Features:**
- In-memory persistence (no database required)
- Efficient data structures for O(1) lookups
- Iterative DFS to avoid stack overflow in sum calculations
- Clean architecture with separated layers
- Comprehensive integration tests
- OpenAPI/Swagger documentation
- Docker support

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Maven**
- **JUnit 5** + **MockMvc** for testing
- **SpringDoc OpenAPI** for Swagger UI
- **Docker** for containerization

## Architecture

The project follows a clean, layered architecture:

```
com.transactiongraph
├── config          # Configuration classes (OpenAPI)
├── controller      # REST controllers
├── dto            # Data Transfer Objects (records)
├── exception      # Custom exceptions and global handler
├── model          # Domain models
├── repository     # In-memory data access layer
└── service        # Business logic layer
```

### Design Decisions

1. **In-Memory Storage**: Uses `ConcurrentHashMap` for thread-safe, O(1) average case lookups
2. **Thread-Safe Sets**: Uses `ConcurrentHashMap.newKeySet()` for all internal sets to ensure thread safety
3. **Multiple Indexes**: Maintains separate indexes for:
   - Transaction ID lookup
   - Type-based queries
   - Parent-child relationships
4. **Iterative DFS with Cycle Detection**: Avoids stack overflow in deep hierarchies by using an explicit stack, with cycle detection using a visited set to prevent infinite loops
5. **Records for DTOs**: Leverages Java 17+ records for immutable, concise DTOs
6. **REST Semantics**: Returns 404 Not Found for non-existent parent transactions (resource doesn't exist) rather than 400 Bad Request
7. **Global Exception Handler**: Centralized error handling with appropriate HTTP status codes

### Time Complexity

- **createTransaction**: O(1) average case
- **getTransactionIdsByType**: O(1) average case
- **calculateTransitiveSum**: O(n) where n is the number of connected transactions

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.9 or higher
- Docker (optional, for containerized deployment)

### Running Locally

1. Clone the repository:
```bash
git clone https://github.com/PabloGuasco-dev/transaction-graph-service.git
cd transaction-graph-service
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

### Running with Docker

1. Build the Docker image:
```bash
docker build -t transaction-graph-service .
```

2. Run the container:
```bash
docker run -p 8080:8080 transaction-graph-service
```

## API Documentation

Once the service is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

The OpenAPI JSON is available at:
```
http://localhost:8080/api-docs
```

## API Endpoints

### 1. Create Transaction

**Endpoint:** `PUT /transactions/{transactionId}`

**Request Body:**
```json
{
  "amount": 5000,
  "type": "cars",
  "parent_id": 10
}
```

**Response:** `201 Created`
```json
{
  "id": 11,
  "amount": 5000,
  "type": "cars",
  "parentId": 10
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/transactions/10 \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "type": "cars"
  }'
```

**Validation Rules:**
- `amount` is required
- `type` is required
- `parent_id` is optional but must reference an existing transaction (returns 404 Not Found if parent doesn't exist)
- Transaction IDs must be unique (returns 409 Conflict if duplicate)

### 2. Get Transactions by Type

**Endpoint:** `GET /transactions/types/{type}`

**Response:** `200 OK`
```json
[10, 11, 15]
```

**cURL Example:**
```bash
curl http://localhost:8080/transactions/types/cars
```

### 3. Calculate Transitive Sum

**Endpoint:** `GET /transactions/sum/{transactionId}`

**Response:** `200 OK`
```json
{
  "sum": 20000
}
```

**cURL Example:**
```bash
curl http://localhost:8080/transactions/sum/10
```

**Transitive Sum Logic:**
The sum includes the transaction itself and all its descendants connected via `parent_id`.

**Example Hierarchy:**
```
10 (5000) -> 11 (10000) -> 12 (5000)
```

- `sum(10)` = 5000 + 10000 + 5000 = 20000
- `sum(11)` = 10000 + 5000 = 15000
- `sum(12)` = 5000

## Error Handling

The service uses standard HTTP status codes:

- `201 Created`: Transaction created successfully
- `200 OK`: Query successful
- `400 Bad Request`: Validation error (missing required fields)
- `404 Not Found`: Transaction or parent transaction not found
- `409 Conflict`: Duplicate transaction ID
- `500 Internal Server Error`: Unexpected error

**Error Response Format:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Transaction not found with id: 999",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Testing

Run the integration tests:
```bash
mvn test
```

The test suite covers:
- Successful transaction creation
- Duplicate transaction ID handling
- Invalid parent transaction handling (404 Not Found)
- Missing required fields validation
- Query by type
- Transitive sum calculation
- Complex hierarchies
- Transaction not found scenarios
- Cycle detection in sum calculation

## Project Structure

```
transaction-graph-service/
├── src/
│   ├── main/
│   │   ├── java/com/transactiongraph/
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   └── TransactionController.java
│   │   │   ├── dto/
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── SumResponse.java
│   │   │   │   ├── TransactionRequest.java
│   │   │   │   └── TransactionResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidParentTransactionException.java
│   │   │   │   ├── TransactionAlreadyExistsException.java
│   │   │   │   └── TransactionNotFoundException.java
│   │   │   ├── model/
│   │   │   │   └── Transaction.java
│   │   │   ├── repository/
│   │   │   │   └── TransactionRepository.java
│   │   │   ├── service/
│   │   │   │   └── TransactionService.java
│   │   │   └── TransactionGraphServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/transactiongraph/
│           └── TransactionControllerIntegrationTest.java
├── Dockerfile
├── pom.xml
└── README.md
```

## SOLID Principles Applied

1. **Single Responsibility**: Each class has one clear purpose
2. **Open/Closed**: Open for extension, closed for modification
3. **Liskov Substitution**: Proper exception hierarchy
4. **Interface Segregation**: Focused interfaces (implicit through method design)
5. **Dependency Inversion**: Service depends on repository abstraction

## Future Enhancements

- Add transaction update/delete operations
- Implement pagination for type queries
- Add transaction search by amount range
- Implement caching for frequently accessed sums
- Add metrics and monitoring
- Support for transaction metadata

## License

MIT License

## Author

Pablo Guasco

## Version

1.0.0
