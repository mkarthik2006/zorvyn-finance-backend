package com.zorvyn.finance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("Finance Data Processing & Access Control API")
                        .version("1.0.0")
                        .description("Backend API for Finance Dashboard - Zorvyn FinTech Assignment. " +
                                "Provides user management, financial records CRUD, dashboard analytics, " +
                                "and role-based access control.")
                        .contact(new Contact()
                                .name("Karthik M")
                                .email("mkarthik2006@example.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }

    @Bean
    public OpenApiCustomizer globalErrorResponseCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
            pathItem.readOperations().forEach(operation -> {
                ApiResponses responses = operation.getResponses();

                if (!responses.containsKey("400")) {
                    responses.addApiResponse("400", createErrorResponse("Bad Request - Validation failed"));
                }
                if (!responses.containsKey("401")) {
                    responses.addApiResponse("401", createErrorResponse("Unauthorized - Authentication required"));
                }
                if (!responses.containsKey("403")) {
                    responses.addApiResponse("403", createErrorResponse("Forbidden - Insufficient permissions"));
                }
                if (!responses.containsKey("404")) {
                    responses.addApiResponse("404", createErrorResponse("Not Found - Resource does not exist"));
                }
                if (!responses.containsKey("500")) {
                    responses.addApiResponse("500", createErrorResponse("Internal Server Error"));
                }
            })
        );
    }

    @SuppressWarnings("rawtypes")
    private ApiResponse createErrorResponse(String description) {
        Schema schema = new Schema()
                .addProperty("success", new Schema().type("boolean").example(false))
                .addProperty("message", new Schema().type("string").example(description))
                .addProperty("timestamp", new Schema().type("string").format("date-time"));

        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(schema)));
    }
}