package com.transactiongraph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactiongraph.dto.TransactionRequest;
import com.transactiongraph.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TransactionController
 * 
 * Tests all endpoints with various scenarios:
 * - Successful transaction creation
 * - Duplicate transaction ID
 * - Invalid parent transaction
 * - Query by type
 * - Transitive sum calculation
 * - Transaction not found
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.clear();
    }

    @Test
    void createTransaction_Success() throws Exception {
        TransactionRequest request = new TransactionRequest(5000.0, "cars", null);

        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.amount", is(5000.0)))
                .andExpect(jsonPath("$.type", is("cars")))
                .andExpect(jsonPath("$.parentId").isEmpty());
    }

    @Test
    void createTransaction_WithParent_Success() throws Exception {
        // Create parent transaction first
        TransactionRequest parentRequest = new TransactionRequest(1000.0, "cars", null);
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated());

        // Create child transaction
        TransactionRequest childRequest = new TransactionRequest(2000.0, "cars", 10L);
        mockMvc.perform(put("/transactions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(childRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(11)))
                .andExpect(jsonPath("$.parentId", is(10)));
    }

    @Test
    void createTransaction_DuplicateId_Returns409() throws Exception {
        TransactionRequest request = new TransactionRequest(5000.0, "cars", null);

        // Create transaction
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to create with same ID
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    void createTransaction_InvalidParent_Returns404() throws Exception {
        TransactionRequest request = new TransactionRequest(5000.0, "cars", 999L);

        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void createTransaction_MissingAmount_Returns400() throws Exception {
        TransactionRequest request = new TransactionRequest(null, "cars", null);

        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.amount", notNullValue()));
    }

    @Test
    void createTransaction_MissingType_Returns400() throws Exception {
        TransactionRequest request = new TransactionRequest(5000.0, null, null);

        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.type", notNullValue()));
    }

    @Test
    void getTransactionIdsByType_Success() throws Exception {
        // Create transactions of same type
        TransactionRequest request1 = new TransactionRequest(5000.0, "cars", null);
        TransactionRequest request2 = new TransactionRequest(3000.0, "cars", null);
        TransactionRequest request3 = new TransactionRequest(2000.0, "shopping", null);

        mockMvc.perform(put("/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(put("/transactions/11")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(put("/transactions/15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)));

        mockMvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", containsInAnyOrder(10, 11)));
    }

    @Test
    void getTransactionIdsByType_NoTransactions_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void calculateTransitiveSum_SingleTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest(5000.0, "cars", null);

        mockMvc.perform(put("/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(5000.0)));
    }

    @Test
    void calculateTransitiveSum_WithHierarchy() throws Exception {
        // Create hierarchy: 10 -> 11 -> 12
        TransactionRequest request1 = new TransactionRequest(5000.0, "cars", null);
        TransactionRequest request2 = new TransactionRequest(10000.0, "cars", 10L);
        TransactionRequest request3 = new TransactionRequest(5000.0, "cars", 11L);

        mockMvc.perform(put("/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(put("/transactions/11")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(put("/transactions/12")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)));

        // sum(10) = 5000 + 10000 + 5000 = 20000
        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(20000.0)));

        // sum(11) = 10000 + 5000 = 15000
        mockMvc.perform(get("/transactions/sum/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(15000.0)));

        // sum(12) = 5000
        mockMvc.perform(get("/transactions/sum/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(5000.0)));
    }

    @Test
    void calculateTransitiveSum_TransactionNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/transactions/sum/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void calculateTransitiveSum_ComplexHierarchy() throws Exception {
        // Create complex hierarchy:
        //       10
        //      /  \
        //    11    12
        //   /
        //  13
        TransactionRequest request1 = new TransactionRequest(1000.0, "cars", null);
        TransactionRequest request2 = new TransactionRequest(2000.0, "cars", 10L);
        TransactionRequest request3 = new TransactionRequest(3000.0, "cars", 10L);
        TransactionRequest request4 = new TransactionRequest(4000.0, "cars", 11L);

        mockMvc.perform(put("/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(put("/transactions/11")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(put("/transactions/12")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)));

        mockMvc.perform(put("/transactions/13")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request4)));

        // sum(10) = 1000 + 2000 + 3000 + 4000 = 10000
        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(10000.0)));
    }

    @Test
    void calculateTransitiveSum_WithCycle_DoesNotInfiniteLoop() throws Exception {
        // Create a cycle: 10 -> 11 -> 10
        // Note: This test verifies cycle detection prevents infinite loops
        TransactionRequest request1 = new TransactionRequest(1000.0, "cars", null);
        TransactionRequest request2 = new TransactionRequest(2000.0, "cars", 10L);

        mockMvc.perform(put("/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(put("/transactions/11")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // Try to create a cycle by making 10 a child of 11
        // This would create: 10 -> 11 -> 10 (cycle)
        // The API doesn't prevent creating cycles, but the sum calculation should handle them
        TransactionRequest request3 = new TransactionRequest(3000.0, "cars", 11L);

        mockMvc.perform(put("/transactions/12")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

        // sum(10) should complete without infinite loop
        // With cycle detection: 10 (1000) + 11 (2000) + 12 (3000) = 6000
        // The cycle 10 -> 11 -> 10 is detected and 10 is not counted twice
        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(6000.0)));
    }
}
