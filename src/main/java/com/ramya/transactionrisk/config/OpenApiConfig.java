package com.ramya.transactionrisk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI transactionRiskOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Risk API")
                        .version("1.0.0")
                        .description("""
                                Production-style Spring Boot API for submitting
                                transactions and generating explainable risk assessments.
                                """)
                        .contact(new Contact()
                                .name("Ramya Reddy Koppula")
                                .url("https://github.com/ramya-reddy-k"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
