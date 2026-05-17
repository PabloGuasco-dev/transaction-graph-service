package com.transactiongraph.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration
 * 
 * Configures the API documentation with metadata about the service
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transactionGraphServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Graph Service API")
                        .description("RESTful API for managing transaction graphs with transitive sum calculations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pablo Guasco")
                                .email("pablo.guasco@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
